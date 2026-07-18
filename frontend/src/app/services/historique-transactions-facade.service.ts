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
import { HistoriquePaiementResponse } from '../models/paiement.model';
import { extraireMessageErreur } from '../shared/api-error.util';
import { AuthContextService } from './auth-context.service';
import { PaiementApiService } from './paiement-api.service';

@Injectable()
export class HistoriqueTransactionsFacadeService {
  private readonly transactionsSignal =
    signal<HistoriquePaiementResponse[]>([]);

  private readonly chargementSignal =
    signal(false);

  private readonly rechercheEffectueeSignal =
    signal(false);

  private readonly messageErreurSignal =
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

  readonly transactions =
    this.transactionsSignal.asReadonly();

  readonly chargement =
    this.chargementSignal.asReadonly();

  readonly rechercheEffectuee =
    this.rechercheEffectueeSignal.asReadonly();

  readonly messageErreur =
    this.messageErreurSignal.asReadonly();

  readonly totalPaye = computed(
    () => this.transactionsSignal().reduce(
      (total, transaction) =>
        total + transaction.montant,
      0
    )
  );

  constructor(
    private readonly authContextService:
    AuthContextService,
    private readonly paiementApiService:
    PaiementApiService
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

    this.synchroniserAvecJoueur(joueur);
  }

  chargerHistorique(): void {
    this.messageErreurSignal.set('');
    this.transactionsSignal.set([]);
    this.rechercheEffectueeSignal.set(true);

    const joueur = this.joueur();

    if (!joueur) {
      this.messageErreurSignal.set(
        'Aucun joueur connecté.'
      );
      return;
    }

    this.chargementSignal.set(true);

    this.paiementApiService
      .consulterHistoriquePaiements(
        joueur.matricule
      )
      .pipe(
        timeout(10000),
        tap(transactions => {
          this.transactionsSignal.set(
            transactions
          );
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

  private synchroniserAvecJoueur(
    joueur: AuthJoueurResponse | null
  ): void {
    this.changementSession$.next();
    this.reinitialiserParcours();

    if (joueur) {
      this.chargerHistorique();
    }
  }

  private reinitialiserParcours(): void {
    this.transactionsSignal.set([]);
    this.chargementSignal.set(false);
    this.rechercheEffectueeSignal.set(false);
    this.messageErreurSignal.set('');
  }
}
