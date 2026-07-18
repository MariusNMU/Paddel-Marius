import { HttpErrorResponse } from '@angular/common/http';
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
import { InvitationPriveeResponse } from '../models/invitation.model';
import { PaiementResponse } from '../models/paiement.model';
import { AuthContextService } from './auth-context.service';
import { InvitationApiService } from './invitation-api.service';
import { InvitationsRecuesFacadeService } from './invitations-recues-facade.service';
import { PaiementApiService } from './paiement-api.service';

describe(
  'InvitationsRecuesFacadeService',
  () => {
    let service:
      InvitationsRecuesFacadeService;

    let joueurSignal:
      WritableSignal<AuthJoueurResponse | null>;

    let authContextService: {
      joueur:
        Signal<AuthJoueurResponse | null>;
    };

    let invitationApiService: {
      listerInvitationsRecues:
        ReturnType<typeof vi.fn>;
      declinerInvitation:
        ReturnType<typeof vi.fn>;
    };

    let paiementApiService: {
      payerParticipationStandard:
        ReturnType<typeof vi.fn>;
    };

    const joueur:
      AuthJoueurResponse = {
      membreId: 1,
      matricule: 'TEST001',
      nom: 'Test',
      prenom: 'Joueur',
      categorieMembre: 'GLOBAL',
      siteRattachementId: null,
      nomSiteRattachement: null,
      actif: true
    };

    const joueurDeux:
      AuthJoueurResponse = {
      membreId: 2,
      matricule: 'TEST002',
      nom: 'Deux',
      prenom: 'Joueur',
      categorieMembre: 'SITE',
      siteRattachementId: 1001,
      nomSiteRattachement:
        'Padel Bruxelles',
      actif: true
    };

    const invitation:
      InvitationPriveeResponse = {
      participationId: 10,
      matchId: 20,
      siteId: 1,
      nomSite: 'Site Alpha',
      terrainId: 30,
      numeroTerrain: 'T1',
      dateHeureDebut:
        '2026-06-20T09:00:00',
      dateHeureFin:
        '2026-06-20T10:30:00',
      organisateurId: 40,
      matriculeOrganisateur: 'ORG001',
      nomOrganisateur: 'Organisateur',
      prenomOrganisateur: 'Test',
      joueurInviteId: 1,
      matriculeInvite: 'TEST001',
      nomInvite: 'Test',
      prenomInvite: 'Joueur',
      statutParticipation:
        'EN_ATTENTE_PAIEMENT'
    };

    const invitationDeux:
      InvitationPriveeResponse = {
      ...invitation,
      participationId: 11,
      joueurInviteId: 2,
      matriculeInvite: 'TEST002',
      nomInvite: 'Deux'
    };

    const paiement:
      PaiementResponse = {
      paiementId: 100,
      participationId: 10,
      membreId: 1,
      matriculeMembre: 'TEST001',
      montant: 15,
      montantDettesReglees: 0,
      montantTotalDebite: 15,
      naturePaiement: 'PARTICIPATION',
      statutPaiement: 'PAYE',
      statutParticipation: 'CONFIRMEE',
      dateHeurePaiement:
        '2026-06-20T08:00:00',
      dateConfirmationParticipation:
        '2026-06-20T08:00:00'
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

      invitationApiService = {
        listerInvitationsRecues:
          vi.fn(() => of([invitation])),

        declinerInvitation:
          vi.fn(() => of({
            ...invitation,
            statutParticipation:
              'LIBEREE'
          }))
      };

      paiementApiService = {
        payerParticipationStandard:
          vi.fn(() => of(paiement))
      };

      TestBed.configureTestingModule({
        providers: [
          InvitationsRecuesFacadeService,
          {
            provide:
            AuthContextService,
            useValue:
            authContextService
          },
          {
            provide:
            InvitationApiService,
            useValue:
            invitationApiService
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
        InvitationsRecuesFacadeService
      );
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it(
      'doit charger les invitations à l initialisation',
      () => {
        service.initialiser();

        expect(
          invitationApiService
            .listerInvitationsRecues
        ).toHaveBeenCalledWith(
          'TEST001'
        );

        expect(service.joueur())
          .toEqual(joueur);

        expect(service.invitations())
          .toEqual([invitation]);

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

        expect(service.invitations())
          .toEqual([]);

        expect(
          service.rechercheEffectuee()
        ).toBe(false);

        expect(
          invitationApiService
            .listerInvitationsRecues
        ).not.toHaveBeenCalled();
      }
    );

    it(
      'doit refuser une actualisation sans joueur',
      () => {
        joueurSignal.set(null);
        TestBed.tick();

        service.chargerInvitations();

        expect(service.messageErreur())
          .toBe(
            'Aucun joueur connecté.'
          );

        expect(
          service.rechercheEffectuee()
        ).toBe(true);

        expect(
          invitationApiService
            .listerInvitationsRecues
        ).not.toHaveBeenCalled();
      }
    );

    it(
      'doit effacer le message de succès lors d une actualisation normale',
      () => {
        service.initialiser();
        service.confirmerEtPayer(invitation);

        expect(service.messageSucces())
          .toBe(
            'Invitation confirmée et participation payée.'
          );

        service.chargerInvitations();

        expect(service.messageSucces())
          .toBe('');
      }
    );

    it(
      'doit exposer une erreur de chargement',
      () => {
        invitationApiService
          .listerInvitationsRecues
          .mockReturnValue(
            throwError(
              () =>
                new HttpErrorResponse({
                  status: 500,
                  error: {
                    message:
                      'Erreur backend invitations.'
                  }
                })
            )
          );

        service.initialiser();

        expect(service.messageErreur())
          .toBe(
            'Erreur backend invitations.'
          );

        expect(service.invitations())
          .toEqual([]);

        expect(service.chargement())
          .toBe(false);
      }
    );

    it(
      'doit terminer un chargement après expiration du délai',
      () => {
        invitationApiService
          .listerInvitationsRecues
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
      'doit confirmer et payer une invitation',
      () => {
        service.initialiser();
        service.confirmerEtPayer(invitation);

        expect(
          paiementApiService
            .payerParticipationStandard
        ).toHaveBeenCalledWith(10);

        expect(service.messageSucces())
          .toBe(
            'Invitation confirmée et participation payée.'
          );

        expect(
          invitationApiService
            .listerInvitationsRecues
        ).toHaveBeenCalledTimes(2);

        expect(
          service
            .actionEnCoursParticipationId()
        ).toBeNull();
      }
    );

    it(
      'doit exposer une erreur de paiement',
      () => {
        paiementApiService
          .payerParticipationStandard
          .mockReturnValue(
            throwError(
              () =>
                new HttpErrorResponse({
                  status: 409,
                  error: {
                    message:
                      'Paiement impossible.'
                  }
                })
            )
          );

        service.initialiser();
        service.confirmerEtPayer(invitation);

        expect(service.messageErreur())
          .toBe('Paiement impossible.');

        expect(service.messageSucces())
          .toBe('');

        expect(
          service
            .actionEnCoursParticipationId()
        ).toBeNull();
      }
    );

    it(
      'doit terminer un paiement après expiration du délai',
      () => {
        paiementApiService
          .payerParticipationStandard
          .mockReturnValue(NEVER);

        service.initialiser();
        service.confirmerEtPayer(invitation);

        expect(
          service
            .actionEnCoursParticipationId()
        ).toBe(10);

        vi.advanceTimersByTime(10001);

        expect(service.messageErreur())
          .toBe(messageErreurGenerique);

        expect(
          service
            .actionEnCoursParticipationId()
        ).toBeNull();
      }
    );

    it(
      'doit refuser le paiement sans joueur',
      () => {
        joueurSignal.set(null);
        TestBed.tick();

        service.initialiser();
        service.confirmerEtPayer(invitation);

        expect(service.messageErreur())
          .toBe(
            'Aucun joueur connecté.'
          );

        expect(
          paiementApiService
            .payerParticipationStandard
        ).not.toHaveBeenCalled();
      }
    );

    it(
      'doit décliner une invitation',
      () => {
        service.initialiser();
        service.decliner(invitation);

        expect(
          invitationApiService
            .declinerInvitation
        ).toHaveBeenCalledWith(
          10,
          {
            matriculeJoueur: 'TEST001'
          }
        );

        expect(service.messageSucces())
          .toBe('Invitation déclinée.');

        expect(
          invitationApiService
            .listerInvitationsRecues
        ).toHaveBeenCalledTimes(2);

        expect(
          service
            .actionEnCoursParticipationId()
        ).toBeNull();
      }
    );

    it(
      'doit exposer une erreur de refus',
      () => {
        invitationApiService
          .declinerInvitation
          .mockReturnValue(
            throwError(
              () =>
                new HttpErrorResponse({
                  status: 409,
                  error: {
                    message:
                      'Refus impossible.'
                  }
                })
            )
          );

        service.initialiser();
        service.decliner(invitation);

        expect(service.messageErreur())
          .toBe('Refus impossible.');

        expect(service.messageSucces())
          .toBe('');

        expect(
          service
            .actionEnCoursParticipationId()
        ).toBeNull();
      }
    );

    it(
      'doit terminer un refus après expiration du délai',
      () => {
        invitationApiService
          .declinerInvitation
          .mockReturnValue(NEVER);

        service.initialiser();
        service.decliner(invitation);

        expect(
          service
            .actionEnCoursParticipationId()
        ).toBe(10);

        vi.advanceTimersByTime(10001);

        expect(service.messageErreur())
          .toBe(messageErreurGenerique);

        expect(
          service
            .actionEnCoursParticipationId()
        ).toBeNull();
      }
    );

    it(
      'doit refuser de décliner sans joueur',
      () => {
        joueurSignal.set(null);
        TestBed.tick();

        service.initialiser();
        service.decliner(invitation);

        expect(service.messageErreur())
          .toBe(
            'Aucun joueur connecté.'
          );

        expect(
          invitationApiService
            .declinerInvitation
        ).not.toHaveBeenCalled();
      }
    );

    it(
      'doit charger les invitations du nouveau joueur',
      () => {
        invitationApiService
          .listerInvitationsRecues
          .mockImplementation(
            (matricule: string) =>
              of(
                matricule === 'TEST002'
                  ? [invitationDeux]
                  : [invitation]
              )
          );

        service.initialiser();

        joueurSignal.set(joueurDeux);
        TestBed.tick();

        expect(service.joueur())
          .toEqual(joueurDeux);

        expect(
          invitationApiService
            .listerInvitationsRecues
        ).toHaveBeenLastCalledWith(
          'TEST002'
        );

        expect(service.invitations())
          .toEqual([invitationDeux]);
      }
    );

    it(
      'doit vider le parcours après déconnexion',
      () => {
        service.initialiser();
        service.confirmerEtPayer(invitation);

        joueurSignal.set(null);
        TestBed.tick();

        expect(service.joueur())
          .toBeNull();

        expect(service.invitations())
          .toEqual([]);

        expect(
          service.rechercheEffectuee()
        ).toBe(false);

        expect(service.messageErreur())
          .toBe('');

        expect(service.messageSucces())
          .toBe('');

        expect(
          service
            .actionEnCoursParticipationId()
        ).toBeNull();
      }
    );

    it(
      'doit ignorer une ancienne liste après un changement de joueur',
      () => {
        const ancienneListe$ =
          new Subject<
            InvitationPriveeResponse[]
          >();

        invitationApiService
          .listerInvitationsRecues
          .mockReset();

        invitationApiService
          .listerInvitationsRecues
          .mockReturnValueOnce(
            ancienneListe$
          )
          .mockReturnValueOnce(
            of([invitationDeux])
          );

        service.initialiser();

        joueurSignal.set(joueurDeux);
        TestBed.tick();

        ancienneListe$.next([
          invitation
        ]);

        expect(service.invitations())
          .toEqual([invitationDeux]);
      }
    );

    it(
      'doit ignorer un ancien paiement après déconnexion',
      () => {
        const ancienPaiement$ =
          new Subject<PaiementResponse>();

        paiementApiService
          .payerParticipationStandard
          .mockReturnValue(
            ancienPaiement$
          );

        service.initialiser();
        service.confirmerEtPayer(invitation);

        joueurSignal.set(null);
        TestBed.tick();

        ancienPaiement$.next(paiement);

        expect(service.messageSucces())
          .toBe('');

        expect(service.invitations())
          .toEqual([]);

        expect(
          service
            .actionEnCoursParticipationId()
        ).toBeNull();
      }
    );

    it(
      'doit ignorer un ancien refus après déconnexion',
      () => {
        const ancienRefus$ =
          new Subject<
            InvitationPriveeResponse
          >();

        invitationApiService
          .declinerInvitation
          .mockReturnValue(ancienRefus$);

        service.initialiser();
        service.decliner(invitation);

        joueurSignal.set(null);
        TestBed.tick();

        ancienRefus$.next({
          ...invitation,
          statutParticipation: 'LIBEREE'
        });

        expect(service.messageSucces())
          .toBe('');

        expect(service.invitations())
          .toEqual([]);

        expect(
          service
            .actionEnCoursParticipationId()
        ).toBeNull();
      }
    );

    it(
      'doit annuler l action en cours lors d un changement de joueur',
      () => {
        const paiementEnCours$ =
          new Subject<PaiementResponse>();

        paiementApiService
          .payerParticipationStandard
          .mockReturnValue(
            paiementEnCours$
          );

        invitationApiService
          .listerInvitationsRecues
          .mockImplementation(
            (matricule: string) =>
              of(
                matricule === 'TEST002'
                  ? [invitationDeux]
                  : [invitation]
              )
          );

        service.initialiser();
        service.confirmerEtPayer(invitation);

        expect(
          service
            .actionEnCoursParticipationId()
        ).toBe(10);

        joueurSignal.set(joueurDeux);
        TestBed.tick();

        expect(
          service
            .actionEnCoursParticipationId()
        ).toBeNull();

        expect(service.messageSucces())
          .toBe('');

        expect(service.invitations())
          .toEqual([invitationDeux]);
      }
    );
  }
);
