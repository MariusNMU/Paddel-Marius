import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AuthJoueurResponse } from '../models/auth.model';
import {
  MatchPublicResponse,
  RejoindreMatchPublicResponse
} from '../models/match-public.model';
import { ParametresMetierResponse } from '../models/parametres-metier.model';
import { SiteResponse } from '../models/site.model';
import { AuthContextService } from './auth-context.service';
import { MatchPublicApiService } from './match-public-api.service';
import { MatchesPublicsFacadeService } from './matches-publics-facade.service';
import { ParametresMetierApiService } from './parametres-metier-api.service';
import { SiteApiService } from './site-api.service';

describe('MatchesPublicsFacadeService', () => {
  let service: MatchesPublicsFacadeService;

  let matchPublicApiService: {
    listerMatchesPublics: ReturnType<typeof vi.fn>;
    rejoindreEtPayer: ReturnType<typeof vi.fn>;
  };

  let siteApiService: {
    listerSitesActifs: ReturnType<typeof vi.fn>;
  };

  let parametresMetierApiService: {
    consulterParametresMetier: ReturnType<typeof vi.fn>;
  };

  let authContextService: {
    joueur: ReturnType<typeof vi.fn>;
  };

  const joueur: AuthJoueurResponse = {
    membreId: 1,
    matricule: 'TEST001',
    nom: 'Test',
    prenom: 'Joueur',
    categorieMembre: 'GLOBAL',
    siteRattachementId: null,
    nomSiteRattachement: null,
    actif: true
  };

  const sites: SiteResponse[] = [
    {
      siteId: 1,
      code: 'ALP',
      nom: 'Site Alpha',
      adresse: 'Rue du Test 1'
    },
    {
      siteId: 2,
      code: 'BET',
      nom: 'Site Beta',
      adresse: 'Rue du Test 2'
    }
  ];

  const parametresMetier: ParametresMetierResponse = {
    dureeMatchMinutes: 90,
    pauseEntreMatchesMinutes: 15,
    nombreJoueursMaximum: 4,
    prixTotalMatch: 60,
    montantParticipationStandard: 15,
    soldeInitialJoueur: 100
  };

  const matchPublic: MatchPublicResponse = {
    matchId: 10,
    siteId: 1,
    nomSite: 'Site Alpha',
    terrainId: 20,
    numeroTerrain: 'T1',
    dateHeureDebut: '2026-06-20T09:00:00',
    dateHeureFin: '2026-06-20T10:30:00',
    nombreParticipantsActifs: 2,
    placesDisponibles: 2,
    prixTotal: 60,
    montantParticipation: 15,
    peutRejoindre: true,
    motifNonEligibilite: null
  };

  const paiement: RejoindreMatchPublicResponse = {
    matchId: 10,
    participationId: 30,
    paiementId: 40,
    matriculeJoueur: 'TEST001',
    montantPaye: 15,
    statutParticipation: 'CONFIRMEE',
    soldeRestant: 85
  };

  beforeEach(() => {
    matchPublicApiService = {
      listerMatchesPublics: vi.fn(() => of([matchPublic])),
      rejoindreEtPayer: vi.fn()
    };

    siteApiService = {
      listerSitesActifs: vi.fn(() => of(sites))
    };

    parametresMetierApiService = {
      consulterParametresMetier: vi.fn(
        () => of(parametresMetier)
      )
    };

    authContextService = {
      joueur: vi.fn(() => joueur)
    };

    TestBed.configureTestingModule({
      providers: [
        MatchesPublicsFacadeService,
        {
          provide: MatchPublicApiService,
          useValue: matchPublicApiService
        },
        {
          provide: SiteApiService,
          useValue: siteApiService
        },
        {
          provide: ParametresMetierApiService,
          useValue: parametresMetierApiService
        },
        {
          provide: AuthContextService,
          useValue: authContextService
        }
      ]
    });

    service = TestBed.inject(MatchesPublicsFacadeService);
  });

  it('doit initialiser les sites et les paramètres métier', () => {
    service.initialiser();

    expect(siteApiService.listerSitesActifs)
      .toHaveBeenCalled();
    expect(parametresMetierApiService.consulterParametresMetier)
      .toHaveBeenCalled();

    expect(service.sites()).toEqual(sites);
    expect(service.siteId()).toBe(1);
    expect(service.parametresMetier()).toEqual(parametresMetier);
    expect(service.joursRapides()).toHaveLength(7);
    expect(service.date()).not.toBe('');
  });

  it('doit rechercher les matches publics', () => {
    service.initialiser();
    service.modifierSiteId(2);
    service.modifierDate('2026-06-20');

    service.rechercherMatchesPublics();

    expect(matchPublicApiService.listerMatchesPublics)
      .toHaveBeenCalledWith(2, '2026-06-20');
    expect(service.matches()).toEqual([matchPublic]);
    expect(service.rechercheEffectuee()).toBe(true);
    expect(service.chargementRecherche()).toBe(false);
  });

  it('doit garder visible un match hors site sans permettre sa réservation', () => {
    authContextService.joueur.mockReturnValue({
      ...joueur,
      matricule: 'S1001',
      categorieMembre: 'SITE',
      siteRattachementId: 1,
      nomSiteRattachement: 'Site Alpha'
    });

    const matchHorsSite: MatchPublicResponse = {
      ...matchPublic,
      matchId: 11,
      siteId: 2,
      nomSite: 'Site Beta',
      peutRejoindre: false,
      motifNonEligibilite:
        'Un membre SITE ne peut réserver que sur son site de rattachement.'
    };

    matchPublicApiService.listerMatchesPublics
      .mockReturnValue(of([matchHorsSite]));

    service.initialiser();

    expect(service.sites()).toEqual(sites);

    service.modifierSiteId(2);
    service.modifierDate('2026-06-20');
    service.rechercherMatchesPublics();

    expect(service.matches()).toEqual([
      matchHorsSite
    ]);

    service.rejoindreEtPayer(matchHorsSite);

    expect(service.messageErreur()).toBe(
      'Un membre SITE ne peut réserver que sur son site de rattachement.'
    );

    expect(
      matchPublicApiService.rejoindreEtPayer
    ).not.toHaveBeenCalled();
  });

  it('doit refuser une recherche incomplète', () => {
    service.initialiser();
    service.modifierSiteId(null);
    service.modifierDate('');

    service.rechercherMatchesPublics();

    expect(service.messageErreur()).toBe(
      'Le site et la date sont obligatoires.'
    );
    expect(matchPublicApiService.listerMatchesPublics)
      .not.toHaveBeenCalled();
  });

  it('doit exposer une erreur de recherche du backend', () => {
    matchPublicApiService.listerMatchesPublics.mockReturnValue(
      throwError(() => new HttpErrorResponse({
        status: 500,
        error: {
          message: 'Erreur backend matches publics.'
        }
      }))
    );

    service.initialiser();
    service.modifierSiteId(1);
    service.modifierDate('2026-06-20');

    service.rechercherMatchesPublics();

    expect(service.messageErreur()).toBe(
      'Erreur backend matches publics.'
    );
    expect(service.matches()).toEqual([]);
    expect(service.chargementRecherche()).toBe(false);
  });

  it('doit refuser le paiement sans joueur connecté', () => {
    authContextService.joueur.mockReturnValue(null);

    service.initialiser();
    service.rejoindreEtPayer(matchPublic);

    expect(service.messageErreur()).toBe(
      'Connecte-toi comme joueur pour rejoindre un match public.'
    );
    expect(matchPublicApiService.rejoindreEtPayer)
      .not.toHaveBeenCalled();
  });

  it('doit refuser un match non éligible', () => {
    const matchNonEligible: MatchPublicResponse = {
      ...matchPublic,
      peutRejoindre: false,
      motifNonEligibilite: 'Tu participes déjà à ce match.'
    };

    service.initialiser();
    service.rejoindreEtPayer(matchNonEligible);

    expect(service.messageErreur()).toBe(
      'Tu participes déjà à ce match.'
    );
    expect(matchPublicApiService.rejoindreEtPayer)
      .not.toHaveBeenCalled();
  });

  it('doit payer puis actualiser sans perdre la confirmation', () => {
    matchPublicApiService.rejoindreEtPayer.mockReturnValue(
      of(paiement)
    );
    matchPublicApiService.listerMatchesPublics.mockReturnValue(
      of([])
    );

    service.initialiser();
    service.modifierSiteId(1);
    service.modifierDate('2026-06-20');

    service.rejoindreEtPayer(matchPublic);

    expect(matchPublicApiService.rejoindreEtPayer)
      .toHaveBeenCalledWith(10, {
        matriculeJoueur: 'TEST001'
      });

    expect(matchPublicApiService.listerMatchesPublics)
      .toHaveBeenCalledWith(1, '2026-06-20');

    expect(service.dernierPaiement()).toEqual(paiement);
    expect(service.messageSucces()).toContain('TEST001');
    expect(service.messageSucces()).toContain('15.00 €');
    expect(service.matches()).toEqual([]);
    expect(service.chargementPaiement()).toBe(false);
  });

  it('doit exposer une erreur de paiement du backend', () => {
    matchPublicApiService.rejoindreEtPayer.mockReturnValue(
      throwError(() => new HttpErrorResponse({
        status: 409,
        error: {
          message: 'Le paiement ne peut pas être effectué.'
        }
      }))
    );

    service.initialiser();
    service.rejoindreEtPayer(matchPublic);

    expect(service.messageErreur()).toBe(
      'Le paiement ne peut pas être effectué.'
    );
    expect(service.dernierPaiement()).toBeNull();
    expect(service.chargementPaiement()).toBe(false);
  });

  it('doit réinitialiser la recherche avec le choix rapide', () => {
    service.initialiser();
    service.modifierSiteId(1);
    service.modifierDate('2026-06-20');
    service.rechercherMatchesPublics();

    service.selectionnerJour('2026-06-21');

    expect(service.date()).toBe('2026-06-21');
    expect(service.matches()).toEqual([]);
    expect(service.messageErreur()).toBe('');
    expect(service.messageSucces()).toBe('');
    expect(service.rechercheEffectuee()).toBe(false);
  });
});
