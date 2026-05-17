import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthContextService } from '../services/auth-context.service';

export const joueurGuard: CanActivateFn = () => {
  const authContextService = inject(AuthContextService);
  const router = inject(Router);

  if (authContextService.joueurConnecte()) {
    return true;
  }

  return router.createUrlTree(['/joueur']);
};
