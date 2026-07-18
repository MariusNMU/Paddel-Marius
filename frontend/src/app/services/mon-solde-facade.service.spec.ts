import {
  HttpErrorResponse
} from '@angular/common/http';
import {
  signal,
  type Signal,
  type WritableSignal
} from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  NEVER,
  of,
  Subject,
  throwError
} from 'rxjs';
import { AuthJoueurResponse } from '../models/auth.model';
import { ParametresMetierResponse } from '../models/parametres-metier.model';
import { SoldeJoueurResponse } from '../models/solde-joueur.model';
import { AuthContextService } from './auth-context.service';
import { MonSoldeFacadeService } from './mon-solde-facade.service';
import { ParametresMetierApiService } from './parametres-metier-api.service';
import { SoldeJoueurApiService } from './solde-joueur-api.service';

describe('MonSoldeFacadeService', () => {
  let service: MonSoldeFacadeService;

  let joueurSignal:
    WritableSignal<AuthJoueurResponse | null>;

  let authContextService: {
    joueur:
      Signal<AuthJoueurResponse | null>;
  };

  let soldeJoueurApiService: {
    consulterSolde:
      ReturnType<typeof vi.fn>;
  };

  let parametresMetierApiService: {
    consulterParametresMetier:
      ReturnType<typeof vi.fn>;
  };

  const joueur: AuthJoueurResponse = {
    membreId: 2001,
    matricule: 'G1001',
    nom: 'Dupont',
    prenom: 'Marie',
    categorieMembre: 'GLOBAL',
    siteRattachementId: null,
    nomSiteRattachement: null,
    actif: true
  };

  const joueurDeux: AuthJoueurResponse = {
    ...joueur,
    membreId: 2002,
    matricule: 'G1002',
    nom: 'Lambert',
    prenom: 'Paul'
  };

  const parametres:
    ParametresMetierResponse = {
    dureeMatchMinutes: 90,
    pauseEntreMatchesMinutes: 15,
    nombreJoueursMaximum: 4,
    prixTotalMatch: 60,
    montantParticipationStandard: 15,
    soldeInitialJoueur: 100
  };

  const solde: SoldeJoueurResponse = {
    membreId: 2001,
    matricule: 'G1001',
    soldeCredit: 70
  };

  const soldeDeux:
    SoldeJoueurResponse = {
    membreId: 2002,
    matricule: 'G1002',
    soldeCredit: 100
  };

  const messageErreurGenerique =
    'Une erreur est survenue. Vérifie les données saisies puis réessaie.';

  beforeEach(() => {
    vi.useFakeTimers();

    joueurSignal =
      signal<AuthJoueurResponse | null>(
        joueur
      );

    authContextService = {
      joueur: joueurSignal.asReadonly()
    };

    soldeJoueurApiService = {
      consulterSolde:
        vi.fn((matricule: string) =>
          of(
            matricule === 'G1002'
              ? soldeDeux
              : solde
          )
        )
    };

    parametresMetierApiService = {
      consulterParametresMetier:
        vi.fn(() => of(parametres))
    };

    TestBed.configureTestingModule({
      providers: [
        MonSoldeFacadeService,
        {
          provide: AuthContextService,
          useValue: authContextService
        },
        {
          provide: SoldeJoueurApiService,
          useValue: soldeJoueurApiService
        },
        {
          provide:
          ParametresMetierApiService,
          useValue:
          parametresMetierApiService
        }
      ]
    });

    service = TestBed.inject(
      MonSoldeFacadeService
    );
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it(
    'doit charger les paramètres et le solde à l initialisation',
    () => {
      service.initialiser();

      expect(
        parametresMetierApiService
          .consulterParametresMetier
      ).toHaveBeenCalledTimes(1);

      expect(service.parametresMetier())
        .toEqual(parametres);

      expect(
        soldeJoueurApiService
          .consulterSolde
      ).toHaveBeenCalledWith('G1001');

      expect(service.solde())
        .toEqual(solde);

      expect(service.chargement())
        .toBe(false);
    }
  );

  it(
    'doit charger uniquement les paramètres sans joueur',
    () => {
      joueurSignal.set(null);
      TestBed.tick();

      service.initialiser();

      expect(service.parametresMetier())
        .toEqual(parametres);

      expect(service.solde()).toBeNull();

      expect(
        soldeJoueurApiService
          .consulterSolde
      ).not.toHaveBeenCalled();
    }
  );

  it(
    'doit refuser une actualisation sans joueur',
    () => {
      joueurSignal.set(null);
      TestBed.tick();

      service.initialiser();
      service.chargerSolde();

      expect(service.messageErreur())
        .toBe('Aucun joueur connecté.');

      expect(
        soldeJoueurApiService
          .consulterSolde
      ).not.toHaveBeenCalled();
    }
  );

  it(
    'doit exposer une erreur des paramètres métier',
    () => {
      parametresMetierApiService
        .consulterParametresMetier
        .mockReturnValue(
          throwError(
            () =>
              new HttpErrorResponse({
                status: 500,
                error: {
                  message:
                    'Paramètres indisponibles.'
                }
              })
          )
        );

      service.initialiser();

      expect(service.parametresMetier())
        .toBeNull();

      expect(service.messageErreur())
        .toBe(
          'Paramètres indisponibles.'
        );
    }
  );

  it(
    'doit exposer une erreur de solde',
    () => {
      soldeJoueurApiService
        .consulterSolde
        .mockReturnValue(
          throwError(
            () =>
              new HttpErrorResponse({
                status: 500,
                error: {
                  message:
                    'Solde indisponible.'
                }
              })
          )
        );

      service.initialiser();

      expect(service.solde()).toBeNull();

      expect(service.messageErreur())
        .toBe('Solde indisponible.');

      expect(service.chargement())
        .toBe(false);
    }
  );

  it(
    'doit terminer le chargement du solde après expiration du délai',
    () => {
      soldeJoueurApiService
        .consulterSolde
        .mockReturnValue(NEVER);

      service.initialiser();

      expect(service.chargement())
        .toBe(true);

      vi.advanceTimersByTime(10001);

      expect(service.messageErreur())
        .toBe(messageErreurGenerique);

      expect(service.chargement())
        .toBe(false);
    }
  );

  it(
    'doit terminer le chargement des paramètres après expiration du délai',
    () => {
      parametresMetierApiService
        .consulterParametresMetier
        .mockReturnValue(NEVER);

      service.initialiser();

      expect(
        service.chargementParametres()
      ).toBe(true);

      vi.advanceTimersByTime(10001);

      expect(service.messageErreur())
        .toBe(messageErreurGenerique);

      expect(
        service.chargementParametres()
      ).toBe(false);
    }
  );

  it(
    'doit charger le solde du nouveau joueur',
    () => {
      service.initialiser();

      joueurSignal.set(joueurDeux);
      TestBed.tick();

      expect(service.joueur())
        .toEqual(joueurDeux);

      expect(
        soldeJoueurApiService
          .consulterSolde
      ).toHaveBeenLastCalledWith(
        'G1002'
      );

      expect(service.solde())
        .toEqual(soldeDeux);

      expect(
        parametresMetierApiService
          .consulterParametresMetier
      ).toHaveBeenCalledTimes(1);
    }
  );

  it(
    'doit vider le solde après déconnexion',
    () => {
      service.initialiser();

      joueurSignal.set(null);
      TestBed.tick();

      expect(service.joueur()).toBeNull();
      expect(service.solde()).toBeNull();

      expect(service.parametresMetier())
        .toEqual(parametres);

      expect(service.messageErreur())
        .toBe('');

      expect(service.chargement())
        .toBe(false);
    }
  );

  it(
    'doit ignorer un ancien solde après un changement de joueur',
    () => {
      const ancienSolde$ =
        new Subject<SoldeJoueurResponse>();

      soldeJoueurApiService
        .consulterSolde
        .mockReset();

      soldeJoueurApiService
        .consulterSolde
        .mockReturnValueOnce(
          ancienSolde$
        )
        .mockReturnValueOnce(
          of(soldeDeux)
        );

      service.initialiser();

      joueurSignal.set(joueurDeux);
      TestBed.tick();

      ancienSolde$.next(solde);

      expect(service.solde())
        .toEqual(soldeDeux);
    }
  );
});
