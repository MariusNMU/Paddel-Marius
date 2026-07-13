import {
  ComponentFixture,
  TestBed
} from '@angular/core/testing';
import { of } from 'rxjs';
import { AuthAdminResponse } from '../../models/auth.model';
import { MembreResponse } from '../../models/membre.model';
import { SiteResponse } from '../../models/site.model';
import { AdminMembreApiService } from '../../services/admin-membre-api.service';
import { AuthContextService } from '../../services/auth-context.service';
import { SiteApiService } from '../../services/site-api.service';
import { AdminMembresComponent } from './admin-membres.component';

describe('AdminMembresComponent', () => {
  let fixture: ComponentFixture<AdminMembresComponent>;
  let component: AdminMembresComponent;

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

  const sites: SiteResponse[] = [{
    siteId: 1001,
    code: 'BRU',
    nom: 'Padel Bruxelles',
    adresse: 'Rue du Padel 1'
  }];

  beforeEach(async () => {
    adminMembreApiService = {
      listerTousLesMembres:
        vi.fn(() => of([membreSite])),
      listerMembresParSite:
        vi.fn(() => of([membreSite]))
    };

    siteApiService = {
      listerSitesActifs:
        vi.fn(() => of(sites))
    };

    authContextService = {
      admin: vi.fn(() => adminGlobal)
    };

    await TestBed.configureTestingModule({
      imports: [AdminMembresComponent],
      providers: [
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
    }).compileComponents();

    fixture = TestBed.createComponent(
      AdminMembresComponent
    );
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('doit créer le composant', () => {
    expect(component).toBeTruthy();
  });

  it('doit charger tous les membres pour un admin global', () => {
    expect(
      siteApiService.listerSitesActifs
    ).toHaveBeenCalled();

    expect(
      adminMembreApiService.listerTousLesMembres
    ).toHaveBeenCalled();

    expect(component.membres).toEqual([membreSite]);

    expect(
      fixture.nativeElement.textContent
    ).toContain('Afficher tous les membres');
  });

  it('doit limiter automatiquement un admin SITE à son propre site', () => {
    fixture.destroy();
    vi.clearAllMocks();

    authContextService.admin.mockReturnValue(adminSite);

    fixture = TestBed.createComponent(
      AdminMembresComponent
    );
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(
      adminMembreApiService.listerTousLesMembres
    ).not.toHaveBeenCalled();

    expect(
      siteApiService.listerSitesActifs
    ).not.toHaveBeenCalled();

    expect(
      adminMembreApiService.listerMembresParSite
    ).toHaveBeenCalledWith(1001);

    expect(component.siteId).toBe(1001);
    expect(component.membres).toEqual([membreSite]);

    expect(
      fixture.nativeElement.textContent
    ).not.toContain('Afficher tous les membres');

    expect(
      fixture.nativeElement.textContent
    ).toContain('Padel Bruxelles');
  });

  it('doit refuser l action globale si elle est appelée pour un admin SITE', () => {
    fixture.destroy();
    vi.clearAllMocks();

    authContextService.admin.mockReturnValue(adminSite);

    fixture = TestBed.createComponent(
      AdminMembresComponent
    );
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.afficherTousLesMembres();

    expect(
      adminMembreApiService.listerTousLesMembres
    ).not.toHaveBeenCalled();

    expect(component.messageErreur).toBe(
      'Cette action est réservée aux administrateurs globaux.'
    );
  });
});
