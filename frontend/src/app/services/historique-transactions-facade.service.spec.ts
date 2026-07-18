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
import { HistoriquePaiementResponse } from '../models/paiement.model';
import { AuthContextService } from './auth-context.service';
import { HistoriqueTransactionsFacadeService } from './historique-transactions-facade.service';
import { PaiementApiService } from './paiement-api.service';

describe(
  'HistoriqueTransactionsFacadeService',
  () => {
    let service:
      HistoriqueTransactionsFacadeService;

    let joueurSignal:
      WritableSignal<AuthJoueurResponse | null>;

    let authContextService: {
      joueur:
        Signal<AuthJoueurResponse | null>;
    };

    let paiementApiService: {
      consulterHistoriquePaiements:
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
      ...joueur,
      membreId: 2002,
      matricule: 'G1002',
      nom: 'Lambert',
      prenom: 'Paul'
    };

    const transaction:
      HistoriquePaiementResponse = {
      paiementId: 5001,
      membreId: 2001,
      matriculeMembre: 'G1001',
      naturePaiement: 'PARTICIPATION',
      montant: 15,
      statutPaiement: 'PAYE',
      dateHeurePaiement:
        '2026-06-01T12:00:00',
      participationId: 3101,
      detteId: null,
      matchId: 3001
    };

    const transactionDette:
      HistoriquePaiementResponse = {
      paiementId: 5002,
      membreId: 2001,
      matriculeMembre: 'G1001',
      naturePaiement:
        'REGLEMENT_DETTE',
      montant: 45,
      statutPaiement: 'PAYE',
      dateHeurePaiement:
        '2026-06-02T12:00:00',
      participationId: null,
      detteId: 4001,
      matchId: 3002
    };

    const transactionJoueurDeux:
      HistoriquePaiementResponse = {
      ...transaction,
      paiementId: 5003,
      membreId: 2002,
      matriculeMembre: 'G1002',
      participationId: 3201,
      matchId: 3003
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

      paiementApiService = {
        consulterHistoriquePaiements:
          vi.fn((matricule: string) =>
            of(
              matricule === 'G1002'
                ? [transactionJoueurDeux]
                : [
                  transaction,
                  transactionDette
                ]
            )
          )
      };

      TestBed.configureTestingModule({
        providers: [
          HistoriqueTransactionsFacadeService,
          {
            provide: AuthContextService,
            useValue: authContextService
          },
          {
            provide: PaiementApiService,
            useValue: paiementApiService
          }
        ]
      });

      service = TestBed.inject(
        HistoriqueTransactionsFacadeService
      );
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it(
      'doit charger l historique et calculer le total',
      () => {
        service.initialiser();

        expect(
          paiementApiService
            .consulterHistoriquePaiements
        ).toHaveBeenCalledWith(
          'G1001'
        );

        expect(service.transactions())
          .toEqual([
            transaction,
            transactionDette
          ]);

        expect(service.totalPaye())
          .toBe(60);

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

        expect(service.transactions())
          .toEqual([]);

        expect(
          service.rechercheEffectuee()
        ).toBe(false);

        expect(
          paiementApiService
            .consulterHistoriquePaiements
        ).not.toHaveBeenCalled();
      }
    );

    it(
      'doit refuser une actualisation sans joueur',
      () => {
        joueurSignal.set(null);
        TestBed.tick();

        service.initialiser();
        service.chargerHistorique();

        expect(service.messageErreur())
          .toBe('Aucun joueur connecté.');

        expect(
          service.rechercheEffectuee()
        ).toBe(true);

        expect(
          paiementApiService
            .consulterHistoriquePaiements
        ).not.toHaveBeenCalled();
      }
    );

    it(
      'doit accepter un historique vide',
      () => {
        paiementApiService
          .consulterHistoriquePaiements
          .mockReturnValue(of([]));

        service.initialiser();

        expect(service.transactions())
          .toEqual([]);

        expect(service.totalPaye())
          .toBe(0);

        expect(
          service.rechercheEffectuee()
        ).toBe(true);

        expect(service.messageErreur())
          .toBe('');
      }
    );

    it(
      'doit exposer une erreur de chargement',
      () => {
        paiementApiService
          .consulterHistoriquePaiements
          .mockReturnValue(
            throwError(
              () =>
                new HttpErrorResponse({
                  status: 500,
                  error: {
                    message:
                      'Historique indisponible.'
                  }
                })
            )
          );

        service.initialiser();

        expect(service.messageErreur())
          .toBe(
            'Historique indisponible.'
          );

        expect(service.transactions())
          .toEqual([]);

        expect(service.chargement())
          .toBe(false);
      }
    );

    it(
      'doit terminer le chargement après expiration du délai',
      () => {
        paiementApiService
          .consulterHistoriquePaiements
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
      'doit charger l historique du nouveau joueur',
      () => {
        service.initialiser();

        joueurSignal.set(joueurDeux);
        TestBed.tick();

        expect(service.joueur())
          .toEqual(joueurDeux);

        expect(
          paiementApiService
            .consulterHistoriquePaiements
        ).toHaveBeenLastCalledWith(
          'G1002'
        );

        expect(service.transactions())
          .toEqual([
            transactionJoueurDeux
          ]);
      }
    );

    it(
      'doit vider l historique après déconnexion',
      () => {
        service.initialiser();

        joueurSignal.set(null);
        TestBed.tick();

        expect(service.joueur()).toBeNull();

        expect(service.transactions())
          .toEqual([]);

        expect(service.totalPaye())
          .toBe(0);

        expect(
          service.rechercheEffectuee()
        ).toBe(false);

        expect(service.messageErreur())
          .toBe('');
      }
    );

    it(
      'doit ignorer un ancien historique après un changement de joueur',
      () => {
        const ancienHistorique$ =
          new Subject<
            HistoriquePaiementResponse[]
          >();

        paiementApiService
          .consulterHistoriquePaiements
          .mockReset();

        paiementApiService
          .consulterHistoriquePaiements
          .mockReturnValueOnce(
            ancienHistorique$
          )
          .mockReturnValueOnce(
            of([
              transactionJoueurDeux
            ])
          );

        service.initialiser();

        joueurSignal.set(joueurDeux);
        TestBed.tick();

        ancienHistorique$.next([
          transaction
        ]);

        expect(service.transactions())
          .toEqual([
            transactionJoueurDeux
          ]);
      }
    );
  }
);
