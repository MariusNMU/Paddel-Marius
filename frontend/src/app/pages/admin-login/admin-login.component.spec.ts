import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthAdminResponse } from '../../models/auth.model';
import { AuthFacadeService } from '../../services/auth-facade.service';
import { AdminLoginComponent } from './admin-login.component';

describe('AdminLoginComponent', () => {
  let fixture: ComponentFixture<AdminLoginComponent>;
  let component: AdminLoginComponent;

  const admin: AuthAdminResponse = {
    administrateurId: 1,
    login: 'admin-test',
    nom: 'Admin',
    prenom: 'Test',
    roleAdministrateur: 'GLOBAL',
    siteId: null,
    nomSite: null,
    actif: true
  };

  const adminSignal = signal<AuthAdminResponse | null>(null);
  const chargementAdminSignal = signal(false);
  const messageErreurAdminSignal = signal<string | null>(null);
  const messageSuccesAdminSignal = signal<string | null>(null);

  let authFacade: {
    admin: typeof adminSignal;
    chargementAdmin: typeof chargementAdminSignal;
    messageErreurAdmin: typeof messageErreurAdminSignal;
    messageSuccesAdmin: typeof messageSuccesAdminSignal;
    preparerConnexionAdmin: ReturnType<typeof vi.fn>;
    connecterAdmin: ReturnType<typeof vi.fn>;
    deconnecterAdmin: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    adminSignal.set(null);
    chargementAdminSignal.set(false);
    messageErreurAdminSignal.set(null);
    messageSuccesAdminSignal.set(null);

    authFacade = {
      admin: adminSignal,
      chargementAdmin: chargementAdminSignal,
      messageErreurAdmin: messageErreurAdminSignal,
      messageSuccesAdmin: messageSuccesAdminSignal,
      preparerConnexionAdmin: vi.fn(),
      connecterAdmin: vi.fn(),
      deconnecterAdmin: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [AdminLoginComponent],
      providers: [
        provideRouter([]),
        {
          provide: AuthFacadeService,
          useValue: authFacade
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminLoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('doit créer le composant', () => {
    expect(component).toBeTruthy();
  });

  it('doit préparer le parcours de connexion admin', () => {
    expect(authFacade.preparerConnexionAdmin).toHaveBeenCalled();
  });

  it('ne doit pas préremplir le formulaire', () => {
    expect(component.login).toBe('');
    expect(component.motDePasse).toBe('');
  });

  it('doit borner les champs comme le DTO backend', () => {
    const login: HTMLInputElement =
      fixture.nativeElement.querySelector('input[name="login"]');
    const motDePasse: HTMLInputElement =
      fixture.nativeElement.querySelector('input[name="motDePasse"]');

    expect(login.maxLength).toBe(150);
    expect(motDePasse.maxLength).toBe(72);
  });

  it('doit transmettre la demande de connexion à la façade', () => {
    component.login = ' admin-test ';
    component.motDePasse = 'motdepasse-test';

    component.connecter();

    expect(authFacade.connecterAdmin).toHaveBeenCalledWith(
      ' admin-test ',
      'motdepasse-test'
    );
  });

  it('doit transmettre la déconnexion à la façade', () => {
    adminSignal.set(admin);

    component.deconnecterAdmin();

    expect(authFacade.deconnecterAdmin).toHaveBeenCalled();
  });
});
