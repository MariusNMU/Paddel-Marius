import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { RouterLink } from '@angular/router';
import { AuthFacadeService } from '../../services/auth-facade.service';
import { enumLabel } from '../../shared/enum-label.util';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    MatButtonModule,
    MatCardModule,
    RouterLink
  ],
  template: `
    <section class="page">
      <h2>Dashboard admin</h2>

      @if (authFacade.admin(); as admin) {
        <mat-card
          appearance="outlined"
          class="dashboard-header"
        >
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
        </mat-card>

        <mat-card
          appearance="outlined"
          class="bloc-info"
        >
          <h3>Périmètre d'accès</h3>

          @if (admin.siteId) {
            <p>
              Cet administrateur est limité au site :
              <strong>{{ admin.nomSite }}</strong>.
            </p>
          } @else {
            <p>
              Cet administrateur a un accès global à tous les sites.
            </p>
          }
        </mat-card>

        <mat-card
          appearance="outlined"
          class="bloc-info"
        >
          <h3>Actions principales</h3>

          <div class="admin-actions-grid">
            <mat-card
              appearance="outlined"
              class="admin-action-card"
            >
              <h4>Statistiques</h4>
              <p>
                Consulter les matches, paiements, dettes ouvertes,
                chiffre d'affaires et taux de remplissage.
              </p>
              <a
                mat-flat-button
                class="admin-action-button"
                routerLink="/admin/statistiques"
              >
                Ouvrir les statistiques
              </a>
            </mat-card>

            <mat-card
              appearance="outlined"
              class="admin-action-card"
            >
              <h4>État des matchs et terrains</h4>
              <p>
                Consulter l’occupation, les fermetures et l’état
                des matchs pour une date et un site.
              </p>
              <a
                mat-flat-button
                class="admin-action-button"
                routerLink="/admin/etat-operationnel"
              >
                Ouvrir la vue opérationnelle
              </a>
            </mat-card>

            @if (
              admin.roleAdministrateur
              === 'GLOBAL'
              ) {
              <mat-card
                appearance="outlined"
                class="admin-action-card"
              >
                <h4>Traitement de veille</h4>

                <p>
                  Lancer le traitement manuel qui analyse les matches du lendemain,
                  libère les places non payées et rend publics les matchs privés incomplets.
                </p>

                <a
                  mat-flat-button
                  class="admin-action-button"
                  routerLink="/admin/traitement-veille"
                >
                  Lancer le traitement
                </a>
              </mat-card>

              <mat-card
                appearance="outlined"
                class="admin-action-card"
              >
                <h4>Traitement d'échéance</h4>

                <p>
                  Faire évoluer les matches démarrés ou terminés
                  et créer les dettes nécessaires à l'heure courante.
                </p>

                <a
                  mat-flat-button
                  class="admin-action-button"
                  routerLink="/admin/traitement-echeance"
                >
                  Lancer le traitement
                </a>
              </mat-card>
            }
          </div>
        </mat-card>

        <mat-card
          appearance="outlined"
          class="bloc-info"
        >
          <h3>Rappel pour la démo</h3>

          <ul>
            <li>L'admin Global peut consulter les données de tous les sites.</li>
            <li>L'admin Site est lié à un site précis.</li>
            <li>Le frontend ne contient aucun SQL.</li>
            <li>Le frontend consomme uniquement l'API REST du backend.</li>
          </ul>
        </mat-card>

        <button
          mat-flat-button
          type="button"
          class="danger-button"
          (click)="deconnecter()"
        >
          Déconnecter l'admin
        </button>
      } @else {
        <p class="erreur">
          Aucun administrateur connecté.
        </p>

        <p>
          <a mat-button routerLink="/admin/login">
            Aller à la connexion admin
          </a>
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
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 18px;
      margin-top: 16px;
    }

    .admin-action-card {
      display: flex;
      flex-direction: column;
      min-height: 220px;
      padding: 20px;
      border: 1px solid #bfdbfe;
      border-radius: 12px;
      background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
      box-shadow: 0 6px 16px rgba(15, 23, 42, 0.07);
    }

    .admin-action-card h4 {
      margin: 0 0 10px;
      color: #003b95;
    }

    .admin-action-card p {
      flex: 1;
      margin: 0 0 14px;
      color: #334155;
    }

    .admin-action-card .admin-action-button {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      min-height: 44px;
      margin-top: auto;
      align-self: stretch;
      justify-self: stretch;
      border-radius: 8px;
      line-height: 1.25;
      text-align: center;
    }

    .danger-button {
      margin-top: 20px;
      background: #991b1b;
    }

    @media (max-width: 700px) {
      .dashboard-header {
        grid-template-columns: 1fr;
      }

      .admin-actions-grid {
        grid-template-columns: 1fr;
      }

      .admin-action-card {
        min-height: 0;
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
