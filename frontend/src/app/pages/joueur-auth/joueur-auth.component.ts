import { Component } from '@angular/core';

@Component({
  selector: 'app-joueur-auth',
  standalone: true,
  template: `
    <section class="page">
      <h2>Connexion joueur</h2>
      <p>Connexion prévue par matricule uniquement.</p>
      <p>Endpoint cible : POST /api/auth/joueur</p>
    </section>
  `
})
export class JoueurAuthComponent {
}
