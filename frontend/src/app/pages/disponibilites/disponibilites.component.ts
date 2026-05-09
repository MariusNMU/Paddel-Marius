import { Component } from '@angular/core';

@Component({
  selector: 'app-disponibilites',
  standalone: true,
  template: `
    <section class="page">
      <h2>Disponibilités</h2>
      <p>Consultation des créneaux disponibles par site et par date.</p>
      <p>Endpoint cible : GET /api/disponibilites</p>
    </section>
  `
})
export class DisponibilitesComponent {
}
