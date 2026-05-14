import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthContextService } from './services/auth-context.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  constructor(
    readonly authContextService: AuthContextService,
    private readonly router: Router
  ) {
  }

  deconnecterJoueur(): void {
    this.authContextService.deconnecterJoueur();
    void this.router.navigate(['/accueil']);
  }

  deconnecterAdmin(): void {
    this.authContextService.deconnecterAdmin();
    void this.router.navigate(['/accueil']);
  }
}
