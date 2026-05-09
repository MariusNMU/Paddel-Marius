import { Component } from '@angular/core';

@Component({
  selector: 'app-admin-login',
  standalone: true,
  template: `
    <section class="page">
      <h2>Connexion admin</h2>
      <p>Connexion administrateur par login et mot de passe.</p>
      <p>Endpoint cible : POST /api/auth/admin</p>
    </section>
  `
})
export class AdminLoginComponent {
}
