import { Component } from '@angular/core';

@Component({
  selector: 'app-accueil',
  standalone: true,
  template: `
    <section class="page">
      <h2>Accueil</h2>
      <p>Bienvenue dans l'application Padel Marius.</p>
      <p>Cette interface Angular consommera uniquement l'API REST du backend.</p>
    </section>
  `
})
export class AccueilComponent {
}
