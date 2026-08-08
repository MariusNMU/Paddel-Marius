import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { Subject, throwError } from 'rxjs';
import { RafraichissementTokenResponse } from '../models/auth.model';
import { AuthApiService } from './auth-api.service';
import { AuthContextService } from './auth-context.service';
import { AuthRefreshService } from './auth-refresh.service';

describe('AuthRefreshService', () => {
  let service: AuthRefreshService;
  let authApiService: {
    rafraichir: ReturnType<typeof vi.fn>;
  };
  let authContextService: {
    token: ReturnType<typeof vi.fn>;
    joueur: ReturnType<typeof vi.fn>;
    admin: ReturnType<typeof vi.fn>;
    mettreAJourToken: ReturnType<typeof vi.fn>;
    deconnecterTout: ReturnType<typeof vi.fn>;
  };
  let router: {
    navigate: ReturnType<typeof vi.fn>;
  };
  let tokenCourant: string | null;

  beforeEach(() => {
    authApiService = {
      rafraichir: vi.fn()
    };
    tokenCourant = 'jwt-joueur';
    authContextService = {
      token: vi.fn(() => tokenCourant),
      joueur: vi.fn(() => ({ matricule: 'G1001' })),
      admin: vi.fn(() => null),
      mettreAJourToken: vi.fn(),
      deconnecterTout: vi.fn(() => {
        tokenCourant = null;
      })
    };
    router = {
      navigate: vi.fn().mockResolvedValue(true)
    };

    TestBed.configureTestingModule({
      providers: [
        AuthRefreshService,
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

    service = TestBed.inject(AuthRefreshService);
  });

  it('doit partager un seul refresh entre plusieurs requêtes 401', () => {
    const sujet = new Subject<RafraichissementTokenResponse>();
    const reponses: string[] = [];

    authApiService.rafraichir.mockReturnValue(sujet.asObservable());

    service.rafraichir().subscribe(response => {
      reponses.push(response.token);
    });
    service.rafraichir().subscribe(response => {
      reponses.push(response.token);
    });

    expect(authApiService.rafraichir).toHaveBeenCalledTimes(1);

    sujet.next({
      token: 'nouvel-access',
      expirationToken: '2099-12-31T23:59:59'
    });
    sujet.complete();

    expect(reponses).toEqual([
      'nouvel-access',
      'nouvel-access'
    ]);
    expect(
      authContextService.mettreAJourToken
    ).toHaveBeenCalledTimes(1);
  });

  it('doit vider la session joueur et rediriger si le refresh est refusé', () => {
    authApiService.rafraichir.mockReturnValue(
      throwError(() => new Error('refresh refusé'))
    );

    service.rafraichir().subscribe({
      error: () => undefined
    });

    expect(authContextService.deconnecterTout)
      .toHaveBeenCalledTimes(1);
    expect(authContextService.mettreAJourToken)
      .not.toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/joueur']);
  });

  it('doit rediriger un admin vers sa page de connexion', () => {
    tokenCourant = 'jwt-admin';
    authContextService.joueur.mockReturnValue(null);
    authContextService.admin.mockReturnValue({
      login: 'admin-global'
    });
    authApiService.rafraichir.mockReturnValue(
      throwError(() => new Error('refresh refusé'))
    );

    service.rafraichir().subscribe({
      error: () => undefined
    });

    expect(authContextService.deconnecterTout)
      .toHaveBeenCalledTimes(1);
    expect(router.navigate)
      .toHaveBeenCalledWith(['/admin/login']);
  });

  it('ne doit pas effacer une nouvelle session créée pendant le refresh', () => {
    const sujet = new Subject<RafraichissementTokenResponse>();
    authApiService.rafraichir.mockReturnValue(sujet.asObservable());

    service.rafraichir().subscribe({
      error: () => undefined
    });

    tokenCourant = 'jwt-nouvelle-session';
    sujet.error(new Error('ancien refresh refusé'));

    expect(authContextService.deconnecterTout)
      .not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
