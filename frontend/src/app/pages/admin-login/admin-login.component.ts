import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthFacadeService } from '../../services/auth-facade.service';
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

      @if (authFacade.admin(); as admin) {
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

            <button type="button" (click)="deconnecterAdmin()">
              Déconnecter l'admin
            </button>
          </div>
        </div>
      } @else {
        @if (authFacade.messageSuccesAdmin()) {
          <p class="succes">
            {{ authFacade.messageSuccesAdmin() }}
          </p>
        }

        <div class="bloc-info">
          <h3>Compte de test</h3>

          <p>
            Les comptes administrateurs de démonstration restent temporairement indiqués
            sur la homepage. Cette page de connexion ne préremplit plus d'identifiants.
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

          <button
            type="submit"
            [disabled]="authFacade.chargementAdmin()"
          >
            {{
              authFacade.chargementAdmin()
                ? 'Connexion...'
                : 'Se connecter'
            }}
          </button>
        </form>
      }

      @if (authFacade.messageErreurAdmin()) {
        <p class="erreur">
          {{ authFacade.messageErreurAdmin() }}
        </p>
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
export class AdminLoginComponent implements OnInit {
  readonly enumLabel = enumLabel;

  login = '';
  motDePasse = '';

  constructor(
    readonly authFacade: AuthFacadeService
  ) {
  }

  ngOnInit(): void {
    this.authFacade.preparerConnexionAdmin();
  }

  connecter(): void {
    this.authFacade.connecterAdmin(
      this.login,
      this.motDePasse
    );
  }

  deconnecterAdmin(): void {
    this.authFacade.deconnecterAdmin();
  }
}
