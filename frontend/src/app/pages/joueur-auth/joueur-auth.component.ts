import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthJoueurResponse } from '../../models/auth.model';
import { AuthApiService } from '../../services/auth-api.service';
import { AuthContextService } from '../../services/auth-context.service';
import { extraireMessageErreur } from '../../shared/api-error.util';

@Component({
  selector: 'app-joueur-auth',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <section class="page">
      <h2>Connexion joueur</h2>

      <p>
        Connexion par matricule uniquement. Aucun login ni mot de passe n'est demandé au joueur.
      </p>

      <p>
        <a routerLink="/accueil">Retour à la Homepage</a>
      </p>

      <div *ngIf="joueurConnecte" class="bloc-info">
        <h3>Joueur déjà connecté</h3>

        <p>
          <strong>{{ joueurConnecte.prenom }} {{ joueurConnecte.nom }}</strong>
          — matricule {{ joueurConnecte.matricule }}
        </p>

        <p>
          Catégorie :
          <strong>{{ joueurConnecte.categorieMembre }}</strong>
        </p>

        <p *ngIf="joueurConnecte.nomSiteRattachement">
          Site de rattachement :
          <strong>{{ joueurConnecte.nomSiteRattachement }} ({{ joueurConnecte.siteRattachementId }})</strong>
        </p>

        <div class="actions">
          <a routerLink="/joueur/disponibilites">Réserver un terrain</a>
          <a routerLink="/joueur/creer-match">Créer un match</a>
          <a routerLink="/joueur/mes-dettes">Voir mes dettes</a>
          <button type="button" (click)="deconnecter()">Déconnecter</button>
        </div>
      </div>

      <div class="bloc-info">
        <h3>Joueurs de démonstration</h3>

        <div class="joueurs-demo-grid">
          <article class="joueur-demo-card">
            <h4>Joueur global</h4>
            <p>
              Peut réserver sur tous les sites selon les règles métier.
            </p>
            <p><strong>Matricule :</strong> G1001</p>
            <p><strong>Statut :</strong> actif</p>

            <button type="button" (click)="utiliserG1001()">
              Utiliser G1001
            </button>
          </article>

          <article class="joueur-demo-card">
            <h4>Joueur avec dette</h4>
            <p>
              Permet de tester l'écran Mes dettes et le paiement d'une dette.
            </p>
            <p><strong>Matricule :</strong> G1002</p>
            <p><strong>Statut :</strong> actif avec dette ouverte</p>

            <button type="button" (click)="utiliserG1002()">
              Utiliser G1002
            </button>
          </article>

          <article class="joueur-demo-card warning-card">
            <h4>Joueur inactif</h4>
            <p>
              Permet de tester le refus backend lors de la connexion joueur.
            </p>
            <p><strong>Matricule :</strong> G9999</p>
            <p><strong>Statut :</strong> inactif</p>

            <button type="button" (click)="utiliserG9999()">
              Utiliser G9999
            </button>
          </article>
        </div>
      </div>

      <form (ngSubmit)="connecterJoueur()">
        <label for="matricule">Matricule</label>
        <input
          id="matricule"
          name="matricule"
          type="text"
          [(ngModel)]="matricule"
          placeholder="Exemple : G1001"
          required
        />

        <button type="submit" [disabled]="chargement">
          {{ chargement ? 'Connexion...' : 'Se connecter' }}
        </button>
      </form>

      <p *ngIf="messageErreur" class="erreur">
        {{ messageErreur }}
      </p>
    </section>
  `,
  styles: [`
    .joueurs-demo-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
      gap: 16px;
      margin-top: 14px;
    }

    .joueur-demo-card {
      padding: 16px;
      border: 1px solid #bfdbfe;
      border-radius: 12px;
      background: #ffffff;
      box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
    }

    .joueur-demo-card h4 {
      margin: 0 0 10px;
      color: #003b95;
    }

    .joueur-demo-card p {
      margin: 8px 0;
    }

    .joueur-demo-card button {
      margin-top: 12px;
    }

    .warning-card {
      border-color: #fecaca;
      background: #fff7f7;
    }

    .warning-card h4 {
      color: #991b1b;
    }
  `]
})
export class JoueurAuthComponent {
  matricule = 'G1001';
  chargement = false;
  messageErreur = '';
  joueurConnecte: AuthJoueurResponse | null = null;

  constructor(
    private readonly authApiService: AuthApiService,
    private readonly authContext: AuthContextService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {
    this.joueurConnecte = this.authContext.joueur();
  }

  utiliserG1001(): void {
    this.matricule = 'G1001';
    this.messageErreur = '';
  }

  utiliserG1002(): void {
    this.matricule = 'G1002';
    this.messageErreur = '';
  }

  utiliserG9999(): void {
    this.matricule = 'G9999';
    this.messageErreur = '';
  }

  connecterJoueur(): void {
    this.messageErreur = '';

    const matriculeNettoye = this.matricule.trim();

    if (!matriculeNettoye) {
      this.messageErreur = 'Le matricule est obligatoire.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    this.chargement = true;
    this.changeDetectorRef.detectChanges();

    this.authApiService.connecterJoueur({ matricule: matriculeNettoye }).subscribe({
      next: (joueur) => {
        this.authContext.definirJoueur(joueur);
        this.joueurConnecte = joueur;
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

  deconnecter(): void {
    this.authContext.deconnecterJoueur();
    this.joueurConnecte = null;
    this.changeDetectorRef.detectChanges();
  }
}
