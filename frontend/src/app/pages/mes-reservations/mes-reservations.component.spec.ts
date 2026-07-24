import {
  signal,
  WritableSignal
} from '@angular/core';
import {
  ComponentFixture,
  TestBed
} from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthJoueurResponse } from '../../models/auth.model';
import { PaiementResponse } from '../../models/paiement.model';
import { ReservationJoueurResponse } from '../../models/reservation.model';
import { MesReservationsFacadeService } from '../../services/mes-reservations-facade.service';
import { MesReservationsComponent } from './mes-reservations.component';

describe(
  'MesReservationsComponent',
  () => {
    let fixture:
      ComponentFixture<MesReservationsComponent>;

    let component:
      MesReservationsComponent;

    let facade: {
      initialiser:
        ReturnType<typeof vi.fn>;

      chargerReservations:
        ReturnType<typeof vi.fn>;

      payerParticipation:
        ReturnType<typeof vi.fn>;

      joueur:
        WritableSignal<AuthJoueurResponse | null>;

      reservations:
        WritableSignal<
          ReservationJoueurResponse[]
        >;

      messageErreur:
        WritableSignal<string>;

      messageSucces:
        WritableSignal<string>;

      chargement:
        WritableSignal<boolean>;

      rechercheEffectuee:
        WritableSignal<boolean>;

      paiementEnCoursParticipationId:
        WritableSignal<number | null>;

      dernierPaiement:
        WritableSignal<PaiementResponse | null>;
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

    beforeEach(async () => {
      facade = {
        initialiser: vi.fn(),
        chargerReservations: vi.fn(),
        payerParticipation: vi.fn(),

        joueur:
          signal<AuthJoueurResponse | null>(
            null
          ),

        reservations:
          signal<
            ReservationJoueurResponse[]
          >([]),

        messageErreur: signal(''),
        messageSucces: signal(''),
        chargement: signal(false),
        rechercheEffectuee: signal(false),

        paiementEnCoursParticipationId:
          signal<number | null>(null),

        dernierPaiement:
          signal<PaiementResponse | null>(
            null
          )
      };

      await TestBed
        .configureTestingModule({
          imports: [
            MesReservationsComponent
          ],
          providers: [
            provideRouter([])
          ]
        })
        .overrideComponent(
          MesReservationsComponent,
          {
            set: {
              providers: [
                {
                  provide:
                  MesReservationsFacadeService,
                  useValue:
                  facade
                }
              ]
            }
          }
        )
        .compileComponents();

      fixture = TestBed.createComponent(
        MesReservationsComponent
      );

      component =
        fixture.componentInstance;

      fixture.detectChanges();
    });

    it(
      'doit créer le composant et initialiser la façade',
      () => {
        expect(component).toBeTruthy();

        expect(facade.initialiser)
          .toHaveBeenCalled();
      }
    );

    it(
      'doit afficher l absence de joueur connecté',
      () => {
        const contenu =
          fixture.nativeElement
            .textContent as string;

        expect(contenu)
          .toContain(
            'Aucun joueur connecté'
          );
      }
    );

    it(
      'doit afficher le joueur et ses réservations',
      () => {
        facade.joueur.set(joueur);

        facade.reservations.set([
          reservation
        ]);

        fixture.detectChanges();

        const contenu =
          fixture.nativeElement
            .textContent as string;

        expect(contenu)
          .toContain('G1001');

        expect(contenu)
          .toContain('Dupont');

        expect(contenu)
          .toContain('Marie');

        expect(contenu)
          .toContain(
            'Réservation du 20/06/2026, 09:00'
          );

        expect(contenu)
          .not.toContain('Match #3001');

        expect(contenu)
          .not.toContain('(1101)');

        expect(contenu)
          .toContain('Padel Bruxelles');

        expect(contenu)
          .toContain(
            'Participation en attente de paiement.'
          );
      }
    );

    it(
      'doit afficher une annulation administrative sans demander de paiement',
      () => {
        facade.joueur.set(joueur);

        facade.reservations.set([
          {
            ...reservation,
            etatCycle: 'ANNULE'
          }
        ]);

        fixture.detectChanges();

        const contenu =
          fixture.nativeElement
            .textContent as string;

        expect(contenu)
          .toContain(
            'Match annulé à la suite d\'une fermeture administrative.'
          );

        expect(contenu)
          .toContain(
            'Aucun paiement n\'est requis pour cette réservation.'
          );

        expect(contenu)
          .toContain(
            'Annulée'
          );

        expect(contenu)
          .not.toContain(
            'Participation en attente de paiement.'
          );

        expect(contenu)
          .not.toContain(
            'Payer ma participation'
          );

        expect(
          facade.payerParticipation
        ).not.toHaveBeenCalled();
      }
    );

    it(
      'doit déléguer l actualisation à la façade',
      () => {
        facade.joueur.set(joueur);
        fixture.detectChanges();

        const boutons =
          Array.from(
            fixture.nativeElement
              .querySelectorAll('button')
          ) as HTMLButtonElement[];

        const boutonActualiser =
          boutons.find(
            bouton =>
              bouton.textContent
                ?.includes(
                  'Actualiser mes réservations'
                )
          );

        boutonActualiser?.click();

        expect(
          facade.chargerReservations
        ).toHaveBeenCalled();
      }
    );

    it(
      'doit déléguer le paiement à la façade',
      () => {
        facade.joueur.set(joueur);

        facade.reservations.set([
          reservation
        ]);

        fixture.detectChanges();

        const boutons =
          Array.from(
            fixture.nativeElement
              .querySelectorAll('button')
          ) as HTMLButtonElement[];

        const boutonPaiement =
          boutons.find(
            bouton =>
              bouton.textContent
                ?.includes(
                  'Payer ma participation'
                )
          );

        boutonPaiement?.click();

        expect(
          facade.payerParticipation
        ).toHaveBeenCalledWith(
          reservation
        );
      }
    );

    it(
      'doit afficher le détail du paiement',
      () => {
        facade.joueur.set(joueur);
        facade.dernierPaiement.set(
          paiement
        );

        facade.messageSucces.set(
          'Participation payée avec succès.'
        );

        fixture.detectChanges();

        const contenu =
          fixture.nativeElement
            .textContent as string;

        expect(contenu)
          .toContain(
            'Paiement enregistré'
          );

        expect(contenu)
          .toContain('15.00');

        expect(contenu)
          .toContain('30.00');

        expect(contenu)
          .toContain('45.00');

        expect(contenu)
          .toContain(
            'Participation payée avec succès.'
          );
      }
    );

    it(
      'doit afficher l erreur de la façade',
      () => {
        facade.messageErreur.set(
          'Erreur backend réservations.'
        );

        fixture.detectChanges();

        const contenu =
          fixture.nativeElement
            .textContent as string;

        expect(contenu)
          .toContain(
            'Erreur backend réservations.'
          );
      }
    );
  }
);
