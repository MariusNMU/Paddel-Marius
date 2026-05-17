import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthContextService } from '../services/auth-context.service';
import { adminGuard } from './admin.guard';

describe('adminGuard', () => {
  let authContextService: {
    adminConnecte: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    authContextService = {
      adminConnecte: vi.fn()
    };

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthContextService, useValue: authContextService }
      ]
    });
  });

  it('doit autoriser la route si un admin est connecté', () => {
    authContextService.adminConnecte.mockReturnValue(true);

    const result = TestBed.runInInjectionContext(() => adminGuard({} as never, {} as never));

    expect(result).toBe(true);
  });

  it('doit rediriger vers /admin/login si aucun admin n est connecté', () => {
    authContextService.adminConnecte.mockReturnValue(false);

    const router = TestBed.inject(Router);
    const result = TestBed.runInInjectionContext(() => adminGuard({} as never, {} as never));

    expect(result).toEqual(router.createUrlTree(['/admin/login']));
  });
});
