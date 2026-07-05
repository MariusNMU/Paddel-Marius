import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize, timeout } from 'rxjs';
import { ParametresMetierResponse } from '../../models/parametres-metier.model';
import { SoldeJoueurResponse } from '../../models/solde-joueur.model';
import { AuthContextService } from '../../services/auth-context.service';
import { ParametresMetierApiService } from '../../services/parametres-metier-api.service';
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
          <li>Chaque nouveau joueur reçoit <strong>{{ parametresMetier?.soldeInitialJoueur | number:'1.2-2' }} €</strong> au départ.</li>
          <li>Une participation coûte <strong>{{ parametresMetier?.montantParticipationStandard | number:'1.2-2' }} €</strong>.</li>
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

        <button type="button" (click)="chargerSolde()" [disabled]="chargement()">
          {{ chargement() ? 'Chargement...' : 'Actualiser mon solde' }}
        </button>
      }

      @if (messageErreur()) {
        <p class="erreur">
          {{ messageErreur() }}
        </p>
      }

      @if (solde(); as soldeActuel) {
        <div class="resultat solde-card">
          <h3>Solde disponible</h3>

          <p class="montant-principal">
            {{ soldeActuel.soldeCredit | number:'1.2-2' }} €
          </p>

          <div class="resume-grid">
            <p><strong>ID membre</strong><br>{{ soldeActuel.membreId }}</p>
            <p><strong>Matricule</strong><br>{{ soldeActuel.matricule }}</p>
            <p><strong>Solde crédit</strong><br>{{ soldeActuel.soldeCredit | number:'1.2-2' }} €</p>
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
export class MonSoldeComponent implements OnInit {
  parametresMetier: ParametresMetierResponse | null = null;

  readonly solde = signal<SoldeJoueurResponse | null>(null);
  readonly messageErreur = signal('');
  readonly chargement = signal(false);

  constructor(
    private readonly authContextService: AuthContextService,
    private readonly soldeJoueurApiService: SoldeJoueurApiService,
    private readonly parametresMetierApiService: ParametresMetierApiService
  ) {
  }

  ngOnInit(): void {
    this.chargerParametresMetier();

    if (this.joueurConnecte()) {
      this.chargerSolde();
    }
  }

  private chargerParametresMetier(): void {
    this.parametresMetierApiService.consulterParametresMetier().subscribe({
      next: parametres => {
        this.parametresMetier = parametres;
      },
      error: error => {
        this.messageErreur.set(extraireMessageErreur(error));
        this.parametresMetier = null;
      }
    });
  }

  joueurConnecte() {
    return this.authContextService.joueur();
  }

  chargerSolde(): void {
    this.messageErreur.set('');
    this.solde.set(null);

    const joueur = this.joueurConnecte();

    if (!joueur) {
      this.messageErreur.set('Aucun joueur connecté.');
      return;
    }

    this.chargement.set(true);

    this.soldeJoueurApiService.consulterSolde(joueur.matricule)
      .pipe(
        timeout(10000),
        finalize(() => this.chargement.set(false))
      )
      .subscribe({
        next: (response) => {
          this.solde.set(response);
        },
        error: (error) => {
          this.messageErreur.set(extraireMessageErreur(error));
        }
      });
  }
}
