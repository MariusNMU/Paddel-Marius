import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  CategorieMembre,
  InscriptionMembreRequest,
  MembreResponse
} from '../../models/membre.model';
import { MembreApiService } from '../../services/membre-api.service';
import { extraireMessageErreur } from '../../shared/api-error.util';

interface SiteDemo {
  id: number;
  nom: string;
}

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
          <li><strong>GLOBAL</strong> : matricule Gxxxx, peut réserver sur tous les sites.</li>
          <li><strong>SITE</strong> : matricule Sxxxx, rattaché à un site précis.</li>
          <li><strong>LIBRE</strong> : matricule Lxxxx, accès libre selon les règles métier.</li>
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
        >
          <option value="GLOBAL">GLOBAL</option>
          <option value="SITE">SITE</option>
          <option value="LIBRE">LIBRE</option>
        </select>

        @if (categorieMembre === 'SITE') {
          <label for="siteRattachementId">Site de rattachement</label>
          <select
            id="siteRattachementId"
            name="siteRattachementId"
            [(ngModel)]="siteRattachementId"
          >
            <option [ngValue]="1001">Padel Bruxelles (1001)</option>
            <option [ngValue]="1002">Padel Namur (1002)</option>
          </select>
        }

        <div class="bloc-info">
          <h3>Résumé de la demande</h3>

          <p><strong>Nom :</strong> {{ nom || 'Non renseigné' }}</p>
          <p><strong>Prénom :</strong> {{ prenom || 'Non renseigné' }}</p>
          <p><strong>Catégorie :</strong> {{ categorieMembre }}</p>

          @if (categorieMembre === 'SITE') {
            <p><strong>Site :</strong> {{ nomSiteSelectionne() }}</p>
          }
        </div>

        <button type="submit" [disabled]="chargement">
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
            <p><strong>Catégorie</strong><br>{{ membreCree.categorieMembre }}</p>
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
export class InscriptionJoueurComponent {
  sites: SiteDemo[] = [
    { id: 1001, nom: 'Padel Bruxelles' },
    { id: 1002, nom: 'Padel Namur' }
  ];

  nom = '';
  prenom = '';
  categorieMembre: CategorieMembre = 'GLOBAL';
  siteRattachementId = 1001;

  chargement = false;
  messageErreur = '';
  membreCree: MembreResponse | null = null;

  constructor(
    private readonly membreApiService: MembreApiService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {
  }

  envoyerDemande(): void {
    this.messageErreur = '';
    this.membreCree = null;

    if (!this.nom.trim() || !this.prenom.trim()) {
      this.messageErreur = 'Le nom et le prénom sont obligatoires.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    const request: InscriptionMembreRequest = {
      nom: this.nom.trim(),
      prenom: this.prenom.trim(),
      categorieMembre: this.categorieMembre,
      siteRattachementId: this.categorieMembre === 'SITE'
        ? Number(this.siteRattachementId)
        : null
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
    const site = this.sites.find(siteDemo => siteDemo.id === Number(this.siteRattachementId));

    if (!site) {
      return 'Site inconnu';
    }

    return `${site.nom} (${site.id})`;
  }
}
