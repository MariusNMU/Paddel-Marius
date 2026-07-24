import { signal, WritableSignal } from '@angular/core';
import {
  ComponentFixture,
  TestBed
} from '@angular/core/testing';
import { AuthAdminResponse } from '../../models/auth.model';
import {
  FermetureAdminResponse,
  PorteeFermeture
} from '../../models/fermeture.model';
import { SiteResponse } from '../../models/site.model';
import { AdminFermeturesFacadeService } from '../../services/admin-fermetures-facade.service';
import { AdminFermeturesComponent } from './admin-fermetures.component';

describe('AdminFermeturesComponent', () => {
  let fixture: ComponentFixture<AdminFermeturesComponent>;
  let component: AdminFermeturesComponent;

  let facade: {
    initialiser: ReturnType<typeof vi.fn>;
    estAdminGlobal: ReturnType<typeof vi.fn>;
    modifierDateFermeture: ReturnType<typeof vi.fn>;
    modifierPortee: ReturnType<typeof vi.fn>;
    modifierSiteId: ReturnType<typeof vi.fn>;
    modifierMotif: ReturnType<typeof vi.fn>;
    nomSiteSelectionne: ReturnType<typeof vi.fn>;
    creerFermeture: ReturnType<typeof vi.fn>;

    admin: WritableSignal<AuthAdminResponse | null>;
    sites: WritableSignal<SiteResponse[]>;
    dateFermeture: WritableSignal<string>;
    portee: WritableSignal<PorteeFermeture | ''>;
    siteId: WritableSignal<number | null>;
    motif: WritableSignal<string>;
    chargementSites: WritableSignal<boolean>;
    chargementCreation: WritableSignal<boolean>;
    messageErreur: WritableSignal<string>;
    fermetureCreee:
      WritableSignal<FermetureAdminResponse | null>;
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

  const site: SiteResponse = {
    siteId: 1001,
    code: 'BRU',
    nom: 'Padel Bruxelles',
    adresse: 'Rue du Padel 1'
  };

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

  beforeEach(async () => {
    facade = {
      initialiser: vi.fn(),
      estAdminGlobal: vi.fn(
        () => facade.admin()?.roleAdministrateur === 'GLOBAL'
      ),
      modifierDateFermeture: vi.fn(),
      modifierPortee: vi.fn(),
      modifierSiteId: vi.fn(),
      modifierMotif: vi.fn(),
      nomSiteSelectionne: vi.fn(
        () => 'Padel Bruxelles'
      ),
      creerFermeture: vi.fn(),

      admin: signal(adminGlobal),
      sites: signal([site]),
      dateFermeture: signal(''),
      portee: signal(''),
      siteId: signal(1001),
      motif: signal(''),
      chargementSites: signal(false),
      chargementCreation: signal(false),
      messageErreur: signal(''),
      fermetureCreee: signal(null)
    };

    await TestBed.configureTestingModule({
      imports: [AdminFermeturesComponent]
    })
      .overrideComponent(AdminFermeturesComponent, {
        set: {
          providers: [
            {
              provide: AdminFermeturesFacadeService,
              useValue: facade
            }
          ]
        }
      })
      .compileComponents();

    fixture = TestBed.createComponent(
      AdminFermeturesComponent
    );
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('doit créer le composant et initialiser la façade', () => {
    expect(component).toBeTruthy();
    expect(facade.initialiser).toHaveBeenCalled();
  });

  it('doit déléguer la création à la façade', () => {
    component.creerFermeture();

    expect(facade.creerFermeture).toHaveBeenCalled();
  });

  it('doit proposer les portées autorisées à un admin global', () => {
    facade.portee.set('LOCALE');
    fixture.detectChanges();

    const contenu = fixture.nativeElement.textContent;
    const selectionSite = fixture.nativeElement.querySelector(
      'select#siteId'
    );

    expect(contenu).toContain('Globale — tous les sites');
    expect(contenu).toContain('Locale — un site précis');
    expect(selectionSite).not.toBeNull();
  });

  it('doit masquer les choix interdits à un admin SITE', () => {
    facade.admin.set(adminSite);
    facade.portee.set('LOCALE');
    fixture.detectChanges();

    const contenu = fixture.nativeElement.textContent;
    const selectionPortee = fixture.nativeElement.querySelector(
      'select#portee'
    );
    const selectionSite = fixture.nativeElement.querySelector(
      'select#siteId'
    );

    expect(contenu).not.toContain('Globale — tous les sites');
    expect(contenu).toContain('Padel Bruxelles');
    expect(contenu).not.toContain('(1001)');
    expect(selectionPortee).toBeNull();
    expect(selectionSite).toBeNull();
  });

  it('doit afficher le bilan complet de la fermeture', () => {
    facade.fermetureCreee.set(fermetureCreee);
    fixture.detectChanges();

    const contenu = fixture.nativeElement.textContent;

    expect(contenu).toContain('Fermeture créée avec succès');
    expect(contenu).toContain('Matches annulés');
    expect(contenu).toContain('Remboursements crédités');
    expect(contenu).toContain('Montant total remboursé');
    expect(contenu).toContain('45.00');
  });
});
