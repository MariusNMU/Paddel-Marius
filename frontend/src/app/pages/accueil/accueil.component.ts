import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-accueil',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="page">
      <h2>Welcome</h2>

      <p>
        Bienvenue dans l'application Padel faite par Marius. Cette interface permet de tester
        les principales fonctionnalités du MVP : réservation, paiement, dette,
        statistiques et administration.
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

      <div class="actions">
        <a routerLink="/joueur">Connexion joueur</a>
        <a routerLink="/joueur/disponibilites">Réserver un terrain</a>
        <a routerLink="/admin/login">Connexion admin</a>
      </div>
    </section>
  `
})
export class AccueilComponent {
}
