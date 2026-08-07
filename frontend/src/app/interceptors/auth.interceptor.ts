import {
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthContextService } from '../services/auth-context.service';
import { AuthRefreshService } from '../services/auth-refresh.service';

const PREFIXE_API = '/api/';
const PREFIXE_AUTH = '/api/auth/';

export const authInterceptor: HttpInterceptorFn = (
  request,
  next
) => {
  const authContextService = inject(AuthContextService);
  const authRefreshService = inject(AuthRefreshService);

  if (!request.url.startsWith(PREFIXE_API)) {
    return next(request);
  }

  if (request.url.startsWith(PREFIXE_AUTH)) {
    return next(request.clone({
      withCredentials: true
    }));
  }

  const token = authContextService.token();

  if (!token) {
    return next(request);
  }

  const requeteAuthentifiee = request.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });

  return next(requeteAuthentifiee).pipe(
    catchError(error => {
      if (!(error instanceof HttpErrorResponse)
          || error.status !== 401) {
        return throwError(() => error);
      }

      return authRefreshService.rafraichir().pipe(
        switchMap(rafraichissement => next(
          request.clone({
            setHeaders: {
              Authorization:
                `Bearer ${rafraichissement.token}`
            }
          })
        ))
      );
    })
  );
};
