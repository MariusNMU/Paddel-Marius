import {
  computed,
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
  tap,
  timeout
} from 'rxjs';
import { AuthJoueurResponse } from '../models/auth.model';
import { ParametresMetierResponse } from '../models/parametres-metier.model';
import { SoldeJoueurResponse } from '../models/solde-joueur.model';
import { extraireMessageErreur } from '../shared/api-error.util';
import { AuthContextService } from './auth-context.service';
import { ParametresMetierApiService } from './parametres-metier-api.service';
import { SoldeJoueurApiService } from './solde-joueur-api.service';

@Injectable()
export class MonSoldeFacadeService {
  private readonly parametresMetierSignal =
    signal<ParametresMetierResponse | null>(
      null
    );

  private readonly soldeSignal =
    signal<SoldeJoueurResponse | null>(null);

  private readonly chargementSignal =
    signal(false);

  private readonly chargementParametresSignal =
    signal(false);

  private readonly messageErreurSoldeSignal =
    signal('');

  private readonly messageErreurParametresSignal =
    signal('');

  private readonly changementSession$ =
    new Subject<void>();

  private parcoursInitialise = false;

  private joueurObserve:
    AuthJoueurResponse | null | undefined =
    undefined;

  get joueur() {
    return this.authContextService.joueur;
  }

  readonly parametresMetier =
    this.parametresMetierSignal.asReadonly();

  readonly solde =
    this.soldeSignal.asReadonly();

  readonly chargement =
    this.chargementSignal.asReadonly();

  readonly chargementParametres =
    this.chargementParametresSignal
      .asReadonly();

  readonly messageErreur = computed(
    () =>
      this.messageErreurSoldeSignal()
      || this.messageErreurParametresSignal()
  );

  constructor(
    private readonly authContextService:
    AuthContextService,
    private readonly soldeJoueurApiService:
    SoldeJoueurApiService,
    private readonly parametresMetierApiService:
    ParametresMetierApiService
  ) {
    effect(() => {
      const joueur =
        this.authContextService.joueur();

      if (
        !this.parcoursInitialise
        || joueur === this.joueurObserve
      ) {
        return;
      }

      this.joueurObserve = joueur;
      this.synchroniserAvecJoueur(joueur);
    });
  }

  initialiser(): void {
    const joueur =
      this.authContextService.joueur();

    this.parcoursInitialise = true;
    this.joueurObserve = joueur;

    this.chargerParametresMetier();
    this.synchroniserAvecJoueur(joueur);
  }

  chargerSolde(): void {
    this.messageErreurSoldeSignal.set('');
    this.soldeSignal.set(null);

    const joueur = this.joueur();

    if (!joueur) {
      this.messageErreurSoldeSignal.set(
        'Aucun joueur connecté.'
      );
      return;
    }

    this.chargementSignal.set(true);

    this.soldeJoueurApiService
      .consulterSolde(joueur.matricule)
      .pipe(
        timeout(10000),
        tap(solde => {
          this.soldeSignal.set(solde);
        }),
        catchError(error => {
          this.messageErreurSoldeSignal.set(
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

  private chargerParametresMetier(): void {
    this.messageErreurParametresSignal.set('');
    this.parametresMetierSignal.set(null);
    this.chargementParametresSignal.set(true);

    this.parametresMetierApiService
      .consulterParametresMetier()
      .pipe(
        timeout(10000),
        tap(parametres => {
          this.parametresMetierSignal.set(
            parametres
          );
        }),
        catchError(error => {
          this.messageErreurParametresSignal.set(
            extraireMessageErreur(error)
          );

          return EMPTY;
        }),
        finalize(() => {
          this.chargementParametresSignal.set(
            false
          );
        })
      )
      .subscribe();
  }

  private synchroniserAvecJoueur(
    joueur: AuthJoueurResponse | null
  ): void {
    this.changementSession$.next();

    this.soldeSignal.set(null);
    this.chargementSignal.set(false);
    this.messageErreurSoldeSignal.set('');

    if (joueur) {
      this.chargerSolde();
    }
  }
}
