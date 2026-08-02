import { HttpErrorResponse } from '@angular/common/http';
import {
  computed,
  signal,
  type Signal,
  type WritableSignal
} from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  of,
  Subject,
  throwError
} from 'rxjs';
import {
  AuthAdminResponse
} from '../models/auth.model';
import {
  TraitementEcheanceResponse
} from '../models/traitement-echeance.model';
import {
  AdminTraitementEcheanceApiService
} from './admin-traitement-echeance-api.service';
import {
  AdminTraitementEcheanceFacadeService
} from './admin-traitement-echeance-facade.service';
import {
  AuthContextService
} from './auth-context.service';

describe(
  'AdminTraitementEcheanceFacadeService',
  () => {
    let service:
      AdminTraitementEcheanceFacadeService;

    let adminSignal:
      WritableSignal<AuthAdminResponse | null>;

    let traitementEcheanceApiService: {
      traiterEcheance:
        ReturnType<typeof vi.fn>;
    };

    let authContextService: {
      admin:
        Signal<AuthAdminResponse | null>;
      adminConnecte:
        Signal<boolean>;
    };

    const adminGlobal:
      AuthAdminResponse = {
      administrateurId: 1,
      login: 'admin-global',
      nom: 'Admin',
      prenom: 'Global',
      roleAdministrateur: 'GLOBAL',
      siteId: null,
      nomSite: null,
      actif: true
    };

    const adminSite:
      AuthAdminResponse = {
      administrateurId: 2,
      login: 'admin-bruxelles',
      nom: 'Admin',
      prenom: 'Bruxelles',
      roleAdministrateur: 'SITE',
      siteId: 1001,
      nomSite: 'Padel Bruxelles',
      actif: true
    };

    const resultat:
      TraitementEcheanceResponse = {
      dateHeureTraitement:
        '2026-07-20T17:00:00',
      matchesAnalyses: 3,
      matchesDemarres: 2,
      matchesTermines: 1,
      dettesCreees: 1
    };

    beforeEach(() => {
      adminSignal =
        signal<AuthAdminResponse | null>(
          adminGlobal
        );

      traitementEcheanceApiService = {
        traiterEcheance:
          vi.fn(() => of(resultat))
      };

      authContextService = {
        admin: adminSignal.asReadonly(),

        adminConnecte: computed(
          () => adminSignal() !== null
        )
      };

      TestBed.configureTestingModule({
        providers: [
          AdminTraitementEcheanceFacadeService,
          {
            provide:
            AdminTraitementEcheanceApiService,
            useValue:
            traitementEcheanceApiService
          },
          {
            provide:
            AuthContextService,
            useValue:
            authContextService
          }
        ]
      });

      service = TestBed.inject(
        AdminTraitementEcheanceFacadeService
      );
    });

    it(
      'doit initialiser la façade sans résultat',
      () => {
        expect(service.adminConnecte())
          .toBe(true);

        expect(service.estAdminGlobal())
          .toBe(true);

        expect(service.chargement())
          .toBe(false);

        expect(service.resultat())
          .toBeNull();
      }
    );

    it(
      'doit refuser le traitement sans administrateur',
      () => {
        adminSignal.set(null);
        TestBed.tick();

        service.lancerTraitement();

        expect(service.messageErreur())
          .toBe(
            'Tu dois te connecter comme admin avant de lancer le traitement d’échéance.'
          );

        expect(
          traitementEcheanceApiService
            .traiterEcheance
        ).not.toHaveBeenCalled();
      }
    );

    it(
      'doit refuser le traitement à un admin SITE',
      () => {
        adminSignal.set(adminSite);
        TestBed.tick();

        service.lancerTraitement();

        expect(service.estAdminGlobal())
          .toBe(false);

        expect(service.messageErreur())
          .toBe(
            'Cette action est réservée aux administrateurs globaux.'
          );

        expect(
          traitementEcheanceApiService
            .traiterEcheance
        ).not.toHaveBeenCalled();
      }
    );

    it(
      'doit lancer le traitement et exposer le résultat',
      () => {
        service.lancerTraitement();

        expect(
          traitementEcheanceApiService
            .traiterEcheance
        ).toHaveBeenCalledTimes(1);

        expect(service.resultat())
          .toEqual(resultat);

        expect(service.messageErreur())
          .toBeNull();

        expect(service.chargement())
          .toBe(false);
      }
    );

    it(
      'doit exposer une erreur du backend',
      () => {
        traitementEcheanceApiService
          .traiterEcheance
          .mockReturnValue(
            throwError(
              () =>
                new HttpErrorResponse({
                  status: 500,
                  error: {
                    message:
                      'Erreur backend échéance.'
                  }
                })
            )
          );

        service.lancerTraitement();

        expect(service.messageErreur())
          .toBe(
            'Erreur backend échéance.'
          );

        expect(service.resultat())
          .toBeNull();

        expect(service.chargement())
          .toBe(false);
      }
    );

    it(
      'doit ignorer une réponse de l ancienne session',
      () => {
        const traitementEnAttente$ =
          new Subject<TraitementEcheanceResponse>();

        traitementEcheanceApiService
          .traiterEcheance
          .mockReturnValue(
            traitementEnAttente$
          );

        service.lancerTraitement();

        expect(service.chargement())
          .toBe(true);

        adminSignal.set(null);
        TestBed.tick();

        expect(service.adminConnecte())
          .toBe(false);

        expect(service.chargement())
          .toBe(false);

        expect(service.resultat())
          .toBeNull();

        traitementEnAttente$.next(
          resultat
        );

        expect(service.resultat())
          .toBeNull();
      }
    );
  }
);
