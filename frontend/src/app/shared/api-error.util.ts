import { HttpErrorResponse } from '@angular/common/http';
import { ApiErrorResponse } from '../models/api-error.model';

export function extraireMessageErreur(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    const apiError = error.error as Partial<ApiErrorResponse> | string | null;

    if (typeof apiError === 'string' && apiError.trim().length > 0) {
      return apiError;
    }

    if (apiError && typeof apiError === 'object' && apiError.message) {
      return apiError.message;
    }

    if (error.message) {
      return error.message;
    }
  }

  return 'Une erreur est survenue. Vérifie les données saisies puis réessaie.';
}
