import { Component } from '@angular/core';
import { DONNEES_DEMONSTRATION } from '../../config/donnees-demonstration.config';

@Component({
  selector: 'app-accueil',
  standalone: true,
  template: `
    <section class="page">
      <h2>Homepage</h2>

      <p>
        Bienvenue dans l'application Padel Marius.
        Cette interface permet de démontrer le MVP de réservation de terrains de padel :
        connexion joueur, organisation de match, inscription publique, paiement,
        dette, solde, statistiques et administration.
      </p>

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
              categorie of donneesDemonstration.categoriesMembres;
              track categorie.prefixe
              ) {
              <tr>
                <td><strong>{{ categorie.prefixe }}</strong></td>
                <td>{{ categorie.categorie }}</td>
                <td>{{ categorie.regle }}</td>
              </tr>
            }
          </tbody>
        </table>
      </div>

      <div class="bloc-info">
        <h3>Sites de démonstration</h3>

        <ul>
          @for (site of donneesDemonstration.sites; track site.id) {
            <li>
              <strong>{{ site.nom }}</strong>
              — {{ site.code }} — ID {{ site.id }}
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
            administrateur of donneesDemonstration.administrateurs;
            track administrateur.login
            ) {
            <li>
              <strong>{{ administrateur.login }}</strong>
              / {{ administrateur.motDePasse }}
              — {{ administrateur.description }}
            </li>
          }
        </ul>
      </div>
    </section>
  `
})
export class AccueilComponent {

  readonly donneesDemonstration = DONNEES_DEMONSTRATION;
}
