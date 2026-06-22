import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthContextService } from '../services/auth-context.service';

export const adminAuthInterceptor: HttpInterceptorFn = (
  request,
  next
) => {
  const authContextService = inject(AuthContextService);

  if (!request.url.includes('/api/')) {
    return next(request);
  }

  const token = request.url.includes('/api/admin/')
    ? authContextService.admin()?.token
    : authContextService.joueur()?.token;

  if (!token) {
    return next(request);
  }

  return next(
    request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    })
  );
};
