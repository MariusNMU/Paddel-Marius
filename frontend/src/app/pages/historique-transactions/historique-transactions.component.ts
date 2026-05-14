import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HistoriquePaiementResponse } from '../../models/paiement.model';
import { AuthContextService } from '../../services/auth-context.service';
import { PaiementApiService } from '../../services/paiement-api.service';
import { extraireMessageErreur } from '../../shared/api-error.util';

@Component({
  selector: 'app-historique-transactions',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <section class="page">
      <h2>Historique des transactions</h2>

      <p>
        Cette page affiche tous les paiements réalisés par le joueur connecté :
        participations aux matches et règlements de dettes.
      </p>

      @if (!joueurConnecte()) {
        <div class="bloc-info">
          <h3>Aucun joueur connecté</h3>

          <p>
            Connecte-toi avec ton matricule pour consulter ton historique de transactions.
          </p>

          <a routerLink="/joueur" class="lien-action">
            Aller à la connexion joueur
          </a>
        </div>
      }

      @if (joueurConnecte()) {
        <div class="bloc-info">
          <h3>Joueur connecté</h3>

          <p><strong>Matricule :</strong> {{ joueurConnecte()?.matricule }}</p>
          <p><strong>Nom :</strong> {{ joueurConnecte()?.nom }} {{ joueurConnecte()?.prenom }}</p>
        </div>

        <button type="button" (click)="chargerHistorique()" [disabled]="chargement()">
          {{ chargement() ? 'Chargement...' : 'Actualiser l’historique' }}
        </button>
      }

      @if (messageErreur()) {
        <p class="erreur">
          {{ messageErreur() }}
        </p>
      }

      @if (transactions().length === 0 && rechercheEffectuee() && !chargement() && !messageErreur()) {
        <p>
          Aucune transaction trouvée pour ce joueur.
        </p>
      }

      @if (transactions().length > 0) {
        <div class="bloc-info">
          <h3>Résumé</h3>

          <div class="resume-grid">
            <p>
              <strong>Nombre de transactions</strong><br>
              {{ transactions().length }}
            </p>

            <p>
              <strong>Total payé</strong><br>
              {{ totalPaye() | number:'1.2-2' }} €
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
          <tr *ngFor="let transaction of transactions()">
            <td>{{ transaction.dateHeurePaiement }}</td>
            <td>{{ transaction.naturePaiement }}</td>
            <td>{{ transaction.montant | number:'1.2-2' }} €</td>
            <td>{{ transaction.statutPaiement }}</td>
            <td>{{ transaction.matchId || '-' }}</td>
            <td>{{ transaction.participationId || '-' }}</td>
            <td>{{ transaction.detteId || '-' }}</td>
          </tr>
          </tbody>
        </table>
      }
    </section>
  `,
  styles: [`
    .resume-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
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
export class HistoriqueTransactionsComponent implements OnInit {
  readonly transactions = signal<HistoriquePaiementResponse[]>([]);
  readonly chargement = signal(false);
  readonly rechercheEffectuee = signal(false);
  readonly messageErreur = signal('');

  constructor(
    private readonly authContextService: AuthContextService,
    private readonly paiementApiService: PaiementApiService
  ) {
  }

  ngOnInit(): void {
    if (this.joueurConnecte()) {
      this.chargerHistorique();
    }
  }

  joueurConnecte() {
    return this.authContextService.joueur();
  }

  totalPaye(): number {
    return this.transactions()
      .reduce((total, transaction) => total + transaction.montant, 0);
  }

  chargerHistorique(): void {
    this.messageErreur.set('');
    this.transactions.set([]);
    this.rechercheEffectuee.set(true);

    const joueur = this.joueurConnecte();

    if (!joueur) {
      this.messageErreur.set('Aucun joueur connecté.');
      return;
    }

    this.chargement.set(true);

    this.paiementApiService.consulterHistoriquePaiements(joueur.matricule)
      .subscribe({
        next: transactions => {
          this.transactions.set(transactions);
          this.chargement.set(false);
        },
        error: error => {
          this.messageErreur.set(extraireMessageErreur(error));
          this.chargement.set(false);
        }
      });
  }
}
