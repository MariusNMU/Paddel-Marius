import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CreneauDisponibiliteResponse } from '../../models/disponibilite.model';
import { DisponibilitesFacadeService } from '../../services/disponibilites-facade.service';

@Component({
  selector: 'app-disponibilites',
  standalone: true,
  imports: [CommonModule, FormsModule],
  providers: [DisponibilitesFacadeService],
  template: `
    <section class="page">
      <h2>Organiser un match</h2>

      <p>
        Choisis un site et une date pour trouver un créneau disponible, puis utilise ce créneau pour créer un match public ou privé.
      </p>
      <div class="bloc-info">
        <h3>Sites disponibles</h3>

        <p *ngIf="facade.chargementSites()" class="aide">
          Chargement des sites...
        </p>

        <p
          *ngIf="
            !facade.chargementSites()
            && facade.sites().length === 0
          "
          class="aide"
        >
          Aucun site actif disponible.
        </p>

        <div
          *ngIf="
            !facade.chargementSites()
            && facade.sites().length > 0
          "
          class="sites-api"
        >
          <article
            *ngFor="let site of facade.sites()"
            class="site-api-card"
          >
            <h4>{{ site.nom }}</h4>
            <p><strong>Code :</strong> {{ site.code }}</p>
            <p><strong>Adresse :</strong> {{ site.adresse }}</p>
          </article>
        </div>

        <p class="aide">
          Les sites actifs sont chargés depuis le backend.
        </p>
      </div>
      <div class="bloc-info">
        <h3>Choix rapide de la date</h3>

        <div class="jours-rapides">
          <button
            *ngFor="let jour of facade.joursRapides()"
            type="button"
            (click)="selectionnerJour(jour.date)"
            [class.selectionne]="facade.date() === jour.date"
          >
            <span>{{ jour.libelle }}</span>
            <strong>{{ jour.date | date:'dd/MM/yyyy' }}</strong>
          </button>
        </div>
      </div>

      <form (ngSubmit)="consulterDisponibilites()">
        <label for="siteId">Site</label>

        <select
          id="siteId"
          name="siteId"
          [ngModel]="facade.siteId()"
          (ngModelChange)="facade.modifierSiteId($event)"
          required
          [disabled]="
            facade.chargementSites()
            || facade.sites().length === 0
          "
        >
          <option
            *ngFor="let site of facade.sites()"
            [ngValue]="site.siteId"
          >
            {{ site.nom }}
          </option>
        </select>

        <div
          class="bloc-info"
          *ngIf="facade.siteSelectionne() as site"
        >
          <h3>Site sélectionné</h3>

          <p>
            <strong>{{ site.nom }}</strong>
            — code {{ site.code }}
          </p>

          <p>{{ site.adresse }}</p>
        </div>

        <label for="date">Date</label>

        <input
          id="date"
          name="date"
          type="date"
          [ngModel]="facade.date()"
          (ngModelChange)="facade.modifierDate($event)"
          required
        />

        <button
          type="submit"
          [disabled]="
            facade.chargementRecherche()
            || facade.chargementSites()
            || facade.sites().length === 0
          "
        >
          {{
            facade.chargementRecherche()
              ? 'Recherche...'
              : 'Voir les créneaux disponibles'
          }}
        </button>
      </form>

      <p *ngIf="facade.messageErreur()" class="erreur">
        {{ facade.messageErreur() }}
      </p>

      <div
        *ngIf="facade.disponibilites() as disponibilites"
        class="resultat"
      >
        <h3>
          {{ disponibilites.nomSite }} —
          {{ disponibilites.date | date:'dd/MM/yyyy' }}
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
              Terrain {{ creneau.numeroTerrain }}
            </h4>

            <p>
              <strong>Début :</strong>
              {{ creneau.dateHeureDebut | date:'HH:mm' }}
            </p>

            <p>
              <strong>Fin :</strong>
              {{ creneau.dateHeureFin | date:'HH:mm' }}
            </p>

            <p>
              <strong>Durée :</strong>
              {{ facade.dureeMatchLibelle() }}
            </p>

            @if (facade.peutCreerMatchSurSiteSelectionne()) {
              <button
                type="button"
                (click)="allerCreerMatch(creneau)"
              >
                Utiliser ce créneau pour créer un match
              </button>
            } @else {
              <p class="action-indisponible">
                Un membre SITE ne peut réserver que sur son site de rattachement.
              </p>
            }
          </article>
        </div>
      </div>
    </section>
  `,
  styles: [`

    .sites-api {
      display: grid;
      grid-template-columns:
        repeat(
          auto-fill,
          minmax(260px, 320px)
        );
      justify-content: start;
      align-items: stretch;
      gap: 12px;
      margin-top: 12px;
    }

    .site-api-card {
      box-sizing: border-box;
      width: 100%;
      height: 100%;
      border: 1px solid #bfdbfe;
      border-radius: 10px;
      background: #ffffff;
      padding: 14px;
    }

    .site-api-card h4 {
      margin: 0 0 10px;
      color: #003b95;
    }

    .site-api-card p {
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
      grid-template-columns:
        repeat(
          auto-fill,
          minmax(320px, 360px)
        );
      grid-auto-rows: 1fr;
      justify-content: start;
      align-items: stretch;
      gap: 20px;
      margin-top: 18px;
    }

    .creneau-card {
      box-sizing: border-box;
      display: flex;
      flex-direction: column;
      width: 100%;
      min-width: 0;
      height: 100%;
      border: 1px solid #bfdbfe;
      border-radius: 12px;
      background: #ffffff;
      padding: 16px;
      box-shadow:
        0 4px 12px
        rgba(15, 23, 42, 0.06);
    }

    .creneau-card h4 {
      margin: 0 0 12px;
      color: #003b95;
    }

    .creneau-card p {
      margin: 8px 0;
    }

    .action-indisponible {
      margin-top: 12px;
      padding: 10px;
      border-radius: 8px;
      background: #f1f5f9;
      color: #475569;
      font-weight: 600;
    }

    .creneau-card button,
    .creneau-card .action-indisponible {
      align-self: flex-start;
      margin-top: auto;
    }

    @media (max-width: 640px) {
      .sites-api,
      .creneaux-grid {
        grid-template-columns:
          minmax(0, 1fr);
      }
    }
  `]
})
export class DisponibilitesComponent implements OnInit {
  constructor(
    readonly facade: DisponibilitesFacadeService
  ) {
  }

  ngOnInit(): void {
    this.facade.initialiser();
  }

  selectionnerJour(date: string): void {
    this.facade.selectionnerJour(date);
  }

  consulterDisponibilites(): void {
    this.facade.consulterDisponibilites();
  }

  formaterHeure(dateHeure: string): string {
    if (!dateHeure || dateHeure.length < 16) {
      return dateHeure;
    }

    return dateHeure.substring(11, 16);
  }

  allerCreerMatch(
    creneau: CreneauDisponibiliteResponse
  ): void {
    this.facade.allerCreerMatch(creneau);
  }
}
