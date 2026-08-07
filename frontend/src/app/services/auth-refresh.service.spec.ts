import { TestBed } from '@angular/core/testing';
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
    mettreAJourToken: ReturnType<typeof vi.fn>;
    deconnecterTout: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    authApiService = {
      rafraichir: vi.fn()
    };
    authContextService = {
      mettreAJourToken: vi.fn(),
      deconnecterTout: vi.fn()
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

  it('doit vider la session si le refresh est refusé', () => {
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
  });
});
