import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SoldeJoueurResponse } from '../../models/solde-joueur.model';
import { AuthContextService } from '../../services/auth-context.service';
import { SoldeJoueurApiService } from '../../services/solde-joueur-api.service';
import { extraireMessageErreur } from '../../shared/api-error.util';

@Component({
  selector: 'app-mon-solde',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <section class="page">
      <h2>Mon solde</h2>

      <p>
        Cette page affiche le solde crédit du joueur connecté.
        Ce solde sert à payer les participations, régler les dettes et recevoir les remboursements.
      </p>

      <div class="bloc-info">
        <h3>Règles du solde crédit</h3>

        <ul>
          <li>Chaque nouveau joueur reçoit <strong>100 €</strong> au départ.</li>
          <li>Une participation coûte <strong>15 €</strong>.</li>
          <li>Le paiement d'une dette débite le solde du montant restant dû.</li>
          <li>Une annulation de match par fermeture rembourse les joueurs ayant payé.</li>
        </ul>
      </div>

      @if (!joueurConnecte()) {
        <div class="bloc-info">
          <h3>Aucun joueur connecté</h3>

          <p>
            Connecte-toi d'abord avec ton matricule pour consulter ton solde.
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
          <p><strong>Catégorie :</strong> {{ joueurConnecte()?.categorieMembre }}</p>
        </div>

        <button type="button" (click)="chargerSolde()" [disabled]="chargement">
          {{ chargement ? 'Chargement...' : 'Actualiser mon solde' }}
        </button>
      }

      @if (messageErreur) {
        <p class="erreur">
          {{ messageErreur }}
        </p>
      }

      @if (solde) {
        <div class="resultat solde-card">
          <h3>Solde disponible</h3>

          <p class="montant-principal">
            {{ solde.soldeCredit | number:'1.2-2' }} €
          </p>

          <div class="resume-grid">
            <p><strong>ID membre</strong><br>{{ solde.membreId }}</p>
            <p><strong>Matricule</strong><br>{{ solde.matricule }}</p>
            <p><strong>Solde crédit</strong><br>{{ solde.soldeCredit | number:'1.2-2' }} €</p>
          </div>
        </div>
      }
    </section>
  `,
  styles: [`
    .solde-card {
      border-color: #93c5fd;
      background: #f8fbff;
    }

    .montant-principal {
      margin: 12px 0 18px 0;
      font-size: 34px;
      font-weight: 700;
    }

    .resume-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
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
export class MonSoldeComponent {
  solde: SoldeJoueurResponse | null = null;
  messageErreur = '';
  chargement = false;

  constructor(
    private readonly authContextService: AuthContextService,
    private readonly soldeJoueurApiService: SoldeJoueurApiService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {
    if (this.joueurConnecte()) {
      this.chargerSolde();
    }
  }

  joueurConnecte() {
    return this.authContextService.joueur();
  }

  chargerSolde(): void {
    this.messageErreur = '';
    this.solde = null;

    const joueur = this.joueurConnecte();

    if (!joueur) {
      this.messageErreur = 'Aucun joueur connecté.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    this.chargement = true;
    this.changeDetectorRef.detectChanges();

    this.soldeJoueurApiService.consulterSolde(joueur.matricule).subscribe({
      next: (response) => {
        this.solde = response;
        this.chargement = false;
        this.changeDetectorRef.detectChanges();
      },
      error: (error) => {
        this.messageErreur = extraireMessageErreur(error);
        this.chargement = false;
        this.changeDetectorRef.detectChanges();
      }
    });
  }
}
