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
  EtatOperationnelAdminResponse
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

      chargerEtatOperationnel:
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

      etatOperationnel:
        WritableSignal<
          EtatOperationnelAdminResponse | null
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

    const site: SiteResponse = {
      siteId: 1001,
      code: 'BRU',
      nom: 'Padel Bruxelles',
      adresse: 'Rue du Padel 1'
    };

    const etatOperationnel:
      EtatOperationnelAdminResponse = {
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
              visibiliteCourante:
                'PUBLIC',
              etatCycle: 'A_VENIR',
              nombreParticipants: 3
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
        chargerEtatOperationnel:
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
        etatOperationnel:
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
      'doit déléguer le chargement de l état',
      () => {
        component.chargerEtatOperationnel();

        expect(
          facade.chargerEtatOperationnel
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
      'doit afficher les terrains et les matchs reçus',
      () => {
        facade.etatOperationnel.set(
          etatOperationnel
        );

        fixture.detectChanges();

        const contenu =
          fixture.nativeElement
            .textContent as string;

        expect(contenu)
          .toContain('Vue affichée');

        expect(contenu)
          .toContain('Terrain T1');

        expect(contenu)
          .toContain('Réservé');

        expect(contenu)
          .toContain('10:00');

        expect(contenu)
          .toContain('11:30');

        expect(contenu)
          .toContain('3 / 4');
      }
    );

    it(
      'doit afficher le motif de fermeture',
      () => {
        facade.etatOperationnel.set({
          ...etatOperationnel,
          ferme: true,
          motifFermeture:
            'Entretien annuel'
        });

        fixture.detectChanges();

        const contenu =
          fixture.nativeElement
            .textContent as string;

        expect(contenu)
          .toContain(
            'Site fermé pour cette date'
          );

        expect(contenu)
          .toContain('Entretien annuel');
      }
    );
  }
);
