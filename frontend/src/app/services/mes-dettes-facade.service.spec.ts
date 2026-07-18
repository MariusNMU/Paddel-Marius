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
import {
  DetteResponse,
  PaiementDetteResponse
} from '../models/dette.model';
import { AuthContextService } from './auth-context.service';
import { DetteApiService } from './dette-api.service';
import { MesDettesFacadeService } from './mes-dettes-facade.service';

describe('MesDettesFacadeService', () => {
  let service: MesDettesFacadeService;

  let joueurSignal:
    WritableSignal<AuthJoueurResponse | null>;

  let authContextService: {
    joueur:
      Signal<AuthJoueurResponse | null>;
  };

  let detteApiService: {
    consulterDettesOuvertes:
      ReturnType<typeof vi.fn>;
    payerDette:
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

  const dette: DetteResponse = {
    detteId: 4001,
    matchId: 3001,
    membreResponsableId: 2001,
    matriculeResponsable: 'G1001',
    montantInitial: 45,
    montantRestant: 45,
    statutDette: 'OUVERTE',
    dateCreation:
      '2026-06-01T10:00:00',
    dateReglement: null
  };

  const detteDeux: DetteResponse = {
    detteId: 4002,
    matchId: 3002,
    membreResponsableId: 2002,
    matriculeResponsable: 'G1002',
    montantInitial: 30,
    montantRestant: 30,
    statutDette: 'OUVERTE',
    dateCreation:
      '2026-06-02T10:00:00',
    dateReglement: null
  };

  const paiement:
    PaiementDetteResponse = {
    paiementId: 5001,
    detteId: 4001,
    membreId: 2001,
    matriculeMembre: 'G1001',
    naturePaiement: 'REGLEMENT_DETTE',
    montant: 45,
    statutPaiement: 'PAYE',
    statutDette: 'REGLEE',
    dateHeurePaiement:
      '2026-06-03T12:00:00',
    dateReglementDette:
      '2026-06-03T12:00:00'
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

    detteApiService = {
      consulterDettesOuvertes:
        vi.fn((matricule: string) =>
          of(
            matricule === 'G1002'
              ? [detteDeux]
              : [dette]
          )
        ),
      payerDette:
        vi.fn(() => of(paiement))
    };

    TestBed.configureTestingModule({
      providers: [
        MesDettesFacadeService,
        {
          provide: AuthContextService,
          useValue: authContextService
        },
        {
          provide: DetteApiService,
          useValue: detteApiService
        }
      ]
    });

    service = TestBed.inject(
      MesDettesFacadeService
    );
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it(
    'doit charger les dettes et les montants à l initialisation',
    () => {
      service.initialiser();

      expect(
        detteApiService
          .consulterDettesOuvertes
      ).toHaveBeenCalledWith('G1001');

      expect(service.dettes())
        .toEqual([dette]);

      expect(
        service.montantPaiement(4001)
      ).toBe(45);

      expect(
        service.totalMontantRestant()
      ).toBe(45);

      expect(
        service.rechercheEffectuee()
      ).toBe(true);

      expect(service.chargement())
        .toBe(false);
    }
  );

  it(
    'doit rester vide et refuser les actions sans joueur',
    () => {
      joueurSignal.set(null);
      TestBed.tick();

      service.initialiser();
      service.chargerDettes();
      service.payerDette(dette);

      expect(service.joueur()).toBeNull();
      expect(service.dettes()).toEqual([]);

      expect(service.messageErreur())
        .toContain(
          'Aucun joueur connecté'
        );

      expect(
        detteApiService
          .consulterDettesOuvertes
      ).not.toHaveBeenCalled();

      expect(
        detteApiService.payerDette
      ).not.toHaveBeenCalled();
    }
  );

  it(
    'doit mémoriser un montant saisi',
    () => {
      service.initialiser();

      service.modifierMontantPaiement(
        4001,
        40
      );

      expect(
        service.montantPaiement(4001)
      ).toBe(40);

      expect(
        service.montantsPaiement()
      ).toEqual({
        4001: 40
      });
    }
  );

  it(
    'doit exposer une erreur de chargement',
    () => {
      detteApiService
        .consulterDettesOuvertes
        .mockReturnValue(
          throwError(
            () =>
              new HttpErrorResponse({
                status: 500,
                error: {
                  message:
                    'Erreur backend dettes.'
                }
              })
          )
        );

      service.initialiser();

      expect(service.messageErreur())
        .toBe('Erreur backend dettes.');

      expect(service.dettes())
        .toEqual([]);

      expect(service.chargement())
        .toBe(false);
    }
  );

  it(
    'doit terminer le chargement après expiration du délai',
    () => {
      detteApiService
        .consulterDettesOuvertes
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
    'doit refuser un montant nul',
    () => {
      service.initialiser();

      service.modifierMontantPaiement(
        4001,
        0
      );

      service.payerDette(dette);

      expect(service.messageErreur())
        .toBe(
          'Le montant du paiement doit être supérieur à 0.'
        );

      expect(
        detteApiService.payerDette
      ).not.toHaveBeenCalled();
    }
  );

  it(
    'doit payer puis retirer la dette',
    () => {
      service.initialiser();
      service.payerDette(dette);

      expect(
        detteApiService.payerDette
      ).toHaveBeenCalledWith(
        4001,
        { montant: 45 }
      );

      expect(service.messageSucces())
        .toBe(
          'Paiement réussi : dette 4001 payée pour 45 €.'
        );

      expect(service.dettes())
        .toEqual([]);

      expect(
        service.montantPaiement(4001)
      ).toBe(0);

      expect(
        service.totalMontantRestant()
      ).toBe(0);

      expect(
        service.paiementEnCoursDetteId()
      ).toBeNull();
    }
  );

  it(
    'doit exposer une erreur de paiement',
    () => {
      detteApiService.payerDette
        .mockReturnValue(
          throwError(
            () =>
              new HttpErrorResponse({
                status: 409,
                error: {
                  message:
                    'Solde insuffisant pour régler cette dette.'
                }
              })
          )
        );

      service.initialiser();
      service.payerDette(dette);

      expect(service.messageErreur())
        .toBe(
          'Solde insuffisant pour régler cette dette.'
        );

      expect(service.dettes())
        .toEqual([dette]);

      expect(
        service.paiementEnCoursDetteId()
      ).toBeNull();
    }
  );

  it(
    'doit terminer le paiement après expiration du délai',
    () => {
      detteApiService.payerDette
        .mockReturnValue(NEVER);

      service.initialiser();
      service.payerDette(dette);

      expect(
        service.paiementEnCoursDetteId()
      ).toBe(4001);

      vi.advanceTimersByTime(10001);

      expect(service.messageErreur())
        .toBe(messageErreurGenerique);

      expect(
        service.paiementEnCoursDetteId()
      ).toBeNull();
    }
  );

  it(
    'doit charger les dettes du nouveau joueur',
    () => {
      service.initialiser();

      joueurSignal.set(joueurDeux);
      TestBed.tick();

      expect(service.joueur())
        .toEqual(joueurDeux);

      expect(
        detteApiService
          .consulterDettesOuvertes
      ).toHaveBeenLastCalledWith(
        'G1002'
      );

      expect(service.dettes())
        .toEqual([detteDeux]);

      expect(
        service.montantPaiement(4002)
      ).toBe(30);
    }
  );

  it(
    'doit vider le parcours après déconnexion',
    () => {
      service.initialiser();
      service.payerDette(dette);

      joueurSignal.set(null);
      TestBed.tick();

      expect(service.joueur()).toBeNull();
      expect(service.dettes()).toEqual([]);

      expect(
        service.montantsPaiement()
      ).toEqual({});

      expect(
        service.rechercheEffectuee()
      ).toBe(false);

      expect(service.messageErreur())
        .toBe('');

      expect(service.messageSucces())
        .toBe('');

      expect(
        service.paiementEnCoursDetteId()
      ).toBeNull();
    }
  );

  it(
    'doit ignorer une ancienne liste après un changement de joueur',
    () => {
      const ancienneListe$ =
        new Subject<DetteResponse[]>();

      detteApiService
        .consulterDettesOuvertes
        .mockReset();

      detteApiService
        .consulterDettesOuvertes
        .mockReturnValueOnce(
          ancienneListe$
        )
        .mockReturnValueOnce(
          of([detteDeux])
        );

      service.initialiser();

      joueurSignal.set(joueurDeux);
      TestBed.tick();

      ancienneListe$.next([dette]);

      expect(service.dettes())
        .toEqual([detteDeux]);
    }
  );

  it(
    'doit ignorer un ancien paiement après déconnexion',
    () => {
      const ancienPaiement$ =
        new Subject<PaiementDetteResponse>();

      detteApiService.payerDette
        .mockReturnValue(
          ancienPaiement$
        );

      service.initialiser();
      service.payerDette(dette);

      joueurSignal.set(null);
      TestBed.tick();

      ancienPaiement$.next(paiement);

      expect(service.messageSucces())
        .toBe('');

      expect(service.dettes())
        .toEqual([]);
    }
  );

  it(
    'doit empêcher deux paiements simultanés',
    () => {
      const paiementEnCours$ =
        new Subject<PaiementDetteResponse>();

      detteApiService.payerDette
        .mockReturnValue(
          paiementEnCours$
        );

      service.initialiser();

      service.payerDette(dette);
      service.payerDette(dette);

      expect(
        detteApiService.payerDette
      ).toHaveBeenCalledTimes(1);

      expect(
        service.paiementEnCoursDetteId()
      ).toBe(4001);
    }
  );
});
