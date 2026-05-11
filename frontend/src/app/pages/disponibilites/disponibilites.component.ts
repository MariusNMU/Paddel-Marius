import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CreneauDisponibiliteResponse, DisponibilitesResponse } from '../../models/disponibilite.model';
import { DisponibiliteApiService } from '../../services/disponibilite-api.service';
import { extraireMessageErreur } from '../../shared/api-error.util';

interface SiteDemo {
  id: number;
  nom: string;
  code: string;
  description: string;
}

interface JourRapide {
  libelle: string;
  date: string;
}

@Component({
  selector: 'app-disponibilites',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page">
      <h2>Réserver un terrain</h2>

      <p>
        Choisis un site et une date pour consulter les terrains et créneaux disponibles.
      </p>

      <div class="bloc-info">
        <h3>Choix rapide de la date</h3>

        <div class="actions">
          <button
            *ngFor="let jour of joursRapides"
            type="button"
            (click)="selectionnerJour(jour.date)"
          >
            {{ jour.libelle }} — {{ jour.date }}
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

            <button type="button" (click)="allerCreerMatch()">
              Utiliser ce créneau pour créer un match
            </button>
          </article>
        </div>
      </div>
    </section>
  `,
  styles: [`
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
      description: 'Site de démonstration avec les terrains T1 et T2.'
    },
    {
      id: 1002,
      nom: 'Padel Namur',
      code: 'NAM',
      description: 'Site de démonstration avec les terrains T1 et T2.'
    }
  ];

  joursRapides: JourRapide[] = [
    { libelle: 'Samedi', date: '2026-06-20' },
    { libelle: 'Dimanche', date: '2026-06-21' },
    { libelle: 'Lundi', date: '2026-06-22' },
    { libelle: 'Mardi', date: '2026-06-23' },
    { libelle: 'Mercredi', date: '2026-06-24' },
    { libelle: 'Jeudi', date: '2026-06-25' },
    { libelle: 'Vendredi', date: '2026-06-26' }
  ];

  siteId = 1001;
  date = '2026-06-20';

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

  allerCreerMatch(): void {
    this.router.navigate(['/joueur/creer-match']);
  }
}
