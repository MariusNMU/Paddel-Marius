import { Component } from '@angular/core';

@Component({
  selector: 'app-creer-match',
  standalone: true,
  template: `
    <section class="page">
      <h2>Créer un match</h2>
      <p>Création d'un match privé ou public.</p>
      <p>Endpoint cible : POST /api/matches</p>
    </section>
  `
})
export class CreerMatchComponent {
}
