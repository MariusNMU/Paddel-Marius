import {
  effect,
  Injectable,
  signal
} from '@angular/core';
import { Router } from '@angular/router';
import {
  catchError,
  EMPTY,
  Subject,
  takeUntil,
  tap,
  timeout
} from 'rxjs';
import { AuthJoueurResponse } from '../models/auth.model';
import { AuthContextService } from './auth-context.service';
import { InvitationApiService } from './invitation-api.service';

@Injectable()
export class AppShellFacadeService {
  private readonly nombreInvitationsRecuesSignal =
    signal(0);

  private readonly changementJoueur$ =
    new Subject<void>();

  private parcoursInitialise = false;

  private joueurObserve:
    AuthJoueurResponse | null | undefined =
    undefined;

  readonly nombreInvitationsRecues =
    this.nombreInvitationsRecuesSignal.asReadonly();

  constructor(
    private readonly authContextService:
    AuthContextService,
    private readonly invitationApiService:
    InvitationApiService,
    private readonly router:
    Router
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

  deconnecterJoueur(): void {
    this.changementJoueur$.next();
    this.nombreInvitationsRecuesSignal.set(0);

    this.authContextService
      .deconnecterJoueur();

    void this.router.navigate(['/accueil']);
  }

  deconnecterAdmin(): void {
    this.authContextService
      .deconnecterAdmin();

    void this.router.navigate(['/accueil']);
  }

  private synchroniserAvecJoueur(
    joueur: AuthJoueurResponse | null
  ): void {
    this.changementJoueur$.next();
    this.nombreInvitationsRecuesSignal.set(0);

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
          this.nombreInvitationsRecuesSignal
            .set(nombreInvitations);
        }),
        catchError(() => {
          this.nombreInvitationsRecuesSignal
            .set(0);

          return EMPTY;
        }),
        takeUntil(this.changementJoueur$)
      )
      .subscribe();
  }
}
