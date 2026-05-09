import { Component } from '@angular/core';

@Component({
  selector: 'app-mes-dettes',
  standalone: true,
  template: `
    <section class="page">
      <h2>Mes dettes</h2>
      <p>Consultation et paiement des dettes ouvertes.</p>
      <p>Endpoint cible : GET /api/membres/:matricule/dettes/ouvertes</p>
    </section>
  `
})
export class MesDettesComponent {
}
