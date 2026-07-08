import { HttpErrorResponse } from '@angular/common/http';
import { extraireMessageErreur } from './api-error.util';

describe('extraireMessageErreur', () => {
  it('doit préserver les accents d un message métier renvoyé par le backend', () => {
    const messageMetier = 'Le terrain demandé est inactif.';

    const error = new HttpErrorResponse({
      status: 400,
      error: {
        code: 'CONFIGURATION_METIER_INVALIDE',
        message: messageMetier
      }
    });

    expect(extraireMessageErreur(error)).toBe(messageMetier);
  });
});