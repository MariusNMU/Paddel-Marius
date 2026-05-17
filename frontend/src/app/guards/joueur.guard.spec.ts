import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthContextService } from '../services/auth-context.service';
import { joueurGuard } from './joueur.guard';

describe('joueurGuard', () => {
  let authContextService: {
    joueurConnecte: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    authContextService = {
      joueurConnecte: vi.fn()
    };

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthContextService, useValue: authContextService }
      ]
    });
  });

  it('doit autoriser la route si un joueur est connecté', () => {
    authContextService.joueurConnecte.mockReturnValue(true);

    const result = TestBed.runInInjectionContext(() => joueurGuard({} as never, {} as never));

    expect(result).toBe(true);
  });

  it('doit rediriger vers /joueur si aucun joueur n est connecté', () => {
    authContextService.joueurConnecte.mockReturnValue(false);

    const router = TestBed.inject(Router);
    const result = TestBed.runInInjectionContext(() => joueurGuard({} as never, {} as never));

    expect(result).toEqual(router.createUrlTree(['/joueur']));
  });
});
