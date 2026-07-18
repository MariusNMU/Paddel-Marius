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
  tap,
  timeout
} from 'rxjs';
import { AuthJoueurResponse } from '../models/auth.model';
import { PaiementResponse } from '../models/paiement.model';
import { ReservationJoueurResponse } from '../models/reservation.model';
import { extraireMessageErreur } from '../shared/api-error.util';
import { AuthContextService } from './auth-context.service';
import { PaiementApiService } from './paiement-api.service';
import { ReservationApiService } from './reservation-api.service';

@Injectable()
export class MesReservationsFacadeService {
  private readonly reservationsSignal =
    signal<ReservationJoueurResponse[]>([]);

  private readonly messageErreurSignal =
    signal('');

  private readonly messageSuccesSignal =
    signal('');

  private readonly chargementSignal =
    signal(false);

  private readonly rechercheEffectueeSignal =
    signal(false);

  private readonly paiementEnCoursParticipationIdSignal =
    signal<number | null>(null);

  private readonly dernierPaiementSignal =
    signal<PaiementResponse | null>(null);

  private readonly changementSession$ =
    new Subject<void>();

  private parcoursInitialise = false;

  private joueurObserve:
    AuthJoueurResponse | null | undefined =
    undefined;

  get joueur() {
    return this.authContextService.joueur;
  }

  readonly reservations =
    this.reservationsSignal.asReadonly();

  readonly messageErreur =
    this.messageErreurSignal.asReadonly();

  readonly messageSucces =
    this.messageSuccesSignal.asReadonly();

  readonly chargement =
    this.chargementSignal.asReadonly();

  readonly rechercheEffectuee =
    this.rechercheEffectueeSignal.asReadonly();

  readonly paiementEnCoursParticipationId =
    this.paiementEnCoursParticipationIdSignal
      .asReadonly();

  readonly dernierPaiement =
    this.dernierPaiementSignal.asReadonly();

  constructor(
    private readonly authContextService:
    AuthContextService,
    private readonly reservationApiService:
    ReservationApiService,
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

  chargerReservations(): void {
    this.messageErreurSignal.set('');
    this.messageSuccesSignal.set('');
    this.dernierPaiementSignal.set(null);
    this.reservationsSignal.set([]);

    this.rechercheEffectueeSignal.set(
      true
    );

    const joueur = this.joueur();

    if (!joueur) {
      this.messageErreurSignal.set(
        'Aucun joueur connecté.'
      );
      return;
    }

    this.chargementSignal.set(true);

    this.reservationApiService
      .consulterMesReservations(
        joueur.matricule
      )
      .pipe(
        timeout(10000),
        tap(reservations => {
          this.reservationsSignal.set(
            reservations
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

  payerParticipation(
    reservation: ReservationJoueurResponse
  ): void {
    if (
      this.paiementEnCoursParticipationIdSignal()
      !== null
    ) {
      return;
    }

    this.messageErreurSignal.set('');
    this.messageSuccesSignal.set('');
    this.dernierPaiementSignal.set(null);

    if (!this.joueur()) {
      this.messageErreurSignal.set(
        'Aucun joueur connecté.'
      );
      return;
    }

    this.paiementEnCoursParticipationIdSignal
      .set(reservation.participationId);

    this.paiementApiService
      .payerParticipationStandard(
        reservation.participationId
      )
      .pipe(
        timeout(10000),
        tap(paiement => {
          this.dernierPaiementSignal.set(
            paiement
          );

          this.messageSuccesSignal.set(
            'Participation payée avec succès.'
          );

          this.confirmerParticipation(
            reservation.participationId
          );
        }),
        catchError(error => {
          this.messageErreurSignal.set(
            extraireMessageErreur(error)
          );

          return EMPTY;
        }),
        finalize(() => {
          this.paiementEnCoursParticipationIdSignal
            .set(null);
        }),
        takeUntil(this.changementSession$)
      )
      .subscribe();
  }

  private confirmerParticipation(
    participationId: number
  ): void {
    this.reservationsSignal.update(
      reservations =>
        reservations.map(
          reservation =>
            reservation.participationId
            === participationId
              ? {
                ...reservation,
                statutParticipation:
                  'CONFIRMEE'
              }
              : reservation
        )
    );
  }

  private synchroniserAvecJoueur(
    joueur: AuthJoueurResponse | null
  ): void {
    this.changementSession$.next();
    this.reinitialiserParcours();

    if (joueur) {
      this.chargerReservations();
    }
  }

  private reinitialiserParcours(): void {
    this.reservationsSignal.set([]);
    this.messageErreurSignal.set('');
    this.messageSuccesSignal.set('');
    this.chargementSignal.set(false);

    this.rechercheEffectueeSignal.set(
      false
    );

    this.paiementEnCoursParticipationIdSignal
      .set(null);

    this.dernierPaiementSignal.set(null);
  }
}
