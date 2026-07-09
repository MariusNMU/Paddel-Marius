import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApiService } from '../../services/auth-api.service';
import { AuthContextService } from '../../services/auth-context.service';
import { extraireMessageErreur } from '../../shared/api-error.util';
import { enumLabel } from '../../shared/enum-label.util';

@Component({
  selector: 'app-admin-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <section class="page">
      <h2>Connexion admin</h2>

      <p>
        Connecte-toi avec un compte administrateur valide pour accéder au dashboard,
        aux statistiques et au traitement de veille.
      </p>

      @if (authContextService.admin(); as admin) {
        <div class="bloc-info">
          <h3>Admin connecté</h3>

          <p>
            <strong>{{ admin.prenom }} {{ admin.nom }}</strong>
            — rôle {{ enumLabel(admin.roleAdministrateur) }}
          </p>

          @if (admin.siteId) {
            <p>Site : {{ admin.nomSite }} ({{ admin.siteId }})</p>
          } @else {
            <p>Accès global à tous les sites.</p>
          }

          <p>
            Pour connecter un autre administrateur, déconnecte d'abord l'admin actuel.
          </p>

          <div class="actions">
            <a routerLink="/admin/dashboard">Aller au dashboard</a>
            <button type="button" (click)="deconnecterAdmin()">Déconnecter l'admin</button>
          </div>
        </div>
      } @else {
        @if (messageSucces()) {
          <p class="succes">{{ messageSucces() }}</p>
        }

        <div class="bloc-info">
          <h3>Compte de test</h3>

          <p>
            Les comptes administrateurs de démonstration restent temporairement indiqués sur la homepage.
            Cette page de connexion ne préremplit plus d'identifiants.
          </p>

          <a routerLink="/accueil" class="lien-action">
            Voir les informations de démonstration
          </a>
        </div>

        <form (ngSubmit)="connecter()" class="formulaire">
          <label for="login">Login</label>
          <input
            id="login"
            name="login"
            type="text"
            [(ngModel)]="login"
            placeholder="Votre login admin"
            required
          >

          <label for="motDePasse">Mot de passe</label>
          <input
            id="motDePasse"
            name="motDePasse"
            type="password"
            [(ngModel)]="motDePasse"
            placeholder="Votre mot de passe"
            required
          >

          <button type="submit" [disabled]="chargement()">
            {{ chargement() ? 'Connexion...' : 'Se connecter' }}
          </button>
        </form>
      }

      @if (messageErreur()) {
        <p class="erreur">{{ messageErreur() }}</p>
      }

      <p>
        <a routerLink="/accueil">Retour à la Homepage</a>
      </p>
    </section>
  `,
  styles: [`
    .succes {
      margin-top: 16px;
      color: #047857;
      font-weight: 700;
    }

    .lien-action {
      display: inline-block;
      margin-top: 12px;
      font-weight: 600;
    }
  `]
})
export class AdminLoginComponent {
  readonly enumLabel = enumLabel;
  login = '';
  motDePasse = '';

  readonly chargement = signal(false);
  readonly messageErreur = signal<string | null>(null);
  readonly messageSucces = signal<string | null>(null);

  constructor(
    private readonly authApiService: AuthApiService,
    readonly authContextService: AuthContextService,
    private readonly router: Router
  ) {
  }

  deconnecterAdmin(): void {
    const admin = this.authContextService.admin();

    this.authContextService.deconnecterAdmin();
    this.messageErreur.set(null);
    this.messageSucces.set(
      admin
        ? `Admin déconnecté : ${admin.prenom} ${admin.nom}.`
        : 'Admin déconnecté.'
    );

    void this.router.navigate(['/accueil']);
  }

  connecter(): void {
    this.messageErreur.set(null);
    this.messageSucces.set(null);

    if (!this.login.trim() || !this.motDePasse.trim()) {
      this.messageErreur.set('Le login et le mot de passe sont obligatoires.');
      return;
    }

    this.chargement.set(true);

    this.authApiService.connecterAdmin({
      login: this.login.trim(),
      motDePasse: this.motDePasse
    }).subscribe({
      next: admin => {
        this.authContextService.definirAdmin(admin);
        this.chargement.set(false);
        this.router.navigate(['/admin/dashboard']);
      },
      error: error => {
        this.messageErreur.set(extraireMessageErreur(error));
        this.chargement.set(false);
      }
    });
  }
}
