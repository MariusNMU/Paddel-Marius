import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AuthAdminResponse } from '../models/auth.model';
import { MembreResponse } from '../models/membre.model';
import { SiteResponse } from '../models/site.model';
import { AdminMembreApiService } from './admin-membre-api.service';
import { AdminMembresFacadeService } from './admin-membres-facade.service';
import { AuthContextService } from './auth-context.service';
import { SiteApiService } from './site-api.service';

describe('AdminMembresFacadeService', () => {
  let service: AdminMembresFacadeService;

  let adminMembreApiService: {
    listerTousLesMembres: ReturnType<typeof vi.fn>;
    listerMembresParSite: ReturnType<typeof vi.fn>;
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

  const membreSite: MembreResponse = {
    membreId: 2002,
    matricule: 'S1001',
    nom: 'Martin',
    prenom: 'Sophie',
    categorieMembre: 'SITE',
    siteRattachementId: 1001,
    nomSiteRattachement: 'Padel Bruxelles',
    actif: true,
    soldeCredit: 100
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

  beforeEach(() => {
    adminMembreApiService = {
      listerTousLesMembres:
        vi.fn(() => of([membreSite])),
      listerMembresParSite:
        vi.fn(() => of([membreSite]))
    };

    siteApiService = {
      listerSitesActifs: vi.fn(() => of(sites))
    };

    authContextService = {
      admin: vi.fn(() => adminGlobal)
    };

    TestBed.configureTestingModule({
      providers: [
        AdminMembresFacadeService,
        {
          provide: AdminMembreApiService,
          useValue: adminMembreApiService
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
      AdminMembresFacadeService
    );
  });

  it('doit charger les sites et tous les membres pour un admin global', () => {
    service.initialiser();

    expect(
      siteApiService.listerSitesActifs
    ).toHaveBeenCalled();

    expect(
      adminMembreApiService.listerTousLesMembres
    ).toHaveBeenCalled();

    expect(service.admin()).toEqual(adminGlobal);
    expect(service.sites()).toEqual(sites);
    expect(service.membres()).toEqual([membreSite]);
    expect(service.titreResultat()).toBe(
      'Tous les membres'
    );
    expect(service.estAdminGlobal()).toBe(true);
  });

  it('doit limiter un admin SITE à son propre site', () => {
    authContextService.admin.mockReturnValue(adminSite);

    service.initialiser();

    expect(
      siteApiService.listerSitesActifs
    ).not.toHaveBeenCalled();

    expect(
      adminMembreApiService.listerTousLesMembres
    ).not.toHaveBeenCalled();

    expect(
      adminMembreApiService.listerMembresParSite
    ).toHaveBeenCalledWith(1001);

    expect(service.siteId()).toBe(1001);
    expect(service.membres()).toEqual([membreSite]);
    expect(service.titreResultat()).toContain(
      'Padel Bruxelles'
    );
    expect(service.estAdminGlobal()).toBe(false);
  });

  it('doit filtrer les membres selon le site sélectionné', () => {
    service.initialiser();
    service.modifierSiteId(1002);

    service.afficherMembresDuSiteSelectionne();

    expect(
      adminMembreApiService.listerMembresParSite
    ).toHaveBeenCalledWith(1002);

    expect(service.titreResultat()).toBe(
      'Membres rattachés au site Padel Liège'
    );
  });

  it('doit demander un site avant de filtrer', () => {
    service.initialiser();
    service.modifierSiteId(null);

    service.afficherMembresDuSiteSelectionne();

    expect(
      adminMembreApiService.listerMembresParSite
    ).not.toHaveBeenCalled();

    expect(service.messageErreur()).toBe(
      'Sélectionne un site avant de filtrer les membres.'
    );
  });

  it('doit refuser l action globale à un admin SITE', () => {
    authContextService.admin.mockReturnValue(adminSite);
    service.initialiser();

    service.afficherTousLesMembres();

    expect(
      adminMembreApiService.listerTousLesMembres
    ).not.toHaveBeenCalled();

    expect(service.messageErreur()).toBe(
      'Cette action est réservée aux administrateurs globaux.'
    );
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

  it('doit exposer une erreur de chargement des membres', () => {
    adminMembreApiService
      .listerTousLesMembres
      .mockReturnValue(
        throwError(() => new HttpErrorResponse({
          status: 500,
          error: {
            message: 'Erreur backend membres.'
          }
        }))
      );

    service.initialiser();

    expect(service.messageErreur()).toBe(
      'Erreur backend membres.'
    );
    expect(service.membres()).toEqual([]);
    expect(service.chargementMembres()).toBe(false);
  });

  it('doit refuser le chargement sans administrateur connecté', () => {
    authContextService.admin.mockReturnValue(null);

    service.initialiser();

    expect(service.messageErreur()).toBe(
      'Connecte-toi comme administrateur pour consulter les membres.'
    );

    expect(
      siteApiService.listerSitesActifs
    ).not.toHaveBeenCalled();

    expect(
      adminMembreApiService.listerTousLesMembres
    ).not.toHaveBeenCalled();
  });
});
