import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { CreerMatchFacadeService } from '../../services/creer-match-facade.service';
import { enumLabel } from '../../shared/enum-label.util';

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
          <li>Un match dure <strong>{{ facade.dureeMatchLibelle() }}</strong>.</li>
          <li>Un match coûte <strong>{{ facade.parametresMetier()?.prixTotalMatch | number:'1.2-2' }} €</strong>.</li>
          <li>La part théorique est de <strong>{{ facade.parametresMetier()?.montantParticipationStandard | number:'1.2-2' }} € par joueur</strong>.</li>
          <li>Un match contient maximum <strong>{{ facade.parametresMetier()?.nombreJoueursMaximum }} joueurs</strong>.</li>
          <li>Un organisateur avec dette active ne peut pas créer de nouveau match.</li>
        </ul>
      </div>

      <div class="bloc-info">
        <h3>Public ou privé ?</h3>

        <p>
          <strong>Public :</strong> les autres joueurs pourront rejoindre le match via les inscriptions publiques.
        </p>

        <p>
          <strong>Privé :</strong> l'organisateur invite les autres joueurs.
        </p>
      </div>

      <form (ngSubmit)="creerMatch()">
        <label for="terrainId">Terrain</label>
        <select
          id="terrainId"
          name="terrainId"
          [ngModel]="facade.terrainId()"
          (ngModelChange)="facade.modifierTerrainId($event)"
          required
        >
          <option [ngValue]="null" disabled>Choisir un terrain</option>
          <option *ngFor="let terrain of facade.terrains()" [ngValue]="terrain.terrainId">
            {{ terrain.nomSite }} ({{ terrain.siteId }}) — Terrain {{ terrain.numeroTerrain }} ({{ terrain.terrainId }})
          </option>
        </select>

        <p *ngIf="facade.chargementTerrains()" class="aide">
          Chargement des terrains depuis le backend...
        </p>

        <p *ngIf="!facade.chargementTerrains() && facade.terrains().length === 0" class="erreur">
          Aucun terrain actif disponible. Vérifie les sites et terrains actifs en base.
        </p>

        <div class="bloc-info" *ngIf="facade.terrainSelectionne() as terrain">
          <h3>Terrain sélectionné</h3>
          <p>
            <strong>{{ terrain.nomSite }} ({{ terrain.siteId }})</strong>
            — Terrain {{ terrain.numeroTerrain }} ({{ terrain.terrainId }})
          </p>
        </div>

        <label for="matriculeOrganisateur">Matricule organisateur</label>
        <input
          id="matriculeOrganisateur"
          name="matriculeOrganisateur"
          type="text"
          [ngModel]="facade.matriculeOrganisateur()"
          (ngModelChange)="facade.modifierMatriculeOrganisateur($event)"
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
          [ngModel]="facade.dateHeureDebut()"
          (ngModelChange)="facade.modifierDateHeureDebut($event)"
          required
        />

        <label for="modeCreation">Type de match</label>
        <select
          id="modeCreation"
          name="modeCreation"
          [ngModel]="facade.modeCreation()"
          (ngModelChange)="facade.modifierModeCreation($event)"
          required
        >
          <option value="" disabled>Choisir un type de match</option>
          <option value="PUBLIC">Public</option>
          <option value="PRIVE">Privé</option>
        </select>

        <div class="bloc-info">
          <h3>Résumé avant création</h3>

          <p><strong>Prix total :</strong> {{ facade.parametresMetier()?.prixTotalMatch | number:'1.2-2' }} €</p>
          <p><strong>Part par joueur :</strong> {{ facade.parametresMetier()?.montantParticipationStandard | number:'1.2-2' }} €</p>
          <p><strong>Type :</strong> {{ enumLabel(facade.modeCreation()) }}</p>
          <p><strong>Début demandé :</strong> {{ facade.dateHeureDebut() }}</p>
        </div>

        <button type="submit" [disabled]="
          facade.chargementCreation()
          || facade.chargementTerrains()
          || facade.terrains().length === 0
        ">
          {{ facade.chargementCreation() ? 'Création...' : 'Créer le match' }}
        </button>
      </form>

      <p *ngIf="facade.messageErreur()" class="erreur">
        {{ facade.messageErreur() }}
      </p>

      <div *ngIf="facade.matchCree() as matchCree" class="resultat match-card">
        <h3>Match créé avec succès</h3>

        <div class="resume-grid">
          <p><strong>ID match</strong><br>{{ matchCree.matchId }}</p>
          <p><strong>Site</strong><br>{{ matchCree.nomSite }} ({{ matchCree.siteId }})</p>
          <p><strong>Terrain</strong><br>{{ matchCree.numeroTerrain }} ({{ matchCree.terrainId }})</p>
          <p><strong>Début</strong><br>{{ matchCree.dateHeureDebut }}</p>
          <p><strong>Fin</strong><br>{{ matchCree.dateHeureFin }}</p>
          <p><strong>Mode</strong><br>{{ enumLabel(matchCree.modeCreation) }}</p>
          <p><strong>Visibilité</strong><br>{{ enumLabel(matchCree.visibiliteCourante) }}</p>
          <p><strong>Prix total</strong><br>{{ matchCree.prixTotal | number:'1.2-2' }} €</p>
          <p><strong>État</strong><br>{{ enumLabel(matchCree.etatCycle) }}</p>
        </div>
      </div>

      <div *ngIf="facade.matchCree()?.modeCreation === 'PRIVE'" class="bloc-info">
        <h3>Inviter les autres joueurs</h3>

        <p>
          Le match privé doit atteindre
          {{ facade.parametresMetier()?.nombreJoueursMaximum ?? 'le nombre maximum de' }}
          joueurs. Ajoute les matricules un par un.
        </p>

        <label for="matriculeInvite">Matricule du joueur à inviter</label>
        <input
          id="matriculeInvite"
          name="matriculeInvite"
          type="text"
          [ngModel]="facade.matriculeInvite()"
          (ngModelChange)="facade.modifierMatriculeInvite($event)"
        />

        <button type="button" (click)="inviterJoueur()" [disabled]="facade.chargementCreation() || facade.chargementInvitation()">
          Inviter
        </button>

        <p *ngIf="facade.messageInvitation()">
          {{ facade.messageInvitation() }}
        </p>

        <div *ngIf="facade.invites().length > 0">
          <h4>Joueurs invités</h4>

          <ul>
            <li *ngFor="let invite of facade.invites()">
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
export class CreerMatchComponent implements OnInit {
  readonly enumLabel = enumLabel;

  constructor(
    readonly facade: CreerMatchFacadeService,
    private readonly route: ActivatedRoute
  ) {
  }

  ngOnInit(): void {
    this.facade.initialiser(
      this.route.snapshot.queryParamMap.get('terrainId'),
      this.route.snapshot.queryParamMap.get('dateHeureDebut')
    );
  }

  creerMatch(): void {
    this.facade.creerMatch();
  }

  inviterJoueur(): void {
    this.facade.inviterJoueur();
  }
}
