import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthFacadeService } from '../../services/auth-facade.service';
import { enumLabel } from '../../shared/enum-label.util';

@Component({
  selector: 'app-joueur-auth',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <section class="page">
      <h2>Connexion joueur</h2>

      <p>
        Connexion joueur avec un matricule et un mot de passe valides.
      </p>

      <p>
        <a routerLink="/accueil">Retour à la Homepage</a>
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
            <strong>
              {{ joueur.nomSiteRattachement }}
              ({{ joueur.siteRattachementId }})
            </strong>
          </p>

          <div class="actions">
            <button type="button" (click)="deconnecter()">
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

          <a routerLink="/accueil" class="lien-action">
            Voir les informations de démonstration
          </a>
        </div>

        <form (ngSubmit)="connecterJoueur()">
          <label for="matricule">Matricule</label>
          <input
            id="matricule"
            name="matricule"
            type="text"
            [(ngModel)]="matricule"
            placeholder="Votre matricule"
            required
          />

          <label for="motDePasse">Mot de passe</label>
          <input
            id="motDePasse"
            name="motDePasse"
            type="password"
            [(ngModel)]="motDePasse"
            placeholder="Votre mot de passe"
            required
          />

          <button
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
