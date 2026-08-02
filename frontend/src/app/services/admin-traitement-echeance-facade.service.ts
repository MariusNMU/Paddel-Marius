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
import {
  TraitementEcheanceResponse
} from '../models/traitement-echeance.model';
import {
  extraireMessageErreur
} from '../shared/api-error.util';
import {
  AdminTraitementEcheanceApiService
} from './admin-traitement-echeance-api.service';
import {
  AuthContextService
} from './auth-context.service';

@Injectable()
export class AdminTraitementEcheanceFacadeService {
  private readonly chargementSignal =
    signal(false);

  private readonly messageErreurSignal =
    signal<string | null>(null);

  private readonly resultatSignal =
    signal<TraitementEcheanceResponse | null>(
      null
    );

  private readonly changementSession$ =
    new Subject<void>();

  private adminObserve:
    AuthAdminResponse | null;

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
    private readonly traitementEcheanceApiService:
    AdminTraitementEcheanceApiService,
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

  lancerTraitement(): void {
    this.messageErreurSignal.set(null);
    this.resultatSignal.set(null);

    const admin =
      this.authContextService.admin();

    if (!admin) {
      this.messageErreurSignal.set(
        'Tu dois te connecter comme admin avant de lancer le traitement d’échéance.'
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

    this.chargementSignal.set(true);

    this.traitementEcheanceApiService
      .traiterEcheance()
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
    this.chargementSignal.set(false);
    this.messageErreurSignal.set(null);
    this.resultatSignal.set(null);
  }
}
