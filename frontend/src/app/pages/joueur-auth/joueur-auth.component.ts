import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
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
        Connexion joueur avec le matricule et le mot de passe de démonstration.
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
            <strong>{{ joueur.categorieMembre }}</strong>
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
          <h3>Joueurs de démonstration</h3>

          <div class="joueurs-demo-grid">
            <article class="joueur-demo-card">
              <h4>Joueur global</h4>
              <p>
                Peut réserver sur tous les sites selon les règles métier.
              </p>
              <p><strong>Matricule :</strong> G1001</p>
              <p><strong>Mot de passe :</strong> password</p>
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
              <p><strong>Mot de passe :</strong> password</p>
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
              <p><strong>Mot de passe :</strong> password</p>
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

          <label for="motDePasse">Mot de passe</label>
          <input
            id="motDePasse"
            name="motDePasse"
            type="password"
            [(ngModel)]="motDePasse"
            placeholder="password"
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

    .succes {
      margin-top: 16px;
      color: #047857;
      font-weight: 700;
    }
  `]
})
export class JoueurAuthComponent {
  matricule = 'G1001';
  motDePasse = 'password';
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

  utiliserG1001(): void {
    this.matricule = 'G1001';
    this.motDePasse = 'password';
    this.messageErreur = '';
    this.messageSucces = '';
  }

  utiliserG1002(): void {
    this.matricule = 'G1002';
    this.motDePasse = 'password';
    this.messageErreur = '';
    this.messageSucces = '';
  }

  utiliserG9999(): void {
    this.matricule = 'G9999';
    this.motDePasse = 'password';
    this.messageErreur = '';
    this.messageSucces = '';
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
