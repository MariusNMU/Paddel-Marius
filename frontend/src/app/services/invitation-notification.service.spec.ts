import { TestBed } from '@angular/core/testing';
import { InvitationNotificationService } from './invitation-notification.service';

describe(
  'InvitationNotificationService',
  () => {
    let service:
      InvitationNotificationService;

    beforeEach(() => {
      TestBed.configureTestingModule({});

      service = TestBed.inject(
        InvitationNotificationService
      );
    });

    it(
      'doit définir le nombre d invitations reçues',
      () => {
        service.definirNombreInvitationsRecues(3);

        expect(
          service.nombreInvitationsRecues()
        ).toBe(3);
      }
    );

    it(
      'doit retirer immédiatement une invitation traitée',
      () => {
        service.definirNombreInvitationsRecues(2);

        service.signalerInvitationTraitee();

        expect(
          service.nombreInvitationsRecues()
        ).toBe(1);
      }
    );

    it(
      'ne doit jamais produire un compteur négatif',
      () => {
        service.signalerInvitationTraitee();

        expect(
          service.nombreInvitationsRecues()
        ).toBe(0);
      }
    );

    it(
      'doit réinitialiser le compteur',
      () => {
        service.definirNombreInvitationsRecues(2);

        service.reinitialiser();

        expect(
          service.nombreInvitationsRecues()
        ).toBe(0);
      }
    );
  }
);
