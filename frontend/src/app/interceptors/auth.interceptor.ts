import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthContextService } from '../services/auth-context.service';

export const authInterceptor: HttpInterceptorFn = (
  request,
  next
) => {
  const authContextService = inject(AuthContextService);

  if (!request.url.startsWith('/api/')) {
    return next(request);
  }

  const token = authContextService.token();

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
