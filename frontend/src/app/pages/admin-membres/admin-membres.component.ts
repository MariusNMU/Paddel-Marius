import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  OnInit
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MembreResponse } from '../../models/membre.model';
import { SiteResponse } from '../../models/site.model';
import { AdminMembreApiService } from '../../services/admin-membre-api.service';
import { AuthContextService } from '../../services/auth-context.service';
import { SiteApiService } from '../../services/site-api.service';
import { extraireMessageErreur } from '../../shared/api-error.util';
import { enumLabel } from '../../shared/enum-label.util';

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

        @if (estAdminGlobal()) {
          <div class="actions-membres">
            <button
              type="button"
              (click)="afficherTousLesMembres()"
              [disabled]="chargement"
            >
              Afficher tous les membres
            </button>

            <button
              type="button"
              (click)="afficherMembresDuSiteSelectionne()"
              [disabled]="
                chargement
                || chargementSites
                || siteId === null
              "
            >
              Filtrer par site sélectionné
            </button>
          </div>

          <label for="siteId">Site</label>

          <select
            id="siteId"
            name="siteId"
            [(ngModel)]="siteId"
            [disabled]="chargementSites || sites.length === 0"
          >
            <option [ngValue]="null">
              Sélectionner un site
            </option>

            <option
              *ngFor="let site of sites"
              [ngValue]="site.siteId"
            >
              {{ site.nom }} ({{ site.siteId }})
            </option>
          </select>

          <p class="aide">
            Le filtre par site affiche les membres rattachés au site choisi.
          </p>
        } @else {
          @if (adminConnecte(); as admin) {
            <p class="aide">
              Ton accès est limité aux membres du site
              <strong>
                {{ admin.nomSite }} ({{ admin.siteId }})
              </strong>.
            </p>
          }
        }
      </div>

      <p *ngIf="messageErreur" class="erreur">
        {{ messageErreur }}
      </p>

      <div *ngIf="chargement" class="bloc-info">
        Chargement des membres...
      </div>

      <div
        *ngIf="!chargement && membres.length > 0"
        class="resultat"
      >
        <h3>{{ titreResultat }}</h3>

        <p>
          Nombre de membres affichés :
          <strong>{{ membres.length }}</strong>
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
          !chargement
          && !messageErreur
          && membres.length === 0
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

  sites: SiteResponse[] = [];
  siteId: number | null = null;
  membres: MembreResponse[] = [];

  chargementSites = false;
  chargement = false;
  messageErreur = '';
  titreResultat = 'Membres';

  constructor(
    private readonly adminMembreApiService:
    AdminMembreApiService,
    private readonly siteApiService:
    SiteApiService,
    private readonly authContextService:
    AuthContextService,
    private readonly changeDetectorRef:
    ChangeDetectorRef
  ) {
  }

  ngOnInit(): void {
    const admin = this.adminConnecte();

    if (!admin) {
      this.messageErreur =
        'Connecte-toi comme administrateur pour consulter les membres.';
      return;
    }

    if (admin.roleAdministrateur === 'GLOBAL') {
      this.chargerSites();
      this.afficherTousLesMembres();
      return;
    }

    if (admin.siteId === null) {
      this.messageErreur =
        'Aucun site n’est associé à cet administrateur.';
      return;
    }

    this.siteId = admin.siteId;

    this.chargerMembresParSite(
      admin.siteId,
      admin.nomSite ?? `site ${admin.siteId}`
    );
  }

  adminConnecte() {
    return this.authContextService.admin();
  }

  estAdminGlobal(): boolean {
    return this.adminConnecte()
      ?.roleAdministrateur === 'GLOBAL';
  }

  private chargerSites(): void {
    this.chargementSites = true;

    this.siteApiService.listerSitesActifs().subscribe({
      next: sites => {
        this.sites = sites;

        const siteSelectionExiste =
          this.siteId !== null
          && this.sites.some(
            site => site.siteId === this.siteId
          );

        if (!siteSelectionExiste) {
          this.siteId = null;
        }

        this.chargementSites = false;
        this.changeDetectorRef.detectChanges();
      },
      error: error => {
        this.messageErreur =
          extraireMessageErreur(error);
        this.sites = [];
        this.siteId = null;
        this.chargementSites = false;
        this.changeDetectorRef.detectChanges();
      }
    });
  }

  afficherTousLesMembres(): void {
    if (!this.estAdminGlobal()) {
      this.messageErreur =
        'Cette action est réservée aux administrateurs globaux.';
      return;
    }

    this.messageErreur = '';
    this.membres = [];
    this.chargement = true;
    this.titreResultat = 'Tous les membres';

    this.changeDetectorRef.detectChanges();

    this.adminMembreApiService
      .listerTousLesMembres()
      .subscribe({
        next: response => {
          this.membres = response;
          this.chargement = false;
          this.changeDetectorRef.detectChanges();
        },
        error: error => {
          this.messageErreur =
            extraireMessageErreur(error);
          this.membres = [];
          this.chargement = false;
          this.changeDetectorRef.detectChanges();
        }
      });
  }

  afficherMembresDuSiteSelectionne(): void {
    this.messageErreur = '';
    this.membres = [];

    if (this.siteId === null) {
      this.messageErreur =
        'Sélectionne un site avant de filtrer les membres.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    const siteSelectionne = this.sites.find(
      site => site.siteId === Number(this.siteId)
    );

    if (!siteSelectionne) {
      this.messageErreur =
        'Sélectionne un site valide avant de filtrer les membres.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    this.chargerMembresParSite(
      siteSelectionne.siteId,
      siteSelectionne.nom
    );
  }

  private chargerMembresParSite(
    siteId: number,
    nomSite: string
  ): void {
    this.messageErreur = '';
    this.membres = [];
    this.chargement = true;
    this.titreResultat =
      `Membres rattachés au site ${nomSite}`;

    this.changeDetectorRef.detectChanges();

    this.adminMembreApiService
      .listerMembresParSite(siteId)
      .subscribe({
        next: response => {
          this.membres = response;
          this.chargement = false;
          this.changeDetectorRef.detectChanges();
        },
        error: error => {
          this.messageErreur =
            extraireMessageErreur(error);
          this.membres = [];
          this.chargement = false;
          this.changeDetectorRef.detectChanges();
        }
      });
  }
}
