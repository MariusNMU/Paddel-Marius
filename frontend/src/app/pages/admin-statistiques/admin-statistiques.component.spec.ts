import {
  signal,
  WritableSignal
} from '@angular/core';
import {
  ComponentFixture,
  TestBed
} from '@angular/core/testing';
import {
  provideRouter
} from '@angular/router';
import { AuthAdminResponse } from '../../models/auth.model';
import { SiteResponse } from '../../models/site.model';
import { StatistiquesAdminResponse } from '../../models/statistique.model';
import {
  AdminStatistiquesFacadeService,
  PeriodeStatistiques
} from '../../services/admin-statistiques-facade.service';
import { AdminStatistiquesComponent } from './admin-statistiques.component';

describe(
  'AdminStatistiquesComponent',
  () => {
    let fixture:
      ComponentFixture<AdminStatistiquesComponent>;

    let component:
      AdminStatistiquesComponent;

    let facade: {
      initialiser:
        ReturnType<typeof vi.fn>;

      estAdminGlobal:
        ReturnType<typeof vi.fn>;

      modifierDateDebut:
        ReturnType<typeof vi.fn>;

      modifierDateFin:
        ReturnType<typeof vi.fn>;

      modifierSiteId:
        ReturnType<typeof vi.fn>;

      selectionnerPeriode:
        ReturnType<typeof vi.fn>;

      chargerStatistiques:
        ReturnType<typeof vi.fn>;

      admin:
        WritableSignal<AuthAdminResponse | null>;

      sites:
        WritableSignal<SiteResponse[]>;

      dateDebut:
        WritableSignal<string>;

      dateFin:
        WritableSignal<string>;

      siteId:
        WritableSignal<number | null>;

      chargementSites:
        WritableSignal<boolean>;

      chargement:
        WritableSignal<boolean>;

      messageErreur:
        WritableSignal<string>;

      statistiques:
        WritableSignal<
          StatistiquesAdminResponse | null
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

    const statistiques:
      StatistiquesAdminResponse = {
      dateDebut: '2026-07-01',
      dateFin: '2026-07-31',
      siteId: null,
      nomSite: null,
      nombreMatches: 2,
      nombreMatchesAVenir: 1,
      nombreMatchesTermines: 1,
      nombrePaiements: 2,
      chiffreAffaires: 45,
      nombreDettesOuvertes: 1,
      montantDettesOuvertes: 30,
      nombreParticipationsActives: 6,
      capaciteTheoriqueJoueurs: 8,
      tauxRemplissage: 75
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

        modifierDateDebut: vi.fn(),
        modifierDateFin: vi.fn(),
        modifierSiteId: vi.fn(),
        selectionnerPeriode: vi.fn(),
        chargerStatistiques: vi.fn(),

        admin: signal(adminGlobal),
        sites: signal([site]),
        dateDebut:
          signal('2026-07-01'),
        dateFin:
          signal('2026-07-31'),
        siteId: signal(null),
        chargementSites:
          signal(false),
        chargement:
          signal(false),
        messageErreur:
          signal(''),
        statistiques:
          signal(null)
      };

      await TestBed
        .configureTestingModule({
          imports: [
            AdminStatistiquesComponent
          ],
          providers: [
            provideRouter([])
          ]
        })
        .overrideComponent(
          AdminStatistiquesComponent,
          {
            set: {
              providers: [
                {
                  provide:
                  AdminStatistiquesFacadeService,
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
          AdminStatistiquesComponent
        );

      component =
        fixture.componentInstance;

      fixture.detectChanges();
    });

    it(
      'doit créer le composant et initialiser la façade',
      () => {
        expect(component)
          .toBeTruthy();

        expect(
          facade.initialiser
        ).toHaveBeenCalled();
      }
    );

    it(
      'doit déléguer la sélection de période',
      () => {
        const periode:
          PeriodeStatistiques = 'demo';

        component.selectionnerPeriode(
          periode
        );

        expect(
          facade.selectionnerPeriode
        ).toHaveBeenCalledWith(
          'demo'
        );
      }
    );

    it(
      'doit déléguer le chargement des statistiques',
      () => {
        component.chargerStatistiques();

        expect(
          facade.chargerStatistiques
        ).toHaveBeenCalled();
      }
    );

    it(
      'doit proposer la vue globale à un admin GLOBAL',
      () => {
        const selecteurSite =
          fixture.nativeElement
            .querySelector(
              'select#siteId'
            );

        const contenu =
          fixture.nativeElement
            .textContent;

        expect(selecteurSite)
          .not.toBeNull();

        expect(contenu)
          .toContain(
            'Tous les sites'
          );

        expect(contenu)
          .toContain(
            'Padel Bruxelles'
          );
      }
    );

    it(
      'doit limiter visuellement un admin SITE à son propre site',
      () => {
        facade.admin.set(adminSite);
        facade.siteId.set(1001);

        fixture.detectChanges();

        const selecteurSite =
          fixture.nativeElement
            .querySelector(
              'select#siteId'
            );

        const contenu =
          fixture.nativeElement
            .textContent;

        expect(selecteurSite)
          .toBeNull();

        expect(contenu)
          .not.toContain(
          'Tous les sites'
        );

        expect(contenu)
          .toContain(
            'Vue limitée à ton site'
          );

        expect(contenu)
          .toContain(
            'Padel Bruxelles'
          );
      }
    );

    it(
      'doit afficher les statistiques reçues',
      () => {
        facade.statistiques.set(
          statistiques
        );

        fixture.detectChanges();

        const contenu =
          fixture.nativeElement
            .textContent;

        expect(contenu)
          .toContain(
            'Vue affichée'
          );

        expect(contenu)
          .toContain(
            'Nombre de matches'
          );

        expect(contenu)
          .toContain(
            'Chiffre d\'affaires'
          );

        expect(contenu)
          .toContain(
            'Taux de remplissage'
          );

        expect(contenu)
          .toContain('75');
      }
    );
  }
);
