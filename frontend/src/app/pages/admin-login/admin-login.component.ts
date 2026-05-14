import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApiService } from '../../services/auth-api.service';
import { AuthContextService } from '../../services/auth-context.service';
import { extraireMessageErreur } from '../../shared/api-error.util';

@Component({
  selector: 'app-admin-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <section class="page">
      <h2>Connexion admin</h2>

      <p>
        Connecte-toi avec un administrateur de démonstration pour accéder au dashboard,
        aux statistiques et au traitement de veille.
      </p>

      @if (authContextService.admin(); as admin) {
        <div class="bloc-info">
          <h3>Admin connecté</h3>

          <p>
            <strong>{{ admin.prenom }} {{ admin.nom }}</strong>
            — rôle {{ admin.roleAdministrateur }}
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
          <h3>Comptes de démonstration</h3>

          <div class="admin-demo-grid">
            <article class="admin-demo-card">
              <h4>Admin global</h4>
              <p>
                Accès de démonstration à tous les sites.
              </p>
              <p><strong>Login :</strong> admin-global</p>
              <p><strong>Mot de passe :</strong> secret</p>

              <button type="button" (click)="utiliserAdminGlobal()">
                Utiliser ce compte
              </button>
            </article>

            <article class="admin-demo-card">
              <h4>Admin site Bruxelles</h4>
              <p>
                Accès de démonstration limité au site Padel Bruxelles (1001).
              </p>
              <p><strong>Login :</strong> admin-bruxelles</p>
              <p><strong>Mot de passe :</strong> secret-site</p>

              <button type="button" (click)="utiliserAdminBruxelles()">
                Utiliser ce compte
              </button>
            </article>
          </div>
        </div>

        <form (ngSubmit)="connecter()" class="formulaire">
          <label for="login">Login</label>
          <input
            id="login"
            name="login"
            type="text"
            [(ngModel)]="login"
            placeholder="admin-global"
            required
          >

          <label for="motDePasse">Mot de passe</label>
          <input
            id="motDePasse"
            name="motDePasse"
            type="password"
            [(ngModel)]="motDePasse"
            placeholder="secret"
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
    .admin-demo-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
      gap: 16px;
      margin-top: 14px;
    }

    .admin-demo-card {
      padding: 16px;
      border: 1px solid #bfdbfe;
      border-radius: 12px;
      background: #ffffff;
      box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
    }

    .admin-demo-card h4 {
      margin: 0 0 10px;
      color: #003b95;
    }

    .admin-demo-card p {
      margin: 8px 0;
    }

    .admin-demo-card button {
      margin-top: 12px;
    }

    .succes {
      margin-top: 16px;
      color: #047857;
      font-weight: 700;
    }
  `]
})
export class AdminLoginComponent {
  login = 'admin-global';
  motDePasse = 'secret';

  readonly chargement = signal(false);
  readonly messageErreur = signal<string | null>(null);
  readonly messageSucces = signal<string | null>(null);

  constructor(
    private readonly authApiService: AuthApiService,
    readonly authContextService: AuthContextService,
    private readonly router: Router
  ) {
  }

  utiliserAdminGlobal(): void {
    this.login = 'admin-global';
    this.motDePasse = 'secret';
    this.messageErreur.set(null);
    this.messageSucces.set(null);
  }

  utiliserAdminBruxelles(): void {
    this.login = 'admin-bruxelles';
    this.motDePasse = 'secret-site';
    this.messageErreur.set(null);
    this.messageSucces.set(null);
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
