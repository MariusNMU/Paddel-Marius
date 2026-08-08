import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { RouterLink } from '@angular/router';
import { AuthFacadeService } from '../../services/auth-facade.service';
import { enumLabel } from '../../shared/enum-label.util';

@Component({
  selector: 'app-joueur-auth',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule
  ],
  template: `
    <section class="page">
      <h2>Connexion joueur</h2>

      <p>
        Connexion joueur avec un matricule et un mot de passe valides.
      </p>

      <p>
        <a mat-button routerLink="/accueil">
          Retour à la Homepage
        </a>
      </p>

      <p *ngIf="authFacade.messageSuccesJoueur()" class="succes">
        {{ authFacade.messageSuccesJoueur() }}
      </p>

      <p *ngIf="authFacade.messageErreurJoueur()" class="erreur">
        {{ authFacade.messageErreurJoueur() }}
      </p>

      <ng-container *ngIf="authFacade.joueur() as joueur; else formulaireConnexion">
        <div class="bloc-info">
          <h3>Joueur connecté</h3>

          <p>
            <strong>{{ joueur.prenom }} {{ joueur.nom }}</strong>
            — matricule {{ joueur.matricule }}
          </p>

          <p>
            Catégorie :
            <strong>{{ enumLabel(joueur.categorieMembre) }}</strong>
          </p>

          <p *ngIf="joueur.nomSiteRattachement">
            Site de rattachement :
            <strong data-testid="site-rattachement">
              {{ joueur.nomSiteRattachement }}
            </strong>
          </p>

          <div class="actions">
            <button
              mat-stroked-button
              type="button"
              (click)="deconnecter()"
            >
              Déconnecter
            </button>
          </div>
        </div>
      </ng-container>

      <ng-template #formulaireConnexion>
        <div class="bloc-info">
          <h3>Compte de test</h3>

          <p>
            Les comptes de démonstration restent temporairement indiqués sur la homepage.
            Cette page de connexion ne préremplit plus d'identifiants.
          </p>

          <a
            mat-button
            routerLink="/accueil"
            class="lien-action"
          >
            Voir les informations de démonstration
          </a>
        </div>

        <form
          class="formulaire-connexion"
          (ngSubmit)="connecterJoueur()"
        >
          <mat-form-field appearance="outline">
            <mat-label>Matricule</mat-label>

            <input
              matInput
              id="matricule"
              name="matricule"
              type="text"
              [(ngModel)]="matricule"
              autocomplete="username"
              maxlength="10"
              required
            />
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Mot de passe</mat-label>

            <input
              matInput
              id="motDePasse"
              name="motDePasse"
              type="password"
              [(ngModel)]="motDePasse"
              autocomplete="current-password"
              maxlength="72"
              required
            />
          </mat-form-field>

          <button
            mat-flat-button
            type="submit"
            [disabled]="authFacade.chargementJoueur()"
          >
            {{
              authFacade.chargementJoueur()
                ? 'Connexion...'
                : 'Se connecter'
            }}
          </button>
        </form>
      </ng-template>
    </section>
  `,
  styles: [`
    .succes {
      margin-top: 16px;
      color: #047857;
      font-weight: 700;
    }

    .lien-action {
      display: inline-block;
      margin-top: 12px;
      font-weight: 600;
    }

    .formulaire-connexion {
      display: flex;
      max-width: 420px;
      flex-direction: column;
    }

    .formulaire-connexion mat-form-field {
      width: 100%;
    }

    .formulaire-connexion button {
      align-self: flex-start;
    }
  `]
})
export class JoueurAuthComponent implements OnInit {
  readonly enumLabel = enumLabel;

  matricule = '';
  motDePasse = '';

  constructor(
    readonly authFacade: AuthFacadeService
  ) {
  }

  ngOnInit(): void {
    this.authFacade.preparerConnexionJoueur();
  }

  connecterJoueur(): void {
    this.authFacade.connecterJoueur(
      this.matricule,
      this.motDePasse
    );
  }

  deconnecter(): void {
    this.authFacade.deconnecterJoueur();
  }
}
