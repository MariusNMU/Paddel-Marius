import { computed, Injectable, signal } from '@angular/core';
import { AuthAdminResponse, AuthJoueurResponse } from '../models/auth.model';

function lireStockage<T>(cle: string): T | null {
  const valeur = localStorage.getItem(cle);

  if (!valeur) {
    return null;
  }

  try {
    return JSON.parse(valeur) as T;
  } catch {
    localStorage.removeItem(cle);
    return null;
  }
}

@Injectable({
  providedIn: 'root'
})
export class AuthContextService {
  private readonly joueurKey = 'padel-joueur';
  private readonly adminKey = 'padel-admin';

  private readonly joueurSignal = signal<AuthJoueurResponse | null>(
    lireStockage<AuthJoueurResponse>(this.joueurKey)
  );

  private readonly adminSignal = signal<AuthAdminResponse | null>(
    lireStockage<AuthAdminResponse>(this.adminKey)
  );

  readonly joueur = this.joueurSignal.asReadonly();
  readonly admin = this.adminSignal.asReadonly();

  readonly joueurConnecte = computed(() => this.joueurSignal() !== null);
  readonly adminConnecte = computed(() => this.adminSignal() !== null);

  definirJoueur(joueur: AuthJoueurResponse): void {
    this.joueurSignal.set(joueur);
    localStorage.setItem(this.joueurKey, JSON.stringify(joueur));
  }

  definirAdmin(admin: AuthAdminResponse): void {
    this.adminSignal.set(admin);
    localStorage.setItem(this.adminKey, JSON.stringify(admin));
  }

  deconnecterJoueur(): void {
    this.joueurSignal.set(null);
    localStorage.removeItem(this.joueurKey);
  }

  deconnecterAdmin(): void {
    this.adminSignal.set(null);
    localStorage.removeItem(this.adminKey);
  }
}
