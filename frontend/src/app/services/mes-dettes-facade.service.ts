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
import { DetteResponse } from '../models/dette.model';
import { extraireMessageErreur } from '../shared/api-error.util';
import { AuthContextService } from './auth-context.service';
import { DetteApiService } from './dette-api.service';

@Injectable()
export class MesDettesFacadeService {
  private readonly dettesSignal =
    signal<DetteResponse[]>([]);

  private readonly montantsPaiementSignal =
    signal<Record<number, number>>({});

  private readonly chargementSignal =
    signal(false);

  private readonly rechercheEffectueeSignal =
    signal(false);

  private readonly paiementEnCoursDetteIdSignal =
    signal<number | null>(null);

  private readonly messageErreurSignal =
    signal('');

  private readonly messageSuccesSignal =
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

  readonly dettes =
    this.dettesSignal.asReadonly();

  readonly montantsPaiement =
    this.montantsPaiementSignal.asReadonly();

  readonly chargement =
    this.chargementSignal.asReadonly();

  readonly rechercheEffectuee =
    this.rechercheEffectueeSignal.asReadonly();

  readonly paiementEnCoursDetteId =
    this.paiementEnCoursDetteIdSignal.asReadonly();

  readonly messageErreur =
    this.messageErreurSignal.asReadonly();

  readonly messageSucces =
    this.messageSuccesSignal.asReadonly();

  readonly totalMontantRestant = computed(
    () => this.dettesSignal().reduce(
      (total, dette) =>
        total + dette.montantRestant,
      0
    )
  );

  constructor(
    private readonly authContextService:
    AuthContextService,
    private readonly detteApiService:
    DetteApiService
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

  chargerDettes(): void {
    this.messageErreurSignal.set('');
    this.messageSuccesSignal.set('');
    this.dettesSignal.set([]);
    this.montantsPaiementSignal.set({});
    this.rechercheEffectueeSignal.set(false);

    const joueur = this.joueur();

    if (!joueur) {
      this.messageErreurSignal.set(
        'Aucun joueur connecté. Connecte-toi d’abord pour consulter tes dettes.'
      );
      return;
    }

    this.chargementSignal.set(true);

    this.detteApiService
      .consulterDettesOuvertes(
        joueur.matricule
      )
      .pipe(
        timeout(10000),
        tap(dettes => {
          this.dettesSignal.set(dettes);

          const montants:
            Record<number, number> = {};

          for (const dette of dettes) {
            montants[dette.detteId] =
              dette.montantRestant;
          }

          this.montantsPaiementSignal.set(
            montants
          );

          this.rechercheEffectueeSignal.set(
            true
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

  montantPaiement(
    detteId: number
  ): number {
    return this.montantsPaiementSignal()[
      detteId
      ] ?? 0;
  }

  modifierMontantPaiement(
    detteId: number,
    montant: number | null
  ): void {
    this.montantsPaiementSignal.update(
      montants => ({
        ...montants,
        [detteId]: montant ?? 0
      })
    );
  }

  payerDette(
    dette: DetteResponse
  ): void {
    if (
      this.paiementEnCoursDetteIdSignal()
      !== null
    ) {
      return;
    }

    this.messageErreurSignal.set('');
    this.messageSuccesSignal.set('');

    if (!this.joueur()) {
      this.messageErreurSignal.set(
        'Aucun joueur connecté. Connecte-toi d’abord pour consulter tes dettes.'
      );
      return;
    }

    const montant =
      this.montantPaiement(dette.detteId);

    if (
      !Number.isFinite(montant)
      || montant <= 0
    ) {
      this.messageErreurSignal.set(
        'Le montant du paiement doit être supérieur à 0.'
      );
      return;
    }

    this.paiementEnCoursDetteIdSignal.set(
      dette.detteId
    );

    this.detteApiService
      .payerDette(
        dette.detteId,
        { montant }
      )
      .pipe(
        timeout(10000),
        tap(response => {
          this.messageSuccesSignal.set(
            `Paiement réussi : dette ${response.detteId} payée pour ${response.montant} €.`
          );

          this.dettesSignal.update(
            dettesOuvertes =>
              dettesOuvertes.filter(
                detteOuverte =>
                  detteOuverte.detteId
                  !== response.detteId
              )
          );

          const montants = {
            ...this.montantsPaiementSignal()
          };

          delete montants[response.detteId];

          this.montantsPaiementSignal.set(
            montants
          );

          this.rechercheEffectueeSignal.set(
            true
          );
        }),
        catchError(error => {
          this.messageErreurSignal.set(
            extraireMessageErreur(error)
          );

          return EMPTY;
        }),
        finalize(() => {
          this.paiementEnCoursDetteIdSignal
            .set(null);
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
      this.chargerDettes();
    }
  }

  private reinitialiserParcours(): void {
    this.dettesSignal.set([]);
    this.montantsPaiementSignal.set({});
    this.chargementSignal.set(false);
    this.rechercheEffectueeSignal.set(false);

    this.paiementEnCoursDetteIdSignal.set(
      null
    );

    this.messageErreurSignal.set('');
    this.messageSuccesSignal.set('');
  }
}
