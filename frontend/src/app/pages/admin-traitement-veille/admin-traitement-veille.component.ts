import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AdminTraitementVeilleApiService } from '../../services/admin-traitement-veille-api.service';
import { AuthContextService } from '../../services/auth-context.service';
import { TraitementVeilleResponse } from '../../models/traitement-veille.model';
import { extraireMessageErreur } from '../../shared/api-error.util';

@Component({
  selector: 'app-admin-traitement-veille',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <section class="page">
      <h2>Traitement de veille</h2>

      @if (!authContextService.adminConnecte()) {
        <p class="erreur">
          Tu dois te connecter comme admin avant de lancer le traitement de veille.
        </p>

        <p>
          <a routerLink="/admin/login">Connexion admin</a>
        </p>
      } @else {
        <p>
          Ce traitement analyse les matches du lendemain par rapport à la date choisie.
        </p>

        <form (ngSubmit)="lancerTraitement()" class="formulaire">
          <label for="dateTraitement">Date de traitement</label>
          <input
            id="dateTraitement"
            name="dateTraitement"
            type="date"
            [(ngModel)]="dateTraitement"
            required
          >

          <button type="submit" [disabled]="chargement()">
            {{ chargement() ? 'Traitement...' : 'Lancer le traitement' }}
          </button>
        </form>

        @if (messageErreur()) {
          <p class="erreur">{{ messageErreur() }}</p>
        }

        @if (resultat(); as traitement) {
          <div class="bloc-info">
            <h3>Résultat du traitement</h3>

            <table>
              <tbody>
                <tr>
                  <th>Date de traitement</th>
                  <td>{{ traitement.dateTraitement }}</td>
                </tr>
                <tr>
                  <th>Date des matches traités</th>
                  <td>{{ traitement.dateMatchTraitee }}</td>
                </tr>
                <tr>
                  <th>Matches analysés</th>
                  <td>{{ traitement.matchesAnalyses }}</td>
                </tr>
                <tr>
                  <th>Matches passés publics</th>
                  <td>{{ traitement.matchesPassesPublics }}</td>
                </tr>
                <tr>
                  <th>Participations libérées</th>
                  <td>{{ traitement.participationsLiberees }}</td>
                </tr>
                <tr>
                  <th>Pénalités créées</th>
                  <td>{{ traitement.penalitesCreees }}</td>
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
export class AdminTraitementVeilleComponent {
  dateTraitement = '2026-05-19';

  readonly chargement = signal(false);
  readonly messageErreur = signal<string | null>(null);
  readonly resultat = signal<TraitementVeilleResponse | null>(null);

  constructor(
    private readonly traitementVeilleApiService: AdminTraitementVeilleApiService,
    readonly authContextService: AuthContextService
  ) {
  }

  lancerTraitement(): void {
    this.messageErreur.set(null);
    this.resultat.set(null);

    if (!this.dateTraitement) {
      this.messageErreur.set('La date de traitement est obligatoire.');
      return;
    }

    this.chargement.set(true);

    this.traitementVeilleApiService.traiterVeille(this.dateTraitement).subscribe({
      next: resultat => {
        this.resultat.set(resultat);
        this.chargement.set(false);
      },
      error: error => {
        this.messageErreur.set(extraireMessageErreur(error));
        this.chargement.set(false);
      }
    });
  }
}
