import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, EMPTY, finalize, tap } from 'rxjs';
import { extraireMessageErreur } from '../shared/api-error.util';
import { AuthApiService } from './auth-api.service';
import { AuthContextService } from './auth-context.service';

@Injectable({
  providedIn: 'root'
})
export class AuthFacadeService {
  private readonly chargementJoueurSignal = signal(false);
  private readonly messageErreurJoueurSignal = signal<string | null>(null);
  private readonly messageSuccesJoueurSignal = signal<string | null>(null);

  private readonly chargementAdminSignal = signal(false);
  private readonly messageErreurAdminSignal = signal<string | null>(null);
  private readonly messageSuccesAdminSignal = signal<string | null>(null);

  get joueur() {
    return this.authContextService.joueur;
  }

  get admin() {
    return this.authContextService.admin;
  }

  readonly chargementJoueur = this.chargementJoueurSignal.asReadonly();
  readonly messageErreurJoueur = this.messageErreurJoueurSignal.asReadonly();
  readonly messageSuccesJoueur = this.messageSuccesJoueurSignal.asReadonly();

  readonly chargementAdmin = this.chargementAdminSignal.asReadonly();
  readonly messageErreurAdmin = this.messageErreurAdminSignal.asReadonly();
  readonly messageSuccesAdmin = this.messageSuccesAdminSignal.asReadonly();

  constructor(
    private readonly authApiService: AuthApiService,
    private readonly authContextService: AuthContextService,
    private readonly router: Router
  ) {
  }

  preparerConnexionJoueur(): void {
    this.chargementJoueurSignal.set(false);
    this.messageErreurJoueurSignal.set(null);
    this.messageSuccesJoueurSignal.set(null);
  }

  preparerConnexionAdmin(): void {
    this.chargementAdminSignal.set(false);
    this.messageErreurAdminSignal.set(null);
    this.messageSuccesAdminSignal.set(null);
  }

  connecterJoueur(matricule: string, motDePasse: string): void {
    this.messageErreurJoueurSignal.set(null);
    this.messageSuccesJoueurSignal.set(null);

    const matriculeNettoye = matricule.trim();

    if (!matriculeNettoye || !motDePasse.trim()) {
      this.messageErreurJoueurSignal.set(
        'Le matricule et le mot de passe sont obligatoires.'
      );
      return;
    }

    this.chargementJoueurSignal.set(true);

    this.authApiService.connecterJoueur({
      matricule: matriculeNettoye,
      motDePasse
    }).pipe(
      tap(joueur => {
        this.authContextService.definirJoueur(joueur);
        this.messageSuccesJoueurSignal.set(
          `Joueur connecté : ${joueur.prenom} ${joueur.nom} (${joueur.matricule}).`
        );
      }),
      catchError(error => {
        this.messageErreurJoueurSignal.set(
          extraireMessageErreur(error)
        );
        return EMPTY;
      }),
      finalize(() => {
        this.chargementJoueurSignal.set(false);
      })
    ).subscribe();
  }

  connecterAdmin(login: string, motDePasse: string): void {
    this.messageErreurAdminSignal.set(null);
    this.messageSuccesAdminSignal.set(null);

    const loginNettoye = login.trim();

    if (!loginNettoye || !motDePasse.trim()) {
      this.messageErreurAdminSignal.set(
        'Le login et le mot de passe sont obligatoires.'
      );
      return;
    }

    this.chargementAdminSignal.set(true);

    this.authApiService.connecterAdmin({
      login: loginNettoye,
      motDePasse
    }).pipe(
      tap(admin => {
        this.authContextService.definirAdmin(admin);
        void this.router.navigate(['/admin/dashboard']);
      }),
      catchError(error => {
        this.messageErreurAdminSignal.set(
          extraireMessageErreur(error)
        );
        return EMPTY;
      }),
      finalize(() => {
        this.chargementAdminSignal.set(false);
      })
    ).subscribe();
  }

  deconnecterJoueur(): void {
    const joueur = this.authContextService.joueur();

    this.authContextService.deconnecterJoueur();
    this.messageErreurJoueurSignal.set(null);
    this.messageSuccesJoueurSignal.set(
      joueur
        ? `Joueur déconnecté : ${joueur.prenom} ${joueur.nom} (${joueur.matricule}).`
        : 'Joueur déconnecté.'
    );

    void this.router.navigate(['/accueil']);
  }

  deconnecterAdmin(): void {
    const admin = this.authContextService.admin();

    this.authContextService.deconnecterAdmin();
    this.messageErreurAdminSignal.set(null);
    this.messageSuccesAdminSignal.set(
      admin
        ? `Admin déconnecté : ${admin.prenom} ${admin.nom}.`
        : 'Admin déconnecté.'
    );

    void this.router.navigate(['/accueil']);
  }
}
