import {
  Component,
  OnInit
} from '@angular/core';
import {
  RouterLink,
  RouterLinkActive,
  RouterOutlet
} from '@angular/router';
import { AppShellFacadeService } from './services/app-shell-facade.service';

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive
  ],
  providers: [
    AppShellFacadeService
  ],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  constructor(
    readonly facade:
    AppShellFacadeService
  ) {
  }

  ngOnInit(): void {
    this.facade.initialiser();
  }
}
