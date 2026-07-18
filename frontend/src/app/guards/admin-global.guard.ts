import { inject } from '@angular/core';
import {
  CanActivateFn,
  Router
} from '@angular/router';
import { AuthContextService } from '../services/auth-context.service';

export const adminGlobalGuard:
  CanActivateFn = () => {
  const authContextService =
    inject(AuthContextService);

  const router = inject(Router);
  const admin =
    authContextService.admin();

  if (!admin) {
    return router.createUrlTree([
      '/admin/login'
    ]);
  }

  if (
    admin.roleAdministrateur === 'GLOBAL'
  ) {
    return true;
  }

  return router.createUrlTree([
    '/admin/dashboard'
  ]);
};
