import { CommonModule } from '@angular/common';
import {
  Component,
  OnInit
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { MonSoldeFacadeService } from '../../services/mon-solde-facade.service';
import { enumLabel } from '../../shared/enum-label.util';

@Component({
  selector: 'app-mon-solde',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink
  ],
  providers: [
    MonSoldeFacadeService
  ],
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
          <li>
            Chaque nouveau joueur reçoit
            <strong>
              {{
                facade.parametresMetier()
                  ?.soldeInitialJoueur
                  | number:'1.2-2'
              }} €
            </strong>
            au départ.
          </li>

          <li>
            Une participation coûte
            <strong>
              {{
                facade.parametresMetier()
                  ?.montantParticipationStandard
                  | number:'1.2-2'
              }} €
            </strong>.
          </li>

          <li>
            Le paiement d'une dette débite le solde du montant restant dû.
          </li>

          <li>
            Une annulation de match par fermeture rembourse les joueurs ayant payé.
          </li>
        </ul>
      </div>

      @if (!facade.joueur()) {
        <div class="bloc-info">
          <h3>Aucun joueur connecté</h3>

          <p>
            Connecte-toi d'abord avec ton matricule pour consulter ton solde.
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

          <p>
            <strong>Catégorie :</strong>
            {{
              enumLabel(
                joueur.categorieMembre
              )
            }}
          </p>
        </div>

        <button
          type="button"
          (click)="facade.chargerSolde()"
          [disabled]="facade.chargement()"
        >
          {{
            facade.chargement()
              ? 'Chargement...'
              : 'Actualiser mon solde'
          }}
        </button>
      }

      @if (facade.messageErreur()) {
        <p class="erreur">
          {{ facade.messageErreur() }}
        </p>
      }

      @if (facade.solde(); as soldeActuel) {
        <div class="resultat solde-card">
          <h3>Solde disponible</h3>

          <p class="montant-principal">
            {{
              soldeActuel.soldeCredit
                | number:'1.2-2'
            }} €
          </p>

          <div class="resume-grid">
            <p>
              <strong>Matricule</strong>
              <br>
              {{ soldeActuel.matricule }}
            </p>

            <p>
              <strong>Solde crédit</strong>
              <br>
              {{
                soldeActuel.soldeCredit
                  | number:'1.2-2'
              }} €
            </p>
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
      grid-template-columns:
        repeat(
          auto-fit,
          minmax(180px, 1fr)
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
export class MonSoldeComponent
  implements OnInit {
  readonly enumLabel = enumLabel;

  constructor(
    readonly facade:
    MonSoldeFacadeService
  ) {
  }

  ngOnInit(): void {
    this.facade.initialiser();
  }
}
