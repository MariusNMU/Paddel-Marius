import { HttpErrorResponse } from '@angular/common/http';
import {
  signal,
  type Signal,
  type WritableSignal
} from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi
} from 'vitest';
import {
  of,
  throwError
} from 'rxjs';
import { AuthAdminResponse } from '../models/auth.model';
import {
  EtatOperationnelAdminResponse,
  OccupationHebdomadaireAdminResponse
} from '../models/etat-operationnel.model';
import { SiteResponse } from '../models/site.model';
import {
  AdminEtatOperationnelApiService
} from './admin-etat-operationnel-api.service';
import {
  AdminEtatOperationnelFacadeService
} from './admin-etat-operationnel-facade.service';
import {
  AuthContextService
} from './auth-context.service';

describe(
  'AdminEtatOperationnelFacadeService',
  () => {
    let service:
      AdminEtatOperationnelFacadeService;

    let etatOperationnelApiService: {
      consulterEtatOperationnel:
        ReturnType<typeof vi.fn>;
      consulterOccupationHebdomadaire:
        ReturnType<typeof vi.fn>;
      listerTousSites:
        ReturnType<typeof vi.fn>;
    };

    let adminSignal:
      WritableSignal<AuthAdminResponse | null>;

    let authContextService: {
      admin:
        Signal<AuthAdminResponse | null>;
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

    const sites: SiteResponse[] = [
      {
        siteId: 1001,
        code: 'BRU',
        nom: 'Padel Bruxelles',
        adresse: 'Rue du Padel 1'
      },
      {
        siteId: 1002,
        code: 'NAM',
        nom: 'Padel Namur',
        adresse: 'Rue du Padel 2'
      }
    ];

    const etatOperationnel:
      EtatOperationnelAdminResponse = {
      date: '2026-07-20',
      siteId: 1001,
      nomSite: 'Padel Bruxelles',
      siteActif: true,
      ferme: false,
      motifFermeture: null,
      terrains: []
    };

    const occupationHebdomadaire:
      OccupationHebdomadaireAdminResponse = {
      dateDebut: '2026-07-20',
      dateFin: '2026-07-26',
      siteId: 1001,
      nomSite: 'Padel Bruxelles',
      siteActif: true,
      jours: [etatOperationnel]
    };

    beforeEach(() => {
      vi.useFakeTimers();

      vi.setSystemTime(
        new Date(
          2026,
          6,
          20,
          12,
          0,
          0
        )
      );

      etatOperationnelApiService = {
        consulterEtatOperationnel:
          vi.fn(
            () => of(etatOperationnel)
          ),
        consulterOccupationHebdomadaire:
          vi.fn(
            () => of(occupationHebdomadaire)
          ),
        listerTousSites:
          vi.fn(() => of(sites))
      };

      adminSignal =
        signal<AuthAdminResponse | null>(
          adminGlobal
        );

      authContextService = {
        admin: adminSignal.asReadonly()
      };

      TestBed.configureTestingModule({
        providers: [
          AdminEtatOperationnelFacadeService,
          {
            provide:
            AdminEtatOperationnelApiService,
            useValue:
            etatOperationnelApiService
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
        AdminEtatOperationnelFacadeService
      );
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it(
      'doit initialiser la date et les sites pour un admin global',
      () => {
        service.initialiser();

        expect(service.date())
          .toBe('2026-07-20');

        expect(service.sites())
          .toEqual(sites);

        expect(service.siteId())
          .toBe(1001);

        expect(
          etatOperationnelApiService
            .listerTousSites
        ).toHaveBeenCalledTimes(1);

        expect(
          service.estAdminGlobal()
        ).toBe(true);
      }
    );

    it(
      'doit limiter un admin SITE à son site',
      () => {
        adminSignal.set(adminSite);

        service.initialiser();
        service.modifierSiteId(1002);

        expect(service.siteId())
          .toBe(1001);

        expect(service.sites())
          .toEqual([]);

        expect(
          etatOperationnelApiService
            .listerTousSites
        ).not.toHaveBeenCalled();

        expect(
          service.estAdminGlobal()
        ).toBe(false);
      }
    );

    it(
      'doit charger l état du site sélectionné',
      () => {
        service.initialiser();

        service.modifierDate(
          '2026-07-20'
        );

        service.modifierSiteId(1002);
        service.chargerEtatOperationnel();

        expect(
          etatOperationnelApiService
            .consulterEtatOperationnel
        ).toHaveBeenCalledWith(
          '2026-07-20',
          1002
        );

        expect(
          service.etatOperationnel()
        ).toEqual(etatOperationnel);

        expect(service.chargement())
          .toBe(false);
      }
    );

    it(
      'doit utiliser le site imposé à l admin SITE',
      () => {
        adminSignal.set(adminSite);

        service.initialiser();
        service.modifierSiteId(1002);
        service.chargerEtatOperationnel();

        expect(
          etatOperationnelApiService
            .consulterEtatOperationnel
        ).toHaveBeenCalledWith(
          '2026-07-20',
          1001
        );
      }
    );

    it(
      'doit charger l occupation hebdomadaire du site sélectionné',
      () => {
        service.initialiser();
        service.modifierDate('2026-07-22');
        service.modifierSiteId(1002);

        service.chargerOccupationHebdomadaire();

        expect(
          etatOperationnelApiService
            .consulterOccupationHebdomadaire
        ).toHaveBeenCalledWith(
          '2026-07-22',
          1002
        );

        expect(
          service.occupationHebdomadaire()
        ).toEqual(occupationHebdomadaire);

        expect(service.chargement())
          .toBe(false);
      }
    );

    it(
      'doit naviguer vers les semaines précédente et courante',
      () => {
        service.initialiser();
        service.modifierDate('2026-07-22');

        service.decalerSemaine(-1);

        expect(service.date())
          .toBe('2026-07-15');

        expect(
          etatOperationnelApiService
            .consulterOccupationHebdomadaire
        ).toHaveBeenLastCalledWith(
          '2026-07-15',
          1001
        );

        service.selectionnerSemaineCourante();

        expect(service.date())
          .toBe('2026-07-20');

        expect(
          etatOperationnelApiService
            .consulterOccupationHebdomadaire
        ).toHaveBeenLastCalledWith(
          '2026-07-20',
          1001
        );
      }
    );

    it(
      'doit demander un site à l admin GLOBAL',
      () => {
        etatOperationnelApiService
          .listerTousSites
          .mockReturnValue(of([]));

        service.initialiser();
        service.chargerEtatOperationnel();

        expect(service.messageErreur())
          .toBe('Sélectionne un site.');

        expect(
          etatOperationnelApiService
            .consulterEtatOperationnel
        ).not.toHaveBeenCalled();
      }
    );

    it(
      'doit exposer l erreur renvoyée par le backend',
      () => {
        etatOperationnelApiService
          .consulterEtatOperationnel
          .mockReturnValue(
            throwError(
              () =>
                new HttpErrorResponse({
                  status: 500,
                  error: {
                    message:
                      'Erreur backend.'
                  }
                })
            )
          );

        service.initialiser();
        service.chargerEtatOperationnel();

        expect(service.messageErreur())
          .toBe('Erreur backend.');

        expect(
          service.etatOperationnel()
        ).toBeNull();

        expect(service.chargement())
          .toBe(false);
      }
    );

    it(
      'doit exposer une erreur de chargement hebdomadaire',
      () => {
        etatOperationnelApiService
          .consulterOccupationHebdomadaire
          .mockReturnValue(
            throwError(
              () =>
                new HttpErrorResponse({
                  status: 500,
                  error: {
                    message:
                      'Planning indisponible.'
                  }
                })
            )
          );

        service.initialiser();
        service.chargerOccupationHebdomadaire();

        expect(service.messageErreur())
          .toBe('Planning indisponible.');

        expect(
          service.occupationHebdomadaire()
        ).toBeNull();

        expect(service.chargement())
          .toBe(false);
      }
    );
  }
);
