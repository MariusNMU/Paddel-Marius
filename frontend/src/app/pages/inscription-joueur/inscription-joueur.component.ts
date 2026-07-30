import { CommonModule } from '@angular/common';
import {
  Component,
  OnInit
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { RouterLink } from '@angular/router';
import { InscriptionJoueurFacadeService } from '../../services/inscription-joueur-facade.service';
import { enumLabel } from '../../shared/enum-label.util';

@Component({
  selector: 'app-inscription-joueur',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    RouterLink
  ],
  providers: [
    InscriptionJoueurFacadeService
  ],
  template: `
    <section class="page">
      <h2>Inscription joueur</h2>

      <p>
        Crée un nouveau joueur dans le système.
        Le matricule est généré automatiquement
        par le backend selon la catégorie choisie.
      </p>

      <mat-card
        appearance="outlined"
        class="bloc-info"
      >
        <h3>Catégories disponibles</h3>

        <ul>
          <li>
            <strong>Global</strong> :
            matricule Gxxxx, peut réserver sur
            tous les sites.
          </li>

          <li>
            <strong>Site</strong> :
            matricule Sxxxx, rattaché à un site
            précis.
          </li>

          <li>
            <strong>Libre</strong> :
            matricule Lxxxx, accès libre selon
            les règles métier.
          </li>
        </ul>
      </mat-card>

      <form
        (ngSubmit)="facade.envoyerDemande()"
      >
        <mat-form-field appearance="outline">
          <mat-label>Nom</mat-label>
          <input
            matInput
            id="nom"
            name="nom"
            type="text"
            [ngModel]="facade.nom()"
            (ngModelChange)="
              facade.modifierNom($event)
            "
            required
          >
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Prénom</mat-label>
          <input
            matInput
            id="prenom"
            name="prenom"
            type="text"
            [ngModel]="facade.prenom()"
            (ngModelChange)="
              facade.modifierPrenom($event)
            "
            required
          >
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Mot de passe</mat-label>
          <input
            matInput
            id="motDePasse"
            name="motDePasse"
            type="password"
            autocomplete="new-password"
            [ngModel]="facade.motDePasse()"
            (ngModelChange)="
              facade.modifierMotDePasse($event)
            "
            required
            minlength="12"
            maxlength="72"
          >
          <mat-hint>
            Entre 12 et 72 caractères.
          </mat-hint>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>
            Confirmer le mot de passe
          </mat-label>
          <input
            matInput
            id="confirmationMotDePasse"
            name="confirmationMotDePasse"
            type="password"
            autocomplete="new-password"
            [ngModel]="
              facade.confirmationMotDePasse()
            "
            (ngModelChange)="
              facade
                .modifierConfirmationMotDePasse(
                  $event
                )
            "
            required
            minlength="12"
            maxlength="72"
          >
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Catégorie</mat-label>
          <select
            matNativeControl
            id="categorieMembre"
            name="categorieMembre"
            [ngModel]="
              facade.categorieMembre()
            "
            (ngModelChange)="
              facade.modifierCategorieMembre(
                $event
              )
            "
          >
            <option value="GLOBAL">
              Global
            </option>

            <option value="SITE">
              Site
            </option>

            <option value="LIBRE">
              Libre
            </option>
          </select>
        </mat-form-field>

        @if (
          facade.categorieMembre()
          === 'SITE'
          ) {
          <mat-form-field appearance="outline">
            <mat-label>
              Site de rattachement
            </mat-label>
            <select
              matNativeControl
              id="siteRattachementId"
              name="siteRattachementId"
              [ngModel]="
                facade.siteRattachementId()
              "
              (ngModelChange)="
                facade
                  .modifierSiteRattachementId(
                    $event
                  )
              "
              [disabled]="
                facade.chargementSites()
                || facade.sites().length === 0
              "
            >
              <option
                *ngFor="
                  let site of facade.sites()
                "
                [ngValue]="site.siteId"
              >
                {{ site.nom }}
              </option>
            </select>
          </mat-form-field>
        }

        <mat-card
          appearance="outlined"
          class="bloc-info"
        >
          <h3>Résumé de la demande</h3>

          <p>
            <strong>Nom :</strong>
            {{
              facade.nom()
              || 'Non renseigné'
            }}
          </p>

          <p>
            <strong>Prénom :</strong>
            {{
              facade.prenom()
              || 'Non renseigné'
            }}
          </p>

          <p>
            <strong>Catégorie :</strong>
            {{
              enumLabel(
                facade.categorieMembre()
              )
            }}
          </p>

          @if (
            facade.categorieMembre()
            === 'SITE'
            ) {
            <p>
              <strong>Site :</strong>
              {{
                facade.nomSiteSelectionne()
              }}
            </p>
          }
        </mat-card>

        <button
          mat-flat-button
          type="submit"
          [disabled]="
            facade.chargement()
            || (
              facade.categorieMembre()
                === 'SITE'
              && facade.chargementSites()
            )
          "
        >
          {{
            facade.chargement()
              ? 'Envoi...'
              : 'Envoyer la demande'
          }}
        </button>
      </form>

      @if (facade.messageErreur()) {
        <p class="erreur">
          {{ facade.messageErreur() }}
        </p>
      }

      @if (
        facade.membreCree();
        as membreCree
        ) {
        <mat-card
          appearance="outlined"
          class="resultat"
        >
          <h3>
            Joueur créé avec succès
          </h3>

          <div class="resume-grid">
            <p>
              <strong>Matricule</strong>
              <br>
              {{ membreCree.matricule }}
            </p>

            <p>
              <strong>Nom</strong>
              <br>
              {{ membreCree.nom }}
            </p>

            <p>
              <strong>Prénom</strong>
              <br>
              {{ membreCree.prenom }}
            </p>

            <p>
              <strong>Catégorie</strong>
              <br>
              {{
                enumLabel(
                  membreCree
                    .categorieMembre
                )
              }}
            </p>

            <p>
              <strong>Actif</strong>
              <br>
              {{
                membreCree.actif
                  ? 'Oui'
                  : 'Non'
              }}
            </p>
          </div>

          <p>
            Le joueur peut maintenant se
            connecter avec le matricule
            <strong>
              {{ membreCree.matricule }}
            </strong>
            et le mot de passe qu’il vient
            de choisir.
          </p>

          <a
            mat-flat-button
            routerLink="/joueur"
            class="lien-action"
          >
            Aller à la connexion joueur
          </a>
        </mat-card>
      }
    </section>
  `,
  styles: [`
    .resume-grid {
      display: grid;
      grid-template-columns:
        repeat(
          auto-fit,
          minmax(180px, 1fr)
        );
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

    form mat-form-field {
      width: 100%;
    }
  `]
})
export class InscriptionJoueurComponent
  implements OnInit {
  readonly enumLabel = enumLabel;

  constructor(
    readonly facade:
    InscriptionJoueurFacadeService
  ) {
  }

  ngOnInit(): void {
    this.facade.initialiser();
  }
}
