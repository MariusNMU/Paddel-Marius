import { DatePipe } from '@angular/common';
import {
  Component,
  OnInit
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { RouterLink } from '@angular/router';
import { AdminStatistiquesFacadeService } from '../../services/admin-statistiques-facade.service';

@Component({
  selector: 'app-admin-statistiques',
  standalone: true,
  imports: [
    DatePipe,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    RouterLink
  ],
  providers: [AdminStatistiquesFacadeService],
  template: `
    <section class="page">
      <h2>Statistiques admin</h2>

      @if (!facade.admin()) {
        <p class="erreur">
          Tu dois te connecter comme admin avant de consulter les statistiques.
        </p>

        <p>
          <a mat-button routerLink="/admin/login">
            Connexion admin
          </a>
        </p>
      } @else {
        <p>
          Cette page permet de consulter les indicateurs principaux du MVP :
          matches, paiements, chiffre d'affaires, dettes et taux de remplissage.
        </p>

        <mat-card
          appearance="outlined"
          class="bloc-info"
        >
          <h3>Périodes rapides</h3>

          <div class="actions">
            <button
              mat-stroked-button
              type="button"
              (click)="selectionnerPeriode('moisCourant')"
            >
              Mois courant
            </button>

            <button
              mat-stroked-button
              type="button"
              (click)="selectionnerPeriode('prochainsJours')"
            >
              7 prochains jours
            </button>

            <button
              mat-stroked-button
              type="button"
              (click)="selectionnerPeriode('demo')"
            >
              Période démo complète
            </button>
          </div>
        </mat-card>

        <form (ngSubmit)="chargerStatistiques()" class="formulaire">
          <mat-form-field appearance="outline">
            <mat-label>Date début</mat-label>
            <input
              matInput
              id="dateDebut"
              name="dateDebut"
              type="date"
              [ngModel]="facade.dateDebut()"
              (ngModelChange)="
                facade.modifierDateDebut($event)
              "
              required
            >
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Date fin</mat-label>
            <input
              matInput
              id="dateFin"
              name="dateFin"
              type="date"
              [ngModel]="facade.dateFin()"
              (ngModelChange)="
                facade.modifierDateFin($event)
              "
              required
            >
          </mat-form-field>

          @if (facade.estAdminGlobal()) {
            <mat-form-field appearance="outline">
              <mat-label>Vue</mat-label>
              <select
                matNativeControl
                id="siteId"
                name="siteId"
                [ngModel]="facade.siteId()"
                (ngModelChange)="
                  facade.modifierSiteId($event)
                "
                [disabled]="facade.chargementSites()"
              >
                <option [ngValue]="null">
                  Tous les sites
                </option>

                @for (
                  site of facade.sites();
                  track site.siteId
                  ) {
                  <option [ngValue]="site.siteId">
                    {{ site.nom }}
                  </option>
                }
              </select>
            </mat-form-field>
          } @else if (facade.admin(); as admin) {
            <mat-card
              appearance="outlined"
              class="bloc-info"
            >
              <strong>Vue limitée à ton site :</strong>
              {{ admin.nomSite || 'Site' }}
            </mat-card>
          }

          <button
            mat-flat-button
            type="submit"
            [disabled]="
              facade.chargement()
              || facade.chargementSites()
            "
          >
            {{ facade.chargement() ? 'Chargement...' : 'Charger les statistiques' }}
          </button>
        </form>

        @if (facade.messageErreur()) {
          <p class="erreur">{{ facade.messageErreur() }}</p>
        }

        @if (facade.statistiques(); as stats) {
          <mat-card
            appearance="outlined"
            class="bloc-info"
          >
            <h3>Vue affichée</h3>

            <p>
              Période :
              <strong>{{ stats.dateDebut | date:'dd/MM/yyyy' }}</strong>
              →
              <strong>{{ stats.dateFin | date:'dd/MM/yyyy' }}</strong>
            </p>

            @if (stats.siteId) {
              <p>
                Site :
                <strong>{{ stats.nomSite }}</strong>
              </p>
            } @else {
              <p>
                Vue :
                <strong>globale tous sites</strong>
              </p>
            }
          </mat-card>

          <div class="stats-grid">
            <mat-card appearance="outlined" class="stat-card">
              <span>Matches</span>
              <strong>{{ stats.nombreMatches }}</strong>
            </mat-card>

            <mat-card appearance="outlined" class="stat-card">
              <span>À venir</span>
              <strong>{{ stats.nombreMatchesAVenir }}</strong>
            </mat-card>

            <mat-card appearance="outlined" class="stat-card">
              <span>Terminés</span>
              <strong>{{ stats.nombreMatchesTermines }}</strong>
            </mat-card>

            <mat-card appearance="outlined" class="stat-card">
              <span>Paiements</span>
              <strong>{{ stats.nombrePaiements }}</strong>
            </mat-card>

            <mat-card appearance="outlined" class="stat-card">
              <span>Chiffre d'affaires</span>
              <strong>{{ stats.chiffreAffaires }} €</strong>
            </mat-card>

            <mat-card appearance="outlined" class="stat-card warning">
              <span>Dettes ouvertes</span>
              <strong>{{ stats.nombreDettesOuvertes }}</strong>
            </mat-card>

            <mat-card appearance="outlined" class="stat-card warning">
              <span>Montant dettes</span>
              <strong>{{ stats.montantDettesOuvertes }} €</strong>
            </mat-card>

            <mat-card appearance="outlined" class="stat-card">
              <span>Taux remplissage</span>
              <strong>{{ stats.tauxRemplissage }} %</strong>
            </mat-card>
          </div>

          <mat-card
            appearance="outlined"
            class="bloc-info"
          >
            <h3>Détail complet</h3>

            <table>
              <tbody>
              <tr>
                <th>Nombre de matches</th>
                <td>{{ stats.nombreMatches }}</td>
              </tr>
              <tr>
                <th>Matches à venir</th>
                <td>{{ stats.nombreMatchesAVenir }}</td>
              </tr>
              <tr>
                <th>Matches terminés</th>
                <td>{{ stats.nombreMatchesTermines }}</td>
              </tr>
              <tr>
                <th>Nombre de paiements</th>
                <td>{{ stats.nombrePaiements }}</td>
              </tr>
              <tr>
                <th>Chiffre d'affaires</th>
                <td>{{ stats.chiffreAffaires }} €</td>
              </tr>
              <tr>
                <th>Dettes ouvertes</th>
                <td>{{ stats.nombreDettesOuvertes }}</td>
              </tr>
              <tr>
                <th>Montant dettes ouvertes</th>
                <td>{{ stats.montantDettesOuvertes }} €</td>
              </tr>
              <tr>
                <th>Participations actives</th>
                <td>{{ stats.nombreParticipationsActives }}</td>
              </tr>
              <tr>
                <th>Capacité théorique joueurs</th>
                <td>{{ stats.capaciteTheoriqueJoueurs }}</td>
              </tr>
              <tr>
                <th>Taux de remplissage</th>
                <td>{{ stats.tauxRemplissage }} %</td>
              </tr>
              </tbody>
            </table>
          </mat-card>
        }

        <p>
          <a mat-button routerLink="/admin/dashboard">
            Retour dashboard admin
          </a>
        </p>
      }
    </section>
  `,
  styles: [`
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: 14px;
      margin: 20px 0;
    }

    .stat-card {
      padding: 16px;
      border: 1px solid #bfdbfe;
      border-radius: 12px;
      background: #ffffff;
      box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
    }

    .stat-card span {
      display: block;
      margin-bottom: 8px;
      color: #64748b;
      font-weight: 700;
      font-size: 13px;
      text-transform: uppercase;
    }

    .stat-card strong {
      color: #003b95;
      font-size: 24px;
    }

    .stat-card.warning strong {
      color: #991b1b;
    }

    form mat-form-field {
      width: 100%;
    }
  `]
})
export class AdminStatistiquesComponent
  implements OnInit {

  constructor(
    readonly facade:
    AdminStatistiquesFacadeService
  ) {
  }

  ngOnInit(): void {
    this.facade.initialiser();
  }

  selectionnerPeriode(
    periode:
      'moisCourant'
      | 'prochainsJours'
      | 'demo'
  ): void {
    this.facade.selectionnerPeriode(
      periode
    );
  }

  chargerStatistiques(): void {
    this.facade.chargerStatistiques();
  }
}
