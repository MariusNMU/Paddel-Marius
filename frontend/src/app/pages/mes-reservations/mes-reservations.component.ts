import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ReservationJoueurResponse } from '../../models/reservation.model';
import { AuthContextService } from '../../services/auth-context.service';
import { ReservationApiService } from '../../services/reservation-api.service';
import { extraireMessageErreur } from '../../shared/api-error.util';

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
          <p><strong>Catégorie :</strong> {{ joueurConnecte()?.categorieMembre }}</p>
        </div>

        <button type="button" (click)="chargerReservations()" [disabled]="chargement">
          {{ chargement ? 'Chargement...' : 'Actualiser mes réservations' }}
        </button>
      }

      @if (messageErreur) {
        <p class="erreur">
          {{ messageErreur }}
        </p>
      }

      @if (reservations.length === 0 && rechercheEffectuee && !chargement) {
        <p>
          Aucune réservation trouvée pour ce joueur.
        </p>
      }

      @if (reservations.length > 0) {
        <div class="reservations-grid">
          <article
            *ngFor="let reservation of reservations"
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
              <p><strong>Rôle</strong><br>{{ reservation.roleParticipation }}</p>
              <p><strong>Entrée</strong><br>{{ reservation.modeEntree }}</p>
              <p><strong>Participation</strong><br>{{ reservation.statutParticipation }}</p>
              <p><strong>Match</strong><br>{{ reservation.etatCycle }}</p>
              <p><strong>Mode</strong><br>{{ reservation.modeCreation }}</p>
              <p><strong>Visibilité</strong><br>{{ reservation.visibiliteCourante }}</p>
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
export class MesReservationsComponent {
  reservations: ReservationJoueurResponse[] = [];
  messageErreur = '';
  chargement = false;
  rechercheEffectuee = false;

  constructor(
    private readonly authContextService: AuthContextService,
    private readonly reservationApiService: ReservationApiService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {
    if (this.joueurConnecte()) {
      this.chargerReservations();
    }
  }

  joueurConnecte() {
    return this.authContextService.joueur();
  }

  chargerReservations(): void {
    this.messageErreur = '';
    this.reservations = [];
    this.rechercheEffectuee = true;

    const joueur = this.joueurConnecte();

    if (!joueur) {
      this.messageErreur = 'Aucun joueur connecté.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    this.chargement = true;
    this.changeDetectorRef.detectChanges();

    this.reservationApiService.consulterMesReservations(joueur.matricule).subscribe({
      next: (response) => {
        this.reservations = response;
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
