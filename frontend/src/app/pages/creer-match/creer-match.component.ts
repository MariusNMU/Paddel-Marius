import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { InvitationPriveeResponse } from '../../models/invitation.model';
import { CreerMatchRequest, MatchResponse, ModeCreation } from '../../models/match.model';
import { AuthContextService } from '../../services/auth-context.service';
import { InvitationApiService } from '../../services/invitation-api.service';
import { MatchApiService } from '../../services/match-api.service';
import { extraireMessageErreur } from '../../shared/api-error.util';
import { dateHeureDuJourPourInput } from '../../shared/date-ui.util';

interface TerrainDemo {
  id: number;
  numero: string;
  siteId: number;
  nomSite: string;
}

@Component({
  selector: 'app-creer-match',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page">
      <h2>Créer un match</h2>

      <p>
        Crée une réservation de terrain sous forme de match public ou privé.
        Le backend vérifie les règles métier : disponibilité, dette active,
        pénalité active, catégorie membre et conflit horaire.
      </p>

      <div class="bloc-info">
        <h3>Rappel métier</h3>

        <ul>
          <li>Un match dure <strong>1h30</strong>.</li>
          <li>Un match coûte <strong>60 €</strong>.</li>
          <li>La part théorique est de <strong>15 € par joueur</strong>.</li>
          <li>Un match contient maximum <strong>4 joueurs</strong>.</li>
          <li>Un organisateur avec dette active ne peut pas créer de nouveau match.</li>
        </ul>
      </div>

      <div class="bloc-info">
        <h3>Public ou privé ?</h3>

        <p>
          <strong>PUBLIC :</strong> les autres joueurs pourront rejoindre le match via les inscriptions publiques.
        </p>

        <p>
          <strong>PRIVE :</strong> l'organisateur invite les autres joueurs.
        </p>
      </div>

      <form (ngSubmit)="creerMatch()">
        <label for="terrainId">Terrain</label>
        <select
          id="terrainId"
          name="terrainId"
          [(ngModel)]="terrainId"
          required
        >
          <option *ngFor="let terrain of terrains" [ngValue]="terrain.id">
            {{ terrain.nomSite }} ({{ terrain.siteId }}) — Terrain {{ terrain.numero }} ({{ terrain.id }})
          </option>
        </select>

        <div class="bloc-info" *ngIf="terrainSelectionne() as terrain">
          <h3>Terrain sélectionné</h3>
          <p>
            <strong>{{ terrain.nomSite }} ({{ terrain.siteId }})</strong>
            — Terrain {{ terrain.numero }} ({{ terrain.id }})
          </p>
        </div>

        <label for="matriculeOrganisateur">Matricule organisateur</label>
        <input
          id="matriculeOrganisateur"
          name="matriculeOrganisateur"
          type="text"
          [(ngModel)]="matriculeOrganisateur"
          required
        />

        <p class="aide">
          Le matricule est prérempli avec le joueur connecté si disponible.
        </p>

        <label for="dateHeureDebut">Date et heure de début</label>
        <input
          id="dateHeureDebut"
          name="dateHeureDebut"
          type="datetime-local"
          [(ngModel)]="dateHeureDebut"
          required
        />

        <label for="modeCreation">Type de match</label>
        <select
          id="modeCreation"
          name="modeCreation"
          [(ngModel)]="modeCreation"
        >
          <option value="PUBLIC">Public</option>
          <option value="PRIVE">Privé</option>
        </select>

        <div class="bloc-info">
          <h3>Résumé avant création</h3>

          <p><strong>Prix total :</strong> 60 €</p>
          <p><strong>Part par joueur :</strong> 15 €</p>
          <p><strong>Mode sélectionné :</strong> {{ modeCreation }}</p>
          <p><strong>Début demandé :</strong> {{ dateHeureDebut }}</p>
        </div>

        <button type="submit" [disabled]="chargement">
          {{ chargement ? 'Création...' : 'Créer le match' }}
        </button>
      </form>

      <p *ngIf="messageErreur" class="erreur">
        {{ messageErreur }}
      </p>

      <div *ngIf="matchCree" class="resultat match-card">
        <h3>Match créé avec succès</h3>

        <div class="resume-grid">
          <p><strong>ID match</strong><br>{{ matchCree.matchId }}</p>
          <p><strong>Site</strong><br>{{ matchCree.nomSite }} ({{ matchCree.siteId }})</p>
          <p><strong>Terrain</strong><br>{{ matchCree.numeroTerrain }} ({{ matchCree.terrainId }})</p>
          <p><strong>Début</strong><br>{{ matchCree.dateHeureDebut }}</p>
          <p><strong>Fin</strong><br>{{ matchCree.dateHeureFin }}</p>
          <p><strong>Mode</strong><br>{{ matchCree.modeCreation }}</p>
          <p><strong>Visibilité</strong><br>{{ matchCree.visibiliteCourante }}</p>
          <p><strong>Prix total</strong><br>{{ matchCree.prixTotal }} €</p>
          <p><strong>État</strong><br>{{ matchCree.etatCycle }}</p>
        </div>
      </div>

      <div *ngIf="matchCree && matchCree.modeCreation === 'PRIVE'" class="bloc-info">
        <h3>Inviter les 3 autres joueurs</h3>

        <p>
          Le match privé doit atteindre 4 joueurs. Ajoute les matricules un par un.
        </p>

        <label for="matriculeInvite">Matricule du joueur à inviter</label>
        <input
          id="matriculeInvite"
          name="matriculeInvite"
          type="text"
          [(ngModel)]="matriculeInvite"
        />

        <button type="button" (click)="inviterJoueur()" [disabled]="chargement">
          Inviter
        </button>

        <p *ngIf="messageInvitation">
          {{ messageInvitation }}
        </p>

        <div *ngIf="invites.length > 0">
          <h4>Joueurs invités</h4>

          <ul>
            <li *ngFor="let invite of invites">
              {{ invite.prenomInvite }} {{ invite.nomInvite }} ({{ invite.matriculeInvite }})
            </li>
          </ul>
        </div>
      </div>
    </section>
  `,
  styles: [`
    .aide {
      margin-top: -4px;
      color: #64748b;
      font-size: 14px;
    }

    .match-card {
      border-color: #93c5fd;
      background: #f8fbff;
    }

    .resume-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
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
  `]
})
export class CreerMatchComponent {
  terrains: TerrainDemo[] = [
    {
      id: 1101,
      numero: 'T1',
      siteId: 1001,
      nomSite: 'Padel Bruxelles'
    },
    {
      id: 1102,
      numero: 'T2',
      siteId: 1001,
      nomSite: 'Padel Bruxelles'
    },
    {
      id: 1201,
      numero: 'T1',
      siteId: 1002,
      nomSite: 'Padel Namur'
    },
    {
      id: 1202,
      numero: 'T2',
      siteId: 1002,
      nomSite: 'Padel Namur'
    }
  ];

  terrainId = 1101;
  matriculeOrganisateur = 'G1001';
  dateHeureDebut = dateHeureDuJourPourInput('13:00');
  modeCreation: ModeCreation = 'PUBLIC';

  chargement = false;
  messageErreur = '';
  matchCree: MatchResponse | null = null;
  matriculeInvite = '';
  invites: InvitationPriveeResponse[] = [];
  messageInvitation = '';

  constructor(
    private readonly matchApiService: MatchApiService,
    private readonly invitationApiService: InvitationApiService,
    private readonly authContext: AuthContextService,
    private readonly changeDetectorRef: ChangeDetectorRef,
    private readonly route: ActivatedRoute
  ) {
    this.matriculeOrganisateur = this.authContext.joueur()?.matricule ?? 'G1001';

    const terrainIdParam = this.route.snapshot.queryParamMap.get('terrainId');
    const dateHeureDebutParam = this.route.snapshot.queryParamMap.get('dateHeureDebut');

    if (terrainIdParam) {
      this.terrainId = Number(terrainIdParam);
    }

    if (dateHeureDebutParam) {
      this.dateHeureDebut = dateHeureDebutParam.substring(0, 16);
    }
  }

  terrainSelectionne(): TerrainDemo | undefined {
    return this.terrains.find(terrain => terrain.id === Number(this.terrainId));
  }

  creerMatch(): void {
    this.messageErreur = '';
    this.matchCree = null;

    if (!this.terrainId || !this.matriculeOrganisateur.trim() || !this.dateHeureDebut) {
      this.messageErreur = 'Tous les champs sont obligatoires.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    const request: CreerMatchRequest = {
      terrainId: this.terrainId,
      matriculeOrganisateur: this.matriculeOrganisateur.trim(),
      dateHeureDebut: this.dateHeureDebut,
      modeCreation: this.modeCreation
    };

    this.chargement = true;
    this.changeDetectorRef.detectChanges();

    this.matchApiService.creerMatch(request).subscribe({
      next: (response) => {
        this.matchCree = response;
        this.invites = [];
        this.messageInvitation = '';
        this.matriculeInvite = '';
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

  inviterJoueur(): void {
    this.messageInvitation = '';

    if (!this.matchCree) {
      this.messageInvitation = 'Crée d’abord le match privé.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    if (!this.matriculeInvite.trim()) {
      this.messageInvitation = 'Le matricule invité est obligatoire.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    this.invitationApiService.inviterJoueur(this.matchCree.matchId, {
      matriculeOrganisateur: this.matriculeOrganisateur.trim(),
      matriculeInvite: this.matriculeInvite.trim()
    }).subscribe({
      next: (response) => {
        this.invites.push(response);
        this.messageInvitation =
          `${response.prenomInvite} ${response.nomInvite} (${response.matriculeInvite}) invité.`;
        this.matriculeInvite = '';
        this.changeDetectorRef.detectChanges();
      },
      error: (error) => {
        this.messageInvitation = extraireMessageErreur(error);
        this.changeDetectorRef.detectChanges();
      }
    });
  }
}
