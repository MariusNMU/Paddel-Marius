import {
  Component,
  OnInit
} from '@angular/core';
import { AccueilFacadeService } from '../../services/accueil-facade.service';

@Component({
  selector: 'app-accueil',
  standalone: true,
  providers: [AccueilFacadeService],
  template: `
    <section class="page">
      <h2>Homepage</h2>

      <p>
        Bienvenue dans l'application Padel Marius.
        Cette interface permet de démontrer le MVP de réservation
        de terrains de padel : connexion joueur, organisation de
        match, inscription publique, paiement, dette, solde,
        statistiques et administration.
      </p>

      @if (facade.chargement()) {
        <p>
          Chargement des données de démonstration...
        </p>
      }

      @if (facade.messageErreur()) {
        <div class="bloc-info">
          <p class="erreur">
            {{ facade.messageErreur() }}
          </p>

          <button
            type="button"
            (click)="facade.reessayer()"
          >
            Réessayer
          </button>
        </div>
      }

      @if (
        facade.donneesDemonstration();
        as donneesDemonstration
      ) {
        <div class="bloc-info">
          <h3>Légende des matricules</h3>

          <table>
            <tbody>
              <tr>
                <th>Préfixe</th>
                <th>Type de membre</th>
                <th>Règle principale</th>
              </tr>

              @for (
                categorie of
                donneesDemonstration.categoriesMembres;
                track categorie.prefixe
              ) {
                <tr>
                  <td>
                    <strong>
                      {{ categorie.prefixe }}
                    </strong>
                  </td>
                  <td>
                    {{ categorie.categorie }}
                  </td>
                  <td>
                    {{ categorie.regle }}
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>

        <div class="bloc-info">
          <h3>Sites de démonstration</h3>

          <ul>
            @for (
              site of donneesDemonstration.sites;
              track site.siteId
            ) {
              <li>
                <strong>{{ site.nom }}</strong>
                — {{ site.code }}
                — ID {{ site.siteId }}
              </li>
            }
          </ul>
        </div>

        <div class="bloc-info">
          <h3>Joueurs de démonstration</h3>

          <ul>
            @for (
              joueur of donneesDemonstration.joueurs;
              track joueur.matricule
            ) {
              <li>
                <strong>{{ joueur.matricule }}</strong>
                / {{ joueur.motDePasse }}
                — {{ joueur.description }}
              </li>
            }
          </ul>
        </div>

        <div class="bloc-info">
          <h3>Administrateurs de démonstration</h3>

          <ul>
            @for (
              administrateur of
              donneesDemonstration.administrateurs;
              track administrateur.login
            ) {
              <li>
                <strong>
                  {{ administrateur.login }}
                </strong>
                / {{ administrateur.motDePasse }}
                — {{ administrateur.description }}
              </li>
            }
          </ul>
        </div>
      }
    </section>
  `
})
export class AccueilComponent
  implements OnInit {

  constructor(
    readonly facade:
    AccueilFacadeService
  ) {
  }

  ngOnInit(): void {
    this.facade.initialiser();
  }
}
