import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthContextService } from '../../services/auth-context.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="page">
      <h2>Dashboard admin</h2>

      @if (authContextService.admin(); as admin) {
        <div class="bloc-info">
          <h3>Admin connecté</h3>
          <p>
            {{ admin.prenom }} {{ admin.nom }}
            — rôle <strong>{{ admin.roleAdministrateur }}</strong>
          </p>

          @if (admin.siteId) {
            <p>Site : {{ admin.nomSite }} — id {{ admin.siteId }}</p>
          } @else {
            <p>Accès global à tous les sites.</p>
          }
        </div>

        <div class="actions">
          <a routerLink="/admin/statistiques">Voir les statistiques</a>
          <a routerLink="/admin/traitement-veille">Lancer le traitement de veille</a>
        </div>

        <button type="button" (click)="deconnecter()">
          Déconnecter l'admin
        </button>
      } @else {
        <p class="erreur">
          Aucun administrateur connecté.
        </p>

        <p>
          <a routerLink="/admin/login">Aller à la connexion admin</a>
        </p>
      }
    </section>
  `
})
export class AdminDashboardComponent {
  constructor(readonly authContextService: AuthContextService) {
  }

  deconnecter(): void {
    this.authContextService.deconnecterAdmin();
  }
}
