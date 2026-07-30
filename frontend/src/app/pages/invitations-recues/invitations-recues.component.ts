import { CommonModule } from '@angular/common';
import {
  Component,
  OnInit
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { RouterLink } from '@angular/router';
import { InvitationsRecuesFacadeService } from '../../services/invitations-recues-facade.service';
import { enumLabel } from '../../shared/enum-label.util';

@Component({
  selector: 'app-invitations-recues',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    RouterLink
  ],
  providers: [
    InvitationsRecuesFacadeService
  ],
  template: `
    <section class="page">
      <h2>Invitations reçues</h2>

      <p>
        Cette page affiche les invitations privées reçues par le joueur connecté.
      </p>

      <ng-container
        *ngIf="
          facade.joueur() as joueur;
          else aucunJoueurConnecte
        "
      >
        <mat-card
          appearance="outlined"
          class="bloc-info"
        >
          <h3>Joueur connecté</h3>

          <p>
            <strong>
              {{ joueur.prenom }}
              {{ joueur.nom }}
            </strong>
            - matricule
            {{ joueur.matricule }}
          </p>

          <button
            mat-flat-button
            type="button"
            (click)="facade.chargerInvitations()"
            [disabled]="facade.chargement()"
          >
            {{
              facade.chargement()
                ? 'Chargement...'
                : 'Actualiser mes invitations'
            }}
          </button>
        </mat-card>

        <p
          *ngIf="facade.messageErreur()"
          class="erreur"
        >
          {{ facade.messageErreur() }}
        </p>

        <p
          *ngIf="facade.messageSucces()"
          class="succes"
        >
          {{ facade.messageSucces() }}
        </p>

        <p
          *ngIf="
            facade.invitations().length === 0
            && facade.rechercheEffectuee()
            && !facade.chargement()
            && !facade.messageErreur()
          "
        >
          Aucune invitation reçue pour ce joueur.
        </p>

        <div
          *ngIf="facade.invitations().length > 0"
          class="invitations-grid"
        >
          <mat-card
            *ngFor="
              let invitation
              of facade.invitations()
            "
            appearance="outlined"
            class="invitation-card"
          >
            <mat-card-header>
              <mat-card-title>
                Invitation au match du
                {{
                  invitation.dateHeureDebut
                    | date:'dd/MM/yyyy, HH:mm'
                }}
              </mat-card-title>
            </mat-card-header>

            <mat-card-content>
              <div class="resume-grid">
                <p>
                  <strong>Site</strong><br>
                  {{ invitation.nomSite }}
                </p>

                <p>
                  <strong>Terrain</strong><br>
                  {{ invitation.numeroTerrain }}
                </p>

                <p>
                  <strong>Début</strong><br>
                  {{
                    invitation.dateHeureDebut
                      | date:'dd/MM/yyyy, HH:mm'
                  }}
                </p>

                <p>
                  <strong>Fin</strong><br>
                  {{
                    invitation.dateHeureFin
                      | date:'dd/MM/yyyy, HH:mm'
                  }}
                </p>

                <p>
                  <strong>Organisateur</strong><br>
                  {{ invitation.prenomOrganisateur }}
                  {{ invitation.nomOrganisateur }}
                </p>

                <p>
                  <strong>Statut</strong><br>
                  {{
                    enumLabel(
                      invitation.statutParticipation
                    )
                  }}
                </p>
              </div>
            </mat-card-content>

            <mat-card-actions>
              <button
                mat-flat-button
                type="button"
                (click)="
                  facade.confirmerEtPayer(
                    invitation
                  )
                "
                [disabled]="
                  facade
                    .actionEnCoursParticipationId()
                    === invitation.participationId
                "
              >
                {{
                  facade
                    .actionEnCoursParticipationId()
                  === invitation.participationId
                    ? 'Paiement...'
                    : 'Confirmer et payer la participation'
                }}
              </button>

              <button
                mat-stroked-button
                type="button"
                class="bouton-secondaire"
                (click)="
                  facade.decliner(invitation)
                "
                [disabled]="
                  facade
                    .actionEnCoursParticipationId()
                    === invitation.participationId
                "
              >
                {{
                  facade
                    .actionEnCoursParticipationId()
                  === invitation.participationId
                    ? 'Traitement...'
                    : 'Décliner'
                }}
              </button>
            </mat-card-actions>
          </mat-card>
        </div>
      </ng-container>

      <ng-template #aucunJoueurConnecte>
        <mat-card
          appearance="outlined"
          class="bloc-info"
        >
          <h3>Aucun joueur connecté</h3>

          <p>
            Connecte-toi avec ton matricule pour consulter tes invitations.
          </p>

          <a
            mat-stroked-button
            routerLink="/joueur"
            class="lien-action"
          >
            Aller à la connexion joueur
          </a>
        </mat-card>
      </ng-template>
    </section>
  `,
  styles: [`
    .succes {
      margin-top: 16px;
      color: #047857;
      font-weight: 700;
    }

    .invitations-grid {
      display: grid;
      grid-template-columns:
        repeat(
          auto-fit,
          minmax(280px, 1fr)
        );
      gap: 16px;
      margin-top: 20px;
    }

    .invitation-card {
      height: 100%;
    }

    .invitation-card mat-card-title {
      color: #003b95;
      font-size: 1.1rem;
      line-height: 1.35;
    }

    .resume-grid {
      display: grid;
      grid-template-columns:
        repeat(
          auto-fit,
          minmax(140px, 1fr)
        );
      gap: 12px;
    }

    .resume-grid p {
      margin: 0;
      padding: 12px;
      border: 1px solid #bfdbfe;
      border-radius: 10px;
      background: #ffffff;
    }

    .actions {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      margin-top: 16px;
    }

    .bouton-secondaire {
      background: #ffffff;
      color: #003b95;
      border: 1px solid #93c5fd;
    }

    .lien-action {
      display: inline-block;
      margin-top: 12px;
      font-weight: 600;
    }
  `]
})
export class InvitationsRecuesComponent
  implements OnInit {
  readonly enumLabel = enumLabel;

  constructor(
    readonly facade:
    InvitationsRecuesFacadeService
  ) {
  }

  ngOnInit(): void {
    this.facade.initialiser();
  }
}
