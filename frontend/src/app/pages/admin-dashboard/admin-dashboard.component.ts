import { Component } from '@angular/core';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  template: `
    <section class="page">
      <h2>Dashboard admin</h2>
      <p>Vue d'administration simple.</p>
      <p>Cette page servira d'entrée vers les statistiques et le traitement de veille.</p>
    </section>
  `
})
export class AdminDashboardComponent {
}
