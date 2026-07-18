import { TestBed } from '@angular/core/testing';
import {
  provideRouter,
  Router
} from '@angular/router';
import { AuthAdminResponse } from '../models/auth.model';
import { AuthContextService } from '../services/auth-context.service';
import { adminGlobalGuard } from './admin-global.guard';

describe('adminGlobalGuard', () => {
  let authContextService: {
    admin: ReturnType<typeof vi.fn>;
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

  beforeEach(() => {
    authContextService = {
      admin: vi.fn()
    };

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: AuthContextService,
          useValue: authContextService
        }
      ]
    });
  });

  it(
    'doit autoriser un admin global',
    () => {
      authContextService.admin
        .mockReturnValue(adminGlobal);

      const resultat =
        TestBed.runInInjectionContext(
          () => adminGlobalGuard(
            {} as never,
            {} as never
          )
        );

      expect(resultat).toBe(true);
    }
  );

  it(
    'doit rediriger un utilisateur déconnecté vers la connexion',
    () => {
      authContextService.admin
        .mockReturnValue(null);

      const router =
        TestBed.inject(Router);

      const resultat =
        TestBed.runInInjectionContext(
          () => adminGlobalGuard(
            {} as never,
            {} as never
          )
        );

      expect(resultat).toEqual(
        router.createUrlTree([
          '/admin/login'
        ])
      );
    }
  );

  it(
    'doit rediriger un admin SITE vers le dashboard',
    () => {
      authContextService.admin
        .mockReturnValue(adminSite);

      const router =
        TestBed.inject(Router);

      const resultat =
        TestBed.runInInjectionContext(
          () => adminGlobalGuard(
            {} as never,
            {} as never
          )
        );

      expect(resultat).toEqual(
        router.createUrlTree([
          '/admin/dashboard'
        ])
      );
    }
  );
});
