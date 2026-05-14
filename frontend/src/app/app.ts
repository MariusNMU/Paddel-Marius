import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthContextService } from './services/auth-context.service';
import { InvitationApiService } from './services/invitation-api.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  nombreInvitationsRecues = 0;

  constructor(
    readonly authContextService: AuthContextService,
    private readonly invitationApiService: InvitationApiService,
    private readonly router: Router
  ) {
    this.chargerNombreInvitations();
  }

  chargerNombreInvitations(): void {
    const joueur = this.authContextService.joueur();

    if (!joueur) {
      this.nombreInvitationsRecues = 0;
      return;
    }

    this.invitationApiService.compterInvitationsRecues(joueur.matricule).subscribe({
      next: (count) => {
        this.nombreInvitationsRecues = count;
      },
      error: () => {
        this.nombreInvitationsRecues = 0;
      }
    });
  }

  deconnecterJoueur(): void {
    this.authContextService.deconnecterJoueur();
    this.nombreInvitationsRecues = 0;
    void this.router.navigate(['/accueil']);
  }

  deconnecterAdmin(): void {
    this.authContextService.deconnecterAdmin();
    void this.router.navigate(['/accueil']);
  }
}
