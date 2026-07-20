import {
  signal,
  WritableSignal
} from '@angular/core';
import {
  ComponentFixture,
  TestBed
} from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import {
  CategorieMembre,
  MembreResponse
} from '../../models/membre.model';
import { SiteResponse } from '../../models/site.model';
import { InscriptionJoueurFacadeService } from '../../services/inscription-joueur-facade.service';
import { InscriptionJoueurComponent } from './inscription-joueur.component';

describe(
  'InscriptionJoueurComponent',
  () => {
    let fixture:
      ComponentFixture<InscriptionJoueurComponent>;

    let component:
      InscriptionJoueurComponent;

    let facade: {
      initialiser:
        ReturnType<typeof vi.fn>;

      modifierNom:
        ReturnType<typeof vi.fn>;

      modifierPrenom:
        ReturnType<typeof vi.fn>;

      modifierMotDePasse:
        ReturnType<typeof vi.fn>;

      modifierConfirmationMotDePasse:
        ReturnType<typeof vi.fn>;

      modifierCategorieMembre:
        ReturnType<typeof vi.fn>;

      modifierSiteRattachementId:
        ReturnType<typeof vi.fn>;

      nomSiteSelectionne:
        ReturnType<typeof vi.fn>;

      envoyerDemande:
        ReturnType<typeof vi.fn>;

      sites:
        WritableSignal<SiteResponse[]>;

      nom:
        WritableSignal<string>;

      prenom:
        WritableSignal<string>;

      motDePasse:
        WritableSignal<string>;

      confirmationMotDePasse:
        WritableSignal<string>;

      categorieMembre:
        WritableSignal<CategorieMembre>;

      siteRattachementId:
        WritableSignal<number | null>;

      chargementSites:
        WritableSignal<boolean>;

      chargement:
        WritableSignal<boolean>;

      messageErreur:
        WritableSignal<string>;

      membreCree:
        WritableSignal<MembreResponse | null>;
    };

    const site: SiteResponse = {
      siteId: 1001,
      code: 'BRU',
      nom: 'Padel Bruxelles',
      adresse: 'Rue de Bruxelles'
    };

    const membreCree: MembreResponse = {
      membreId: 2001,
      matricule: 'G1003',
      nom: 'Dupont',
      prenom: 'Marie',
      categorieMembre: 'GLOBAL',
      siteRattachementId: null,
      nomSiteRattachement: null,
      actif: true,
      soldeCredit: 100
    };

    beforeEach(async () => {
      facade = {
        initialiser: vi.fn(),
        modifierNom: vi.fn(),
        modifierPrenom: vi.fn(),
        modifierMotDePasse: vi.fn(),

        modifierConfirmationMotDePasse:
          vi.fn(),

        modifierCategorieMembre:
          vi.fn(),

        modifierSiteRattachementId:
          vi.fn(),

        nomSiteSelectionne:
          vi.fn(
            () =>
              'Padel Bruxelles (1001)'
          ),

        envoyerDemande: vi.fn(),

        sites: signal([site]),
        nom: signal(''),
        prenom: signal(''),
        motDePasse: signal(''),

        confirmationMotDePasse:
          signal(''),

        categorieMembre:
          signal<CategorieMembre>(
            'GLOBAL'
          ),

        siteRattachementId:
          signal<number | null>(null),

        chargementSites:
          signal(false),

        chargement:
          signal(false),

        messageErreur:
          signal(''),

        membreCree:
          signal<MembreResponse | null>(
            null
          )
      };

      await TestBed
        .configureTestingModule({
          imports: [
            InscriptionJoueurComponent
          ],
          providers: [
            provideRouter([])
          ]
        })
        .overrideComponent(
          InscriptionJoueurComponent,
          {
            set: {
              providers: [
                {
                  provide:
                  InscriptionJoueurFacadeService,
                  useValue:
                  facade
                }
              ]
            }
          }
        )
        .compileComponents();

      fixture = TestBed.createComponent(
        InscriptionJoueurComponent
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
      'doit afficher les champs de mot de passe',
      () => {
        const motDePasse =
          fixture.nativeElement
            .querySelector(
              '#motDePasse'
            ) as HTMLInputElement;

        const confirmation =
          fixture.nativeElement
            .querySelector(
              '#confirmationMotDePasse'
            ) as HTMLInputElement;

        expect(motDePasse)
          .not.toBeNull();

        expect(motDePasse.type)
          .toBe('password');

        expect(confirmation)
          .not.toBeNull();

        expect(confirmation.type)
          .toBe('password');
      }
    );

    it(
      'doit afficher le site pour une catégorie SITE',
      () => {
        facade.categorieMembre.set(
          'SITE'
        );

        facade.siteRattachementId.set(
          1001
        );

        fixture.detectChanges();

        const selecteurSite =
          fixture.nativeElement
            .querySelector(
              '#siteRattachementId'
            );

        const contenu =
          fixture.nativeElement
            .textContent as string;

        expect(selecteurSite)
          .not.toBeNull();

        expect(contenu)
          .toContain(
            'Padel Bruxelles'
          );
      }
    );

    it(
      'doit déléguer l envoi du formulaire à la façade',
      () => {
        const formulaire =
          fixture.nativeElement
            .querySelector(
              'form'
            ) as HTMLFormElement;

        formulaire.dispatchEvent(
          new Event('submit')
        );

        expect(facade.envoyerDemande)
          .toHaveBeenCalled();
      }
    );

    it(
      'doit afficher le message d erreur de la façade',
      () => {
        facade.messageErreur.set(
          'Inscription impossible.'
        );

        fixture.detectChanges();

        const contenu =
          fixture.nativeElement
            .textContent as string;

        expect(contenu)
          .toContain(
            'Inscription impossible.'
          );
      }
    );

    it(
      'doit afficher le membre créé',
      () => {
        facade.membreCree.set(
          membreCree
        );

        fixture.detectChanges();

        const contenu =
          fixture.nativeElement
            .textContent as string;

        expect(contenu)
          .toContain(
            'Joueur créé avec succès'
          );

        expect(contenu)
          .toContain('G1003');

        expect(contenu)
          .toContain('Dupont');

        expect(contenu)
          .toContain('Marie');
      }
    );
  }
);
