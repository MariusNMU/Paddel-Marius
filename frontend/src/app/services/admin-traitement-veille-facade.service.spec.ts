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
import { AuthAdminResponse } from '../models/auth.model';
import { TraitementVeilleResponse } from '../models/traitement-veille.model';
import { AdminTraitementVeilleApiService } from './admin-traitement-veille-api.service';
import { AdminTraitementVeilleFacadeService } from './admin-traitement-veille-facade.service';
import { AuthContextService } from './auth-context.service';

describe(
  'AdminTraitementVeilleFacadeService',
  () => {
    let service:
      AdminTraitementVeilleFacadeService;

    let adminSignal:
      WritableSignal<AuthAdminResponse | null>;

    let traitementVeilleApiService: {
      traiterVeille:
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
      TraitementVeilleResponse = {
      dateTraitement: '2026-07-17',
      dateMatchTraitee: '2026-07-18',
      matchesAnalyses: 3,
      matchesPassesPublics: 1,
      participationsLiberees: 2
    };

    beforeEach(() => {
      vi.useFakeTimers();

      vi.setSystemTime(
        new Date(
          2026,
          6,
          17,
          12,
          0,
          0
        )
      );

      adminSignal =
        signal<AuthAdminResponse | null>(
          adminGlobal
        );

      traitementVeilleApiService = {
        traiterVeille:
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
          AdminTraitementVeilleFacadeService,
          {
            provide:
            AdminTraitementVeilleApiService,
            useValue:
            traitementVeilleApiService
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
        AdminTraitementVeilleFacadeService
      );
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it(
      'doit initialiser la date du jour',
      () => {
        expect(service.dateTraitement())
          .toBe('2026-07-17');

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
      'doit sélectionner une date relative et réinitialiser le résultat',
      () => {
        service.lancerTraitement();

        expect(service.resultat())
          .toEqual(resultat);

        service.selectionnerDateRelative(2);

        expect(service.dateTraitement())
          .toBe('2026-07-19');

        expect(service.resultat())
          .toBeNull();

        expect(service.messageErreur())
          .toBeNull();
      }
    );

    it(
      'doit refuser une date vide',
      () => {
        service.selectionnerDate('');
        service.lancerTraitement();

        expect(service.messageErreur())
          .toBe(
            'La date de traitement est obligatoire.'
          );

        expect(
          traitementVeilleApiService
            .traiterVeille
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
          traitementVeilleApiService
            .traiterVeille
        ).not.toHaveBeenCalled();
      }
    );

    it(
      'doit lancer le traitement et exposer le résultat',
      () => {
        service.lancerTraitement();

        expect(
          traitementVeilleApiService
            .traiterVeille
        ).toHaveBeenCalledWith(
          '2026-07-17'
        );

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
        traitementVeilleApiService
          .traiterVeille
          .mockReturnValue(
            throwError(
              () =>
                new HttpErrorResponse({
                  status: 500,
                  error: {
                    message:
                      'Erreur backend traitement.'
                  }
                })
            )
          );

        service.lancerTraitement();

        expect(service.messageErreur())
          .toBe(
            'Erreur backend traitement.'
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
          new Subject<TraitementVeilleResponse>();

        traitementVeilleApiService
          .traiterVeille
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
