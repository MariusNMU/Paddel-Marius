import { HttpErrorResponse } from '@angular/common/http';
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
import { SiteResponse } from '../models/site.model';
import { StatistiquesAdminResponse } from '../models/statistique.model';
import { AdminStatistiquesFacadeService } from './admin-statistiques-facade.service';
import { AdminStatsApiService } from './admin-stats-api.service';
import { AuthContextService } from './auth-context.service';
import { SiteApiService } from './site-api.service';

describe(
  'AdminStatistiquesFacadeService',
  () => {
    let service:
      AdminStatistiquesFacadeService;

    let adminStatsApiService: {
      consulterStatistiques:
        ReturnType<typeof vi.fn>;
    };

    let siteApiService: {
      listerSitesActifs:
        ReturnType<typeof vi.fn>;
    };

    let authContextService: {
      admin:
        ReturnType<typeof vi.fn>;
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

    const statistiques:
      StatistiquesAdminResponse = {
      dateDebut: '2026-07-01',
      dateFin: '2026-07-31',
      siteId: null,
      nomSite: null,
      nombreMatches: 2,
      nombreMatchesAVenir: 1,
      nombreMatchesTermines: 1,
      nombrePaiements: 2,
      chiffreAffaires: 45,
      nombreDettesOuvertes: 1,
      montantDettesOuvertes: 30,
      nombreParticipationsActives: 6,
      capaciteTheoriqueJoueurs: 8,
      tauxRemplissage: 75
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

      adminStatsApiService = {
        consulterStatistiques:
          vi.fn(() => of(statistiques))
      };

      siteApiService = {
        listerSitesActifs:
          vi.fn(() => of(sites))
      };

      authContextService = {
        admin:
          vi.fn(() => adminGlobal)
      };

      TestBed.configureTestingModule({
        providers: [
          AdminStatistiquesFacadeService,
          {
            provide:
            AdminStatsApiService,
            useValue:
            adminStatsApiService
          },
          {
            provide:
            SiteApiService,
            useValue:
            siteApiService
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
        AdminStatistiquesFacadeService
      );
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it(
      'doit initialiser la période démo et les sites pour un admin global',
      () => {
        service.initialiser();

        expect(service.admin())
          .toEqual(adminGlobal);

        expect(service.dateDebut())
          .toBe('2026-07-03');

        expect(service.dateFin())
          .toBe('2026-07-31');

        expect(service.siteId())
          .toBeNull();

        expect(service.sites())
          .toEqual(sites);

        expect(
          siteApiService
            .listerSitesActifs
        ).toHaveBeenCalled();

        expect(
          service.estAdminGlobal()
        ).toBe(true);
      }
    );

    it(
      'doit limiter un admin SITE à son propre site',
      () => {
        authContextService
          .admin
          .mockReturnValue(adminSite);

        service.initialiser();
        service.modifierSiteId(1002);

        expect(service.siteId())
          .toBe(1001);

        expect(service.sites())
          .toEqual([]);

        expect(
          siteApiService
            .listerSitesActifs
        ).not.toHaveBeenCalled();

        expect(
          service.estAdminGlobal()
        ).toBe(false);
      }
    );

    it(
      'doit sélectionner le mois courant',
      () => {
        service.initialiser();

        service.selectionnerPeriode(
          'moisCourant'
        );

        expect(service.dateDebut())
          .toBe('2026-07-01');

        expect(service.dateFin())
          .toBe('2026-07-31');
      }
    );

    it(
      'doit sélectionner les sept prochains jours',
      () => {
        service.initialiser();

        service.selectionnerPeriode(
          'prochainsJours'
        );

        expect(service.dateDebut())
          .toBe('2026-07-17');

        expect(service.dateFin())
          .toBe('2026-07-24');
      }
    );

    it(
      'doit charger les statistiques globales',
      () => {
        service.initialiser();

        service.modifierDateDebut(
          '2026-07-01'
        );

        service.modifierDateFin(
          '2026-07-31'
        );

        service.chargerStatistiques();

        expect(
          adminStatsApiService
            .consulterStatistiques
        ).toHaveBeenCalledWith(
          '2026-07-01',
          '2026-07-31',
          undefined
        );

        expect(service.statistiques())
          .toEqual(statistiques);

        expect(service.chargement())
          .toBe(false);
      }
    );

    it(
      'doit charger uniquement les statistiques du site autorisé',
      () => {
        authContextService
          .admin
          .mockReturnValue(adminSite);

        service.initialiser();
        service.modifierSiteId(1002);

        service.modifierDateDebut(
          '2026-07-01'
        );

        service.modifierDateFin(
          '2026-07-31'
        );

        service.chargerStatistiques();

        expect(
          adminStatsApiService
            .consulterStatistiques
        ).toHaveBeenCalledWith(
          '2026-07-01',
          '2026-07-31',
          1001
        );
      }
    );

    it(
      'doit refuser une période incomplète',
      () => {
        service.initialiser();

        service.modifierDateDebut('');

        service.chargerStatistiques();

        expect(
          service.messageErreur()
        ).toBe(
          'Les dates de début et de fin sont obligatoires.'
        );

        expect(
          adminStatsApiService
            .consulterStatistiques
        ).not.toHaveBeenCalled();
      }
    );

    it(
      'doit refuser une date de fin antérieure à la date de début',
      () => {
        service.initialiser();

        service.modifierDateDebut(
          '2026-07-31'
        );

        service.modifierDateFin(
          '2026-07-01'
        );

        service.chargerStatistiques();

        expect(
          service.messageErreur()
        ).toBe(
          'La date de fin doit être supérieure ou égale à la date de début.'
        );

        expect(
          adminStatsApiService
            .consulterStatistiques
        ).not.toHaveBeenCalled();
      }
    );

    it(
      'doit exposer une erreur de chargement des sites',
      () => {
        siteApiService
          .listerSitesActifs
          .mockReturnValue(
            throwError(
              () =>
                new HttpErrorResponse({
                  status: 500,
                  error: {
                    message:
                      'Erreur backend sites.'
                  }
                })
            )
          );

        service.initialiser();

        expect(
          service.messageErreur()
        ).toBe(
          'Erreur backend sites.'
        );

        expect(service.sites())
          .toEqual([]);

        expect(
          service.chargementSites()
        ).toBe(false);
      }
    );

    it(
      'doit exposer une erreur de chargement des statistiques',
      () => {
        adminStatsApiService
          .consulterStatistiques
          .mockReturnValue(
            throwError(
              () =>
                new HttpErrorResponse({
                  status: 500,
                  error: {
                    message:
                      'Erreur backend statistiques.'
                  }
                })
            )
          );

        service.initialiser();
        service.chargerStatistiques();

        expect(
          service.messageErreur()
        ).toBe(
          'Erreur backend statistiques.'
        );

        expect(
          service.statistiques()
        ).toBeNull();

        expect(service.chargement())
          .toBe(false);
      }
    );

    it(
      'doit refuser le chargement sans administrateur connecté',
      () => {
        authContextService
          .admin
          .mockReturnValue(null);

        service.initialiser();
        service.chargerStatistiques();

        expect(
          service.messageErreur()
        ).toBe(
          'Tu dois te connecter comme admin avant de consulter les statistiques.'
        );

        expect(
          siteApiService
            .listerSitesActifs
        ).not.toHaveBeenCalled();

        expect(
          adminStatsApiService
            .consulterStatistiques
        ).not.toHaveBeenCalled();
      }
    );
  }
);
