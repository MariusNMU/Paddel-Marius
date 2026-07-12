import { computed, Injectable, OnDestroy, signal } from '@angular/core';
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
export class AuthContextService implements OnDestroy {
  private readonly joueurKey = 'padel-joueur';
  private readonly adminKey = 'padel-admin';

  private readonly joueurSignal = signal<AuthJoueurResponse | null>(null);
  private readonly adminSignal = signal<AuthAdminResponse | null>(null);

  readonly joueur = this.joueurSignal.asReadonly();
  readonly admin = this.adminSignal.asReadonly();

  readonly joueurConnecte = computed(() => this.joueurSignal() !== null);
  readonly adminConnecte = computed(() => this.adminSignal() !== null);

  private readonly gererChangementStockage = (event: StorageEvent): void => {
    const concerneAuthentification =
      event.key === null ||
      event.key === this.joueurKey ||
      event.key === this.adminKey;

    if (concerneAuthentification) {
      this.initialiserDepuisStockage();
    }
  };

  constructor() {
    this.initialiserDepuisStockage();
    window.addEventListener('storage', this.gererChangementStockage);
  }

  ngOnDestroy(): void {
    window.removeEventListener('storage', this.gererChangementStockage);
  }

  definirJoueur(joueur: AuthJoueurResponse): void {
    this.adminSignal.set(null);
    localStorage.removeItem(this.adminKey);

    this.joueurSignal.set(joueur);
    localStorage.setItem(this.joueurKey, JSON.stringify(joueur));
  }

  definirAdmin(admin: AuthAdminResponse): void {
    this.joueurSignal.set(null);
    localStorage.removeItem(this.joueurKey);

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

  private initialiserDepuisStockage(): void {
    const joueur = lireStockage<AuthJoueurResponse>(this.joueurKey);
    const admin = lireStockage<AuthAdminResponse>(this.adminKey);

    if (joueur && admin) {
      localStorage.removeItem(this.joueurKey);
      localStorage.removeItem(this.adminKey);
      this.joueurSignal.set(null);
      this.adminSignal.set(null);
      return;
    }

    this.joueurSignal.set(joueur);
    this.adminSignal.set(admin);
  }
}
