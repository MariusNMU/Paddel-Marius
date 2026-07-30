import { CommonModule } from '@angular/common';
import {
  Component,
  OnInit
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MesDettesFacadeService } from '../../services/mes-dettes-facade.service';
import { enumLabel } from '../../shared/enum-label.util';

@Component({
  selector: 'app-mes-dettes',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule
  ],
  providers: [
    MesDettesFacadeService
  ],
  template: `
    <section class="page">
      <h2>Mes dettes</h2>

      <p>
        Cette page permet de consulter les dettes ouvertes du joueur connecté.
        Une dette ouverte bloque la création d'une nouvelle réservation.
      </p>

      <ng-container *ngIf="facade.joueur() as joueur; else aucunJoueurConnecte">
        <mat-card
          appearance="outlined"
          class="bloc-info"
        >
          <h3>Joueur connecté</h3>

          <p>
            <strong>{{ joueur.prenom }} {{ joueur.nom }}</strong>
            — matricule {{ joueur.matricule }}
          </p>

          <p>
            Seules les dettes de ce joueur peuvent être consultées depuis cet écran.
          </p>

          <button
            mat-flat-button
            type="button"
            (click)="facade.chargerDettes()"
            [disabled]="facade.chargement()"
          >
            {{ facade.chargement() ? 'Chargement...' : 'Actualiser mes dettes' }}
          </button>
        </mat-card>

        <p *ngIf="facade.messageErreur()" class="erreur">
          {{ facade.messageErreur() }}
        </p>

        <p *ngIf="facade.messageSucces()" class="succes">
          {{ facade.messageSucces() }}
        </p>

        <mat-card
          *ngIf="
            facade.rechercheEffectuee()
            && !facade.messageErreur()
          "
          appearance="outlined"
          class="bloc-info"
        >
          <h3>Résumé</h3>

          <div class="resume-grid">
            <p>
              <strong>Matricule</strong><br>
              {{ joueur.matricule }}
            </p>

            <p>
              <strong>Dettes ouvertes</strong><br>
              {{ facade.dettes().length }}
            </p>

            <p>
              <strong>Total restant</strong><br>
              {{ facade.totalMontantRestant() | number:'1.2-2' }} €
            </p>
          </div>
        </mat-card>

        <mat-card
          *ngIf="
            facade.dettes().length === 0
            && facade.rechercheEffectuee()
            && !facade.messageErreur()
          "
          appearance="outlined"
          class="resultat"
        >
          <h3>Aucune dette ouverte</h3>
          <p>
            Ce joueur ne présente actuellement aucune dette ouverte.
          </p>
        </mat-card>

        <div *ngIf="facade.dettes().length > 0" class="dettes-grid">
          <mat-card
            *ngFor="let dette of facade.dettes()"
            appearance="outlined"
            class="dette-card"
          >
            <mat-card-header>
              <mat-card-title>
                Dette à régler
              </mat-card-title>
            </mat-card-header>

            <mat-card-content>
              <div class="resume-grid">
                <p>
                  <strong>Créée le</strong><br>
                  {{ dette.dateCreation | date:'dd/MM/yyyy, HH:mm' }}
                </p>

                <p>
                  <strong>Montant initial</strong><br>
                  {{ dette.montantInitial | number:'1.2-2' }} €
                </p>

                <p>
                  <strong>Montant restant</strong><br>
                  {{ dette.montantRestant | number:'1.2-2' }} €
                </p>

                <p>
                  <strong>Statut</strong><br>
                  {{ enumLabel(dette.statutDette) }}
                </p>
              </div>

              <div class="paiement-zone">
                <mat-form-field appearance="outline">
                  <mat-label>
                    Montant à payer
                  </mat-label>
                  <input
                    matInput
                    [id]="'montantDette' + dette.detteId"
                    type="number"
                    min="0"
                    step="0.01"
                    [ngModel]="
                      facade.montantPaiement(
                        dette.detteId
                      )
                    "
                    (ngModelChange)="
                      facade.modifierMontantPaiement(
                        dette.detteId,
                        $event
                      )
                    "
                    [name]="'montantDette' + dette.detteId"
                  >
                </mat-form-field>

                <button
                  mat-flat-button
                  type="button"
                  (click)="facade.payerDette(dette)"
                  [disabled]="
                    facade.paiementEnCoursDetteId()
                      !== null
                  "
                >
                  {{
                    facade.paiementEnCoursDetteId()
                    === dette.detteId
                      ? 'Paiement...'
                      : 'Payer cette dette'
                  }}
                </button>
              </div>
            </mat-card-content>
          </mat-card>
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
      height: 100%;
    }

    .dette-card mat-card-title {
      color: #003b95;
      font-size: 1.1rem;
    }

    .paiement-zone {
      display: grid;
      gap: 10px;
      margin-top: 16px;
    }

    .paiement-zone mat-form-field {
      width: 100%;
    }
  `]
})
export class MesDettesComponent
  implements OnInit {
  readonly enumLabel = enumLabel;

  constructor(
    readonly facade:
    MesDettesFacadeService
  ) {
  }

  ngOnInit(): void {
    this.facade.initialiser();
  }
}
