import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthContextService } from './services/auth-context.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  constructor(readonly authContextService: AuthContextService) {
  }

  deconnecterJoueur(): void {
    this.authContextService.deconnecterJoueur();
  }
}
