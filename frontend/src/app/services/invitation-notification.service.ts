import {
  Injectable,
  signal
} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class InvitationNotificationService {
  private readonly nombreInvitationsRecuesSignal =
    signal(0);

  readonly nombreInvitationsRecues =
    this.nombreInvitationsRecuesSignal.asReadonly();

  definirNombreInvitationsRecues(
    nombreInvitations: number
  ): void {
    this.nombreInvitationsRecuesSignal.set(
      Math.max(0, nombreInvitations)
    );
  }

  signalerInvitationTraitee(): void {
    this.nombreInvitationsRecuesSignal.update(
      nombreInvitations =>
        Math.max(0, nombreInvitations - 1)
    );
  }

  reinitialiser(): void {
    this.nombreInvitationsRecuesSignal.set(0);
  }
}
