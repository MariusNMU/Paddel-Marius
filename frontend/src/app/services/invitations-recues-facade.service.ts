import {
  effect,
  Injectable,
  signal
} from '@angular/core';
import {
  catchError,
  EMPTY,
  finalize,
  Subject,
  takeUntil,
  tap,
  timeout
} from 'rxjs';
import { AuthJoueurResponse } from '../models/auth.model';
import { InvitationPriveeResponse } from '../models/invitation.model';
import { extraireMessageErreur } from '../shared/api-error.util';
import { AuthContextService } from './auth-context.service';
import { InvitationApiService } from './invitation-api.service';
import { InvitationNotificationService } from './invitation-notification.service';
import { PaiementApiService } from './paiement-api.service';

@Injectable()
export class InvitationsRecuesFacadeService {
  private readonly invitationsSignal =
    signal<InvitationPriveeResponse[]>([]);

  private readonly chargementSignal =
    signal(false);

  private readonly rechercheEffectueeSignal =
    signal(false);

  private readonly actionEnCoursParticipationIdSignal =
    signal<number | null>(null);

  private readonly messageErreurSignal =
    signal('');

  private readonly messageSuccesSignal =
    signal('');

  private readonly changementSession$ =
    new Subject<void>();

  private parcoursInitialise = false;

  private joueurObserve:
    AuthJoueurResponse | null | undefined =
    undefined;

  get joueur() {
    return this.authContextService.joueur;
  }

  readonly invitations =
    this.invitationsSignal.asReadonly();

  readonly chargement =
    this.chargementSignal.asReadonly();

  readonly rechercheEffectuee =
    this.rechercheEffectueeSignal.asReadonly();

  readonly actionEnCoursParticipationId =
    this.actionEnCoursParticipationIdSignal
      .asReadonly();

  readonly messageErreur =
    this.messageErreurSignal.asReadonly();

  readonly messageSucces =
    this.messageSuccesSignal.asReadonly();

  constructor(
    private readonly invitationApiService:
    InvitationApiService,
    private readonly paiementApiService:
    PaiementApiService,
    private readonly invitationNotificationService:
    InvitationNotificationService,
    private readonly authContextService:
    AuthContextService
  ) {
    effect(() => {
      const joueur =
        this.authContextService.joueur();

      if (
        !this.parcoursInitialise
        || joueur === this.joueurObserve
      ) {
        return;
      }

      this.joueurObserve = joueur;
      this.synchroniserAvecJoueur(joueur);
    });
  }

  initialiser(): void {
    const joueur =
      this.authContextService.joueur();

    this.parcoursInitialise = true;
    this.joueurObserve = joueur;

    this.synchroniserAvecJoueur(joueur);
  }

  chargerInvitations(
    conserverMessageSucces = false
  ): void {
    const joueur = this.joueur();

    this.messageErreurSignal.set('');

    if (!conserverMessageSucces) {
      this.messageSuccesSignal.set('');
    }

    this.invitationsSignal.set([]);
    this.rechercheEffectueeSignal.set(true);

    if (!joueur) {
      this.messageErreurSignal.set(
        'Aucun joueur connecté.'
      );
      return;
    }

    this.chargementSignal.set(true);

    this.invitationApiService
      .listerInvitationsRecues(
        joueur.matricule
      )
      .pipe(
        timeout(10000),
        tap(invitations => {
          this.invitationsSignal.set(
            invitations
          );
        }),
        catchError(error => {
          this.messageErreurSignal.set(
            extraireMessageErreur(error)
          );

          return EMPTY;
        }),
        finalize(() => {
          this.chargementSignal.set(false);
        }),
        takeUntil(this.changementSession$)
      )
      .subscribe();
  }

  confirmerEtPayer(
    invitation: InvitationPriveeResponse
  ): void {
    this.messageErreurSignal.set('');
    this.messageSuccesSignal.set('');

    if (!this.joueur()) {
      this.messageErreurSignal.set(
        'Aucun joueur connecté.'
      );
      return;
    }

    this.actionEnCoursParticipationIdSignal
      .set(invitation.participationId);

    this.paiementApiService
      .payerParticipationStandard(
        invitation.participationId
      )
      .pipe(
        timeout(10000),
        tap(() => {
          this.invitationNotificationService
            .signalerInvitationTraitee();

          this.messageSuccesSignal.set(
            'Invitation confirmée et participation payée.'
          );

          this.chargerInvitations(true);
        }),
        catchError(error => {
          this.messageErreurSignal.set(
            extraireMessageErreur(error)
          );

          return EMPTY;
        }),
        finalize(() => {
          this.actionEnCoursParticipationIdSignal
            .set(null);
        }),
        takeUntil(this.changementSession$)
      )
      .subscribe();
  }

  decliner(
    invitation: InvitationPriveeResponse
  ): void {
    const joueur = this.joueur();

    this.messageErreurSignal.set('');
    this.messageSuccesSignal.set('');

    if (!joueur) {
      this.messageErreurSignal.set(
        'Aucun joueur connecté.'
      );
      return;
    }

    this.actionEnCoursParticipationIdSignal
      .set(invitation.participationId);

    this.invitationApiService
      .declinerInvitation(
        invitation.participationId,
        {
          matriculeJoueur:
          joueur.matricule
        }
      )
      .pipe(
        timeout(10000),
        tap(() => {
          this.invitationNotificationService
            .signalerInvitationTraitee();

          this.messageSuccesSignal.set(
            'Invitation déclinée.'
          );

          this.chargerInvitations(true);
        }),
        catchError(error => {
          this.messageErreurSignal.set(
            extraireMessageErreur(error)
          );

          return EMPTY;
        }),
        finalize(() => {
          this.actionEnCoursParticipationIdSignal
            .set(null);
        }),
        takeUntil(this.changementSession$)
      )
      .subscribe();
  }

  private synchroniserAvecJoueur(
    joueur: AuthJoueurResponse | null
  ): void {
    this.changementSession$.next();
    this.reinitialiserParcours();

    if (joueur) {
      this.chargerInvitations();
    }
  }

  private reinitialiserParcours(): void {
    this.invitationsSignal.set([]);
    this.chargementSignal.set(false);
    this.rechercheEffectueeSignal.set(false);

    this.actionEnCoursParticipationIdSignal
      .set(null);

    this.messageErreurSignal.set('');
    this.messageSuccesSignal.set('');
  }
}
