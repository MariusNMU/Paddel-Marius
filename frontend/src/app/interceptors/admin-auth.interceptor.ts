import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthContextService } from '../services/auth-context.service';

export const adminAuthInterceptor: HttpInterceptorFn = (request, next) => {
  const authContextService = inject(AuthContextService);

  if (!request.url.includes('/api/admin/')) {
    return next(request);
  }

  const admin = authContextService.admin();

  if (!admin) {
    return next(request);
  }

  const requestAvecAdmin = request.clone({
    setHeaders: {
      'X-Admin-Login': admin.login
    }
  });

  return next(requestAvecAdmin);
};
