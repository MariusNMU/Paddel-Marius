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
  TraitementEcheanceResponse
} from '../../models/traitement-echeance.model';
import {
  AdminTraitementEcheanceFacadeService
} from '../../services/admin-traitement-echeance-facade.service';
import {
  AdminTraitementEcheanceComponent
} from './admin-traitement-echeance.component';

describe(
  'AdminTraitementEcheanceComponent',
  () => {
    let fixture:
      ComponentFixture<AdminTraitementEcheanceComponent>;

    let component:
      AdminTraitementEcheanceComponent;

    let facade: {
      adminConnecte:
        ReturnType<typeof vi.fn>;

      estAdminGlobal:
        ReturnType<typeof vi.fn>;

      lancerTraitement:
        ReturnType<typeof vi.fn>;

      chargement:
        WritableSignal<boolean>;

      messageErreur:
        WritableSignal<string | null>;

      resultat:
        WritableSignal<
          TraitementEcheanceResponse | null
        >;
    };

    const resultat:
      TraitementEcheanceResponse = {
      dateHeureTraitement:
        '2026-07-20T17:00:00',
      matchesAnalyses: 3,
      matchesDemarres: 2,
      matchesTermines: 1,
      dettesCreees: 1
    };

    beforeEach(async () => {
      facade = {
        adminConnecte:
          vi.fn(() => true),

        estAdminGlobal:
          vi.fn(() => true),

        lancerTraitement:
          vi.fn(),

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
            AdminTraitementEcheanceComponent
          ],
          providers: [
            provideRouter([])
          ]
        })
        .overrideComponent(
          AdminTraitementEcheanceComponent,
          {
            set: {
              providers: [
                {
                  provide:
                  AdminTraitementEcheanceFacadeService,
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
          AdminTraitementEcheanceComponent
        );

      component =
        fixture.componentInstance;
    });

    it(
      'doit créer le composant',
      () => {
        fixture.detectChanges();

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

        const bouton =
          fixture.nativeElement
            .querySelector(
              'button'
            );

        expect(contenu)
          .toContain(
            'réservée aux administrateurs globaux'
          );

        expect(bouton)
          .toBeNull();
      }
    );

    it(
      'doit déléguer le lancement du traitement',
      () => {
        fixture.detectChanges();

        const bouton =
          fixture.nativeElement
            .querySelector(
              'button'
            ) as HTMLButtonElement;

        expect(bouton)
          .not.toBeNull();

        bouton.click();

        expect(
          facade.lancerTraitement
        ).toHaveBeenCalledTimes(1);
      }
    );

    it(
      'doit désactiver le bouton pendant le chargement',
      () => {
        facade.chargement.set(true);

        fixture.detectChanges();

        const bouton =
          fixture.nativeElement
            .querySelector(
              'button'
            ) as HTMLButtonElement;

        expect(bouton.disabled)
          .toBe(true);

        expect(bouton.textContent)
          .toContain(
            'Traitement...'
          );
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
            'Résultat du traitement'
          );

        expect(contenu)
          .toContain(
            '20/07/2026 17:00'
          );

        expect(contenu)
          .toContain(
            'Matches démarrés'
          );

        expect(contenu)
          .toContain(
            'Matches terminés'
          );

        expect(contenu)
          .toContain(
            'Dettes créées'
          );
      }
    );
  }
);
