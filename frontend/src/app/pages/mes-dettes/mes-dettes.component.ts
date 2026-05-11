import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { DetteResponse } from '../../models/dette.model';
import { AuthContextService } from '../../services/auth-context.service';
import { DetteApiService } from '../../services/dette-api.service';
import { extraireMessageErreur } from '../../shared/api-error.util';

@Component({
  selector: 'app-mes-dettes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page">
      <h2>Mes dettes</h2>

      <p>
        Cette page permet de consulter les dettes ouvertes du joueur connecté.
        Une dette ouverte bloque la création d'une nouvelle réservation.
      </p>

      <ng-container *ngIf="authContext.joueur() as joueur; else aucunJoueurConnecte">
        <div class="bloc-info">
          <h3>Joueur connecté</h3>

          <p>
            <strong>{{ joueur.prenom }} {{ joueur.nom }}</strong>
            — matricule {{ joueur.matricule }}
          </p>

          <p>
            Seules les dettes de ce joueur peuvent être consultées depuis cet écran.
          </p>

          <button type="button" (click)="chargerDettes()" [disabled]="chargement">
            {{ chargement ? 'Chargement...' : 'Actualiser mes dettes' }}
          </button>
        </div>

        <p *ngIf="messageErreur" class="erreur">
          {{ messageErreur }}
        </p>

        <p *ngIf="messageSucces" class="succes">
          {{ messageSucces }}
        </p>

        <div *ngIf="rechercheEffectuee && !messageErreur" class="bloc-info">
          <h3>Résumé</h3>

          <div class="resume-grid">
            <p>
              <strong>Matricule</strong><br>
              {{ joueur.matricule }}
            </p>

            <p>
              <strong>Dettes ouvertes</strong><br>
              {{ dettes.length }}
            </p>

            <p>
              <strong>Total restant</strong><br>
              {{ totalMontantRestant() }} €
            </p>
          </div>
        </div>

        <div *ngIf="dettes.length === 0 && rechercheEffectuee && !messageErreur" class="resultat">
          <h3>Aucune dette ouverte</h3>
          <p>
            Ce joueur ne présente actuellement aucune dette ouverte.
          </p>
        </div>

        <div *ngIf="dettes.length > 0" class="dettes-grid">
          <article *ngFor="let dette of dettes" class="dette-card">
            <h3>Dette {{ dette.detteId }}</h3>

            <div class="resume-grid">
              <p>
                <strong>Match</strong><br>
                {{ dette.matchId }}
              </p>

              <p>
                <strong>Montant initial</strong><br>
                {{ dette.montantInitial }} €
              </p>

              <p>
                <strong>Montant restant</strong><br>
                {{ dette.montantRestant }} €
              </p>

              <p>
                <strong>Statut</strong><br>
                {{ dette.statutDette }}
              </p>
            </div>

            <div class="paiement-zone">
              <label [for]="'montantDette' + dette.detteId">
                Montant à payer
              </label>

              <input
                [id]="'montantDette' + dette.detteId"
                type="number"
                min="0"
                step="0.01"
                [(ngModel)]="montantsPaiement[dette.detteId]"
                [name]="'montantDette' + dette.detteId"
              />

              <button
                type="button"
                (click)="payerDette(dette)"
                [disabled]="paiementEnCoursDetteId === dette.detteId"
              >
                {{ paiementEnCoursDetteId === dette.detteId ? 'Paiement...' : 'Payer cette dette' }}
              </button>
            </div>
          </article>
        </div>
      </ng-container>

      <ng-template #aucunJoueurConnecte>
        <p class="erreur">
          Aucun joueur connecté. Connecte-toi d'abord pour consulter tes dettes.
        </p>
      </ng-template>
    </section>
  `,
  styles: [`
    .aide {
      color: #64748b;
      font-size: 14px;
    }

    .succes {
      margin-top: 16px;
      color: #047857;
      font-weight: 700;
    }

    .resume-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
      gap: 12px;
      margin-top: 12px;
    }

    .resume-grid p {
      margin: 0;
      padding: 12px;
      border: 1px solid #bfdbfe;
      border-radius: 10px;
      background: #ffffff;
    }

    .dettes-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 16px;
      margin-top: 20px;
    }

    .dette-card {
      border: 1px solid #bfdbfe;
      border-radius: 12px;
      background: #f8fbff;
      padding: 16px;
      box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
    }

    .dette-card h3 {
      margin-top: 0;
      color: #003b95;
    }

    .paiement-zone {
      display: grid;
      gap: 10px;
      margin-top: 16px;
    }
  `]
})
export class MesDettesComponent implements OnInit {
  dettes: DetteResponse[] = [];
  montantsPaiement: Record<number, number> = {};

  chargement = false;
  rechercheEffectuee = false;
  paiementEnCoursDetteId: number | null = null;

  messageErreur = '';
  messageSucces = '';

  constructor(
    private readonly detteApiService: DetteApiService,
    readonly authContext: AuthContextService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {
  }

  ngOnInit(): void {
    if (this.authContext.joueur()) {
      this.chargerDettes();
    }
  }

  totalMontantRestant(): number {
    return this.dettes.reduce(
      (total, dette) => total + dette.montantRestant,
      0
    );
  }

  chargerDettes(conserverMessageSucces = false): void {
    this.messageErreur = '';

    if (!conserverMessageSucces) {
      this.messageSucces = '';
    }

    this.dettes = [];
    this.rechercheEffectuee = false;

    const joueur = this.authContext.joueur();

    if (!joueur) {
      this.messageErreur = 'Aucun joueur connecté. Connecte-toi d’abord pour consulter tes dettes.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    this.chargement = true;
    this.changeDetectorRef.detectChanges();

    this.detteApiService.consulterDettesOuvertes(joueur.matricule).subscribe({
      next: (dettes) => {
        this.dettes = dettes;
        this.rechercheEffectuee = true;
        this.chargement = false;

        this.montantsPaiement = {};

        for (const dette of dettes) {
          this.montantsPaiement[dette.detteId] = dette.montantRestant;
        }

        this.changeDetectorRef.detectChanges();
      },
      error: (error) => {
        this.messageErreur = extraireMessageErreur(error);
        this.chargement = false;
        this.changeDetectorRef.detectChanges();
      }
    });
  }

  payerDette(dette: DetteResponse): void {
    this.messageErreur = '';
    this.messageSucces = '';

    const montant = this.montantsPaiement[dette.detteId];

    if (!montant || montant <= 0) {
      this.messageErreur = 'Le montant du paiement doit être supérieur à 0.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    this.paiementEnCoursDetteId = dette.detteId;
    this.changeDetectorRef.detectChanges();

    this.detteApiService.payerDette(dette.detteId, { montant })
      .pipe(
        finalize(() => {
          this.paiementEnCoursDetteId = null;
          this.changeDetectorRef.detectChanges();
        })
      )
      .subscribe({
        next: (response) => {
          this.messageSucces = `Paiement réussi : dette ${response.detteId} payée pour ${response.montant} €.`;

          this.dettes = this.dettes.filter(
            detteOuverte => detteOuverte.detteId !== response.detteId
          );

          delete this.montantsPaiement[response.detteId];

          this.rechercheEffectuee = true;
          this.changeDetectorRef.detectChanges();
        },
        error: (error) => {
          this.messageErreur = extraireMessageErreur(error);
          this.changeDetectorRef.detectChanges();
        }
      });
  }
}
