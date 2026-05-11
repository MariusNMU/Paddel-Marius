import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-accueil',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="page">
      <h2>Homepage</h2>

      <p>
        Bienvenue dans l'application Padel Marius. Cette interface permet de démontrer
        le MVP de réservation de terrains de padel : connexion joueur, réservation,
        paiement, dette, statistiques et administration.
      </p>

      <div class="bloc-info">
        <h3>Rappel architecture</h3>

        <ul>
          <li>Le frontend Angular ne contient aucun SQL.</li>
          <li>Le frontend n'accède jamais directement à la base de données.</li>
          <li>Le frontend communique uniquement avec le backend via l'API REST.</li>
          <li>Le backend applique les règles métier et accède à la base H2 du MVP.</li>
        </ul>
      </div>

      <div class="bloc-info">
        <h3>Parcours joueur recommandé</h3>

        <ol>
          <li>Cliquer sur <strong>Connexion joueur</strong>.</li>
          <li>Utiliser un joueur de démonstration, par exemple <strong>G1001</strong>.</li>
          <li>Consulter les créneaux via <strong>Réserver un terrain</strong>.</li>
          <li>Créer un match via <strong>Créer un match</strong>.</li>
          <li>Tester les dettes avec <strong>G1002</strong>.</li>
        </ol>

        <div class="actions">
          <a routerLink="/joueur">Connexion joueur</a>
        </div>
      </div>

      <div class="bloc-info">
        <h3>Parcours admin recommandé</h3>

        <ol>
          <li>Cliquer sur <strong>Connexion admin</strong>.</li>
          <li>Utiliser <strong>admin-global / secret</strong>.</li>
          <li>Ouvrir le dashboard admin.</li>
          <li>Consulter les statistiques.</li>
          <li>Lancer le traitement de veille.</li>
        </ol>

        <div class="actions">
          <a routerLink="/admin/login">Connexion admin</a>
        </div>
      </div>

      <div class="bloc-info">
        <h3>Légende des matricules</h3>

        <table>
          <tbody>
          <tr>
            <th>Préfixe</th>
            <th>Type de membre</th>
            <th>Règle principale</th>
          </tr>
          <tr>
            <td><strong>G</strong></td>
            <td>GLOBAL</td>
            <td>Peut réserver sur tous les sites, jusqu'à 21 jours avant.</td>
          </tr>
          <tr>
            <td><strong>S</strong></td>
            <td>SITE</td>
            <td>Peut réserver uniquement sur son site de rattachement, jusqu'à 14 jours avant.</td>
          </tr>
          <tr>
            <td><strong>L</strong></td>
            <td>LIBRE</td>
            <td>Peut réserver sur tous les sites, jusqu'à 5 jours avant.</td>
          </tr>
          </tbody>
        </table>
      </div>

      <div class="bloc-info">
        <h3>Sites de démonstration</h3>

        <ul>
          <li><strong>Padel Bruxelles</strong> — BRU — ID 1001</li>
          <li><strong>Padel Namur</strong> — NAM — ID 1002</li>
        </ul>
      </div>

      <div class="bloc-info">
        <h3>Joueurs de démonstration</h3>

        <ul>
          <li><strong>G1001</strong> — joueur GLOBAL actif</li>
          <li><strong>G1002</strong> — joueur GLOBAL actif avec dette ouverte</li>
          <li><strong>S1001</strong> — joueur SITE rattaché à Padel Bruxelles (1001)</li>
          <li><strong>S1002</strong> — joueur SITE rattaché à Padel Namur (1002)</li>
          <li><strong>L1001</strong> — joueur LIBRE actif</li>
          <li><strong>L1002</strong> — joueur LIBRE avec pénalité active</li>
          <li><strong>G9999</strong> — joueur inactif pour tester le refus</li>
        </ul>
      </div>

      <div class="bloc-info">
        <h3>Administrateurs de démonstration</h3>

        <ul>
          <li><strong>admin-global</strong> / secret — administrateur GLOBAL</li>
          <li><strong>admin-bruxelles</strong> / secret-site — administrateur SITE Bruxelles (1001)</li>
        </ul>
      </div>
    </section>
  `
})
export class AccueilComponent {
}
