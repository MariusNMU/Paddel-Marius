import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DisponibilitesResponse } from '../../models/disponibilite.model';
import { DisponibiliteApiService } from '../../services/disponibilite-api.service';
import { extraireMessageErreur } from '../../shared/api-error.util';

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

      <form (ngSubmit)="consulterDisponibilites()">
        <label for="siteId">Site</label>
        <select
          id="siteId"
          name="siteId"
          [(ngModel)]="siteId"
          required
        >
          <option [ngValue]="1001">Padel Bruxelles (1001)</option>
          <option [ngValue]="1002">Padel Namur (1002)</option>
        </select>

        <label for="date">Date</label>
        <input
          id="date"
          name="date"
          type="date"
          [(ngModel)]="date"
          required
        />

        <button type="submit" [disabled]="chargement">
          {{ chargement ? 'Recherche...' : 'Consulter' }}
        </button>
      </form>

      <p *ngIf="messageErreur" class="erreur">
        {{ messageErreur }}
      </p>

      <div *ngIf="disponibilites" class="resultat">
        <h3>
          Résultat pour {{ disponibilites.nomSite }} ({{ disponibilites.siteId }}) — {{ disponibilites.date }}
        </h3>

        <p *ngIf="disponibilites.ferme">
          Site fermé : {{ disponibilites.motifFermeture || 'motif non précisé' }}
        </p>

        <p *ngIf="!disponibilites.ferme && disponibilites.creneaux.length === 0">
          Aucun créneau disponible pour cette date.
        </p>

        <table *ngIf="!disponibilites.ferme && disponibilites.creneaux.length > 0">
          <thead>
          <tr>
            <th>Terrain</th>
            <th>Début</th>
            <th>Fin</th>
          </tr>
          </thead>
          <tbody>
          <tr *ngFor="let creneau of disponibilites.creneaux">
            <td>{{ creneau.numeroTerrain }} — ID {{ creneau.terrainId }}</td>
            <td>{{ creneau.dateHeureDebut }}</td>
            <td>{{ creneau.dateHeureFin }}</td>
          </tr>
          </tbody>
        </table>
      </div>
    </section>
  `
})
export class DisponibilitesComponent {
  siteId = 1001;
  date = '2026-06-20';

  chargement = false;
  messageErreur = '';
  disponibilites: DisponibilitesResponse | null = null;

  constructor(
    private readonly disponibiliteApiService: DisponibiliteApiService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {
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
}
