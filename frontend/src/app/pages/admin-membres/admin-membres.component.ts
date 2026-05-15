import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MembreAdminResponse } from '../../models/admin-membre.model';
import { SiteReservationInfoResponse } from '../../models/site-reservation-info.model';
import { AdminMembreApiService } from '../../services/admin-membre-api.service';
import { AuthContextService } from '../../services/auth-context.service';
import { SiteReservationInfoApiService } from '../../services/site-reservation-info-api.service';
import { extraireMessageErreur } from '../../shared/api-error.util';

@Component({
  selector: 'app-admin-membres',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <section class="page">
      <h2>Membres</h2>

      <p>
        Cette page permet à un administrateur de lister tous les membres ou
        uniquement les membres rattachés à un site.
      </p>

      @if (!authContextService.admin()) {
        <p class="erreur">
          Aucun administrateur connecté.
        </p>

        <p>
          <a routerLink="/admin/login">Aller à la connexion admin</a>
        </p>
      }

      @if (authContextService.admin(); as admin) {
        <div class="bloc-info">
          <h3>Administrateur connecté</h3>

          <p>
            <strong>{{ admin.prenom }} {{ admin.nom }}</strong>
            — rôle {{ admin.roleAdministrateur }}
          </p>

          @if (admin.siteId) {
            <p>
              Site admin : {{ admin.nomSite }} ({{ admin.siteId }})
            </p>
          } @else {
            <p>
              Accès global à tous les sites.
            </p>
          }
        </div>

        <div class="bloc-info">
          <h3>Actions</h3>

          <div class="actions">
            <button type="button" (click)="listerTousLesMembres()" [disabled]="chargement">
              Lister tous les membres
            </button>
          </div>

          <label for="siteId">Site pour le filtre</label>
          <select
            id="siteId"
            name="siteId"
            [(ngModel)]="siteId"
          >
            <option *ngFor="let site of sites" [ngValue]="site.siteId">
              {{ site.nomSite }} ({{ site.siteId }})
            </option>
          </select>

          <div class="actions">
            <button type="button" (click)="listerMembresParSite()" [disabled]="chargement || !siteId">
              Lister les membres du site sélectionné
            </button>
          </div>
        </div>

        @if (messageErreur) {
          <p class="erreur">
            {{ messageErreur }}
          </p>
        }

        @if (messageInfo) {
          <p class="message-info">
            {{ messageInfo }}
          </p>
        }

        @if (membres.length > 0) {
          <table>
            <thead>
              <tr>
                <th>Matricule</th>
                <th>Nom</th>
                <th>Prénom</th>
                <th>Catégorie</th>
                <th>Site rattachement</th>
                <th>Actif</th>
                <th>Solde</th>
              </tr>
            </thead>

            <tbody>
              <tr *ngFor="let membre of membres">
                <td>{{ membre.matricule }}</td>
                <td>{{ membre.nom }}</td>
                <td>{{ membre.prenom }}</td>
                <td>{{ membre.categorieMembre }}</td>
                <td>
                  @if (membre.siteRattachementId) {
                    {{ membre.nomSiteRattachement }} ({{ membre.siteRattachementId }})
                  } @else {
                    Tous sites / aucun rattachement
                  }
                </td>
                <td>{{ membre.actif ? 'Oui' : 'Non' }}</td>
                <td>{{ membre.soldeCredit | number:'1.2-2' }} €</td>
              </tr>
            </tbody>
          </table>
        }

        @if (membres.length === 0 && rechercheEffectuee && !chargement) {
          <p>Aucun membre trouvé.</p>
        }
      }
    </section>
  `,
  styles: [`
    .message-info {
      margin-top: 16px;
      color: #003b95;
      font-weight: 700;
    }

    select {
      max-width: 420px;
      margin-top: 8px;
    }
  `]
})
export class AdminMembresComponent {
  sites: SiteReservationInfoResponse[] = [];
  membres: MembreAdminResponse[] = [];

  siteId: number | null = null;
  chargement = false;
  rechercheEffectuee = false;
  messageErreur = '';
  messageInfo = '';

  constructor(
    readonly authContextService: AuthContextService,
    private readonly siteReservationInfoApiService: SiteReservationInfoApiService,
    private readonly adminMembreApiService: AdminMembreApiService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {
    this.chargerSites();
  }

  chargerSites(): void {
    this.siteReservationInfoApiService.listerSitesAvecInfosReservation().subscribe({
      next: response => {
        this.sites = response;

        const admin = this.authContextService.admin();

        if (admin?.siteId) {
          this.siteId = admin.siteId;
        } else if (response.length > 0) {
          this.siteId = response[0].siteId;
        }

        this.changeDetectorRef.detectChanges();
      },
      error: error => {
        this.messageErreur = extraireMessageErreur(error);
        this.changeDetectorRef.detectChanges();
      }
    });
  }

  listerTousLesMembres(): void {
    this.messageErreur = '';
    this.messageInfo = '';
    this.membres = [];
    this.chargement = true;
    this.rechercheEffectuee = true;
    this.changeDetectorRef.detectChanges();

    this.adminMembreApiService.listerTousLesMembres().subscribe({
      next: response => {
        this.membres = response;
        this.messageInfo = `Tous les membres : ${response.length} résultat(s).`;
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

  listerMembresParSite(): void {
    if (!this.siteId) {
      this.messageErreur = 'Sélectionne un site.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    this.messageErreur = '';
    this.messageInfo = '';
    this.membres = [];
    this.chargement = true;
    this.rechercheEffectuee = true;
    this.changeDetectorRef.detectChanges();

    this.adminMembreApiService.listerMembresParSite(this.siteId).subscribe({
      next: response => {
        const site = this.sites.find(item => item.siteId === Number(this.siteId));
        this.membres = response;
        this.messageInfo = `Membres du site ${site?.nomSite ?? this.siteId} : ${response.length} résultat(s).`;
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
