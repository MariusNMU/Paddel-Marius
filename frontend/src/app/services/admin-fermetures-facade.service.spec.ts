import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AuthAdminResponse } from '../models/auth.model';
import { FermetureAdminResponse } from '../models/fermeture.model';
import { SiteResponse } from '../models/site.model';
import { AdminFermetureApiService } from './admin-fermeture-api.service';
import { AdminFermeturesFacadeService } from './admin-fermetures-facade.service';
import { AuthContextService } from './auth-context.service';
import { SiteApiService } from './site-api.service';

describe('AdminFermeturesFacadeService', () => {
  let service: AdminFermeturesFacadeService;

  let adminFermetureApiService: {
    creerFermeture: ReturnType<typeof vi.fn>;
  };

  let siteApiService: {
    listerSitesActifs: ReturnType<typeof vi.fn>;
  };

  let authContextService: {
    admin: ReturnType<typeof vi.fn>;
  };

  const adminGlobal: AuthAdminResponse = {
    administrateurId: 1,
    login: 'admin-global',
    nom: 'Admin',
    prenom: 'Global',
    roleAdministrateur: 'GLOBAL',
    siteId: null,
    nomSite: null,
    actif: true
  };

  const adminSite: AuthAdminResponse = {
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
      code: 'LIE',
      nom: 'Padel Liège',
      adresse: 'Rue du Padel 2'
    }
  ];

  const fermetureCreee: FermetureAdminResponse = {
    fermetureId: 50,
    dateFermeture: '2026-07-20',
    portee: 'GLOBALE',
    siteId: null,
    nomSite: null,
    motif: 'Maintenance',
    nombreMatchesAnnules: 2,
    nombreRemboursementsCredites: 3,
    montantTotalRembourse: 45
  };

  beforeEach(() => {
    adminFermetureApiService = {
      creerFermeture: vi.fn(() => of(fermetureCreee))
    };

    siteApiService = {
      listerSitesActifs: vi.fn(() => of(sites))
    };

    authContextService = {
      admin: vi.fn(() => adminGlobal)
    };

    TestBed.configureTestingModule({
      providers: [
        AdminFermeturesFacadeService,
        {
          provide: AdminFermetureApiService,
          useValue: adminFermetureApiService
        },
        {
          provide: SiteApiService,
          useValue: siteApiService
        },
        {
          provide: AuthContextService,
          useValue: authContextService
        }
      ]
    });

    service = TestBed.inject(
      AdminFermeturesFacadeService
    );
  });

  it('doit charger les sites pour un administrateur global', () => {
    service.initialiser();

    expect(siteApiService.listerSitesActifs).toHaveBeenCalled();
    expect(service.admin()).toEqual(adminGlobal);
    expect(service.sites()).toEqual(sites);
    expect(service.siteId()).toBe(1001);
    expect(service.portee()).toBe('');
    expect(service.estAdminGlobal()).toBe(true);
  });

  it('doit limiter un administrateur SITE à son propre site', () => {
    authContextService.admin.mockReturnValue(adminSite);

    service.initialiser();

    expect(siteApiService.listerSitesActifs).not.toHaveBeenCalled();
    expect(service.portee()).toBe('LOCALE');
    expect(service.siteId()).toBe(1001);
    expect(service.estAdminGlobal()).toBe(false);
    expect(service.nomSiteSelectionne()).toBe(
      'Padel Bruxelles (1001)'
    );
  });

  it('doit créer une fermeture globale sans site', () => {
    service.initialiser();
    service.modifierDateFermeture('2026-07-20');
    service.modifierPortee('GLOBALE');
    service.modifierMotif('  Maintenance  ');

    service.creerFermeture();

    expect(
      adminFermetureApiService.creerFermeture
    ).toHaveBeenCalledWith({
      dateFermeture: '2026-07-20',
      portee: 'GLOBALE',
      siteId: null,
      motif: 'Maintenance'
    });

    expect(service.fermetureCreee()).toEqual(fermetureCreee);
    expect(service.chargementCreation()).toBe(false);
  });

  it('doit créer une fermeture locale sur le site choisi', () => {
    service.initialiser();
    service.modifierDateFermeture('2026-07-20');
    service.modifierPortee('LOCALE');
    service.modifierSiteId(1002);

    service.creerFermeture();

    expect(
      adminFermetureApiService.creerFermeture
    ).toHaveBeenCalledWith({
      dateFermeture: '2026-07-20',
      portee: 'LOCALE',
      siteId: 1002,
      motif: ''
    });
  });

  it('doit refuser une création sans date', () => {
    service.initialiser();
    service.modifierPortee('GLOBALE');

    service.creerFermeture();

    expect(service.messageErreur()).toBe(
      'La date de fermeture est obligatoire.'
    );

    expect(
      adminFermetureApiService.creerFermeture
    ).not.toHaveBeenCalled();
  });

  it('doit conserver la portée et le site autorisés pour un admin SITE', () => {
    authContextService.admin.mockReturnValue(adminSite);
    service.initialiser();

    service.modifierPortee('GLOBALE');
    service.modifierSiteId(1002);
    service.modifierDateFermeture('2026-07-20');
    service.creerFermeture();

    expect(service.portee()).toBe('LOCALE');
    expect(service.siteId()).toBe(1001);

    expect(
      adminFermetureApiService.creerFermeture
    ).toHaveBeenCalledWith({
      dateFermeture: '2026-07-20',
      portee: 'LOCALE',
      siteId: 1001,
      motif: ''
    });
  });

  it('doit exposer une erreur de création du backend', () => {
    adminFermetureApiService.creerFermeture.mockReturnValue(
      throwError(() => new HttpErrorResponse({
        status: 409,
        error: {
          message: 'Une fermeture existe déjà pour cette date.'
        }
      }))
    );

    service.initialiser();
    service.modifierDateFermeture('2026-07-20');
    service.modifierPortee('GLOBALE');

    service.creerFermeture();

    expect(service.messageErreur()).toBe(
      'Une fermeture existe déjà pour cette date.'
    );
    expect(service.fermetureCreee()).toBeNull();
    expect(service.chargementCreation()).toBe(false);
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

  it('doit refuser la création sans administrateur connecté', () => {
    authContextService.admin.mockReturnValue(null);

    service.initialiser();
    service.creerFermeture();

    expect(service.messageErreur()).toBe(
      'Connecte-toi comme administrateur pour créer une fermeture.'
    );
    expect(siteApiService.listerSitesActifs).not.toHaveBeenCalled();
    expect(
      adminFermetureApiService.creerFermeture
    ).not.toHaveBeenCalled();
  });
});
