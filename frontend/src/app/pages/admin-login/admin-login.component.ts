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
        Connecte-toi avec un administrateur de démonstration.
      </p>

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

      @if (messageErreur()) {
        <p class="erreur">{{ messageErreur() }}</p>
      }

      <div class="bloc-info">
        <h3>Comptes de démonstration</h3>
        <ul>
          <li><strong>admin-global</strong> / secret</li>
          <li><strong>admin-bruxelles</strong> / secret-site</li>
        </ul>
      </div>

      <p>
        <a routerLink="/accueil">Retour à l'accueil</a>
      </p>
    </section>
  `
})
export class AdminLoginComponent {
  login = 'admin-global';
  motDePasse = 'secret';

  readonly chargement = signal(false);
  readonly messageErreur = signal<string | null>(null);

  constructor(
    private readonly authApiService: AuthApiService,
    private readonly authContextService: AuthContextService,
    private readonly router: Router
  ) {
  }

  connecter(): void {
    this.messageErreur.set(null);

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
