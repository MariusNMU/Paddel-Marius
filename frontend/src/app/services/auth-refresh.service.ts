import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
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
    private readonly authContextService: AuthContextService,
    private readonly router: Router
  ) {
  }

  rafraichir(): Observable<RafraichissementTokenResponse> {
    if (this.rafraichissementEnCours$) {
      return this.rafraichissementEnCours$;
    }

    const tokenAvantRafraichissement =
      this.authContextService.token();
    const routeConnexion = this.authContextService.admin()
      ? ['/admin/login']
      : ['/joueur'];

    const rafraichissement$ = this.authApiService.rafraichir().pipe(
      tap(rafraichissement => {
        this.authContextService.mettreAJourToken(
          rafraichissement
        );
      }),
      catchError(error => {
        if (
          this.authContextService.token()
          === tokenAvantRafraichissement
        ) {
          this.authContextService.deconnecterTout();
          void this.router.navigate(routeConnexion);
        }

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
