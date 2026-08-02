import { CommonModule } from '@angular/common';
import {
  Component,
  OnInit
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { RouterLink } from '@angular/router';
import { MesReservationsFacadeService } from '../../services/mes-reservations-facade.service';
import { enumLabel } from '../../shared/enum-label.util';

@Component({
  selector: 'app-mes-reservations',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    RouterLink
  ],
  providers: [
    MesReservationsFacadeService
  ],
  template: `
    <section class="page">
      <h2>Mes réservations</h2>

      <p>
        Cette page affiche les matches du
        joueur connecté : matches organisés,
        matches rejoints, statuts de
        participation et états du match.
      </p>

      @if (!facade.joueur()) {
        <div class="bloc-info">
          <h3>Aucun joueur connecté</h3>

          <p>
            Connecte-toi avec ton matricule
            pour consulter tes réservations.
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
          mat-flat-button
          type="button"
          (click)="
            facade.chargerReservations()
          "
          [disabled]="facade.chargement()"
        >
          {{
            facade.chargement()
              ? 'Chargement...'
              : 'Actualiser mes réservations'
          }}
        </button>
      }

      @if (facade.messageErreur()) {
        <p class="erreur">
          {{ facade.messageErreur() }}
        </p>
      }

      @if (facade.messageSucces()) {
        <p class="succes">
          {{ facade.messageSucces() }}
        </p>
      }

      @if (
        facade.dernierPaiement();
        as paiement
      ) {
        <div class="resultat">
          <h3>Paiement enregistré</h3>

          <div class="resume-grid">
            <p>
              <strong>Participation</strong>
              <br>
              {{
                paiement.montant
                  | number:'1.2-2'
              }}
              €
            </p>

            <p>
              <strong>Dettes réglées</strong>
              <br>
              {{
                paiement.montantDettesReglees
                  | number:'1.2-2'
              }}
              €
            </p>

            <p>
              <strong>Total débité</strong>
              <br>
              {{
                paiement.montantTotalDebite
                  | number:'1.2-2'
              }}
              €
            </p>
          </div>
        </div>
      }

      @if (
        facade.reservations().length === 0
        && facade.rechercheEffectuee()
        && !facade.chargement()
        && !facade.messageErreur()
      ) {
        <p>
          Aucune réservation trouvée pour
          ce joueur.
        </p>
      }

      @if (
        facade.reservations().length > 0
      ) {
        <div class="reservations-grid">
          <mat-card
            *ngFor="
              let reservation
              of facade.reservations()
            "
            appearance="outlined"
            class="reservation-card"
            [class.annulee]="
              reservation.etatCycle
                === 'ANNULE'
            "
          >
            <mat-card-header>
              <mat-card-title>
                Réservation du
                {{
                  reservation.dateHeureDebut
                    | date:'dd/MM/yyyy, HH:mm'
                }}
              </mat-card-title>
            </mat-card-header>

            <mat-card-content>
              <p>
                <strong>Site :</strong>
                {{ reservation.nomSite }}
              </p>

              <p>
                <strong>Terrain :</strong>
                {{ reservation.numeroTerrain }}
              </p>

              <p>
                <strong>Début :</strong>
                {{
                  reservation.dateHeureDebut
                    | date:'dd/MM/yyyy, HH:mm'
                }}
              </p>

              <p>
                <strong>Fin :</strong>
                {{
                  reservation.dateHeureFin
                    | date:'dd/MM/yyyy, HH:mm'
                }}
              </p>

              <div class="resume-grid">
                <p>
                  <strong>Rôle</strong>
                  <br>
                  {{
                    enumLabel(
                      reservation
                        .roleParticipation
                    )
                  }}
                </p>

                <p>
                  <strong>Entrée</strong>
                  <br>
                  {{
                    enumLabel(
                      reservation.modeEntree
                    )
                  }}
                </p>

                <p>
                  <strong>Participation</strong>
                  <br>
                  {{
                    reservation.etatCycle
                      === 'ANNULE'
                      ? 'Annulée'
                      : enumLabel(
                          reservation
                            .statutParticipation
                        )
                  }}
                </p>

                <p>
                  <strong>Match</strong>
                  <br>
                  {{
                    enumLabel(
                      reservation.etatCycle
                    )
                  }}
                </p>

                <p>
                  <strong>Mode</strong>
                  <br>
                  {{
                    enumLabel(
                      reservation.modeCreation
                    )
                  }}
                </p>

                <p>
                  <strong>Visibilité</strong>
                  <br>
                  {{
                    enumLabel(
                      reservation
                        .visibiliteCourante
                    )
                  }}
                </p>

                <p>
                  <strong>Prix total</strong>
                  <br>
                  {{
                    reservation.prixTotal
                      | number:'1.2-2'
                  }}
                  €
                </p>
              </div>

              @if (
                reservation.etatCycle
                  === 'ANNULE'
              ) {
                <p class="badge-attention">
                  Match annulé à la suite d'une
                  fermeture administrative.
                  Aucun paiement n'est requis
                  pour cette réservation.
                </p>
              } @else if (
                reservation.statutParticipation
                  === 'EN_ATTENTE_PAIEMENT'
              ) {
                <p class="badge-attention">
                  Participation en attente de
                  paiement.
                </p>
              }

              @if (
                reservation.etatCycle
                  !== 'ANNULE'
                && reservation
                  .statutParticipation
                  === 'CONFIRMEE'
              ) {
                <p class="badge-ok">
                  Participation confirmée.
                </p>
              }
            </mat-card-content>

            @if (
              reservation.etatCycle
                !== 'ANNULE'
              && reservation
                .statutParticipation
                === 'EN_ATTENTE_PAIEMENT'
            ) {
              <mat-card-actions align="start">
                <button
                  mat-flat-button
                  type="button"
                  (click)="
                    facade
                      .payerParticipation(
                        reservation
                      )
                  "
                  [disabled]="
                    facade
                      .paiementEnCoursParticipationId()
                      !== null
                  "
                >
                  {{
                    facade
                      .paiementEnCoursParticipationId()
                      === reservation
                        .participationId
                      ? 'Paiement...'
                      : 'Payer ma participation'
                  }}
                </button>
              </mat-card-actions>
            }
          </mat-card>
        </div>
      }
    </section>
  `,
  styles: [`
    .reservations-grid {
      display: grid;
      grid-template-columns:
        repeat(
          auto-fill,
          minmax(320px, 360px)
        );
      grid-auto-rows: 1fr;
      justify-content: start;
      align-items: stretch;
      gap: 20px;
      margin-top: 18px;
    }

    .reservation-card {
      box-sizing: border-box;
      width: 100%;
      min-width: 0;
      height: 100%;
    }

    .reservation-card.annulee {
      border-color: #fecaca;
      background: #fff7f7;
    }

    .reservation-card mat-card-title {
      color: #003b95;
      font-size: 1.1rem;
      line-height: 1.35;
    }

    .reservation-card mat-card-content {
      display: flex;
      flex: 1;
      flex-direction: column;
      padding-top: 12px;
    }

    .reservation-card mat-card-content > p {
      margin: 8px 0;
    }

    .reservation-card mat-card-actions {
      padding-top: 0;
    }

    .resume-grid {
      display: grid;
      grid-template-columns:
        repeat(
          auto-fit,
          minmax(130px, 1fr)
        );
      gap: 10px;
      margin-top: 14px;
    }

    .resume-grid p {
      margin: 0;
      padding: 10px;
      border: 1px solid #bfdbfe;
      border-radius: 10px;
      background: #ffffff;
      font-size: 14px;
    }

    .badge-attention {
      margin-top: 12px;
      padding: 10px;
      border-radius: 10px;
      background: #fff1f2;
      color: #9f1239;
      font-weight: 600;
    }

    .badge-ok {
      margin-top: 12px;
      padding: 10px;
      border-radius: 10px;
      background: #ecfdf5;
      color: #047857;
      font-weight: 600;
    }

    .lien-action {
      display: inline-block;
      margin-top: 12px;
      font-weight: 600;
    }

    @media (max-width: 640px) {
      .reservations-grid {
        grid-template-columns:
          minmax(0, 1fr);
      }
    }
  `]
})
export class MesReservationsComponent
  implements OnInit {
  readonly enumLabel = enumLabel;

  constructor(
    readonly facade:
    MesReservationsFacadeService
  ) {
  }

  ngOnInit(): void {
    this.facade.initialiser();
  }
}
