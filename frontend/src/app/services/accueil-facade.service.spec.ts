import { TestBed } from '@angular/core/testing';
import {
  of,
  Subject,
  throwError
} from 'rxjs';
import { PresentationDemoResponse } from '../models/donnees-demonstration.model';
import { AccueilFacadeService } from './accueil-facade.service';
import { PresentationDemoApiService } from './presentation-demo-api.service';

describe('AccueilFacadeService', () => {
  let service: AccueilFacadeService;

  let presentationDemoApiService: {
    consulterPresentation:
      ReturnType<typeof vi.fn>;
  };

  const response: PresentationDemoResponse = {
    categoriesMembres: [
      {
        prefixe: 'G',
        categorie: 'GLOBAL',
        regle: "Peut réserver jusqu'à 21 jours avant."
      }
    ],
    sites: [
      {
        siteId: 1001,
        code: 'BRU',
        nom: 'Padel Bruxelles',
        adresse: 'Rue du Padel 1'
      }
    ],
    joueurs: [
      {
        matricule: 'G1001',
        motDePasse: 'password',
        description: 'joueur GLOBAL actif'
      }
    ],
    administrateurs: [
      {
        login: 'admin-global',
        motDePasse: 'secret',
        description: 'administrateur GLOBAL'
      }
    ]
  };

  beforeEach(() => {
    presentationDemoApiService = {
      consulterPresentation:
        vi.fn(() => of(response))
    };

    TestBed.configureTestingModule({
      providers: [
        AccueilFacadeService,
        {
          provide:
          PresentationDemoApiService,
          useValue:
          presentationDemoApiService
        }
      ]
    });

    service = TestBed.inject(
      AccueilFacadeService
    );
  });

  it('doit charger la présentation de démonstration', () => {
    service.initialiser();

    expect(
      service.donneesDemonstration()
    ).toEqual(response);

    expect(service.chargement())
      .toBe(false);

    expect(service.messageErreur())
      .toBe('');
  });

  it('doit exposer le chargement pendant l appel HTTP', () => {
    const attente$ =
      new Subject<PresentationDemoResponse>();

    presentationDemoApiService
      .consulterPresentation
      .mockReturnValue(attente$);

    service.initialiser();

    expect(service.chargement())
      .toBe(true);

    attente$.next(response);
    attente$.complete();

    expect(service.chargement())
      .toBe(false);

    expect(
      service.donneesDemonstration()
    ).toEqual(response);
  });

  it('doit afficher un message lorsque le mode démo est indisponible', () => {
    presentationDemoApiService
      .consulterPresentation
      .mockReturnValue(
        throwError(
          () => new Error('indisponible')
        )
      );

    service.initialiser();

    expect(
      service.donneesDemonstration()
    ).toBeNull();

    expect(service.chargement())
      .toBe(false);

    expect(service.messageErreur())
      .toContain(
        'données de démonstration ne sont pas disponibles'
      );
  });
});
