import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApiService } from '../../services/auth-api.service';
import { AuthContextService } from '../../services/auth-context.service';
import { extraireMessageErreur } from '../../shared/api-error.util';
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

      <p *ngIf="messageSucces" class="succes">
        {{ messageSucces }}
      </p>

      <p *ngIf="messageErreur" class="erreur">
        {{ messageErreur }}
      </p>

      <ng-container *ngIf="authContext.joueur() as joueur; else formulaireConnexion">
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
            <strong>{{ joueur.nomSiteRattachement }} ({{ joueur.siteRattachementId }})</strong>
          </p>

          <div class="actions">
            <button type="button" (click)="deconnecter()">Déconnecter</button>
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

          <button type="submit" [disabled]="chargement">
            {{ chargement ? 'Connexion...' : 'Se connecter' }}
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
export class JoueurAuthComponent {
  readonly enumLabel = enumLabel;
  matricule = '';
  motDePasse = '';
  chargement = false;
  messageErreur = '';
  messageSucces = '';

  constructor(
    private readonly authApiService: AuthApiService,
    readonly authContext: AuthContextService,
    private readonly changeDetectorRef: ChangeDetectorRef,
    private readonly router: Router
  ) {
  }

  connecterJoueur(): void {
    this.messageErreur = '';
    this.messageSucces = '';

    const matriculeNettoye = this.matricule.trim();
    const motDePasseNettoye = this.motDePasse.trim();

    if (!matriculeNettoye || !motDePasseNettoye) {
      this.messageErreur = 'Le matricule et le mot de passe sont obligatoires.';
      this.changeDetectorRef.detectChanges();
      return;
    }

    this.chargement = true;
    this.changeDetectorRef.detectChanges();

    this.authApiService.connecterJoueur({
      matricule: matriculeNettoye,
      motDePasse: motDePasseNettoye
    }).subscribe({
      next: (joueur) => {
        this.authContext.definirJoueur(joueur);
        this.messageSucces = `Joueur connecté : ${joueur.prenom} ${joueur.nom} (${joueur.matricule}).`;
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
    const joueur = this.authContext.joueur();

    this.authContext.deconnecterJoueur();
    this.messageErreur = '';
    this.messageSucces = joueur
      ? `Joueur déconnecté : ${joueur.prenom} ${joueur.nom} (${joueur.matricule}).`
      : 'Joueur déconnecté.';

    this.changeDetectorRef.detectChanges();
    void this.router.navigate(['/accueil']);
  }
}
