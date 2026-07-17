import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminMembresFacadeService } from '../../services/admin-membres-facade.service';
import { enumLabel } from '../../shared/enum-label.util';

@Component({
  selector: 'app-admin-membres',
  standalone: true,
  imports: [CommonModule, FormsModule],
  providers: [AdminMembresFacadeService],
  template: `
    <section class="page">
      <h2>Statistiques membres</h2>

      <p>
        Cette page permet à l'administrateur de consulter les membres.
        Les données sont chargées via l'API REST du backend.
      </p>

      <div class="bloc-info">
        <h3>Recherche</h3>

        @if (facade.estAdminGlobal()) {
          <div class="actions-membres">
            <button
              type="button"
              (click)="afficherTousLesMembres()"
              [disabled]="facade.chargementMembres()"
            >
              Afficher tous les membres
            </button>

            <button
              type="button"
              (click)="afficherMembresDuSiteSelectionne()"
              [disabled]="
                facade.chargementMembres()
                || facade.chargementSites()
                || facade.siteId() === null
              "
            >
              Filtrer par site sélectionné
            </button>
          </div>

          <label for="siteId">Site</label>

          <select
            id="siteId"
            name="siteId"
            [ngModel]="facade.siteId()"
            (ngModelChange)="facade.modifierSiteId($event)"
            [disabled]="facade.chargementSites() || facade.sites().length === 0"
          >
            <option [ngValue]="null">
              Sélectionner un site
            </option>

            <option
              *ngFor="let site of facade.sites()"
              [ngValue]="site.siteId"
            >
              {{ site.nom }} ({{ site.siteId }})
            </option>
          </select>

          <p class="aide">
            Le filtre par site affiche les membres rattachés au site choisi.
          </p>
        } @else {
          @if (facade.admin(); as admin) {
            <p class="aide">
              Ton accès est limité aux membres du site
              <strong>
                {{ admin.nomSite }} ({{ admin.siteId }})
              </strong>.
            </p>
          }
        }
      </div>

      <p *ngIf="facade.messageErreur()" class="erreur">
        {{ facade.messageErreur() }}
      </p>

      <div *ngIf="facade.chargementMembres()" class="bloc-info">
        Chargement des membres...
      </div>

      <div
        *ngIf="!facade.chargementMembres() && facade.membres().length > 0"
        class="resultat"
      >
        <h3>{{ facade.titreResultat() }}</h3>

        <p>
          Nombre de membres affichés :
          <strong>{{ facade.membres().length }}</strong>
        </p>

        <div class="table-wrapper">
          <table>
            <thead>
            <tr>
              <th>Matricule</th>
              <th>Nom</th>
              <th>Prénom</th>
              <th>Catégorie</th>
              <th>Site de rattachement</th>
              <th>Solde</th>
              <th>Statut</th>
            </tr>
            </thead>

            <tbody>
            <tr *ngFor="let membre of facade.membres()">
              <td>{{ membre.matricule }}</td>
              <td>{{ membre.nom }}</td>
              <td>{{ membre.prenom }}</td>
              <td>
                {{ enumLabel(membre.categorieMembre) }}
              </td>
              <td>
                <span *ngIf="membre.nomSiteRattachement">
                  {{ membre.nomSiteRattachement }}
                  ({{ membre.siteRattachementId }})
                </span>

                <span *ngIf="!membre.nomSiteRattachement">
                  -
                </span>
              </td>
              <td>{{ membre.soldeCredit }} €</td>
              <td>
                <span [class.inactif]="!membre.actif">
                  {{ membre.actif ? 'Actif' : 'Inactif' }}
                </span>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div
        *ngIf="
          !facade.chargementMembres()
          && !facade.messageErreur()
          && facade.membres().length === 0
        "
        class="bloc-info"
      >
        Aucun membre à afficher.
      </div>
    </section>
  `,
  styles: [`
    .actions-membres {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      margin-bottom: 16px;
    }

    .aide {
      color: #64748b;
      font-size: 14px;
    }

    .table-wrapper {
      overflow-x: auto;
      margin-top: 16px;
    }

    table {
      width: 100%;
      border-collapse: collapse;
      background: #ffffff;
    }

    th,
    td {
      padding: 10px;
      border: 1px solid #bfdbfe;
      text-align: left;
      white-space: nowrap;
    }

    th {
      background: #dbeafe;
      color: #001f5c;
    }

    .inactif {
      color: #991b1b;
      font-weight: 700;
    }
  `]
})
export class AdminMembresComponent implements OnInit {
  readonly enumLabel = enumLabel;

  constructor(
    readonly facade: AdminMembresFacadeService
  ) {
  }

  ngOnInit(): void {
    this.facade.initialiser();
  }

  afficherTousLesMembres(): void {
    this.facade.afficherTousLesMembres();
  }

  afficherMembresDuSiteSelectionne(): void {
    this.facade.afficherMembresDuSiteSelectionne();
  }
}
