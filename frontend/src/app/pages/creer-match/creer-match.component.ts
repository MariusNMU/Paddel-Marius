import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CreerMatchRequest, MatchResponse, ModeCreation } from '../../models/match.model';
import { AuthContextService } from '../../services/auth-context.service';
import { MatchApiService } from '../../services/match-api.service';
import { extraireMessageErreur } from '../../shared/api-error.util';

@Component({
  selector: 'app-creer-match',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page">
      <h2>Créer un match</h2>

      <p>
        Création d'un match privé ou public. Le backend vérifie les règles métier :
        disponibilité, dette active, pénalité active, catégorie membre et conflit horaire.
      </p>

      <form (ngSubmit)="creerMatch()">
        <label for="terrainId">ID terrain</label>
        <input
          id="terrainId"
          name="terrainId"
          type="number"
          [(ngModel)]="terrainId"
          required
        />

        <label for="matriculeOrganisateur">Matricule organisateur</label>
        <input
          id="matriculeOrganisateur"
          name="matriculeOrganisateur"
          type="text"
          [(ngModel)]="matriculeOrganisateur"
          required
        />

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

        <button type="submit" [disabled]="chargement">
          {{ chargement ? 'Création...' : 'Créer le match' }}
        </button>
      </form>

      <p *ngIf="messageErreur" class="erreur">
        {{ messageErreur }}
      </p>

      <div *ngIf="matchCree" class="resultat">
        <h3>Match créé</h3>

        <p><strong>ID match :</strong> {{ matchCree.matchId }}</p>
        <p><strong>Site :</strong> {{ matchCree.nomSite }} — ID {{ matchCree.siteId }}</p>
        <p><strong>Terrain :</strong> {{ matchCree.numeroTerrain }} — ID {{ matchCree.terrainId }}</p>
        <p><strong>Début :</strong> {{ matchCree.dateHeureDebut }}</p>
        <p><strong>Fin :</strong> {{ matchCree.dateHeureFin }}</p>
        <p><strong>Mode :</strong> {{ matchCree.modeCreation }}</p>
        <p><strong>Visibilité :</strong> {{ matchCree.visibiliteCourante }}</p>
        <p><strong>Prix total :</strong> {{ matchCree.prixTotal }} €</p>
        <p><strong>État :</strong> {{ matchCree.etatCycle }}</p>
      </div>
    </section>
  `
})
export class CreerMatchComponent {
  terrainId = 1101;
  matriculeOrganisateur = 'G1001';
  dateHeureDebut = '2026-06-20T13:00';
  modeCreation: ModeCreation = 'PUBLIC';

  chargement = false;
  messageErreur = '';
  matchCree: MatchResponse | null = null;

  constructor(
    private readonly matchApiService: MatchApiService,
    private readonly authContext: AuthContextService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {
    this.matriculeOrganisateur = this.authContext.joueur()?.matricule ?? 'G1001';
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
