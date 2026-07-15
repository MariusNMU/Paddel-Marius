import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { DisponibilitesResponse } from '../models/disponibilite.model';
import { ParametresMetierResponse } from '../models/parametres-metier.model';
import { SiteResponse } from '../models/site.model';
import { DisponibiliteApiService } from './disponibilite-api.service';
import { DisponibilitesFacadeService } from './disponibilites-facade.service';
import { ParametresMetierApiService } from './parametres-metier-api.service';
import { SiteApiService } from './site-api.service';

describe('DisponibilitesFacadeService', () => {
  let service: DisponibilitesFacadeService;

  let disponibiliteApiService: {
    consulterDisponibilites: ReturnType<typeof vi.fn>;
  };

  let siteApiService: {
    listerSitesActifs: ReturnType<typeof vi.fn>;
  };

  let parametresMetierApiService: {
    consulterParametresMetier: ReturnType<typeof vi.fn>;
  };

  let router: {
    navigate: ReturnType<typeof vi.fn>;
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

  const disponibilites: DisponibilitesResponse = {
    siteId: 2,
    nomSite: 'Site Beta',
    date: '2026-06-20',
    ferme: false,
    motifFermeture: null,
    creneaux: [
      {
        terrainId: 20,
        numeroTerrain: 'T2',
        dateHeureDebut: '2026-06-20T09:00:00',
        dateHeureFin: '2026-06-20T10:30:00'
      }
    ]
  };

  beforeEach(() => {
    disponibiliteApiService = {
      consulterDisponibilites: vi.fn(
        () => of(disponibilites)
      )
    };

    siteApiService = {
      listerSitesActifs: vi.fn(() => of(sites))
    };

    parametresMetierApiService = {
      consulterParametresMetier: vi.fn(
        () => of(parametresMetier)
      )
    };

    router = {
      navigate: vi.fn()
    };

    TestBed.configureTestingModule({
      providers: [
        DisponibilitesFacadeService,
        {
          provide: DisponibiliteApiService,
          useValue: disponibiliteApiService
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
          provide: Router,
          useValue: router
        }
      ]
    });

    service = TestBed.inject(
      DisponibilitesFacadeService
    );
  });

  it('doit initialiser le parcours avec les données du backend', () => {
    service.initialiser();

    expect(
      siteApiService.listerSitesActifs
    ).toHaveBeenCalled();

    expect(
      parametresMetierApiService.consulterParametresMetier
    ).toHaveBeenCalled();

    expect(service.sites()).toEqual(sites);
    expect(service.siteId()).toBe(1);
    expect(service.parametresMetier())
      .toEqual(parametresMetier);
    expect(service.dureeMatchLibelle()).toBe('1h30');
    expect(service.joursRapides()).toHaveLength(7);
    expect(service.date()).not.toBe('');
  });

  it('doit rechercher les disponibilités', () => {
    service.initialiser();
    service.modifierSiteId(2);
    service.modifierDate('2026-06-20');

    service.consulterDisponibilites();

    expect(
      disponibiliteApiService.consulterDisponibilites
    ).toHaveBeenCalledWith(2, '2026-06-20');

    expect(service.disponibilites())
      .toEqual(disponibilites);
    expect(service.chargementRecherche()).toBe(false);
    expect(service.messageErreur()).toBe('');
  });

  it('doit refuser une recherche incomplète', () => {
    service.initialiser();
    service.modifierSiteId(null);
    service.modifierDate('');

    service.consulterDisponibilites();

    expect(service.messageErreur()).toBe(
      'Le site et la date sont obligatoires.'
    );

    expect(
      disponibiliteApiService.consulterDisponibilites
    ).not.toHaveBeenCalled();
  });

  it('doit exposer une erreur de recherche du backend', () => {
    disponibiliteApiService.consulterDisponibilites
      .mockReturnValue(
        throwError(() => new HttpErrorResponse({
          status: 500,
          error: {
            message: 'Erreur backend disponibilités.'
          }
        }))
      );

    service.initialiser();
    service.modifierSiteId(2);
    service.modifierDate('2026-06-20');

    service.consulterDisponibilites();

    expect(service.messageErreur()).toBe(
      'Erreur backend disponibilités.'
    );
    expect(service.disponibilites()).toBeNull();
    expect(service.chargementRecherche()).toBe(false);
  });

  it('doit exposer une erreur de chargement des sites', () => {
    siteApiService.listerSitesActifs.mockReturnValue(
      throwError(() => new HttpErrorResponse({
        status: 500,
        error: {
          message: 'Erreur backend sites.'
        }
      }))
    );

    service.initialiser();

    expect(service.messageErreur()).toBe(
      'Erreur backend sites.'
    );
    expect(service.sites()).toEqual([]);
    expect(service.siteId()).toBeNull();
    expect(service.chargementSites()).toBe(false);
  });

  it('doit réinitialiser le résultat avec le choix rapide', () => {
    service.initialiser();
    service.modifierSiteId(2);
    service.modifierDate('2026-06-20');
    service.consulterDisponibilites();

    service.selectionnerJour('2026-06-21');

    expect(service.date()).toBe('2026-06-21');
    expect(service.disponibilites()).toBeNull();
    expect(service.messageErreur()).toBe('');
  });

  it('doit naviguer vers la création avec le créneau choisi', () => {
    const creneau = disponibilites.creneaux[0];

    service.allerCreerMatch(creneau);

    expect(router.navigate).toHaveBeenCalledWith(
      ['/joueur/creer-match'],
      {
        queryParams: {
          terrainId: 20,
          dateHeureDebut:
            '2026-06-20T09:00:00'
        }
      }
    );
  });
});
