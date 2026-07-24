import { DatePipe } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AdminTraitementVeilleFacadeService } from '../../services/admin-traitement-veille-facade.service';

@Component({
  selector: 'app-admin-traitement-veille',
  standalone: true,
  imports: [DatePipe, FormsModule, RouterLink],
  providers: [
    AdminTraitementVeilleFacadeService
  ],
  template: `
    <section class="page">
      <h2>Traitement de veille</h2>

      @if (!facade.adminConnecte()) {
        <p class="erreur">
          Tu dois te connecter comme admin avant de lancer le traitement de veille.
        </p>

        <p>
          <a routerLink="/admin/login">
            Connexion admin
          </a>
        </p>
      } @else if (!facade.estAdminGlobal()) {
        <p class="erreur">
          Cette action est réservée aux administrateurs globaux.
        </p>

        <p>
          <a routerLink="/admin/dashboard">
            Retour dashboard admin
          </a>
        </p>
      } @else {
        <p>
          Le traitement de veille sert à appliquer les règles métier avant les matches :
          passage public des matches privés incomplets, libération des places non payées
          et création éventuelle de pénalités.
        </p>

        <div class="bloc-info">
          <h3>Ce que fait le traitement</h3>

          <ul>
            <li>
              Il analyse les matches du lendemain par rapport à la date choisie.
            </li>
            <li>
              Un match privé incomplet peut devenir public.
            </li>
            <li>
              Une participation non payée peut être libérée.
            </li>
            <li>
              Une pénalité peut être créée pour l'organisateur responsable.
            </li>
          </ul>
        </div>

        <div class="bloc-info">
          <h3>Dates rapides de démonstration</h3>

          <div class="actions">
            <button
              type="button"
              (click)="facade.selectionnerDateRelative(0)"
            >
              Aujourd'hui
            </button>

            <button
              type="button"
              (click)="facade.selectionnerDateRelative(2)"
            >
              Avant match démo public
            </button>

            <button
              type="button"
              (click)="facade.selectionnerDateRelative(3)"
            >
              Avant match démo privé
            </button>
          </div>
        </div>

        <form
          (ngSubmit)="facade.lancerTraitement()"
          class="formulaire"
        >
          <label for="dateTraitement">
            Date de traitement
          </label>

          <input
            id="dateTraitement"
            name="dateTraitement"
            type="date"
            [ngModel]="facade.dateTraitement()"
            (ngModelChange)="facade.selectionnerDate($event)"
            required
          >

          <button
            type="submit"
            [disabled]="facade.chargement()"
          >
            {{
              facade.chargement()
                ? 'Traitement...'
                : 'Lancer le traitement de veille'
            }}
          </button>
        </form>

        @if (facade.messageErreur()) {
          <p class="erreur">
            {{ facade.messageErreur() }}
          </p>
        }

        @if (facade.resultat(); as traitement) {
          <div class="bloc-info">
            <h3>Vue affichée</h3>

            <p>
              Date de traitement :
              <strong>
                {{ traitement.dateTraitement | date:'dd/MM/yyyy' }}
              </strong>
            </p>

            <p>
              Matches analysés pour le :
              <strong>
                {{ traitement.dateMatchTraitee | date:'dd/MM/yyyy' }}
              </strong>
            </p>
          </div>

          <div class="traitement-grid">
            <article class="traitement-card">
              <span>Matches analysés</span>
              <strong>
                {{ traitement.matchesAnalyses }}
              </strong>
            </article>

            <article class="traitement-card">
              <span>Passés publics</span>
              <strong>
                {{ traitement.matchesPassesPublics }}
              </strong>
            </article>

            <article class="traitement-card">
              <span>Participations libérées</span>
              <strong>
                {{ traitement.participationsLiberees }}
              </strong>
            </article>

            <article
              class="traitement-card warning"
            >
              <span>Pénalités créées</span>
              <strong>
                {{ traitement.penalitesCreees }}
              </strong>
            </article>
          </div>

          <div class="bloc-info">
            <h3>Détail complet</h3>

            <table>
              <tbody>
              <tr>
                <th>Date de traitement</th>
                <td>
                  {{ traitement.dateTraitement | date:'dd/MM/yyyy' }}
                </td>
              </tr>
              <tr>
                <th>Date des matches traités</th>
                <td>
                  {{ traitement.dateMatchTraitee | date:'dd/MM/yyyy' }}
                </td>
              </tr>
              <tr>
                <th>Matches analysés</th>
                <td>
                  {{ traitement.matchesAnalyses }}
                </td>
              </tr>
              <tr>
                <th>Matches passés publics</th>
                <td>
                  {{ traitement.matchesPassesPublics }}
                </td>
              </tr>
              <tr>
                <th>Participations libérées</th>
                <td>
                  {{ traitement.participationsLiberees }}
                </td>
              </tr>
              <tr>
                <th>Pénalités créées</th>
                <td>
                  {{ traitement.penalitesCreees }}
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        }

        <p>
          <a routerLink="/admin/dashboard">
            Retour dashboard admin
          </a>
        </p>
      }
    </section>
  `,
  styles: [`
    .traitement-grid {
      display: grid;
      grid-template-columns:
        repeat(
          auto-fit,
          minmax(190px, 1fr)
        );
      gap: 14px;
      margin: 20px 0;
    }

    .traitement-card {
      padding: 16px;
      border: 1px solid #bfdbfe;
      border-radius: 12px;
      background: #ffffff;
      box-shadow:
        0 4px 12px
        rgba(15, 23, 42, 0.06);
    }

    .traitement-card span {
      display: block;
      margin-bottom: 8px;
      color: #64748b;
      font-weight: 700;
      font-size: 13px;
      text-transform: uppercase;
    }

    .traitement-card strong {
      color: #003b95;
      font-size: 24px;
    }

    .traitement-card.warning strong {
      color: #991b1b;
    }
  `]
})
export class AdminTraitementVeilleComponent {
  constructor(
    readonly facade:
    AdminTraitementVeilleFacadeService
  ) {
  }
}
