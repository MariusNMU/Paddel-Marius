import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import {
  AuthAdminResponse,
  AuthJoueurResponse
} from '../models/auth.model';
import { AuthApiService } from './auth-api.service';
import { AuthContextService } from './auth-context.service';
import { AuthFacadeService } from './auth-facade.service';

describe('AuthFacadeService', () => {
  let service: AuthFacadeService;

  let authApiService: {
    connecterJoueur: ReturnType<typeof vi.fn>;
    connecterAdmin: ReturnType<typeof vi.fn>;
  };

  let authContextService: {
    joueur: ReturnType<typeof vi.fn>;
    admin: ReturnType<typeof vi.fn>;
    definirJoueur: ReturnType<typeof vi.fn>;
    definirAdmin: ReturnType<typeof vi.fn>;
    deconnecterJoueur: ReturnType<typeof vi.fn>;
    deconnecterAdmin: ReturnType<typeof vi.fn>;
  };

  let router: {
    navigate: ReturnType<typeof vi.fn>;
  };

  const joueur: AuthJoueurResponse = {
    membreId: 1,
    matricule: 'TEST001',
    nom: 'Test',
    prenom: 'Joueur',
    categorieMembre: 'GLOBAL',
    siteRattachementId: null,
    nomSiteRattachement: null,
    actif: true
  };

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

  beforeEach(() => {
    authApiService = {
      connecterJoueur: vi.fn(),
      connecterAdmin: vi.fn()
    };

    authContextService = {
      joueur: vi.fn(() => null),
      admin: vi.fn(() => null),
      definirJoueur: vi.fn(),
      definirAdmin: vi.fn(),
      deconnecterJoueur: vi.fn(),
      deconnecterAdmin: vi.fn()
    };

    router = {
      navigate: vi.fn().mockResolvedValue(true)
    };

    TestBed.configureTestingModule({
      providers: [
        AuthFacadeService,
        {
          provide: AuthApiService,
          useValue: authApiService
        },
        {
          provide: AuthContextService,
          useValue: authContextService
        },
        {
          provide: Router,
          useValue: router
        }
      ]
    });

    service = TestBed.inject(AuthFacadeService);
  });

  it('doit refuser une connexion joueur incomplète', () => {
    service.connecterJoueur('   ', 'motdepasse-test');

    expect(service.messageErreurJoueur()).toBe(
      'Le matricule et le mot de passe sont obligatoires.'
    );
    expect(authApiService.connecterJoueur).not.toHaveBeenCalled();
  });

  it('doit connecter un joueur et mettre à jour le contexte', () => {
    authApiService.connecterJoueur.mockReturnValue(of(joueur));

    service.connecterJoueur(
      ' TEST001 ',
      ' motdepasse-test '
    );

    expect(authApiService.connecterJoueur).toHaveBeenCalledWith({
      matricule: 'TEST001',
      motDePasse: ' motdepasse-test '
    });
    expect(authContextService.definirJoueur).toHaveBeenCalledWith(joueur);
    expect(service.messageSuccesJoueur()).toContain('Joueur connecté');
    expect(service.messageErreurJoueur()).toBeNull();
    expect(service.chargementJoueur()).toBe(false);
  });

  it('doit exposer une erreur de connexion joueur', () => {
    authApiService.connecterJoueur.mockReturnValue(
      throwError(() => new HttpErrorResponse({
        status: 401,
        error: {
          message:
            'Identifiant ou mot de passe invalide.'
        }
      }))
    );

    service.connecterJoueur('TEST001', 'motdepasse-test');

    expect(service.messageErreurJoueur()).toBe(
      'Identifiant ou mot de passe invalide.'
    );
    expect(authContextService.definirJoueur).not.toHaveBeenCalled();
    expect(service.chargementJoueur()).toBe(false);
  });

  it('doit refuser une connexion admin incomplète', () => {
    service.connecterAdmin('   ', 'motdepasse-test');

    expect(service.messageErreurAdmin()).toBe(
      'Le login et le mot de passe sont obligatoires.'
    );
    expect(authApiService.connecterAdmin).not.toHaveBeenCalled();
  });

  it('doit connecter un admin et aller au dashboard', () => {
    authApiService.connecterAdmin.mockReturnValue(of(admin));

    service.connecterAdmin(' admin-test ', 'motdepasse-test');

    expect(authApiService.connecterAdmin).toHaveBeenCalledWith({
      login: 'admin-test',
      motDePasse: 'motdepasse-test'
    });
    expect(authContextService.definirAdmin).toHaveBeenCalledWith(admin);
    expect(router.navigate).toHaveBeenCalledWith(['/admin/dashboard']);
    expect(service.messageErreurAdmin()).toBeNull();
    expect(service.chargementAdmin()).toBe(false);
  });

  it('doit exposer une erreur de connexion admin', () => {
    authApiService.connecterAdmin.mockReturnValue(
      throwError(() => new HttpErrorResponse({
        status: 401,
        error: {
          message:
            'Identifiant ou mot de passe invalide.'
        }
      }))
    );

    service.connecterAdmin('admin-test', 'motdepasse-test');

    expect(service.messageErreurAdmin()).toBe(
      'Identifiant ou mot de passe invalide.'
    );
    expect(authContextService.definirAdmin).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
    expect(service.chargementAdmin()).toBe(false);
  });

  it('doit déconnecter le joueur et revenir à l accueil', () => {
    authContextService.joueur.mockReturnValue(joueur);

    service.deconnecterJoueur();

    expect(authContextService.deconnecterJoueur).toHaveBeenCalled();
    expect(service.messageSuccesJoueur()).toContain('Joueur déconnecté');
    expect(router.navigate).toHaveBeenCalledWith(['/accueil']);
  });

  it('doit déconnecter l admin et revenir à l accueil', () => {
    authContextService.admin.mockReturnValue(admin);

    service.deconnecterAdmin();

    expect(authContextService.deconnecterAdmin).toHaveBeenCalled();
    expect(service.messageSuccesAdmin()).toContain('Admin déconnecté');
    expect(router.navigate).toHaveBeenCalledWith(['/accueil']);
  });
});
