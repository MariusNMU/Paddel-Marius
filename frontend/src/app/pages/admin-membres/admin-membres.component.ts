import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MembreResponse } from '../../models/membre.model';
import { AdminMembreApiService } from '../../services/admin-membre-api.service';
import { extraireMessageErreur } from '../../shared/api-error.util';

interface SiteOption {
  id: number;
  nom: string;
}

@Component({
  selector: 'app-admin-membres',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page">
      <h2>Statistiques membres</h2>

      <p>
        Cette page permet à l'administrateur de consulter les membres.
        Les données sont chargées via l'API REST du backend.
      </p>

      <div class="bloc-info">
        <h3>Recherche</h3>

        <div class="actions-membres">
          <button type="button" (click)="afficherTousLesMembres()" [disabled]="chargement">
            Afficher tous les membres
          </button>

          <button type="button" (click)="afficherMembresDuSiteSelectionne()" [disabled]="chargement">
            Filtrer par site sélectionné
          </button>
        </div>

        <label for="siteId">Site</label>
        <select
          id="siteId"
          name="siteId"
          [(ngModel)]="siteId"
        >
          <option *ngFor="let site of sites" [ngValue]="site.id">
            {{ site.nom }} ({{ site.id }})
          </option>
        </select>

        <p class="aide">
          Le filtre par site affiche les membres rattachés au site choisi.
        </p>
      </div>

      <p *ngIf="messageErreur" class="erreur">
        {{ messageErreur }}
      </p>

      <div *ngIf="chargement" class="bloc-info">
        Chargement des membres...
      </div>

      <div *ngIf="!chargement && membres.length > 0" class="resultat">
        <h3>{{ titreResultat }}</h3>

        <p>
          Nombre de membres affichés : <strong>{{ membres.length }}</strong>
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
            <tr *ngFor="let membre of membres">
              <td>{{ membre.matricule }}</td>
              <td>{{ membre.nom }}</td>
              <td>{{ membre.prenom }}</td>
              <td>{{ membre.categorieMembre }}</td>
              <td>
                  <span *ngIf="membre.nomSiteRattachement">
                    {{ membre.nomSiteRattachement }} ({{ membre.siteRattachementId }})
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

      <div *ngIf="!chargement && !messageErreur && membres.length === 0" class="bloc-info">
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
  sites: SiteOption[] = [
    {
      id: 1001,
      nom: 'Padel Bruxelles'
    },
    {
      id: 1002,
      nom: 'Padel Namur'
    }
  ];

  siteId = 1001;
  membres: MembreResponse[] = [];
  chargement = false;
  messageErreur = '';
  titreResultat = 'Tous les membres';

  constructor(
    private readonly adminMembreApiService: AdminMembreApiService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {
  }

  ngOnInit(): void {
    this.afficherTousLesMembres();
  }

  afficherTousLesMembres(): void {
    this.messageErreur = '';
    this.membres = [];
    this.chargement = true;
    this.titreResultat = 'Tous les membres';

    this.changeDetectorRef.detectChanges();

    this.adminMembreApiService.listerTousLesMembres().subscribe({
      next: (response) => {
        this.membres = response;
        this.chargement = false;
        this.changeDetectorRef.detectChanges();
      },
      error: (error) => {
        this.messageErreur = extraireMessageErreur(error);
        this.membres = [];
        this.chargement = false;
        this.changeDetectorRef.detectChanges();
      }
    });
  }

  afficherMembresDuSiteSelectionne(): void {
    this.messageErreur = '';
    this.membres = [];
    this.chargement = true;

    const siteSelectionne = this.sites.find(site => site.id === Number(this.siteId));

    this.titreResultat = siteSelectionne
      ? `Membres rattachés au site ${siteSelectionne.nom}`
      : 'Membres du site sélectionné';

    this.changeDetectorRef.detectChanges();

    this.adminMembreApiService.listerMembresParSite(Number(this.siteId)).subscribe({
      next: (response) => {
        this.membres = response;
        this.chargement = false;
        this.changeDetectorRef.detectChanges();
      },
      error: (error) => {
        this.messageErreur = extraireMessageErreur(error);
        this.membres = [];
        this.chargement = false;
        this.changeDetectorRef.detectChanges();
      }
    });
  }
}
