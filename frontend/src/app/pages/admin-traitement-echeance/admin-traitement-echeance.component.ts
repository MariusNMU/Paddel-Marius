import { DatePipe } from '@angular/common';
import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { RouterLink } from '@angular/router';
import {
  AdminTraitementEcheanceFacadeService
} from '../../services/admin-traitement-echeance-facade.service';

@Component({
  selector: 'app-admin-traitement-echeance',
  standalone: true,
  imports: [
    DatePipe,
    MatButtonModule,
    MatCardModule,
    RouterLink
  ],
  providers: [
    AdminTraitementEcheanceFacadeService
  ],
  template: `
    <section class="page">
      <h2>Traitement d'échéance</h2>

      @if (!facade.adminConnecte()) {
        <p class="erreur">
          Tu dois te connecter comme admin avant de lancer le traitement d'échéance.
        </p>

        <p>
          <a mat-button routerLink="/admin/login">
            Connexion admin
          </a>
        </p>
      } @else if (!facade.estAdminGlobal()) {
        <p class="erreur">
          Cette action est réservée aux administrateurs globaux.
        </p>

        <p>
          <a mat-button routerLink="/admin/dashboard">
            Retour dashboard admin
          </a>
        </p>
      } @else {
        <p>
          Le traitement d'échéance applique les règles métier aux matches
          dont l'heure de début ou de fin est atteinte.
        </p>

        <mat-card
          appearance="outlined"
          class="bloc-info"
        >
          <h3>Ce que fait le traitement</h3>

          <ul>
            <li>
              Il fait passer les matches arrivés à leur heure de début
              de « À venir » à « Démarré ».
            </li>
            <li>
              Il fait passer les matches arrivés à leur heure de fin
              de « Démarré » à « Terminé ».
            </li>
            <li>
              Il crée une dette quand le prix total du match
              n'est pas entièrement payé.
            </li>
            <li>
              Il peut créer une pénalité pour un match privé incomplet.
            </li>
          </ul>
        </mat-card>

        <mat-card
          appearance="outlined"
          class="bloc-info warning"
        >
          <h3>Déclenchement manuel</h3>

          <p>
            Le backend utilise la date et l'heure actuelles.
            Aucun choix de date n'est nécessaire.
          </p>

          <button
            mat-flat-button
            type="button"
            [disabled]="facade.chargement()"
            (click)="facade.lancerTraitement()"
          >
            {{
              facade.chargement()
                ? 'Traitement...'
                : 'Lancer le traitement d’échéance'
            }}
          </button>
        </mat-card>

        @if (facade.messageErreur()) {
          <p class="erreur">
            {{ facade.messageErreur() }}
          </p>
        }

        @if (facade.resultat(); as traitement) {
          <mat-card
            appearance="outlined"
            class="bloc-info"
          >
            <h3>Résultat du traitement</h3>

            <p>
              Exécuté le :
              <strong>
                {{
                  traitement.dateHeureTraitement
                    | date:'dd/MM/yyyy HH:mm'
                }}
              </strong>
            </p>
          </mat-card>

          <div class="traitement-grid">
            <mat-card
              appearance="outlined"
              class="traitement-card"
            >
              <span>Matches analysés</span>
              <strong>
                {{ traitement.matchesAnalyses }}
              </strong>
            </mat-card>

            <mat-card
              appearance="outlined"
              class="traitement-card"
            >
              <span>Matches démarrés</span>
              <strong>
                {{ traitement.matchesDemarres }}
              </strong>
            </mat-card>

            <mat-card
              appearance="outlined"
              class="traitement-card"
            >
              <span>Matches terminés</span>
              <strong>
                {{ traitement.matchesTermines }}
              </strong>
            </mat-card>

            <mat-card
              appearance="outlined"
              class="traitement-card warning"
            >
              <span>Dettes créées</span>
              <strong>
                {{ traitement.dettesCreees }}
              </strong>
            </mat-card>
          </div>
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

    .warning {
      border-color: #f5c2c7;
      background: #fff8f8;
    }

    .traitement-card.warning strong {
      color: #991b1b;
    }
  `]
})
export class AdminTraitementEcheanceComponent {
  constructor(
    readonly facade:
    AdminTraitementEcheanceFacadeService
  ) {
  }
}
