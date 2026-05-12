import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  MatchPublicResponse,
  RejoindreMatchPublicResponse
} from '../../models/match-public.model';
import { AuthContextService } from '../../services/auth-context.service';
import { MatchPublicApiService } from '../../services/match-public-api.service';
import { extraireMessageErreur } from '../../shared/api-error.util';

interface SiteDemo {
  id: number;
  nom: string;
}

@Component({
  selector: 'app-matches-publics',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page">
      <h2>Rejoindre un match public</h2>

      <p>
        Retrouve les matches publics disponibles et rejoins une place en payant directement 15 €.
        Premier payé = premier servi.
      </p>

      <div class="bloc-info">
        <h3>Règles métier</h3>

        <ul>
          <li>Un match public peut être rejoint par les joueurs disponibles.</li>
          <li>Un match contient maximum 4 joueurs.</li>
          <li>La place est validée uniquement après paiement.</li>
          <li>Le paiement débite ton solde crédit de 15 €.</li>
        </ul>
      </div>

      @if (!joueurConnecte()) {
        <p class="erreur">
          Connecte-toi d'abord comme joueur pour rejoindre un match public.
        </p>
      }

      @if (joueurConnecte()) {
        <div class="bloc-info">
          <h3>Joueur connecté</h3>
          <p><strong>Matricule :</strong> {{ joueurConnecte()?.matricule }}</p>
          <p><strong>Nom :</strong> {{ joueurConnecte()?.nom }} {{ joueurConnecte()?.prenom }}</p>
        </div>
      }

      <form (ngSubmit)="rechercherMatchesPublics()">
        <label for="siteId">Site</label>
        <select id="siteId" name="siteId" [(ngModel)]="siteId">
          <option *ngFor="let site of sites" [ngValue]="site.id">
            {{ site.nom }} ({{ site.id }})
          </option>
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
          {{ chargement ? 'Recherche...' : 'Rechercher les matches publics' }}
        </button>
      </form>

      @if (messageErreur) {
        <p class="erreur">{{ messageErreur }}</p>
      }

      @if (messageSucces) {
        <div class="resultat">
          <h3>Inscription confirmée</h3>
          <p>{{ messageSucces }}</p>

          @if (dernierPaiement) {
            <div class="resume-grid">
              <p><strong>Match</strong><br>{{ dernierPaiement.matchId }}</p>
              <p><strong>Participation</strong><br>{{ dernierPaiement.participationId }}</p>
              <p><strong>Paiement</strong><br>{{ dernierPaiement.paiementId }}</p>
              <p><strong>Montant payé</strong><br>{{ dernierPaiement.montantPaye | number:'1.2-2' }} €</p>
              <p><strong>Solde restant</strong><br>{{ dernierPaiement.soldeRestant | number:'1.2-2' }} €</p>
            </div>
          }
        </div>
      }

      @if (matches.length === 0 && rechercheEffectuee && !chargement) {
        <p>
          Aucun match public disponible pour cette date et ce site.
        </p>
      }

      @if (matches.length > 0) {
        <div class="matches-grid">
          <article *ngFor="let match of matches" class="match-card">
            <h3>{{ match.nomSite }} — Terrain {{ match.numeroTerrain }}</h3>

            <p><strong>Début :</strong> {{ match.dateHeureDebut }}</p>
            <p><strong>Fin :</strong> {{ match.dateHeureFin }}</p>
            <p><strong>Participants :</strong> {{ match.nombreParticipantsActifs }} / 4</p>
            <p><strong>Places disponibles :</strong> {{ match.placesDisponibles }}</p>
            <p><strong>Prix total :</strong> {{ match.prixTotal | number:'1.2-2' }} €</p>
            <p><strong>Ta place :</strong> {{ match.montantParticipation | number:'1.2-2' }} €</p>

            <button
              type="button"
              [disabled]="chargementPaiement || !joueurConnecte()"
              (click)="rejoindreEtPayer(match)"
            >
              {{ chargementPaiement ? 'Paiement...' : 'Rejoindre et payer 15 €' }}
            </button>
          </article>
        </div>
      }
    </section>
  `,
  styles: [`
    .matches-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
      gap: 16px;
      margin-top: 18px;
    }

    .match-card {
      border: 1px solid #bfdbfe;
      border-radius: 12px;
      background: #ffffff;
      padding: 16px;
      box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
    }

    .match-card h3 {
      margin: 0 0 12px;
      color: #003b95;
    }

    .match-card p {
      margin: 8px 0;
    }

    .match-card button {
      margin-top: 12px;
    }

    .resume-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
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
export class MatchesPublicsComponent {
  sites: SiteDemo[] = [
    { id: 1001, nom: 'Padel Bruxelles' },
    { id: 1002, nom: 'Padel Namur' }
  ];

  siteId = 1001;
  date = '2026-06-20';

  matches: MatchPublicResponse[] = [];
  dernierPaiement: RejoindreMatchPublicResponse | null = null;

  chargement = false;
  chargementPaiement = false;
  rechercheEffectuee = false;
  messageErreur = '';
  messageSucces = '';

  constructor(
    private readonly matchPublicApiService: MatchPublicApiService,
    private readonly authContextService: AuthContextService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {
  }

  joueurConnecte() {
    return this.authContextService.joueur();
  }

  rechercherMatchesPublics(): void {
    this.messageErreur = '';
    this.messageSucces = '';
    this.dernierPaiement = null;
    this.rechercheEffectuee = true;

    if (!this.siteId || !this.date) {
      this.messageErreur = 'Le site et la date sont obligatoires.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    this.chargement = true;
    this.changeDetectorRef.detectChanges();

    this.matchPublicApiService.listerMatchesPublics(this.siteId, this.date).subscribe({
      next: (response) => {
        this.matches = response;
        this.chargement = false;
        this.changeDetectorRef.detectChanges();
      },
      error: (error) => {
        this.messageErreur = extraireMessageErreur(error);
        this.matches = [];
        this.chargement = false;
        this.changeDetectorRef.detectChanges();
      }
    });
  }

  rejoindreEtPayer(match: MatchPublicResponse): void {
    this.messageErreur = '';
    this.messageSucces = '';
    this.dernierPaiement = null;

    const joueur = this.joueurConnecte();

    if (!joueur) {
      this.messageErreur = 'Connecte-toi comme joueur pour rejoindre un match public.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    this.chargementPaiement = true;
    this.changeDetectorRef.detectChanges();

    this.matchPublicApiService.rejoindreEtPayer(match.matchId, {
      matriculeJoueur: joueur.matricule
    }).subscribe({
      next: (response) => {
        this.dernierPaiement = response;
        this.messageSucces =
          `Le joueur ${response.matriculeJoueur} a rejoint le match public et payé ${response.montantPaye.toFixed(2)} €.`;
        this.chargementPaiement = false;
        this.rechercherMatchesPublics();
        this.changeDetectorRef.detectChanges();
      },
      error: (error) => {
        this.messageErreur = extraireMessageErreur(error);
        this.chargementPaiement = false;
        this.changeDetectorRef.detectChanges();
      }
    });
  }
}
