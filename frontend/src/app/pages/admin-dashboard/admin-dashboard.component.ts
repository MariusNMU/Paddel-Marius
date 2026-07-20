import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthFacadeService } from '../../services/auth-facade.service';
import { enumLabel } from '../../shared/enum-label.util';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="page">
      <h2>Dashboard admin</h2>

      @if (authFacade.admin(); as admin) {
        <div class="dashboard-header">
          <div>
            <h3>Administrateur connecté</h3>
            <p class="admin-name">
              {{ admin.prenom }} {{ admin.nom }}
            </p>
            <p>
              Login : <strong>{{ admin.login }}</strong>
            </p>
          </div>

          <div class="role-card">
            <p>Rôle</p>
            <strong>{{ enumLabel(admin.roleAdministrateur) }}</strong>
          </div>
        </div>

        <div class="bloc-info">
          <h3>Périmètre d'accès</h3>

          @if (admin.siteId) {
            <p>
              Cet administrateur est limité au site :
              <strong>{{ admin.nomSite }} ({{ admin.siteId }})</strong>.
            </p>
          } @else {
            <p>
              Cet administrateur a un accès global à tous les sites.
            </p>
          }
        </div>

        <div class="bloc-info">
          <h3>Actions principales</h3>

          <div class="admin-actions-grid">
            <article class="admin-action-card">
              <h4>Statistiques</h4>
              <p>
                Consulter les matches, paiements, dettes ouvertes,
                chiffre d'affaires et taux de remplissage.
              </p>
              <a routerLink="/admin/statistiques">Ouvrir les statistiques</a>
            </article>

            @if (
              admin.roleAdministrateur
                === 'GLOBAL'
            ) {
              <article class="admin-action-card">
                <h4>Traitement de veille</h4>

                <p>
                  Lancer le traitement manuel qui analyse les matches du lendemain,
                  libère les places non payées et applique les règles de pénalité.
                </p>

                <a routerLink="/admin/traitement-veille">
                  Lancer le traitement
                </a>
              </article>
            }
          </div>
        </div>

        <div class="bloc-info">
          <h3>Rappel pour la démo</h3>

          <ul>
            <li>L'admin Global peut consulter les données de tous les sites.</li>
            <li>L'admin Site est lié à un site précis.</li>
            <li>Le frontend ne contient aucun SQL.</li>
            <li>Le frontend consomme uniquement l'API REST du backend.</li>
          </ul>
        </div>

        <button type="button" class="danger-button" (click)="deconnecter()">
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
  `,
  styles: [`
    .dashboard-header {
      display: grid;
      grid-template-columns: minmax(0, 1fr) 180px;
      gap: 16px;
      margin-bottom: 20px;
      padding: 18px;
      border: 1px solid #bfdbfe;
      border-radius: 12px;
      background: #f8fbff;
    }

    .dashboard-header h3 {
      margin: 0 0 8px;
      color: #003b95;
    }

    .admin-name {
      margin: 0 0 8px;
      font-size: 22px;
      font-weight: 700;
    }

    .role-card {
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      border-radius: 12px;
      background: #dbeafe;
      color: #001f5c;
      padding: 16px;
      text-align: center;
    }

    .role-card p {
      margin: 0 0 6px;
      font-size: 13px;
      text-transform: uppercase;
      font-weight: 700;
    }

    .role-card strong {
      font-size: 22px;
    }

    .admin-actions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
      gap: 16px;
      margin-top: 14px;
    }

    .admin-action-card {
      padding: 16px;
      border: 1px solid #bfdbfe;
      border-radius: 12px;
      background: #ffffff;
      box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
    }

    .admin-action-card h4 {
      margin: 0 0 10px;
      color: #003b95;
    }

    .admin-action-card p {
      min-height: 72px;
      margin: 0 0 14px;
      color: #334155;
    }

    .admin-action-card a {
      display: inline-block;
      padding: 10px 14px;
      border-radius: 8px;
      background: #003b95;
      color: #ffffff;
      text-decoration: none;
      font-weight: 700;
    }

    .danger-button {
      margin-top: 20px;
      background: #991b1b;
    }

    @media (max-width: 700px) {
      .dashboard-header {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class AdminDashboardComponent {
  readonly enumLabel = enumLabel;

  constructor(
    readonly authFacade: AuthFacadeService
  ) {
  }

  deconnecter(): void {
    this.authFacade.deconnecterAdmin();
  }
}
