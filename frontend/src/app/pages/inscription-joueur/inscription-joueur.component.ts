import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  CategorieMembre,
  InscriptionMembreRequest,
  MembreResponse
} from '../../models/membre.model';
import { SiteResponse } from '../../models/site.model';
import { MembreApiService } from '../../services/membre-api.service';
import { SiteApiService } from '../../services/site-api.service';
import { extraireMessageErreur } from '../../shared/api-error.util';
import { enumLabel } from '../../shared/enum-label.util';

@Component({
  selector: 'app-inscription-joueur',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <section class="page">
      <h2>Inscription joueur</h2>

      <p>
        Crée un nouveau joueur dans le système. Le matricule est généré automatiquement
        par le backend selon la catégorie choisie.
      </p>

      <div class="bloc-info">
        <h3>Catégories disponibles</h3>

        <ul>
          <li><strong>Global</strong> : matricule Gxxxx, peut réserver sur tous les sites.</li>
          <li><strong>Site</strong> : matricule Sxxxx, rattaché à un site précis.</li>
          <li><strong>Libre</strong> : matricule Lxxxx, accès libre selon les règles métier.</li>
        </ul>
      </div>

      <form (ngSubmit)="envoyerDemande()">
        <label for="nom">Nom</label>
        <input
          id="nom"
          name="nom"
          type="text"
          [(ngModel)]="nom"
          required
        />

        <label for="prenom">Prénom</label>
        <input
          id="prenom"
          name="prenom"
          type="text"
          [(ngModel)]="prenom"
          required
        />

        <label for="categorieMembre">Catégorie</label>
        <select
          id="categorieMembre"
          name="categorieMembre"
          [(ngModel)]="categorieMembre"
          (ngModelChange)="mettreAJourSiteRattachement()"
        >
          <option value="GLOBAL">Global</option>
          <option value="SITE">Site</option>
          <option value="LIBRE">Libre</option>
        </select>

        @if (categorieMembre === 'SITE') {
          <label for="siteRattachementId">Site de rattachement</label>
          <select
            id="siteRattachementId"
            name="siteRattachementId"
            [(ngModel)]="siteRattachementId"
            [disabled]="chargementSites || sites.length === 0"
          >
            <option *ngFor="let site of sites" [ngValue]="site.siteId">
              {{ site.nom }} ({{ site.siteId }})
            </option>
          </select>
        }

        <div class="bloc-info">
          <h3>Résumé de la demande</h3>

          <p><strong>Nom :</strong> {{ nom || 'Non renseigné' }}</p>
          <p><strong>Prénom :</strong> {{ prenom || 'Non renseigné' }}</p>
          <p><strong>Catégorie :</strong> {{ enumLabel(categorieMembre) }}</p>

          @if (categorieMembre === 'SITE') {
            <p><strong>Site :</strong> {{ nomSiteSelectionne() }}</p>
          }
        </div>

        <button type="submit" [disabled]="chargement || (categorieMembre === 'SITE' && chargementSites)">
          {{ chargement ? 'Envoi...' : 'Envoyer la demande' }}
        </button>
      </form>

      @if (messageErreur) {
        <p class="erreur">
          {{ messageErreur }}
        </p>
      }

      @if (membreCree) {
        <div class="resultat">
          <h3>Joueur créé avec succès</h3>

          <div class="resume-grid">
            <p><strong>Matricule</strong><br>{{ membreCree.matricule }}</p>
            <p><strong>Nom</strong><br>{{ membreCree.nom }}</p>
            <p><strong>Prénom</strong><br>{{ membreCree.prenom }}</p>
            <p><strong>Catégorie</strong><br>{{ enumLabel(membreCree.categorieMembre) }}</p>
            <p><strong>Actif</strong><br>{{ membreCree.actif ? 'Oui' : 'Non' }}</p>
          </div>

          <p>
            Le joueur peut maintenant se connecter avec le matricule
            <strong>{{ membreCree.matricule }}</strong>.
          </p>

          <a routerLink="/joueur" class="lien-action">
            Aller à la connexion joueur
          </a>
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

    .lien-action {
      display: inline-block;
      margin-top: 12px;
      font-weight: 600;
    }
  `]
})
export class InscriptionJoueurComponent implements OnInit {
  readonly enumLabel = enumLabel;
  sites: SiteResponse[] = [];

  nom = '';
  prenom = '';
  categorieMembre: CategorieMembre = 'GLOBAL';
  siteRattachementId: number | null = null;

  chargementSites = false;
  chargement = false;
  messageErreur = '';
  membreCree: MembreResponse | null = null;

  constructor(
    private readonly membreApiService: MembreApiService,
    private readonly siteApiService: SiteApiService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {
  }

  ngOnInit(): void {
    this.chargerSites();
  }

  private chargerSites(): void {
    this.chargementSites = true;

    this.siteApiService.listerSitesActifs().subscribe({
      next: (sites) => {
        this.sites = sites;
        this.mettreAJourSiteRattachement();
        this.chargementSites = false;
        this.changeDetectorRef.detectChanges();
      },
      error: (error) => {
        this.messageErreur = extraireMessageErreur(error);
        this.sites = [];
        this.siteRattachementId = null;
        this.chargementSites = false;
        this.changeDetectorRef.detectChanges();
      }
    });
  }

  mettreAJourSiteRattachement(): void {
    if (this.categorieMembre !== 'SITE') {
      this.siteRattachementId = null;
      return;
    }

    const siteSelectionExiste = this.siteRattachementId !== null
      && this.sites.some(site => site.siteId === this.siteRattachementId);

    if (!siteSelectionExiste) {
      this.siteRattachementId = this.sites.length > 0
        ? this.sites[0].siteId
        : null;
    }
  }

  envoyerDemande(): void {
    this.messageErreur = '';
    this.membreCree = null;

    if (!this.nom.trim() || !this.prenom.trim()) {
      this.messageErreur = 'Le nom et le prénom sont obligatoires.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    const siteRattachement = this.categorieMembre === 'SITE'
      ? this.sites.find(site => site.siteId === Number(this.siteRattachementId))
      : undefined;

    if (this.categorieMembre === 'SITE' && !siteRattachement) {
      this.messageErreur = 'Sélectionne un site valide pour une inscription SITE.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    const siteRattachementId = this.categorieMembre === 'SITE'
      ? siteRattachement!.siteId
      : null;

    const request: InscriptionMembreRequest = {
      nom: this.nom.trim(),
      prenom: this.prenom.trim(),
      categorieMembre: this.categorieMembre,
      siteRattachementId
    };

    this.chargement = true;
    this.changeDetectorRef.detectChanges();

    this.membreApiService.inscrireMembre(request).subscribe({
      next: (response) => {
        this.membreCree = response;
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
    const site = this.sites.find(site => site.siteId === Number(this.siteRattachementId));

    if (!site) {
      return 'Site inconnu';
    }

    return `${site.nom} (${site.siteId})`;
  }
}
