import {
  signal,
  type Signal
} from '@angular/core';
import {
  ComponentFixture,
  TestBed
} from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthJoueurResponse } from '../../models/auth.model';
import { InvitationPriveeResponse } from '../../models/invitation.model';
import { InvitationsRecuesFacadeService } from '../../services/invitations-recues-facade.service';
import { InvitationsRecuesComponent } from './invitations-recues.component';

describe(
  'InvitationsRecuesComponent',
  () => {
    let fixture:
      ComponentFixture<
        InvitationsRecuesComponent
      >;

    let facade: {
      joueur:
        Signal<AuthJoueurResponse | null>;
      invitations:
        Signal<InvitationPriveeResponse[]>;
      chargement:
        Signal<boolean>;
      rechercheEffectuee:
        Signal<boolean>;
      actionEnCoursParticipationId:
        Signal<number | null>;
      messageErreur:
        Signal<string>;
      messageSucces:
        Signal<string>;
      initialiser:
        ReturnType<typeof vi.fn>;
      chargerInvitations:
        ReturnType<typeof vi.fn>;
      confirmerEtPayer:
        ReturnType<typeof vi.fn>;
      decliner:
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

    beforeEach(async () => {
      facade = {
        joueur:
          signal<
            AuthJoueurResponse | null
          >(joueur).asReadonly(),

        invitations:
          signal([
            invitation
          ]).asReadonly(),

        chargement:
          signal(false).asReadonly(),

        rechercheEffectuee:
          signal(true).asReadonly(),

        actionEnCoursParticipationId:
          signal<number | null>(
            null
          ).asReadonly(),

        messageErreur:
          signal('').asReadonly(),

        messageSucces:
          signal('').asReadonly(),

        initialiser: vi.fn(),
        chargerInvitations: vi.fn(),
        confirmerEtPayer: vi.fn(),
        decliner: vi.fn()
      };

      TestBed.configureTestingModule({
        imports: [
          InvitationsRecuesComponent
        ],
        providers: [
          provideRouter([])
        ]
      });

      TestBed.overrideComponent(
        InvitationsRecuesComponent,
        {
          set: {
            providers: [
              {
                provide:
                InvitationsRecuesFacadeService,
                useValue: facade
              }
            ]
          }
        }
      );

      await TestBed.compileComponents();

      fixture = TestBed.createComponent(
        InvitationsRecuesComponent
      );

      fixture.detectChanges();
    });

    it(
      'doit initialiser la façade',
      () => {
        expect(facade.initialiser)
          .toHaveBeenCalledOnce();
      }
    );

    it(
      'doit déléguer la confirmation et le paiement',
      () => {
        const boutons =
          Array.from(
            fixture.nativeElement
              .querySelectorAll('button')
          ) as HTMLButtonElement[];

        const boutonConfirmation =
          boutons.find(
            bouton =>
              bouton.textContent
                ?.includes(
                  'Confirmer et payer'
                )
          );

        expect(boutonConfirmation)
          .toBeDefined();

        boutonConfirmation?.click();

        expect(
          facade.confirmerEtPayer
        ).toHaveBeenCalledWith(
          invitation
        );
      }
    );
  }
);
