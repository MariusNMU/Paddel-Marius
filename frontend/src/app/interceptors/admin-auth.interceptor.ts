import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthContextService } from '../services/auth-context.service';

export const adminAuthInterceptor: HttpInterceptorFn = (request, next) => {
  const authContextService = inject(AuthContextService);

  if (!request.url.includes('/api/')) {
    return next(request);
  }

  const headers: Record<string, string> = {};

  if (request.url.includes('/api/admin/')) {
    const admin = authContextService.admin();

    if (admin?.token) {
      headers['Authorization'] = `Bearer ${admin.token}`;
    }

    // Compatibilité MVP temporaire :
    // l'ancien backend acceptait X-Admin-Login.
    // On le garde pendant cette PR pour éviter une régression brutale.
    if (admin?.login) {
      headers['X-Admin-Login'] = admin.login;
    }
  } else {
    const joueur = authContextService.joueur();

    if (joueur?.token) {
      headers['Authorization'] = `Bearer ${joueur.token}`;
    }
  }

  if (Object.keys(headers).length === 0) {
    return next(request);
  }

  return next(
    request.clone({
      setHeaders: headers
    })
  );
};
