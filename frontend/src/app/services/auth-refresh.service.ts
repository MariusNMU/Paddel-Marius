import { Injectable } from '@angular/core';
import {
  catchError,
  finalize,
  Observable,
  shareReplay,
  tap,
  throwError
} from 'rxjs';
import { RafraichissementTokenResponse } from '../models/auth.model';
import { AuthApiService } from './auth-api.service';
import { AuthContextService } from './auth-context.service';

@Injectable({
  providedIn: 'root'
})
export class AuthRefreshService {
  private rafraichissementEnCours$:
    Observable<RafraichissementTokenResponse> | null = null;

  constructor(
    private readonly authApiService: AuthApiService,
    private readonly authContextService: AuthContextService
  ) {
  }

  rafraichir(): Observable<RafraichissementTokenResponse> {
    if (this.rafraichissementEnCours$) {
      return this.rafraichissementEnCours$;
    }

    const rafraichissement$ = this.authApiService.rafraichir().pipe(
      tap(rafraichissement => {
        this.authContextService.mettreAJourToken(
          rafraichissement
        );
      }),
      catchError(error => {
        this.authContextService.deconnecterTout();
        return throwError(() => error);
      }),
      finalize(() => {
        this.rafraichissementEnCours$ = null;
      }),
      shareReplay({
        bufferSize: 1,
        refCount: false
      })
    );

    this.rafraichissementEnCours$ = rafraichissement$;
    return rafraichissement$;
  }
}
