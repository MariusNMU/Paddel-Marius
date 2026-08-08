import {
  effect,
  Injectable
} from '@angular/core';
import {
  catchError,
  EMPTY,
  Subject,
  takeUntil,
  tap,
  timeout
} from 'rxjs';
import { AuthJoueurResponse } from '../models/auth.model';
import { AuthFacadeService } from './auth-facade.service';
import { AuthContextService } from './auth-context.service';
import { InvitationApiService } from './invitation-api.service';
import { InvitationNotificationService } from './invitation-notification.service';

@Injectable()
export class AppShellFacadeService {
  private readonly changementJoueur$ =
    new Subject<void>();

  private parcoursInitialise = false;

  private joueurObserve:
    AuthJoueurResponse | null | undefined =
    undefined;

  constructor(
    private readonly authContextService:
    AuthContextService,
    private readonly authFacadeService:
    AuthFacadeService,
    private readonly invitationApiService:
    InvitationApiService,
    private readonly invitationNotificationService:
    InvitationNotificationService
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
    if (this.parcoursInitialise) {
      return;
    }

    const joueur =
      this.authContextService.joueur();

    this.parcoursInitialise = true;
    this.joueurObserve = joueur;

    this.synchroniserAvecJoueur(joueur);
  }

  joueurConnecte(): boolean {
    return this.authContextService
      .joueurConnecte();
  }

  adminConnecte(): boolean {
    return this.authContextService
      .adminConnecte();
  }

  estAdminGlobal(): boolean {
    return this.authContextService
      .admin()
      ?.roleAdministrateur === 'GLOBAL';
  }

  nombreInvitationsRecues(): number {
    return this.invitationNotificationService
      .nombreInvitationsRecues();
  }

  get messageErreurDeconnexion() {
    return this.authFacadeService
      .messageErreurDeconnexion;
  }

  deconnecterJoueur(): void {
    this.changementJoueur$.next();
    this.invitationNotificationService
      .reinitialiser();

    this.authFacadeService
      .deconnecterJoueur();
  }

  deconnecterAdmin(): void {
    this.authFacadeService
      .deconnecterAdmin();
  }

  private synchroniserAvecJoueur(
    joueur: AuthJoueurResponse | null
  ): void {
    this.changementJoueur$.next();
    this.invitationNotificationService
      .reinitialiser();

    if (joueur) {
      this.chargerNombreInvitations(
        joueur.matricule
      );
    }
  }

  private chargerNombreInvitations(
    matricule: string
  ): void {
    this.invitationApiService
      .compterInvitationsRecues(matricule)
      .pipe(
        timeout(10000),
        tap(nombreInvitations => {
          this.invitationNotificationService
            .definirNombreInvitationsRecues(
              nombreInvitations
            );
        }),
        catchError(() => {
          this.invitationNotificationService
            .reinitialiser();

          return EMPTY;
        }),
        takeUntil(this.changementJoueur$)
      )
      .subscribe();
  }
}
