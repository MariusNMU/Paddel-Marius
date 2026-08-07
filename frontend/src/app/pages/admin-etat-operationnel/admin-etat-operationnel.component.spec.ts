import {
  signal,
  type WritableSignal
} from '@angular/core';
import {
  ComponentFixture,
  TestBed
} from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthAdminResponse } from '../../models/auth.model';
import {
  OccupationHebdomadaireAdminResponse
} from '../../models/etat-operationnel.model';
import { SiteResponse } from '../../models/site.model';
import {
  AdminEtatOperationnelFacadeService
} from '../../services/admin-etat-operationnel-facade.service';
import {
  AdminEtatOperationnelComponent
} from './admin-etat-operationnel.component';

describe(
  'AdminEtatOperationnelComponent',
  () => {
    let fixture:
      ComponentFixture<AdminEtatOperationnelComponent>;

    let component:
      AdminEtatOperationnelComponent;

    let facade: {
      initialiser:
        ReturnType<typeof vi.fn>;

      estAdminGlobal:
        ReturnType<typeof vi.fn>;

      modifierDate:
        ReturnType<typeof vi.fn>;

      modifierSiteId:
        ReturnType<typeof vi.fn>;

      chargerOccupationHebdomadaire:
        ReturnType<typeof vi.fn>;

      decalerSemaine:
        ReturnType<typeof vi.fn>;

      selectionnerSemaineCourante:
        ReturnType<typeof vi.fn>;

      admin:
        WritableSignal<AuthAdminResponse | null>;

      sites:
        WritableSignal<SiteResponse[]>;

      date:
        WritableSignal<string>;

      siteId:
        WritableSignal<number | null>;

      chargementSites:
        WritableSignal<boolean>;

      chargement:
        WritableSignal<boolean>;

      messageErreur:
        WritableSignal<string>;

      occupationHebdomadaire:
        WritableSignal<
          OccupationHebdomadaireAdminResponse | null
        >;
    };

    const adminGlobal:
      AuthAdminResponse = {
      administrateurId: 1,
      login: 'admin-global',
      nom: 'Admin',
      prenom: 'Global',
      roleAdministrateur: 'GLOBAL',
      siteId: null,
      nomSite: null,
      actif: true
    };

    const adminSite:
      AuthAdminResponse = {
      administrateurId: 2,
      login: 'admin-bruxelles',
      nom: 'Admin',
      prenom: 'Bruxelles',
      roleAdministrateur: 'SITE',
      siteId: 1001,
      nomSite: 'Padel Bruxelles',
      actif: true
    };

    const site: SiteResponse = {
      siteId: 1001,
      code: 'BRU',
      nom: 'Padel Bruxelles',
      adresse: 'Rue du Padel 1'
    };

    const occupationHebdomadaire:
      OccupationHebdomadaireAdminResponse = {
      dateDebut: '2026-07-20',
      dateFin: '2026-07-26',
      siteId: 1001,
      nomSite: 'Padel Bruxelles',
      siteActif: true,
      jours: [
        {
          date: '2026-07-20',
          siteId: 1001,
          nomSite: 'Padel Bruxelles',
          siteActif: true,
          ferme: false,
          motifFermeture: null,
          terrains: [
            {
              terrainId: 2001,
              numeroTerrain: 'T1',
              actif: true,
              etatTerrain: 'RESERVE',
              matches: [
                {
                  matchId: 3001,
                  dateHeureDebut:
                    '2026-07-20T10:00:00',
                  dateHeureFin:
                    '2026-07-20T11:30:00',
                  visibiliteCourante: 'PUBLIC',
                  etatCycle: 'A_VENIR',
                  nombreParticipants: 3
                },
                {
                  matchId: 3002,
                  dateHeureDebut:
                    '2026-07-20T14:00:00',
                  dateHeureFin:
                    '2026-07-20T15:30:00',
                  visibiliteCourante: 'PRIVE',
                  etatCycle: 'ANNULE',
                  nombreParticipants: 2
                }
              ]
            }
          ]
        },
        {
          date: '2026-07-21',
          siteId: 1001,
          nomSite: 'Padel Bruxelles',
          siteActif: true,
          ferme: true,
          motifFermeture: 'Entretien annuel',
          terrains: [
            {
              terrainId: 2001,
              numeroTerrain: 'T1',
              actif: true,
              etatTerrain: 'FERME',
              matches: []
            }
          ]
        }
      ]
    };

    beforeEach(async () => {
      facade = {
        initialiser: vi.fn(),

        estAdminGlobal: vi.fn(
          () =>
            facade.admin()
              ?.roleAdministrateur
            === 'GLOBAL'
        ),

        modifierDate: vi.fn(),
        modifierSiteId: vi.fn(),
        chargerOccupationHebdomadaire:
          vi.fn(),
        decalerSemaine: vi.fn(),
        selectionnerSemaineCourante:
          vi.fn(),

        admin: signal(adminGlobal),
        sites: signal([site]),
        date: signal('2026-07-20'),
        siteId: signal(1001),
        chargementSites:
          signal(false),
        chargement:
          signal(false),
        messageErreur:
          signal(''),
        occupationHebdomadaire:
          signal(null)
      };

      await TestBed
        .configureTestingModule({
          imports: [
            AdminEtatOperationnelComponent
          ],
          providers: [
            provideRouter([])
          ]
        })
        .overrideComponent(
          AdminEtatOperationnelComponent,
          {
            set: {
              providers: [
                {
                  provide:
                  AdminEtatOperationnelFacadeService,
                  useValue:
                  facade
                }
              ]
            }
          }
        )
        .compileComponents();

      fixture =
        TestBed.createComponent(
          AdminEtatOperationnelComponent
        );

      component =
        fixture.componentInstance;

      fixture.detectChanges();
    });

    it(
      'doit créer le composant et initialiser la façade',
      () => {
        expect(component).toBeTruthy();

        expect(
          facade.initialiser
        ).toHaveBeenCalledTimes(1);
      }
    );

    it(
      'doit déléguer le chargement hebdomadaire',
      () => {
        component.chargerOccupationHebdomadaire();

        expect(
          facade.chargerOccupationHebdomadaire
        ).toHaveBeenCalledTimes(1);
      }
    );

    it(
      'doit afficher le sélecteur de site à l admin GLOBAL',
      () => {
        const selecteurSite =
          fixture.nativeElement
            .querySelector(
              'select#siteId'
            );

        const contenu =
          fixture.nativeElement
            .textContent as string;

        expect(selecteurSite)
          .not.toBeNull();

        expect(contenu)
          .toContain('Padel Bruxelles');
      }
    );

    it(
      'doit afficher le planning, les réservations et les fermetures',
      () => {
        facade.occupationHebdomadaire.set(
          occupationHebdomadaire
        );

        fixture.detectChanges();

        const contenu =
          fixture.nativeElement
            .textContent as string;

        expect(contenu)
          .toContain('Planning central');

        expect(contenu)
          .toContain('Terrain T1');

        expect(contenu)
          .toContain('10:00');

        expect(contenu)
          .toContain('11:30');

        expect(contenu)
          .toContain('3 / 4 joueurs');

        expect(contenu)
          .toContain('Match annulé');

        expect(contenu)
          .toContain('Entretien annuel');

        expect(
          component.nombreReservationsActives(
            occupationHebdomadaire
          )
        ).toBe(1);

        expect(
          component.nombreJoursFermes(
            occupationHebdomadaire
          )
        ).toBe(1);
      }
    );

    it(
      'doit déléguer la navigation entre les semaines',
      () => {
        facade.occupationHebdomadaire.set(
          occupationHebdomadaire
        );

        fixture.detectChanges();

        const boutons = Array.from(
          fixture.nativeElement
            .querySelectorAll(
              '.navigation-semaine button'
            ) as NodeListOf<HTMLButtonElement>
        );

        boutons[0].click();
        boutons[1].click();
        boutons[2].click();

        expect(
          facade.decalerSemaine
        ).toHaveBeenNthCalledWith(1, -1);

        expect(
          facade.selectionnerSemaineCourante
        ).toHaveBeenCalledTimes(1);

        expect(
          facade.decalerSemaine
        ).toHaveBeenNthCalledWith(2, 1);
      }
    );

    it(
      'doit afficher le site imposé à l admin SITE',
      () => {
        facade.admin.set(adminSite);

        fixture.detectChanges();

        const selecteurSite =
          fixture.nativeElement
            .querySelector(
              'select#siteId'
            );

        const contenu =
          fixture.nativeElement
            .textContent as string;

        expect(selecteurSite).toBeNull();
        expect(contenu)
          .toContain('Site administré');
        expect(contenu)
          .toContain('Padel Bruxelles');
      }
    );

    it(
      'doit afficher un message sans administrateur connecté',
      () => {
        facade.admin.set(null);

        fixture.detectChanges();

        const contenu =
          fixture.nativeElement
            .textContent as string;

        expect(contenu)
          .toContain(
            'Tu dois te connecter comme admin'
          );

        expect(contenu)
          .toContain('Connexion admin');
      }
    );
  }
);
