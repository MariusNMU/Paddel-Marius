import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AuthJoueurResponse } from '../../models/auth.model';
import {
  MatchPublicResponse,
  RejoindreMatchPublicResponse
} from '../../models/match-public.model';
import { ParametresMetierResponse } from '../../models/parametres-metier.model';
import { SiteResponse } from '../../models/site.model';
import { AuthContextService } from '../../services/auth-context.service';
import { MatchPublicApiService } from '../../services/match-public-api.service';
import { ParametresMetierApiService } from '../../services/parametres-metier-api.service';
import { SiteApiService } from '../../services/site-api.service';
import { MatchesPublicsComponent } from './matches-publics.component';

describe('MatchesPublicsComponent', () => {
  let fixture: ComponentFixture<MatchesPublicsComponent>;
  let component: MatchesPublicsComponent;

  let matchPublicApiService: {
    listerMatchesPublics: ReturnType<typeof vi.fn>;
    rejoindreEtPayer: ReturnType<typeof vi.fn>;
  };

  let authContextService: {
    joueur: ReturnType<typeof vi.fn>;
  };

  let siteApiService: {
    listerSitesActifs: ReturnType<typeof vi.fn>;
  };

  let parametresMetierApiService: {
    consulterParametresMetier: ReturnType<typeof vi.fn>;
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

  const parametresMetier: ParametresMetierResponse = {
    dureeMatchMinutes: 90,
    pauseEntreMatchesMinutes: 15,
    nombreJoueursMaximum: 4,
    prixTotalMatch: 60,
    montantParticipationStandard: 15,
    soldeInitialJoueur: 100
  };

  beforeEach(async () => {
    matchPublicApiService = {
      listerMatchesPublics: vi.fn(),
      rejoindreEtPayer: vi.fn()
    };

    authContextService = {
      joueur: vi.fn(() => joueur)
    };

    siteApiService = {
      listerSitesActifs: vi.fn(() => of(sites))
    };

    parametresMetierApiService = {
      consulterParametresMetier: vi.fn(() => of(parametresMetier))
    };

    await TestBed.configureTestingModule({
      imports: [MatchesPublicsComponent],
      providers: [
        { provide: MatchPublicApiService, useValue: matchPublicApiService },
        { provide: SiteApiService, useValue: siteApiService },
        { provide: ParametresMetierApiService, useValue: parametresMetierApiService },
        { provide: AuthContextService, useValue: authContextService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(MatchesPublicsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('doit créer le composant', () => {
    expect(component).toBeTruthy();
  });

  it('doit charger les sites depuis l API backend', () => {
    expect(siteApiService.listerSitesActifs).toHaveBeenCalled();
    expect(component.sites).toEqual(sites);
    expect(component.siteId).toBe(1);
  });

  it('doit charger les paramètres métier depuis l API backend', () => {
    expect(parametresMetierApiService.consulterParametresMetier).toHaveBeenCalled();
    expect(component.parametresMetier).toEqual(parametresMetier);
  });

  it('doit lister les matches publics pour un site et une date', () => {
    matchPublicApiService.listerMatchesPublics.mockReturnValue(of([matchPublic]));

    component.siteId = 1;
    component.date = '2026-06-20';

    component.rechercherMatchesPublics();

    expect(matchPublicApiService.listerMatchesPublics).toHaveBeenCalledWith(1, '2026-06-20');
    expect(component.matches).toEqual([matchPublic]);
    expect(component.rechercheEffectuee).toBe(true);
    expect(component.chargement).toBe(false);
  });

  it('doit afficher une erreur si le site ou la date est manquant', () => {
    component.siteId = null;
    component.date = '';

    component.rechercherMatchesPublics();

    expect(component.messageErreur).toBe('Le site et la date sont obligatoires.');
    expect(matchPublicApiService.listerMatchesPublics).not.toHaveBeenCalled();
  });

  it('doit afficher une erreur si la recherche échoue', () => {
    matchPublicApiService.listerMatchesPublics.mockReturnValue(
      throwError(() => new HttpErrorResponse({
        status: 500,
        error: {
          message: 'Erreur backend matches publics.'
        }
      }))
    );

    component.siteId = 1;
    component.date = '2026-06-20';

    component.rechercherMatchesPublics();

    expect(component.messageErreur).toBe('Erreur backend matches publics.');
    expect(component.matches).toEqual([]);
    expect(component.chargement).toBe(false);
  });

  it('doit refuser de rejoindre un match si aucun joueur n est connecté', () => {
    authContextService.joueur.mockReturnValue(null);

    component.rejoindreEtPayer(matchPublic);

    expect(component.messageErreur).toBe('Connecte-toi comme joueur pour rejoindre un match public.');
    expect(matchPublicApiService.rejoindreEtPayer).not.toHaveBeenCalled();
  });

  it('doit appeler l API pour rejoindre et payer un match public', () => {
    const paiement: RejoindreMatchPublicResponse = {
      matchId: 10,
      participationId: 30,
      paiementId: 40,
      matriculeJoueur: 'TEST001',
      montantPaye: 15,
      statutParticipation: 'CONFIRMEE',
      soldeRestant: 85
    };

    authContextService.joueur.mockReturnValue(joueur);
    matchPublicApiService.rejoindreEtPayer.mockReturnValue(of(paiement));
    matchPublicApiService.listerMatchesPublics.mockReturnValue(of([]));

    component.siteId = 1;
    component.date = '2026-06-20';

    component.rejoindreEtPayer(matchPublic);

    expect(matchPublicApiService.rejoindreEtPayer).toHaveBeenCalledWith(10, {
      matriculeJoueur: 'TEST001'
    });

    expect(matchPublicApiService.listerMatchesPublics).toHaveBeenCalledWith(1, '2026-06-20');
    expect(component.chargementPaiement).toBe(false);
  });

  it('ne doit pas appeler l API si le joueur ne peut pas rejoindre le match', () => {
    const matchNonEligible: MatchPublicResponse = {
      ...matchPublic,
      peutRejoindre: false,
      motifNonEligibilite: 'Tu participes déjà à ce match.'
    };

    component.rejoindreEtPayer(matchNonEligible);

    expect(component.messageErreur).toBe(
      'Tu participes déjà à ce match.'
    );
    expect(
      matchPublicApiService.rejoindreEtPayer
    ).not.toHaveBeenCalled();
  });

  it('doit masquer le bouton si le joueur participe déjà', () => {
    component.matches = [{
      ...matchPublic,
      peutRejoindre: false,
      motifNonEligibilite: 'Tu participes déjà à ce match.'
    }];

    fixture.detectChanges();

    const contenu = fixture.nativeElement.textContent;

    expect(contenu).toContain(
      'Tu participes déjà à ce match.'
    );
    expect(contenu).not.toContain(
      'Rejoindre et payer 15.00 €'
    );
  });

  it('doit changer la date avec le choix rapide', () => {
    component.matches = [matchPublic];
    component.messageErreur = 'ancienne erreur';
    component.messageSucces = 'ancien succès';
    component.rechercheEffectuee = true;

    component.selectionnerJour('2026-06-21');

    expect(component.date).toBe('2026-06-21');
    expect(component.matches).toEqual([]);
    expect(component.messageErreur).toBe('');
    expect(component.messageSucces).toBe('');
    expect(component.rechercheEffectuee).toBe(false);
  });
});
