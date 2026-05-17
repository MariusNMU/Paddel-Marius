import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthAdminResponse } from '../../models/auth.model';
import { AuthApiService } from '../../services/auth-api.service';
import { AuthContextService } from '../../services/auth-context.service';
import { AdminLoginComponent } from './admin-login.component';

describe('AdminLoginComponent', () => {
  let fixture: ComponentFixture<AdminLoginComponent>;
  let component: AdminLoginComponent;

  let authApiService: {
    connecterAdmin: ReturnType<typeof vi.fn>;
  };

  let authContextService: {
    admin: ReturnType<typeof vi.fn>;
    definirAdmin: ReturnType<typeof vi.fn>;
    deconnecterAdmin: ReturnType<typeof vi.fn>;
  };

  const admin: AuthAdminResponse = {
    administrateurId: 2101,
    login: 'admin-global',
    nom: 'Admin',
    prenom: 'Global',
    roleAdministrateur: 'GLOBAL',
    siteId: null,
    nomSite: null,
    actif: true
  };

  beforeEach(async () => {
    authApiService = {
      connecterAdmin: vi.fn()
    };

    authContextService = {
      admin: vi.fn(() => null),
      definirAdmin: vi.fn(),
      deconnecterAdmin: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [AdminLoginComponent],
      providers: [
        provideRouter([]),
        { provide: AuthApiService, useValue: authApiService },
        { provide: AuthContextService, useValue: authContextService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminLoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('doit créer le composant', () => {
    expect(component).toBeTruthy();
  });

  it('doit préremplir le formulaire avec l admin global', () => {
    expect(component.login).toBe('admin-global');
    expect(component.motDePasse).toBe('secret');
  });

  it('doit remplir le formulaire avec l admin Bruxelles', () => {
    component.messageErreur.set('ancienne erreur');
    component.messageSucces.set('ancien succès');

    component.utiliserAdminBruxelles();

    expect(component.login).toBe('admin-bruxelles');
    expect(component.motDePasse).toBe('secret-site');
    expect(component.messageErreur()).toBeNull();
    expect(component.messageSucces()).toBeNull();
  });

  it('doit remplir le formulaire avec l admin Namur', () => {
    component.utiliserAdminNamur();

    expect(component.login).toBe('admin-namur');
    expect(component.motDePasse).toBe('secret-site');
  });

  it('doit refuser la connexion si le login est vide', () => {
    component.login = '   ';
    component.motDePasse = 'secret';

    component.connecter();

    expect(component.messageErreur()).toBe('Le login et le mot de passe sont obligatoires.');
    expect(authApiService.connecterAdmin).not.toHaveBeenCalled();
  });

  it('doit connecter un admin valide et aller au dashboard', () => {
    authApiService.connecterAdmin.mockReturnValue(of(admin));

    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    component.login = ' admin-global ';
    component.motDePasse = 'secret';

    component.connecter();

    expect(authApiService.connecterAdmin).toHaveBeenCalledWith({
      login: 'admin-global',
      motDePasse: 'secret'
    });

    expect(authContextService.definirAdmin).toHaveBeenCalledWith(admin);
    expect(component.chargement()).toBe(false);
    expect(navigateSpy).toHaveBeenCalledWith(['/admin/dashboard']);
  });

  it('doit afficher une erreur si la connexion admin échoue', () => {
    authApiService.connecterAdmin.mockReturnValue(
      throwError(() => new HttpErrorResponse({
        status: 401,
        error: {
          message: 'Identifiants administrateur invalides.'
        }
      }))
    );

    component.connecter();

    expect(component.messageErreur()).toBe('Identifiants administrateur invalides.');
    expect(component.chargement()).toBe(false);
  });

  it('doit déconnecter l admin et revenir à l accueil', () => {
    authContextService.admin.mockReturnValue(admin);

    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    component.deconnecterAdmin();

    expect(authContextService.deconnecterAdmin).toHaveBeenCalled();
    expect(component.messageSucces()).toContain('Admin déconnecté');
    expect(navigateSpy).toHaveBeenCalledWith(['/accueil']);
  });
});
