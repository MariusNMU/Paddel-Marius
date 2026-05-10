import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AdminStatsApiService } from '../../services/admin-stats-api.service';
import { AuthContextService } from '../../services/auth-context.service';
import { StatistiquesAdminResponse } from '../../models/statistique.model';
import { extraireMessageErreur } from '../../shared/api-error.util';

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

          <label for="siteId">Site id optionnel</label>
          <input
            id="siteId"
            name="siteId"
            type="number"
            [(ngModel)]="siteId"
            placeholder="1001"
          >

          <button type="submit" [disabled]="chargement()">
            {{ chargement() ? 'Chargement...' : 'Charger les statistiques' }}
          </button>
        </form>

        @if (messageErreur()) {
          <p class="erreur">{{ messageErreur() }}</p>
        }

        @if (statistiques(); as stats) {
          <div class="bloc-info">
            <h3>Résultat</h3>

            <p>
              Période :
              <strong>{{ stats.dateDebut }}</strong>
              →
              <strong>{{ stats.dateFin }}</strong>
            </p>

            @if (stats.siteId) {
              <p>Site : {{ stats.nomSite }} — id {{ stats.siteId }}</p>
            } @else {
              <p>Vue globale tous sites.</p>
            }

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
  `
})
export class AdminStatistiquesComponent {
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
