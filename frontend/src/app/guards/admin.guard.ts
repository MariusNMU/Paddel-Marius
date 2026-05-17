import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthContextService } from '../services/auth-context.service';

export const adminGuard: CanActivateFn = () => {
  const authContextService = inject(AuthContextService);
  const router = inject(Router);

  if (authContextService.adminConnecte()) {
    return true;
  }

  return router.createUrlTree(['/admin/login']);
};
