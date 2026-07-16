import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize, timeout } from 'rxjs';
import { PaiementResponse } from '../../models/paiement.model';
import { ReservationJoueurResponse } from '../../models/reservation.model';
import { AuthContextService } from '../../services/auth-context.service';
import { PaiementApiService } from '../../services/paiement-api.service';
import { ReservationApiService } from '../../services/reservation-api.service';
import { extraireMessageErreur } from '../../shared/api-error.util';
import { enumLabel } from '../../shared/enum-label.util';

@Component({
  selector: 'app-mes-reservations',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <section class="page">
      <h2>Mes réservations</h2>

      <p>
        Cette page affiche les matches du joueur connecté : matches organisés,
        matches rejoints, statuts de participation et états du match.
      </p>

      @if (!joueurConnecte()) {
        <div class="bloc-info">
          <h3>Aucun joueur connecté</h3>

          <p>
            Connecte-toi avec ton matricule pour consulter tes réservations.
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
          <p><strong>Catégorie :</strong> {{ enumLabel(joueurConnecte()?.categorieMembre) }}</p>
        </div>

        <button type="button" (click)="chargerReservations()" [disabled]="chargement()">
          {{ chargement() ? 'Chargement...' : 'Actualiser mes réservations' }}
        </button>
      }

      @if (messageErreur()) {
        <p class="erreur">
          {{ messageErreur() }}
        </p>
      }

      @if (messageSucces()) {
        <p class="succes">
          {{ messageSucces() }}
        </p>
      }

      @if (dernierPaiement(); as paiement) {
        <div class="resultat">
          <h3>Paiement enregistré</h3>

          <div class="resume-grid">
            <p>
              <strong>Participation</strong><br>
              {{ paiement.montant | number:'1.2-2' }} €
            </p>
            <p>
              <strong>Dettes réglées</strong><br>
              {{ (paiement.montantDettesReglees ?? 0) | number:'1.2-2' }} €
            </p>
            <p>
              <strong>Total débité</strong><br>
              {{ (paiement.montantTotalDebite ?? paiement.montant) | number:'1.2-2' }} €
            </p>
          </div>
        </div>
      }

      @if (reservations().length === 0 && rechercheEffectuee() && !chargement() && !messageErreur()) {
        <p>
          Aucune réservation trouvée pour ce joueur.
        </p>
      }

      @if (reservations().length > 0) {
        <div class="reservations-grid">
          <article
            *ngFor="let reservation of reservations()"
            class="reservation-card"
            [class.annulee]="reservation.etatCycle === 'ANNULE'"
          >
            <h3>
              Match #{{ reservation.matchId }}
            </h3>

            <p>
              <strong>Site :</strong>
              {{ reservation.nomSite }} ({{ reservation.siteId }})
            </p>

            <p>
              <strong>Terrain :</strong>
              {{ reservation.numeroTerrain }} ({{ reservation.terrainId }})
            </p>

            <p>
              <strong>Début :</strong>
              {{ reservation.dateHeureDebut }}
            </p>

            <p>
              <strong>Fin :</strong>
              {{ reservation.dateHeureFin }}
            </p>

            <div class="resume-grid">
              <p><strong>Rôle</strong><br>{{ enumLabel(reservation.roleParticipation) }}</p>
              <p><strong>Entrée</strong><br>{{ enumLabel(reservation.modeEntree) }}</p>
              <p><strong>Participation</strong><br>{{ enumLabel(reservation.statutParticipation) }}</p>
              <p><strong>Match</strong><br>{{ enumLabel(reservation.etatCycle) }}</p>
              <p><strong>Mode</strong><br>{{ enumLabel(reservation.modeCreation) }}</p>
              <p><strong>Visibilité</strong><br>{{ enumLabel(reservation.visibiliteCourante) }}</p>
              <p><strong>Prix total</strong><br>{{ reservation.prixTotal | number:'1.2-2' }} €</p>
            </div>

            @if (reservation.etatCycle === 'ANNULE') {
              <p class="badge-attention">
                Match annulé.
              </p>
            }

            @if (reservation.statutParticipation === 'EN_ATTENTE_PAIEMENT') {
              <p class="badge-attention">
                Participation en attente de paiement.
              </p>

              @if (reservation.etatCycle !== 'ANNULE') {
                <button
                  type="button"
                  (click)="payerParticipation(reservation)"
                  [disabled]="paiementEnCoursParticipationId() !== null"
                >
                  {{
                    paiementEnCoursParticipationId() === reservation.participationId
                      ? 'Paiement...'
                      : 'Payer ma participation'
                  }}
                </button>
              }
            }

            @if (reservation.statutParticipation === 'CONFIRMEE') {
              <p class="badge-ok">
                Participation confirmée.
              </p>
            }
          </article>
        </div>
      }
    </section>
  `,
  styles: [`
    .reservations-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 16px;
      margin-top: 18px;
    }

    .reservation-card {
      border: 1px solid #bfdbfe;
      border-radius: 12px;
      background: #ffffff;
      padding: 16px;
      box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
    }

    .reservation-card.annulee {
      border-color: #fecaca;
      background: #fff7f7;
    }

    .reservation-card h3 {
      margin: 0 0 12px;
      color: #003b95;
    }

    .reservation-card p {
      margin: 8px 0;
    }

    .resume-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
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
  `]
})
export class MesReservationsComponent implements OnInit {
  readonly reservations = signal<ReservationJoueurResponse[]>([]);
  readonly messageErreur = signal('');
  readonly messageSucces = signal('');
  readonly chargement = signal(false);
  readonly rechercheEffectuee = signal(false);
  readonly paiementEnCoursParticipationId = signal<number | null>(null);
  readonly dernierPaiement = signal<PaiementResponse | null>(null);
  readonly enumLabel = enumLabel;

  constructor(
    private readonly authContextService: AuthContextService,
    private readonly reservationApiService: ReservationApiService,
    private readonly paiementApiService: PaiementApiService
  ) {
  }

  ngOnInit(): void {
    if (this.joueurConnecte()) {
      this.chargerReservations();
    }
  }

  joueurConnecte() {
    return this.authContextService.joueur();
  }

  chargerReservations(): void {
    this.messageErreur.set('');
    this.messageSucces.set('');
    this.dernierPaiement.set(null);
    this.reservations.set([]);
    this.rechercheEffectuee.set(true);

    const joueur = this.joueurConnecte();

    if (!joueur) {
      this.messageErreur.set('Aucun joueur connecté.');
      return;
    }

    this.chargement.set(true);

    this.reservationApiService.consulterMesReservations(joueur.matricule)
      .pipe(
        timeout(10000),
        finalize(() => this.chargement.set(false))
      )
      .subscribe({
        next: (response) => {
          this.reservations.set(response);
        },
        error: (error) => {
          this.messageErreur.set(extraireMessageErreur(error));
        }
      });
  }

  payerParticipation(reservation: ReservationJoueurResponse): void {
    this.messageErreur.set('');
    this.messageSucces.set('');
    this.dernierPaiement.set(null);
    this.paiementEnCoursParticipationId.set(
      reservation.participationId
    );

    this.paiementApiService
      .payerParticipationStandard(reservation.participationId)
      .pipe(
        timeout(10000),
        finalize(() => {
          this.paiementEnCoursParticipationId.set(null);
        })
      )
      .subscribe({
        next: (response) => {
          this.dernierPaiement.set(response);
          this.messageSucces.set(
            'Participation payée avec succès.'
          );

          this.reservations.update(reservations =>
            reservations.map(reservationActuelle =>
              reservationActuelle.participationId
                === reservation.participationId
                ? {
                  ...reservationActuelle,
                  statutParticipation: 'CONFIRMEE'
                }
                : reservationActuelle
            )
          );
        },
        error: (error) => {
          this.messageErreur.set(
            extraireMessageErreur(error)
          );
        }
      });
  }
}
