import {
  ComponentFixture,
  TestBed
} from '@angular/core/testing';
import { signal } from '@angular/core';
import { PresentationDemoResponse } from '../../models/donnees-demonstration.model';
import { AccueilFacadeService } from '../../services/accueil-facade.service';
import { AccueilComponent } from './accueil.component';

describe('AccueilComponent', () => {
  let fixture:
    ComponentFixture<AccueilComponent>;

  let component:
    AccueilComponent;

  const presentationDemo:
    PresentationDemoResponse = {
    categoriesMembres: [
      {
        prefixe: 'G',
        categorie: 'GLOBAL',
        regle:
          "Peut réserver jusqu'à 21 jours avant."
      }
    ],
    sites: [
      {
        siteId: 1001,
        code: 'BRU',
        nom: 'Padel Bruxelles',
        adresse: 'Rue du Padel 1'
      }
    ],
    joueurs: [
      {
        matricule: 'G1001',
        motDePasse: 'password',
        description: 'joueur GLOBAL actif'
      }
    ],
    administrateurs: [
      {
        login: 'admin-global',
        motDePasse: 'secret',
        description:
          'administrateur GLOBAL'
      }
    ]
  };

  const donneesSignal =
    signal<PresentationDemoResponse | null>(
      presentationDemo
    );

  const chargementSignal =
    signal(false);

  const messageErreurSignal =
    signal('');

  const facade = {
    donneesDemonstration:
      donneesSignal.asReadonly(),
    chargement:
      chargementSignal.asReadonly(),
    messageErreur:
      messageErreurSignal.asReadonly(),
    initialiser:
      vi.fn(),
    reessayer:
      vi.fn()
  };

  beforeEach(async () => {
    facade.initialiser.mockClear();
    facade.reessayer.mockClear();

    donneesSignal.set(
      presentationDemo
    );

    messageErreurSignal.set('');
    chargementSignal.set(false);

    await TestBed
      .configureTestingModule({
        imports: [AccueilComponent]
      })
      .overrideComponent(
        AccueilComponent,
        {
          set: {
            providers: [
              {
                provide:
                AccueilFacadeService,
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
        AccueilComponent
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
      ).toHaveBeenCalledOnce();
    }
  );

  it(
    'doit afficher les données reçues du backend',
    () => {
      const contenu =
        fixture.nativeElement
          .textContent as string;

      expect(contenu)
        .toContain('Padel Bruxelles');

      expect(contenu)
        .toContain('BRU');

      expect(contenu)
        .toContain('G1001');

      expect(contenu)
        .toContain('password');

      expect(contenu)
        .toContain('admin-global');

      expect(contenu)
        .toContain('secret');
    }
  );

  it(
    'doit permettre de réessayer après une erreur',
    () => {
      donneesSignal.set(null);

      messageErreurSignal.set(
        'Mode démo indisponible.'
      );

      fixture.detectChanges();

      const bouton =
        fixture.nativeElement
          .querySelector('button');

      bouton.click();

      expect(
        facade.reessayer
      ).toHaveBeenCalledOnce();
    }
  );
});
