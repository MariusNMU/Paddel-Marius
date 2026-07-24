import { CommonModule } from '@angular/common';
import {
  Component,
  OnInit
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { InscriptionJoueurFacadeService } from '../../services/inscription-joueur-facade.service';
import { enumLabel } from '../../shared/enum-label.util';

@Component({
  selector: 'app-inscription-joueur',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
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

      <div class="bloc-info">
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
      </div>

      <form
        (ngSubmit)="facade.envoyerDemande()"
      >
        <label for="nom">Nom</label>

        <input
          id="nom"
          name="nom"
          type="text"
          [ngModel]="facade.nom()"
          (ngModelChange)="
            facade.modifierNom($event)
          "
          required
        >

        <label for="prenom">Prénom</label>

        <input
          id="prenom"
          name="prenom"
          type="text"
          [ngModel]="facade.prenom()"
          (ngModelChange)="
            facade.modifierPrenom($event)
          "
          required
        >

        <label for="motDePasse">
          Mot de passe
        </label>

        <input
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

        <p class="aide">
          Entre 12 et 72 caractères.
        </p>

        <label for="confirmationMotDePasse">
          Confirmer le mot de passe
        </label>

        <input
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

        <label for="categorieMembre">
          Catégorie
        </label>

        <select
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

        @if (
          facade.categorieMembre()
          === 'SITE'
        ) {
          <label for="siteRattachementId">
            Site de rattachement
          </label>

          <select
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
        }

        <div class="bloc-info">
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
        </div>

        <button
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
        <div class="resultat">
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
            routerLink="/joueur"
            class="lien-action"
          >
            Aller à la connexion joueur
          </a>
        </div>
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
