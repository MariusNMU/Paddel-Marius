import {
  effect,
  Injectable,
  signal
} from '@angular/core';
import {
  catchError,
  EMPTY,
  finalize,
  Subject,
  takeUntil,
  tap
} from 'rxjs';
import { AuthAdminResponse } from '../models/auth.model';
import { TraitementVeilleResponse } from '../models/traitement-veille.model';
import { extraireMessageErreur } from '../shared/api-error.util';
import { AdminTraitementVeilleApiService } from './admin-traitement-veille-api.service';
import { AuthContextService } from './auth-context.service';

function dateIsoDansJours(
  decalageJours: number
): string {
  const date = new Date();

  date.setDate(
    date.getDate() + decalageJours
  );

  const annee = date.getFullYear();

  const mois = String(
    date.getMonth() + 1
  ).padStart(2, '0');

  const jour = String(
    date.getDate()
  ).padStart(2, '0');

  return `${annee}-${mois}-${jour}`;
}

@Injectable()
export class AdminTraitementVeilleFacadeService {
  private readonly dateTraitementSignal =
    signal(dateIsoDansJours(0));

  private readonly chargementSignal =
    signal(false);

  private readonly messageErreurSignal =
    signal<string | null>(null);

  private readonly resultatSignal =
    signal<TraitementVeilleResponse | null>(
      null
    );

  private readonly changementSession$ =
    new Subject<void>();

  private adminObserve:
    AuthAdminResponse | null;

  readonly dateTraitement =
    this.dateTraitementSignal.asReadonly();

  readonly chargement =
    this.chargementSignal.asReadonly();

  readonly messageErreur =
    this.messageErreurSignal.asReadonly();

  readonly resultat =
    this.resultatSignal.asReadonly();

  get adminConnecte() {
    return this.authContextService.adminConnecte;
  }

  constructor(
    private readonly traitementVeilleApiService:
    AdminTraitementVeilleApiService,
    private readonly authContextService:
    AuthContextService
  ) {
    this.adminObserve =
      this.authContextService.admin();

    effect(() => {
      const admin =
        this.authContextService.admin();

      if (admin === this.adminObserve) {
        return;
      }

      this.adminObserve = admin;
      this.reinitialiserApresChangementSession();
    });
  }

  estAdminGlobal(): boolean {
    return this.authContextService.admin()
      ?.roleAdministrateur === 'GLOBAL';
  }

  selectionnerDate(date: string): void {
    this.dateTraitementSignal.set(date);
    this.messageErreurSignal.set(null);
    this.resultatSignal.set(null);
  }

  selectionnerDateRelative(
    decalageJours: number
  ): void {
    this.selectionnerDate(
      dateIsoDansJours(decalageJours)
    );
  }

  lancerTraitement(): void {
    this.messageErreurSignal.set(null);
    this.resultatSignal.set(null);

    const admin =
      this.authContextService.admin();

    if (!admin) {
      this.messageErreurSignal.set(
        'Tu dois te connecter comme admin avant de lancer le traitement de veille.'
      );
      return;
    }

    if (
      admin.roleAdministrateur !== 'GLOBAL'
    ) {
      this.messageErreurSignal.set(
        'Cette action est réservée aux administrateurs globaux.'
      );
      return;
    }

    const dateTraitement =
      this.dateTraitementSignal();

    if (!dateTraitement) {
      this.messageErreurSignal.set(
        'La date de traitement est obligatoire.'
      );
      return;
    }

    this.chargementSignal.set(true);

    this.traitementVeilleApiService
      .traiterVeille(dateTraitement)
      .pipe(
        tap(resultat => {
          this.resultatSignal.set(resultat);
        }),
        catchError(error => {
          this.messageErreurSignal.set(
            extraireMessageErreur(error)
          );

          return EMPTY;
        }),
        finalize(() => {
          this.chargementSignal.set(false);
        }),
        takeUntil(this.changementSession$)
      )
      .subscribe();
  }

  private reinitialiserApresChangementSession():
    void {
    this.changementSession$.next();

    this.dateTraitementSignal.set(
      dateIsoDansJours(0)
    );

    this.chargementSignal.set(false);
    this.messageErreurSignal.set(null);
    this.resultatSignal.set(null);
  }
}
