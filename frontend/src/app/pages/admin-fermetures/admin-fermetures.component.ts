import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  CreerFermetureRequest,
  FermetureAdminResponse,
  PorteeFermeture
} from '../../models/fermeture.model';
import { SiteResponse } from '../../models/site.model';
import { AdminFermetureApiService } from '../../services/admin-fermeture-api.service';
import { SiteApiService } from '../../services/site-api.service';
import { extraireMessageErreur } from '../../shared/api-error.util';
import { enumLabel } from '../../shared/enum-label.util';

@Component({
  selector: 'app-admin-fermetures',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page">
      <h2>Jours de fermeture</h2>

      <p>
        Cette page permet à un administrateur de créer une fermeture globale ou locale.
        Le backend bloque ensuite les disponibilités et annule les matches à venir concernés.
      </p>

      <div class="bloc-info">
        <h3>Règles métier</h3>

        <ul>
          <li><strong>Fermeture globale :</strong> tous les sites sont fermés.</li>
          <li><strong>Fermeture locale :</strong> seul le site sélectionné est fermé.</li>
          <li>Les matches à venir sur les terrains concernés sont annulés.</li>
          <li>Les disponibilités du jour fermé deviennent indisponibles.</li>
        </ul>
      </div>

      <form (ngSubmit)="creerFermeture()">
        <label for="dateFermeture">Date de fermeture</label>
        <input
          id="dateFermeture"
          name="dateFermeture"
          type="date"
          [(ngModel)]="dateFermeture"
          required
        />

        <label for="portee">Portée</label>
        <select
          id="portee"
          name="portee"
          [(ngModel)]="portee"
        >
          <option value="GLOBALE">Globale — tous les sites</option>
          <option value="LOCALE">Locale — un site précis</option>
        </select>

        @if (portee === 'LOCALE') {
          <label for="siteId">Site concerné</label>
          <select
            id="siteId"
            name="siteId"
            [(ngModel)]="siteId"
            [disabled]="chargementSites || sites.length === 0"
          >
            <option *ngFor="let site of sites" [ngValue]="site.siteId">
              {{ site.nom }} ({{ site.siteId }})
            </option>
          </select>
        }

        <label for="motif">Motif</label>
        <input
          id="motif"
          name="motif"
          type="text"
          [(ngModel)]="motif"
          maxlength="255"
        />

        <div class="bloc-info">
          <h3>Résumé avant validation</h3>

          <p><strong>Date :</strong> {{ dateFermeture || 'Non renseignée' }}</p>
          <p><strong>Portée :</strong> {{ enumLabel(portee) }}</p>

          @if (portee === 'LOCALE') {
            <p><strong>Site :</strong> {{ nomSiteSelectionne() }}</p>
          }

          <p><strong>Motif :</strong> {{ motif || 'Aucun motif renseigné' }}</p>
        </div>

        <button type="submit" [disabled]="chargement || chargementSites">
          {{ chargement ? 'Création...' : 'Créer la fermeture' }}
        </button>
      </form>

      @if (messageErreur) {
        <p class="erreur">
          {{ messageErreur }}
        </p>
      }

      @if (fermetureCreee) {
        <div class="resultat">
          <h3>Fermeture créée avec succès</h3>

          <div class="resume-grid">
            <p><strong>ID fermeture</strong><br>{{ fermetureCreee.fermetureId }}</p>
            <p><strong>Date</strong><br>{{ fermetureCreee.dateFermeture }}</p>
            <p><strong>Portée</strong><br>{{ enumLabel(fermetureCreee.portee) }}</p>
            <p><strong>Site</strong><br>{{ fermetureCreee.nomSite || 'Tous les sites' }}</p>
            <p><strong>Matches annulés</strong><br>{{ fermetureCreee.nombreMatchesAnnules }}</p>
          </div>
        </div>
      }
    </section>
  `,
  styles: [`
    .resume-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
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
export class AdminFermeturesComponent implements OnInit {
  readonly enumLabel = enumLabel;
  sites: SiteResponse[] = [];

  dateFermeture = '';
  portee: PorteeFermeture | '' = '';
  siteId: number | null = null;
  motif = '';

  chargementSites = false;
  chargement = false;
  messageErreur = '';
  fermetureCreee: FermetureAdminResponse | null = null;

  constructor(
    private readonly adminFermetureApiService: AdminFermetureApiService,
    private readonly siteApiService: SiteApiService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {
  }

  ngOnInit(): void {
    this.chargerSites();
  }

  private chargerSites(): void {
    this.messageErreur = '';
    this.chargementSites = true;

    this.siteApiService.listerSitesActifs().subscribe({
      next: (sites) => {
        this.sites = sites;

        const siteSelectionExiste = this.siteId !== null
          && this.sites.some(site => site.siteId === this.siteId);

        if (!siteSelectionExiste) {
          this.siteId = this.sites.length > 0
            ? this.sites[0].siteId
            : null;
        }

        this.chargementSites = false;
        this.changeDetectorRef.detectChanges();
      },
      error: (error) => {
        this.messageErreur = extraireMessageErreur(error);
        this.sites = [];
        this.siteId = null;
        this.chargementSites = false;
        this.changeDetectorRef.detectChanges();
      }
    });
  }

  creerFermeture(): void {
    this.messageErreur = '';
    this.fermetureCreee = null;

    if (!this.dateFermeture) {
      this.messageErreur = 'La date de fermeture est obligatoire.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    if (!this.portee) {
      this.messageErreur = 'La portée de fermeture est obligatoire.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    if (this.portee === 'LOCALE' && !this.siteId) {
      this.messageErreur = 'Le site est obligatoire pour une fermeture locale.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    const request: CreerFermetureRequest = {
      dateFermeture: this.dateFermeture,
      portee: this.portee,
      siteId: this.portee === 'LOCALE' ? Number(this.siteId) : null,
      motif: this.motif.trim()
    };

    this.chargement = true;
    this.changeDetectorRef.detectChanges();

    this.adminFermetureApiService.creerFermeture(request).subscribe({
      next: (response) => {
        this.fermetureCreee = response;
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

  nomSiteSelectionne(): string {
    const site = this.sites.find(site => site.siteId === Number(this.siteId));

    if (!site) {
      return 'Site inconnu';
    }

    return `${site.nom} (${site.siteId})`;
  }
}
