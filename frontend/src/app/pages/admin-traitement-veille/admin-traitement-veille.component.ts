import { Component } from '@angular/core';

@Component({
  selector: 'app-admin-traitement-veille',
  standalone: true,
  template: `
    <section class="page">
      <h2>Traitement de veille</h2>
      <p>Lancement manuel du traitement de veille des matches.</p>
      <p>Endpoint cible : POST /api/admin/matches/traitement-veille</p>
    </section>
  `
})
export class AdminTraitementVeilleComponent {
}
