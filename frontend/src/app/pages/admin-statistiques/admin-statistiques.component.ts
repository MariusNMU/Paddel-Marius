import { Component } from '@angular/core';

@Component({
  selector: 'app-admin-statistiques',
  standalone: true,
  template: `
    <section class="page">
      <h2>Statistiques admin</h2>
      <p>Chiffre d'affaires, dettes ouvertes, matches et taux de remplissage.</p>
      <p>Endpoint cible : GET /api/admin/statistiques</p>
    </section>
  `
})
export class AdminStatistiquesComponent {
}
