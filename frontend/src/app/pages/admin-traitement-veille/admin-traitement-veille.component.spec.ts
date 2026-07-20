import {
  signal,
  type WritableSignal
} from '@angular/core';
import {
  ComponentFixture,
  TestBed
} from '@angular/core/testing';
import {
  provideRouter
} from '@angular/router';
import {
  TraitementVeilleResponse
} from '../../models/traitement-veille.model';
import {
  AdminTraitementVeilleFacadeService
} from '../../services/admin-traitement-veille-facade.service';
import {
  AdminTraitementVeilleComponent
} from './admin-traitement-veille.component';

describe(
  'AdminTraitementVeilleComponent',
  () => {
    let fixture:
      ComponentFixture<AdminTraitementVeilleComponent>;

    let component:
      AdminTraitementVeilleComponent;

    let facade: {
      adminConnecte:
        ReturnType<typeof vi.fn>;

      estAdminGlobal:
        ReturnType<typeof vi.fn>;

      selectionnerDate:
        ReturnType<typeof vi.fn>;

      selectionnerDateRelative:
        ReturnType<typeof vi.fn>;

      lancerTraitement:
        ReturnType<typeof vi.fn>;

      dateTraitement:
        WritableSignal<string>;

      chargement:
        WritableSignal<boolean>;

      messageErreur:
        WritableSignal<string | null>;

      resultat:
        WritableSignal<
          TraitementVeilleResponse | null
        >;
    };

    const resultat:
      TraitementVeilleResponse = {
      dateTraitement: '2026-07-20',
      dateMatchTraitee: '2026-07-21',
      matchesAnalyses: 3,
      matchesPassesPublics: 1,
      participationsLiberees: 2,
      penalitesCreees: 1
    };

    beforeEach(async () => {
      facade = {
        adminConnecte:
          vi.fn(() => true),

        estAdminGlobal:
          vi.fn(() => true),

        selectionnerDate:
          vi.fn(),

        selectionnerDateRelative:
          vi.fn(),

        lancerTraitement:
          vi.fn(),

        dateTraitement:
          signal('2026-07-20'),

        chargement:
          signal(false),

        messageErreur:
          signal(null),

        resultat:
          signal(null)
      };

      await TestBed
        .configureTestingModule({
          imports: [
            AdminTraitementVeilleComponent
          ],
          providers: [
            provideRouter([])
          ]
        })
        .overrideComponent(
          AdminTraitementVeilleComponent,
          {
            set: {
              providers: [
                {
                  provide:
                  AdminTraitementVeilleFacadeService,
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
          AdminTraitementVeilleComponent
        );

      component =
        fixture.componentInstance;

      fixture.detectChanges();
    });

    it(
      'doit créer le composant',
      () => {
        expect(component)
          .toBeTruthy();
      }
    );

    it(
      'doit demander une connexion sans administrateur',
      () => {
        facade.adminConnecte
          .mockReturnValue(false);

        fixture.detectChanges();

        const contenu =
          fixture.nativeElement
            .textContent as string;

        expect(contenu)
          .toContain(
            'Tu dois te connecter comme admin'
          );

        expect(contenu)
          .toContain(
            'Connexion admin'
          );
      }
    );

    it(
      'doit refuser visuellement le traitement à un admin SITE',
      () => {
        facade.estAdminGlobal
          .mockReturnValue(false);

        fixture.detectChanges();

        const contenu =
          fixture.nativeElement
            .textContent as string;

        const formulaire =
          fixture.nativeElement
            .querySelector('form');

        expect(contenu)
          .toContain(
            'réservée aux administrateurs globaux'
          );

        expect(formulaire)
          .toBeNull();
      }
    );

    it(
      'doit déléguer la sélection de la date du jour',
      () => {
        const boutons =
          Array.from(
            fixture.nativeElement
              .querySelectorAll('button')
          ) as HTMLButtonElement[];

        const boutonAujourdhui =
          boutons.find(bouton =>
            bouton.textContent
              ?.trim() === 'Aujourd\'hui'
          );

        expect(boutonAujourdhui)
          .toBeDefined();

        boutonAujourdhui?.click();

        expect(
          facade.selectionnerDateRelative
        ).toHaveBeenCalledWith(0);
      }
    );

    it(
      'doit déléguer le lancement du traitement',
      () => {
        const formulaire =
          fixture.nativeElement
            .querySelector(
              'form'
            ) as HTMLFormElement;

        expect(formulaire)
          .not.toBeNull();

        formulaire.dispatchEvent(
          new Event('submit')
        );

        expect(
          facade.lancerTraitement
        ).toHaveBeenCalledTimes(1);
      }
    );

    it(
      'doit afficher le résultat du traitement',
      () => {
        facade.resultat.set(
          resultat
        );

        fixture.detectChanges();

        const contenu =
          fixture.nativeElement
            .textContent as string;

        expect(contenu)
          .toContain(
            'Vue affichée'
          );

        expect(contenu)
          .toContain(
            '2026-07-21'
          );

        expect(contenu)
          .toContain(
            'Matches analysés'
          );

        expect(contenu)
          .toContain(
            'Participations libérées'
          );

        expect(contenu)
          .toContain(
            'Pénalités créées'
          );
      }
    );
  }
);
