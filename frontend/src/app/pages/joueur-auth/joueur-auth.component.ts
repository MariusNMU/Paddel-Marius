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

      <div *ngIf="joueurConnecte" class="resultat">
        <h3>Joueur connecté</h3>

        <p><strong>Matricule :</strong> {{ joueurConnecte.matricule }}</p>
        <p><strong>Nom :</strong> {{ joueurConnecte.prenom }} {{ joueurConnecte.nom }}</p>
        <p><strong>Catégorie :</strong> {{ joueurConnecte.categorieMembre }}</p>

        <p *ngIf="joueurConnecte.nomSiteRattachement">
          <strong>Site de rattachement :</strong>
          {{ joueurConnecte.nomSiteRattachement }} ({{ joueurConnecte.siteRattachementId }})
        </p>

        <div class="actions">
          <button type="button" (click)="deconnecter()">Déconnecter</button>
        </div>
      </div>
    </section>
  `
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

