import {
  HttpErrorResponse
} from '@angular/common/http';
import {
  signal,
  type Signal,
  type WritableSignal
} from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  NEVER,
  of,
  Subject,
  throwError
} from 'rxjs';
import { AuthJoueurResponse } from '../models/auth.model';
import { PaiementResponse } from '../models/paiement.model';
import { ReservationJoueurResponse } from '../models/reservation.model';
import { AuthContextService } from './auth-context.service';
import { MesReservationsFacadeService } from './mes-reservations-facade.service';
import { PaiementApiService } from './paiement-api.service';
import { ReservationApiService } from './reservation-api.service';

describe(
  'MesReservationsFacadeService',
  () => {
    let service:
      MesReservationsFacadeService;

    let joueurSignal:
      WritableSignal<AuthJoueurResponse | null>;

    let authContextService: {
      joueur:
        Signal<AuthJoueurResponse | null>;
    };

    let reservationApiService: {
      consulterMesReservations:
        ReturnType<typeof vi.fn>;
    };

    let paiementApiService: {
      payerParticipation:
        ReturnType<typeof vi.fn>;
    };

    const joueur: AuthJoueurResponse = {
      membreId: 2001,
      matricule: 'G1001',
      nom: 'Dupont',
      prenom: 'Marie',
      categorieMembre: 'GLOBAL',
      siteRattachementId: null,
      nomSiteRattachement: null,
      actif: true
    };

    const joueurDeux:
      AuthJoueurResponse = {
      membreId: 2002,
      matricule: 'G1002',
      nom: 'Lambert',
      prenom: 'Paul',
      categorieMembre: 'GLOBAL',
      siteRattachementId: null,
      nomSiteRattachement: null,
      actif: true
    };

    const reservation:
      ReservationJoueurResponse = {
      participationId: 3101,
      matchId: 3001,
      siteId: 1001,
      nomSite: 'Padel Bruxelles',
      terrainId: 1101,
      numeroTerrain: 'T1',
      dateHeureDebut:
        '2026-06-20T09:00:00',
      dateHeureFin:
        '2026-06-20T10:30:00',
      roleParticipation:
        'ORGANISATEUR',
      modeEntree: 'CREATION',
      statutParticipation:
        'EN_ATTENTE_PAIEMENT',
      modeCreation: 'PUBLIC',
      visibiliteCourante: 'PUBLIC',
      etatCycle: 'A_VENIR',
      prixTotal: 60
    };

    const reservationDeux:
      ReservationJoueurResponse = {
      ...reservation,
      participationId: 3202,
      matchId: 3002,
      siteId: 1002,
      nomSite: 'Padel Namur',
      terrainId: 1201,
      numeroTerrain: 'T1'
    };

    const paiement:
      PaiementResponse = {
      paiementId: 4101,
      participationId: 3101,
      membreId: 2001,
      matriculeMembre: 'G1001',
      montant: 15,
      montantDettesReglees: 30,
      montantTotalDebite: 45,
      naturePaiement: 'PARTICIPATION',
      statutPaiement: 'PAYE',
      statutParticipation: 'CONFIRMEE',
      dateHeurePaiement:
        '2026-06-01T12:00:00',
      dateConfirmationParticipation:
        '2026-06-01T12:00:00'
    };

    const messageErreurGenerique =
      'Une erreur est survenue. Vérifie les données saisies puis réessaie.';

    beforeEach(() => {
      vi.useFakeTimers();

      joueurSignal =
        signal<AuthJoueurResponse | null>(
          joueur
        );

      authContextService = {
        joueur: joueurSignal.asReadonly()
      };

      reservationApiService = {
        consulterMesReservations:
          vi.fn((matricule: string) =>
            of(
              matricule === 'G1002'
                ? [reservationDeux]
                : [reservation]
            )
          )
      };

      paiementApiService = {
        payerParticipation:
          vi.fn(() => of(paiement))
      };

      TestBed.configureTestingModule({
        providers: [
          MesReservationsFacadeService,
          {
            provide:
            AuthContextService,
            useValue:
            authContextService
          },
          {
            provide:
            ReservationApiService,
            useValue:
            reservationApiService
          },
          {
            provide:
            PaiementApiService,
            useValue:
            paiementApiService
          }
        ]
      });

      service = TestBed.inject(
        MesReservationsFacadeService
      );
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it(
      'doit charger les réservations à l initialisation',
      () => {
        service.initialiser();

        expect(
          reservationApiService
            .consulterMesReservations
        ).toHaveBeenCalledWith(
          'G1001'
        );

        expect(service.joueur())
          .toEqual(joueur);

        expect(service.reservations())
          .toEqual([reservation]);

        expect(
          service.rechercheEffectuee()
        ).toBe(true);

        expect(service.chargement())
          .toBe(false);
      }
    );

    it(
      'doit rester vide sans joueur à l initialisation',
      () => {
        joueurSignal.set(null);
        TestBed.tick();

        service.initialiser();

        expect(service.joueur())
          .toBeNull();

        expect(service.reservations())
          .toEqual([]);

        expect(
          service.rechercheEffectuee()
        ).toBe(false);

        expect(
          reservationApiService
            .consulterMesReservations
        ).not.toHaveBeenCalled();
      }
    );

    it(
      'doit refuser une actualisation sans joueur',
      () => {
        joueurSignal.set(null);
        TestBed.tick();

        service.chargerReservations();

        expect(service.messageErreur())
          .toBe(
            'Aucun joueur connecté.'
          );

        expect(
          service.rechercheEffectuee()
        ).toBe(true);

        expect(
          reservationApiService
            .consulterMesReservations
        ).not.toHaveBeenCalled();
      }
    );

    it(
      'doit exposer une erreur de chargement',
      () => {
        reservationApiService
          .consulterMesReservations
          .mockReturnValue(
            throwError(
              () =>
                new HttpErrorResponse({
                  status: 500,
                  error: {
                    message:
                      'Erreur backend réservations.'
                  }
                })
            )
          );

        service.initialiser();

        expect(service.messageErreur())
          .toBe(
            'Erreur backend réservations.'
          );

        expect(service.reservations())
          .toEqual([]);

        expect(service.chargement())
          .toBe(false);
      }
    );

    it(
      'doit terminer le chargement après expiration du délai',
      () => {
        reservationApiService
          .consulterMesReservations
          .mockReturnValue(NEVER);

        service.initialiser();

        expect(service.chargement())
          .toBe(true);

        vi.advanceTimersByTime(10001);

        expect(service.messageErreur())
          .toBe(messageErreurGenerique);

        expect(service.chargement())
          .toBe(false);
      }
    );

    it(
      'doit payer et confirmer la participation',
      () => {
        service.initialiser();

        service.payerParticipation(
          reservation
        );

        expect(
          paiementApiService
            .payerParticipation
        ).toHaveBeenCalledWith(3101);

        expect(service.dernierPaiement())
          .toEqual(paiement);

        expect(service.messageSucces())
          .toBe(
            'Participation payée avec succès.'
          );

        expect(
          service.reservations()[0]
            .statutParticipation
        ).toBe('CONFIRMEE');

        expect(
          service
            .paiementEnCoursParticipationId()
        ).toBeNull();
      }
    );

    it(
      'doit exposer une erreur de paiement',
      () => {
        paiementApiService
          .payerParticipation
          .mockReturnValue(
            throwError(
              () =>
                new HttpErrorResponse({
                  status: 409,
                  error: {
                    message:
                      'Cette participation possède déjà un paiement.'
                  }
                })
            )
          );

        service.initialiser();

        service.payerParticipation(
          reservation
        );

        expect(service.messageErreur())
          .toBe(
            'Cette participation possède déjà un paiement.'
          );

        expect(service.dernierPaiement())
          .toBeNull();

        expect(
          service.reservations()[0]
            .statutParticipation
        ).toBe(
          'EN_ATTENTE_PAIEMENT'
        );

        expect(
          service
            .paiementEnCoursParticipationId()
        ).toBeNull();
      }
    );

    it(
      'doit terminer le paiement après expiration du délai',
      () => {
        paiementApiService
          .payerParticipation
          .mockReturnValue(NEVER);

        service.initialiser();

        service.payerParticipation(
          reservation
        );

        expect(
          service
            .paiementEnCoursParticipationId()
        ).toBe(3101);

        vi.advanceTimersByTime(10001);

        expect(service.messageErreur())
          .toBe(messageErreurGenerique);

        expect(
          service
            .paiementEnCoursParticipationId()
        ).toBeNull();
      }
    );

    it(
      'doit refuser un paiement sans joueur',
      () => {
        joueurSignal.set(null);
        TestBed.tick();

        service.initialiser();

        service.payerParticipation(
          reservation
        );

        expect(service.messageErreur())
          .toBe(
            'Aucun joueur connecté.'
          );

        expect(
          paiementApiService
            .payerParticipation
        ).not.toHaveBeenCalled();
      }
    );

    it(
      'doit charger les réservations du nouveau joueur',
      () => {
        service.initialiser();

        joueurSignal.set(joueurDeux);
        TestBed.tick();

        expect(service.joueur())
          .toEqual(joueurDeux);

        expect(
          reservationApiService
            .consulterMesReservations
        ).toHaveBeenLastCalledWith(
          'G1002'
        );

        expect(service.reservations())
          .toEqual([reservationDeux]);
      }
    );

    it(
      'doit vider le parcours après déconnexion',
      () => {
        service.initialiser();

        service.payerParticipation(
          reservation
        );

        joueurSignal.set(null);
        TestBed.tick();

        expect(service.joueur())
          .toBeNull();

        expect(service.reservations())
          .toEqual([]);

        expect(
          service.rechercheEffectuee()
        ).toBe(false);

        expect(service.messageErreur())
          .toBe('');

        expect(service.messageSucces())
          .toBe('');

        expect(service.dernierPaiement())
          .toBeNull();

        expect(
          service
            .paiementEnCoursParticipationId()
        ).toBeNull();
      }
    );

    it(
      'doit ignorer une ancienne liste après un changement de joueur',
      () => {
        const ancienneListe$ =
          new Subject<
            ReservationJoueurResponse[]
          >();

        reservationApiService
          .consulterMesReservations
          .mockReset();

        reservationApiService
          .consulterMesReservations
          .mockReturnValueOnce(
            ancienneListe$
          )
          .mockReturnValueOnce(
            of([reservationDeux])
          );

        service.initialiser();

        joueurSignal.set(joueurDeux);
        TestBed.tick();

        ancienneListe$.next([
          reservation
        ]);

        expect(service.reservations())
          .toEqual([reservationDeux]);
      }
    );

    it(
      'doit ignorer un ancien paiement après déconnexion',
      () => {
        const ancienPaiement$ =
          new Subject<PaiementResponse>();

        paiementApiService
          .payerParticipation
          .mockReturnValue(
            ancienPaiement$
          );

        service.initialiser();

        service.payerParticipation(
          reservation
        );

        joueurSignal.set(null);
        TestBed.tick();

        ancienPaiement$.next(paiement);

        expect(service.messageSucces())
          .toBe('');

        expect(service.dernierPaiement())
          .toBeNull();

        expect(service.reservations())
          .toEqual([]);
      }
    );

    it(
      'doit empêcher deux paiements simultanés',
      () => {
        const paiementEnCours$ =
          new Subject<PaiementResponse>();

        paiementApiService
          .payerParticipation
          .mockReturnValue(
            paiementEnCours$
          );

        service.initialiser();

        service.payerParticipation(
          reservation
        );

        service.payerParticipation(
          reservation
        );

        expect(
          paiementApiService
            .payerParticipation
        ).toHaveBeenCalledTimes(1);

        expect(
          service
            .paiementEnCoursParticipationId()
        ).toBe(3101);
      }
    );
  }
);
