import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { CreerMatchRequest, MatchResponse, ModeCreation } from '../../models/match.model';
import { SiteReservationInfoResponse, TerrainReservationInfoResponse } from '../../models/site-reservation-info.model';
import { AuthContextService } from '../../services/auth-context.service';
import { MatchApiService } from '../../services/match-api.service';
import { SiteReservationInfoApiService } from '../../services/site-reservation-info-api.service';
import { extraireMessageErreur } from '../../shared/api-error.util';
import { dateHeureDuJourPourInput } from '../../shared/date-ui.util';

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

      @if (chargementSites) {
        <p>Chargement des sites et terrains...</p>
      }

      @if (!chargementSites) {
        <form (ngSubmit)="creerMatch()">
          <label for="siteId">Site</label>
          <select
            id="siteId"
            name="siteId"
            [(ngModel)]="siteId"
            (ngModelChange)="changerSite()"
            required
          >
            <option *ngFor="let site of sites" [ngValue]="site.siteId">
              {{ site.nomSite }} ({{ site.siteId }})
            </option>
          </select>

          @if (siteSelectionne(); as site) {
            <div class="bloc-info site-hours-card">
              <h3>Programme d'ouverture du site</h3>

              <p>
                <strong>{{ site.nomSite }}</strong> :
                site ouvert entre
                <strong>{{ formaterHeure(site.heureDebutReservation) }}</strong>
                et
                <strong>{{ formaterHeure(site.heureFinReservation) }}</strong>.
              </p>

              <p>
                Terrains actifs disponibles :
                <strong>{{ site.terrains.length }}</strong>
              </p>
            </div>
          }

          <label for="terrainId">Terrain</label>
          <select
            id="terrainId"
            name="terrainId"
            [(ngModel)]="terrainId"
            required
          >
            <option *ngFor="let terrain of terrainsDuSite()" [ngValue]="terrain.terrainId">
              Terrain {{ terrain.numeroTerrain }} ({{ terrain.terrainId }})
            </option>
          </select>

          @if (terrainSelectionne(); as terrain) {
            <div class="bloc-info">
              <h3>Terrain sélectionné</h3>
              <p>
                <strong>{{ siteSelectionne()?.nomSite }}</strong>
                — Terrain {{ terrain.numeroTerrain }} ({{ terrain.terrainId }})
              </p>
            </div>
          }

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

            <p><strong>Site :</strong> {{ siteSelectionne()?.nomSite }}</p>
            <p><strong>Terrain :</strong> {{ terrainSelectionne()?.numeroTerrain }}</p>
            <p><strong>Prix total :</strong> 60 €</p>
            <p><strong>Part par joueur :</strong> 15 €</p>
            <p><strong>Mode sélectionné :</strong> {{ modeCreation }}</p>
            <p><strong>Début demandé :</strong> {{ dateHeureDebut }}</p>
          </div>

          <button type="submit" [disabled]="chargement">
            {{ chargement ? 'Création...' : 'Créer le match' }}
          </button>
        </form>
      }

      <p *ngIf="messageErreur" class="erreur">
        {{ messageErreur }}
      </p>

      <div *ngIf="matchCree" class="resultat match-card">
        <h3>Match créé avec succès</h3>

        <div class="resume-grid">
          <p><strong>ID match</strong><br>{{ matchCree.matchId }}</p>
          <p><strong>Site</strong><br>{{ siteSelectionne()?.nomSite }} ({{ matchCree.siteId }})</p>
          <p><strong>Terrain</strong><br>{{ terrainSelectionne()?.numeroTerrain }} ({{ matchCree.terrainId }})</p>
          <p><strong>Début</strong><br>{{ matchCree.dateHeureDebut }}</p>
          <p><strong>Fin</strong><br>{{ matchCree.dateHeureFin }}</p>
          <p><strong>Mode</strong><br>{{ matchCree.modeCreation }}</p>
          <p><strong>Visibilité</strong><br>{{ matchCree.visibiliteCourante }}</p>
          <p><strong>Prix total</strong><br>{{ matchCree.prixTotal }} €</p>
          <p><strong>État</strong><br>{{ matchCree.etatCycle }}</p>
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

    .site-hours-card {
      border-color: #93c5fd;
      background: #f8fbff;
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
  sites: SiteReservationInfoResponse[] = [];

  siteId: number | null = null;
  terrainId: number | null = null;

  matriculeOrganisateur = 'G1001';
  dateHeureDebut = dateHeureDuJourPourInput('13:00');
  modeCreation: ModeCreation = 'PUBLIC';

  chargement = false;
  chargementSites = false;
  messageErreur = '';
  matchCree: MatchResponse | null = null;

  private readonly terrainIdParam: number | null;
  private readonly dateHeureDebutParam: string | null;

  constructor(
    private readonly matchApiService: MatchApiService,
    private readonly siteReservationInfoApiService: SiteReservationInfoApiService,
    private readonly authContext: AuthContextService,
    private readonly changeDetectorRef: ChangeDetectorRef,
    private readonly route: ActivatedRoute
  ) {
    this.matriculeOrganisateur = this.authContext.joueur()?.matricule ?? 'G1001';

    const terrainIdQueryParam = this.route.snapshot.queryParamMap.get('terrainId');

    this.terrainIdParam = terrainIdQueryParam ? Number(terrainIdQueryParam) : null;
    this.dateHeureDebutParam = this.route.snapshot.queryParamMap.get('dateHeureDebut');

    if (this.dateHeureDebutParam) {
      this.dateHeureDebut = this.dateHeureDebutParam.substring(0, 16);
    }

    this.chargerSites();
  }

  chargerSites(): void {
    this.chargementSites = true;
    this.messageErreur = '';
    this.changeDetectorRef.detectChanges();

    this.siteReservationInfoApiService.listerSitesAvecInfosReservation().subscribe({
      next: response => {
        this.sites = response;
        this.selectionnerValeursInitiales();
        this.chargementSites = false;
        this.changeDetectorRef.detectChanges();
      },
      error: error => {
        this.messageErreur = extraireMessageErreur(error);
        this.chargementSites = false;
        this.changeDetectorRef.detectChanges();
      }
    });
  }

  selectionnerValeursInitiales(): void {
    if (this.sites.length === 0) {
      this.siteId = null;
      this.terrainId = null;
      return;
    }

    if (this.terrainIdParam) {
      const siteAvecTerrain = this.sites.find(site =>
        site.terrains.some(terrain => terrain.terrainId === this.terrainIdParam)
      );

      if (siteAvecTerrain) {
        this.siteId = siteAvecTerrain.siteId;
        this.terrainId = this.terrainIdParam;
        return;
      }
    }

    this.siteId = this.sites[0].siteId;
    this.terrainId = this.sites[0].terrains[0]?.terrainId ?? null;
  }

  changerSite(): void {
    const terrains = this.terrainsDuSite();
    this.terrainId = terrains.length > 0 ? terrains[0].terrainId : null;
    this.matchCree = null;
    this.changeDetectorRef.detectChanges();
  }

  siteSelectionne(): SiteReservationInfoResponse | undefined {
    return this.sites.find(site => site.siteId === Number(this.siteId));
  }

  terrainsDuSite(): TerrainReservationInfoResponse[] {
    return this.siteSelectionne()?.terrains ?? [];
  }

  terrainSelectionne(): TerrainReservationInfoResponse | undefined {
    return this.terrainsDuSite().find(terrain => terrain.terrainId === Number(this.terrainId));
  }

  formaterHeure(heure: string): string {
    if (!heure) {
      return '-';
    }

    const [heures, minutes] = heure.split(':');
    return `${Number(heures)}.${minutes} h`;
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
      next: response => {
        this.matchCree = response;
        this.chargement = false;
        this.changeDetectorRef.detectChanges();
      },
      error: error => {
        this.messageErreur = extraireMessageErreur(error);
        this.chargement = false;
        this.changeDetectorRef.detectChanges();
      }
    });
  }
}
