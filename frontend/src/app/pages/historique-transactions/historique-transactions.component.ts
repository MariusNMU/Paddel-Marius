import { CommonModule } from '@angular/common';
import {
  Component,
  OnInit
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { HistoriqueTransactionsFacadeService } from '../../services/historique-transactions-facade.service';
import { enumLabel } from '../../shared/enum-label.util';

@Component({
  selector: 'app-historique-transactions',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink
  ],
  providers: [
    HistoriqueTransactionsFacadeService
  ],
  template: `
    <section class="page">
      <h2>Historique des transactions</h2>

      <p>
        Cette page affiche tous les paiements réalisés par le joueur connecté :
        participations aux matches et règlements de dettes.
      </p>

      @if (!facade.joueur()) {
        <div class="bloc-info">
          <h3>Aucun joueur connecté</h3>

          <p>
            Connecte-toi avec ton matricule pour consulter ton historique de transactions.
          </p>

          <a
            routerLink="/joueur"
            class="lien-action"
          >
            Aller à la connexion joueur
          </a>
        </div>
      }

      @if (facade.joueur(); as joueur) {
        <div class="bloc-info">
          <h3>Joueur connecté</h3>

          <p>
            <strong>Matricule :</strong>
            {{ joueur.matricule }}
          </p>

          <p>
            <strong>Nom :</strong>
            {{ joueur.nom }}
            {{ joueur.prenom }}
          </p>
        </div>

        <button
          type="button"
          (click)="facade.chargerHistorique()"
          [disabled]="facade.chargement()"
        >
          {{
            facade.chargement()
              ? 'Chargement...'
              : 'Actualiser l’historique'
          }}
        </button>
      }

      @if (facade.messageErreur()) {
        <p class="erreur">
          {{ facade.messageErreur() }}
        </p>
      }

      @if (
        facade.transactions().length === 0
        && facade.rechercheEffectuee()
        && !facade.chargement()
        && !facade.messageErreur()
      ) {
        <p>
          Aucune transaction trouvée pour ce joueur.
        </p>
      }

      @if (facade.transactions().length > 0) {
        <div class="bloc-info">
          <h3>Résumé</h3>

          <div class="resume-grid">
            <p>
              <strong>
                Nombre de transactions
              </strong>
              <br>
              {{ facade.transactions().length }}
            </p>

            <p>
              <strong>Total payé</strong>
              <br>
              {{
                facade.totalPaye()
                  | number:'1.2-2'
              }} €
            </p>
          </div>
        </div>

        <table>
          <thead>
            <tr>
              <th>Date</th>
              <th>Nature</th>
              <th>Montant</th>
              <th>Statut</th>
              <th>Match</th>
              <th>Participation</th>
              <th>Dette</th>
            </tr>
          </thead>

          <tbody>
            <tr
              *ngFor="
                let transaction
                of facade.transactions()
              "
            >
              <td>
                {{
                  transaction
                    .dateHeurePaiement
                }}
              </td>

              <td>
                {{
                  enumLabel(
                    transaction
                      .naturePaiement
                  )
                }}
              </td>

              <td>
                {{
                  transaction.montant
                    | number:'1.2-2'
                }} €
              </td>

              <td>
                {{
                  enumLabel(
                    transaction
                      .statutPaiement
                  )
                }}
              </td>

              <td>
                {{ transaction.matchId || '-' }}
              </td>

              <td>
                {{
                  transaction
                    .participationId || '-'
                }}
              </td>

              <td>
                {{ transaction.detteId || '-' }}
              </td>
            </tr>
          </tbody>
        </table>
      }
    </section>
  `,
  styles: [`
    .resume-grid {
      display: grid;
      grid-template-columns:
        repeat(
          auto-fit,
          minmax(190px, 1fr)
        );
      gap: 12px;
      margin-top: 16px;
    }

    .resume-grid p {
      margin: 0;
      padding: 12px;
      border: 1px solid #bfdbfe;
      border-radius: 10px;
      background: #ffffff;
    }

    .lien-action {
      display: inline-block;
      margin-top: 12px;
      font-weight: 600;
    }
  `]
})
export class HistoriqueTransactionsComponent
  implements OnInit {
  readonly enumLabel = enumLabel;

  constructor(
    readonly facade:
    HistoriqueTransactionsFacadeService
  ) {
  }

  ngOnInit(): void {
    this.facade.initialiser();
  }
}
