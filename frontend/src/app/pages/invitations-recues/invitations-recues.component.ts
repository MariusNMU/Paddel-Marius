import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize, timeout } from 'rxjs';
import { AuthJoueurResponse } from '../../models/auth.model';
import { InvitationPriveeResponse } from '../../models/invitation.model';
import { AuthContextService } from '../../services/auth-context.service';
import { InvitationApiService } from '../../services/invitation-api.service';
import { PaiementApiService } from '../../services/paiement-api.service';
import { extraireMessageErreur } from '../../shared/api-error.util';

@Component({
  selector: 'app-invitations-recues',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <section class="page">
      <h2>Invitations reçues</h2>

      <p>
        Cette page affiche les invitations privées reçues par le joueur connecté.
      </p>

      <ng-container *ngIf="joueur; else aucunJoueurConnecte">
        <div class="bloc-info">
          <h3>Joueur connecté</h3>

          <p>
            <strong>{{ joueur.prenom }} {{ joueur.nom }}</strong>
            - matricule {{ joueur.matricule }}
          </p>

          <button type="button" (click)="chargerInvitations()" [disabled]="chargement">
            {{ chargement ? 'Chargement...' : 'Actualiser mes invitations' }}
          </button>
        </div>

        <p *ngIf="messageErreur" class="erreur">
          {{ messageErreur }}
        </p>

        <p *ngIf="messageSucces" class="succes">
          {{ messageSucces }}
        </p>

        <p *ngIf="invitations.length === 0 && rechercheEffectuee && !chargement && !messageErreur">
          Aucune invitation reçue pour ce joueur.
        </p>

        <div *ngIf="invitations.length > 0" class="invitations-grid">
          <article *ngFor="let invitation of invitations" class="invitation-card">
            <h3>Match #{{ invitation.matchId }}</h3>

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
                {{ invitation.dateHeureDebut }}
              </p>

              <p>
                <strong>Fin</strong><br>
                {{ invitation.dateHeureFin }}
              </p>

              <p>
                <strong>Organisateur</strong><br>
                {{ invitation.prenomOrganisateur }} {{ invitation.nomOrganisateur }}
              </p>

              <p>
                <strong>Statut</strong><br>
                {{ invitation.statutParticipation }}
              </p>
            </div>

            <div class="actions">
              <button
                type="button"
                (click)="confirmerEtPayer(invitation)"
                [disabled]="actionEnCoursParticipationId === invitation.participationId"
              >
                {{
                  actionEnCoursParticipationId === invitation.participationId
                    ? 'Paiement...'
                    : 'Confirmer et payer la participation'
                }}
              </button>

              <button
                type="button"
                class="bouton-secondaire"
                (click)="decliner(invitation)"
                [disabled]="actionEnCoursParticipationId === invitation.participationId"
              >
                {{
                  actionEnCoursParticipationId === invitation.participationId
                    ? 'Traitement...'
                    : 'Décliner'
                }}
              </button>
            </div>
          </article>
        </div>
      </ng-container>

      <ng-template #aucunJoueurConnecte>
        <div class="bloc-info">
          <h3>Aucun joueur connecté</h3>

          <p>
            Connecte-toi avec ton matricule pour consulter tes invitations.
          </p>

          <a routerLink="/joueur" class="lien-action">
            Aller à la connexion joueur
          </a>
        </div>
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
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 16px;
      margin-top: 20px;
    }

    .invitation-card {
      border: 1px solid #bfdbfe;
      border-radius: 12px;
      background: #f8fbff;
      padding: 16px;
      box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
    }

    .invitation-card h3 {
      margin: 0 0 12px;
      color: #003b95;
    }

    .resume-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
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
export class InvitationsRecuesComponent implements OnInit {
  joueur: AuthJoueurResponse | null = null;
  invitations: InvitationPriveeResponse[] = [];
  chargement = false;
  rechercheEffectuee = false;
  actionEnCoursParticipationId: number | null = null;
  messageErreur = '';
  messageSucces = '';

  constructor(
    private readonly authContextService: AuthContextService,
    private readonly invitationApiService: InvitationApiService,
    private readonly paiementApiService: PaiementApiService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {
  }

  ngOnInit(): void {
    this.chargerJoueurConnecte();

    if (this.joueur) {
      this.chargerInvitations();
    }
  }

  chargerJoueurConnecte(): void {
    this.joueur = this.authContextService.joueur();
  }

  chargerInvitations(conserverMessageSucces = false): void {
    this.chargerJoueurConnecte();
    this.messageErreur = '';

    if (!conserverMessageSucces) {
      this.messageSucces = '';
    }

    this.invitations = [];
    this.rechercheEffectuee = true;

    if (!this.joueur) {
      this.messageErreur = 'Aucun joueur connecté.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    this.chargement = true;
    this.changeDetectorRef.detectChanges();

    this.invitationApiService.listerInvitationsRecues(this.joueur.matricule)
      .pipe(
        timeout(10000),
        finalize(() => {
          this.chargement = false;
          this.changeDetectorRef.detectChanges();
        })
      )
      .subscribe({
        next: (invitations) => {
          this.invitations = invitations;
          this.changeDetectorRef.detectChanges();
        },
        error: (error) => {
          this.messageErreur = extraireMessageErreur(error);
          this.changeDetectorRef.detectChanges();
        }
      });
  }

  confirmerEtPayer(invitation: InvitationPriveeResponse): void {
    this.messageErreur = '';
    this.messageSucces = '';
    this.actionEnCoursParticipationId = invitation.participationId;

    this.paiementApiService.payerParticipationStandard(invitation.participationId)
      .pipe(
        timeout(10000),
        finalize(() => {
          this.actionEnCoursParticipationId = null;
          this.changeDetectorRef.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.messageSucces = 'Invitation confirmée et participation payée.';
          this.chargerInvitations(true);
        },
        error: (error) => {
          this.messageErreur = extraireMessageErreur(error);
          this.changeDetectorRef.detectChanges();
        }
      });
  }

  decliner(invitation: InvitationPriveeResponse): void {
    this.chargerJoueurConnecte();
    this.messageErreur = '';
    this.messageSucces = '';

    if (!this.joueur) {
      this.messageErreur = 'Aucun joueur connecté.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    this.actionEnCoursParticipationId = invitation.participationId;

    this.invitationApiService.declinerInvitation(invitation.participationId, {
      matriculeJoueur: this.joueur.matricule
    })
      .pipe(
        timeout(10000),
        finalize(() => {
          this.actionEnCoursParticipationId = null;
          this.changeDetectorRef.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.messageSucces = 'Invitation déclinée.';
          this.chargerInvitations(true);
        },
        error: (error) => {
          this.messageErreur = extraireMessageErreur(error);
          this.changeDetectorRef.detectChanges();
        }
      });
  }
}
