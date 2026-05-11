import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AdminStatsApiService } from '../../services/admin-stats-api.service';
import { AuthContextService } from '../../services/auth-context.service';
import { StatistiquesAdminResponse } from '../../models/statistique.model';
import { extraireMessageErreur } from '../../shared/api-error.util';

interface SiteOption {
  id: number | null;
  libelle: string;
}

@Component({
  selector: 'app-admin-statistiques',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <section class="page">
      <h2>Statistiques admin</h2>

      @if (!authContextService.adminConnecte()) {
        <p class="erreur">
          Tu dois te connecter comme admin avant de consulter les statistiques.
        </p>

        <p>
          <a routerLink="/admin/login">Connexion admin</a>
        </p>
      } @else {
        <p>
          Cette page permet de consulter les indicateurs principaux du MVP :
          matches, paiements, chiffre d'affaires, dettes et taux de remplissage.
        </p>

        <div class="bloc-info">
          <h3>Périodes rapides</h3>

          <div class="actions">
            <button type="button" (click)="selectionnerPeriode('mai')">
              Mai 2026
            </button>

            <button type="button" (click)="selectionnerPeriode('juin')">
              Juin 2026
            </button>

            <button type="button" (click)="selectionnerPeriode('demo')">
              Période démo complète
            </button>
          </div>
        </div>

        <form (ngSubmit)="chargerStatistiques()" class="formulaire">
          <label for="dateDebut">Date début</label>
          <input
            id="dateDebut"
            name="dateDebut"
            type="date"
            [(ngModel)]="dateDebut"
            required
          >

          <label for="dateFin">Date fin</label>
          <input
            id="dateFin"
            name="dateFin"
            type="date"
            [(ngModel)]="dateFin"
            required
          >

          <label for="siteId">Vue</label>
          <select
            id="siteId"
            name="siteId"
            [(ngModel)]="siteId"
          >
            @for (site of sites; track site.libelle) {
              <option [ngValue]="site.id">
                {{ site.libelle }}
              </option>
            }
          </select>

          <button type="submit" [disabled]="chargement()">
            {{ chargement() ? 'Chargement...' : 'Charger les statistiques' }}
          </button>
        </form>

        @if (messageErreur()) {
          <p class="erreur">{{ messageErreur() }}</p>
        }

        @if (statistiques(); as stats) {
          <div class="bloc-info">
            <h3>Vue affichée</h3>

            <p>
              Période :
              <strong>{{ stats.dateDebut }}</strong>
              →
              <strong>{{ stats.dateFin }}</strong>
            </p>

            @if (stats.siteId) {
              <p>
                Site :
                <strong>{{ stats.nomSite }} ({{ stats.siteId }})</strong>
              </p>
            } @else {
              <p>
                Vue :
                <strong>globale tous sites</strong>
              </p>
            }
          </div>

          <div class="stats-grid">
            <article class="stat-card">
              <span>Matches</span>
              <strong>{{ stats.nombreMatches }}</strong>
            </article>

            <article class="stat-card">
              <span>À venir</span>
              <strong>{{ stats.nombreMatchesAVenir }}</strong>
            </article>

            <article class="stat-card">
              <span>Terminés</span>
              <strong>{{ stats.nombreMatchesTermines }}</strong>
            </article>

            <article class="stat-card">
              <span>Paiements</span>
              <strong>{{ stats.nombrePaiements }}</strong>
            </article>

            <article class="stat-card">
              <span>Chiffre d'affaires</span>
              <strong>{{ stats.chiffreAffaires }} €</strong>
            </article>

            <article class="stat-card warning">
              <span>Dettes ouvertes</span>
              <strong>{{ stats.nombreDettesOuvertes }}</strong>
            </article>

            <article class="stat-card warning">
              <span>Montant dettes</span>
              <strong>{{ stats.montantDettesOuvertes }} €</strong>
            </article>

            <article class="stat-card">
              <span>Taux remplissage</span>
              <strong>{{ stats.tauxRemplissage }} %</strong>
            </article>
          </div>

          <div class="bloc-info">
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
          </div>
        }

        <p>
          <a routerLink="/admin/dashboard">Retour dashboard admin</a>
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
  `]
})
export class AdminStatistiquesComponent {
  sites: SiteOption[] = [
    { id: null, libelle: 'Tous les sites' },
    { id: 1001, libelle: 'Padel Bruxelles (1001)' },
    { id: 1002, libelle: 'Padel Namur (1002)' }
  ];

  dateDebut = '2026-05-01';
  dateFin = '2026-06-30';
  siteId: number | null = null;

  readonly chargement = signal(false);
  readonly messageErreur = signal<string | null>(null);
  readonly statistiques = signal<StatistiquesAdminResponse | null>(null);

  constructor(
    private readonly adminStatsApiService: AdminStatsApiService,
    readonly authContextService: AuthContextService
  ) {
  }

  selectionnerPeriode(periode: 'mai' | 'juin' | 'demo'): void {
    if (periode === 'mai') {
      this.dateDebut = '2026-05-01';
      this.dateFin = '2026-05-31';
    }

    if (periode === 'juin') {
      this.dateDebut = '2026-06-01';
      this.dateFin = '2026-06-30';
    }

    if (periode === 'demo') {
      this.dateDebut = '2026-05-01';
      this.dateFin = '2026-06-30';
    }

    this.messageErreur.set(null);
    this.statistiques.set(null);
  }

  chargerStatistiques(): void {
    this.messageErreur.set(null);
    this.statistiques.set(null);

    if (!this.dateDebut || !this.dateFin) {
      this.messageErreur.set('Les dates de début et de fin sont obligatoires.');
      return;
    }

    const siteIdParam = this.siteId === null || this.siteId === undefined
      ? undefined
      : Number(this.siteId);

    this.chargement.set(true);

    this.adminStatsApiService.consulterStatistiques(
      this.dateDebut,
      this.dateFin,
      siteIdParam
    ).subscribe({
      next: statistiques => {
        this.statistiques.set(statistiques);
        this.chargement.set(false);
      },
      error: error => {
        this.messageErreur.set(extraireMessageErreur(error));
        this.chargement.set(false);
      }
    });
  }
}
