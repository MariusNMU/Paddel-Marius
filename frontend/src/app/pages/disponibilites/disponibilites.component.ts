import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CreneauDisponibiliteResponse, DisponibilitesResponse } from '../../models/disponibilite.model';
import { DisponibiliteApiService } from '../../services/disponibilite-api.service';
import { extraireMessageErreur } from '../../shared/api-error.util';
import { JourRapide, dateDuJourPourInput, genererJoursRapides } from '../../shared/date-ui.util';

interface SiteDemo {
  id: number;
  nom: string;
  code: string;
  description: string;
}

@Component({
  selector: 'app-disponibilites',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page">
      <h2>Organiser un match</h2>

      <p>
        Choisis un site et une date pour trouver un créneau disponible, puis utilise ce créneau pour créer un match public ou privé.
      </p>
      <div class="bloc-info">
        <h3>Horaires d'ouverture des sites</h3>

        <div class="horaires-sites">
          <article class="horaire-site-card">
            <h4>Padel Bruxelles</h4>
            <p><strong>Code :</strong> BRU</p>
            <p><strong>Horaires :</strong> 08:00 - 22:00</p>
            <p><strong>Terrains :</strong> T1, T2 et T3</p>
          </article>

          <article class="horaire-site-card">
            <h4>Padel Namur</h4>
            <p><strong>Code :</strong> NAM</p>
            <p><strong>Horaires :</strong> 09:00 - 21:00</p>
            <p><strong>Terrains :</strong> T1 et T2</p>
          </article>
        </div>

        <p class="aide">
          Ces horaires sont affichés pour la démo. Les règles de disponibilité sont toujours vérifiées par le backend.
        </p>
      </div>
      <div class="bloc-info">
        <h3>Choix rapide de la date</h3>

        <div class="jours-rapides">
          <button
            *ngFor="let jour of joursRapides"
            type="button"
            (click)="selectionnerJour(jour.date)"
            [class.selectionne]="date === jour.date"
          >
            <span>{{ jour.libelle }}</span>
            <strong>{{ jour.date }}</strong>
          </button>
        </div>
      </div>

      <form (ngSubmit)="consulterDisponibilites()">
        <label for="siteId">Site</label>
        <select
          id="siteId"
          name="siteId"
          [(ngModel)]="siteId"
          required
        >
          <option *ngFor="let site of sites" [ngValue]="site.id">
            {{ site.nom }} ({{ site.id }})
          </option>
        </select>

        <div class="bloc-info" *ngIf="siteSelectionne() as site">
          <h3>Site sélectionné</h3>
          <p><strong>{{ site.nom }}</strong> — code {{ site.code }} — ID {{ site.id }}</p>
          <p>{{ site.description }}</p>
        </div>

        <label for="date">Date</label>
        <input
          id="date"
          name="date"
          type="date"
          [(ngModel)]="date"
          required
        />

        <button type="submit" [disabled]="chargement">
          {{ chargement ? 'Recherche...' : 'Voir les créneaux disponibles' }}
        </button>
      </form>

      <p *ngIf="messageErreur" class="erreur">
        {{ messageErreur }}
      </p>

      <div *ngIf="disponibilites" class="resultat">
        <h3>
          {{ disponibilites.nomSite }} ({{ disponibilites.siteId }}) — {{ disponibilites.date }}
        </h3>

        <p *ngIf="disponibilites.ferme">
          Site fermé : {{ disponibilites.motifFermeture || 'motif non précisé' }}
        </p>

        <p *ngIf="!disponibilites.ferme && disponibilites.creneaux.length === 0">
          Aucun créneau disponible pour cette date.
        </p>

        <div
          *ngIf="!disponibilites.ferme && disponibilites.creneaux.length > 0"
          class="creneaux-grid"
        >
          <article
            *ngFor="let creneau of disponibilites.creneaux"
            class="creneau-card"
          >
            <h4>
              Terrain {{ creneau.numeroTerrain }} ({{ creneau.terrainId }})
            </h4>

            <p>
              <strong>Début :</strong>
              {{ formaterHeure(creneau.dateHeureDebut) }}
            </p>

            <p>
              <strong>Fin :</strong>
              {{ formaterHeure(creneau.dateHeureFin) }}
            </p>

            <p>
              <strong>Durée :</strong>
              1h30
            </p>

            <button type="button" (click)="allerCreerMatch(creneau)">
              Utiliser ce créneau pour créer un match
            </button>
          </article>
        </div>
      </div>
    </section>
  `,
  styles: [`

    .horaires-sites {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 12px;
      margin-top: 12px;
    }

    .horaire-site-card {
      border: 1px solid #bfdbfe;
      border-radius: 10px;
      background: #ffffff;
      padding: 14px;
    }

    .horaire-site-card h4 {
      margin: 0 0 10px;
      color: #003b95;
    }

    .horaire-site-card p {
      margin: 6px 0;
    }

    .jours-rapides {
      display: grid;
      grid-template-columns: repeat(7, minmax(0, 1fr));
      gap: 8px;
    }

    .jours-rapides button {
      width: 100%;
      padding: 8px 6px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 4px;
      font-size: 13px;
      line-height: 1.15;
    }

    .jours-rapides button.selectionne {
      background: #dbeafe;
      color: #001f5c;
      outline: 2px solid #003b95;
    }

    @media (max-width: 1100px) {
      .jours-rapides {
        grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
      }
    }

    .creneaux-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(230px, 1fr));
      gap: 16px;
      margin-top: 18px;
    }

    .creneau-card {
      border: 1px solid #bfdbfe;
      border-radius: 12px;
      background: #ffffff;
      padding: 16px;
      box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
    }

    .creneau-card h4 {
      margin: 0 0 12px;
      color: #003b95;
    }

    .creneau-card p {
      margin: 8px 0;
    }

    .creneau-card button {
      margin-top: 12px;
    }
  `]
})
export class DisponibilitesComponent {
  sites: SiteDemo[] = [
    {
      id: 1001,
      nom: 'Padel Bruxelles',
      code: 'BRU',
      description: 'Site de démonstration avec les terrains T1, T2 et T3.'
    },
    {
      id: 1002,
      nom: 'Padel Namur',
      code: 'NAM',
      description: 'Site de démonstration avec les terrains T1 et T2.'
    }
  ];

  joursRapides: JourRapide[] = genererJoursRapides(7);

  siteId = 1001;
  date = dateDuJourPourInput();

  chargement = false;
  messageErreur = '';
  disponibilites: DisponibilitesResponse | null = null;

  constructor(
    private readonly disponibiliteApiService: DisponibiliteApiService,
    private readonly router: Router,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {
  }

  siteSelectionne(): SiteDemo | undefined {
    return this.sites.find(site => site.id === Number(this.siteId));
  }

  selectionnerJour(date: string): void {
    this.date = date;
    this.messageErreur = '';
    this.disponibilites = null;
    this.changeDetectorRef.detectChanges();
  }

  consulterDisponibilites(): void {
    this.messageErreur = '';
    this.disponibilites = null;

    if (!this.siteId || !this.date) {
      this.messageErreur = 'Le site et la date sont obligatoires.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    this.chargement = true;
    this.changeDetectorRef.detectChanges();

    this.disponibiliteApiService.consulterDisponibilites(this.siteId, this.date).subscribe({
      next: (response) => {
        this.disponibilites = response;
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

  formaterHeure(dateHeure: string): string {
    if (!dateHeure || dateHeure.length < 16) {
      return dateHeure;
    }

    return dateHeure.substring(11, 16);
  }

  allerCreerMatch(creneau: CreneauDisponibiliteResponse): void {
    this.router.navigate(['/joueur/creer-match'], {
      queryParams: {
        terrainId: creneau.terrainId,
        dateHeureDebut: creneau.dateHeureDebut
      }
    });
  }
}
