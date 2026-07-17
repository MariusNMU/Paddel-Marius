import { signal, WritableSignal } from '@angular/core';
import {
  ComponentFixture,
  TestBed
} from '@angular/core/testing';
import { AuthAdminResponse } from '../../models/auth.model';
import { MembreResponse } from '../../models/membre.model';
import { SiteResponse } from '../../models/site.model';
import { AdminMembresFacadeService } from '../../services/admin-membres-facade.service';
import { AdminMembresComponent } from './admin-membres.component';

describe('AdminMembresComponent', () => {
  let fixture: ComponentFixture<AdminMembresComponent>;
  let component: AdminMembresComponent;

  let facade: {
    initialiser: ReturnType<typeof vi.fn>;
    estAdminGlobal: ReturnType<typeof vi.fn>;
    modifierSiteId: ReturnType<typeof vi.fn>;
    afficherTousLesMembres: ReturnType<typeof vi.fn>;
    afficherMembresDuSiteSelectionne:
      ReturnType<typeof vi.fn>;

    admin: WritableSignal<AuthAdminResponse | null>;
    sites: WritableSignal<SiteResponse[]>;
    siteId: WritableSignal<number | null>;
    membres: WritableSignal<MembreResponse[]>;
    chargementSites: WritableSignal<boolean>;
    chargementMembres: WritableSignal<boolean>;
    messageErreur: WritableSignal<string>;
    titreResultat: WritableSignal<string>;
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

  const site: SiteResponse = {
    siteId: 1001,
    code: 'BRU',
    nom: 'Padel Bruxelles',
    adresse: 'Rue du Padel 1'
  };

  beforeEach(async () => {
    facade = {
      initialiser: vi.fn(),

      estAdminGlobal: vi.fn(
        () =>
          facade.admin()
            ?.roleAdministrateur === 'GLOBAL'
      ),

      modifierSiteId: vi.fn(),
      afficherTousLesMembres: vi.fn(),
      afficherMembresDuSiteSelectionne: vi.fn(),

      admin: signal(adminGlobal),
      sites: signal([site]),
      siteId: signal(1001),
      membres: signal([membreSite]),
      chargementSites: signal(false),
      chargementMembres: signal(false),
      messageErreur: signal(''),
      titreResultat: signal('Tous les membres')
    };

    await TestBed.configureTestingModule({
      imports: [AdminMembresComponent]
    })
      .overrideComponent(AdminMembresComponent, {
        set: {
          providers: [
            {
              provide: AdminMembresFacadeService,
              useValue: facade
            }
          ]
        }
      })
      .compileComponents();

    fixture = TestBed.createComponent(
      AdminMembresComponent
    );
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('doit créer le composant et initialiser la façade', () => {
    expect(component).toBeTruthy();
    expect(facade.initialiser).toHaveBeenCalled();
  });

  it('doit déléguer l affichage de tous les membres', () => {
    component.afficherTousLesMembres();

    expect(
      facade.afficherTousLesMembres
    ).toHaveBeenCalled();
  });

  it('doit déléguer le filtre par site', () => {
    component.afficherMembresDuSiteSelectionne();

    expect(
      facade.afficherMembresDuSiteSelectionne
    ).toHaveBeenCalled();
  });

  it('doit afficher les actions globales et les membres', () => {
    const contenu = fixture.nativeElement.textContent;

    expect(contenu).toContain(
      'Afficher tous les membres'
    );
    expect(contenu).toContain(
      'Filtrer par site sélectionné'
    );
    expect(contenu).toContain('S1001');
    expect(contenu).toContain('Sophie');
  });

  it('doit masquer les actions globales à un admin SITE', () => {
    facade.admin.set(adminSite);
    fixture.detectChanges();

    const contenu = fixture.nativeElement.textContent;

    expect(contenu).not.toContain(
      'Afficher tous les membres'
    );
    expect(contenu).not.toContain(
      'Filtrer par site sélectionné'
    );
    expect(contenu).toContain('Padel Bruxelles');
  });
});
