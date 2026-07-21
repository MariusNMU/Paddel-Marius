import {
  signal,
  type Signal
} from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';
import { AppShellFacadeService } from './services/app-shell-facade.service';

describe('App', () => {
  let nombreInvitationsSignal:
    ReturnType<typeof signal<number>>;

  let facade: {
    nombreInvitationsRecues:
      Signal<number>;
    joueurConnecte:
      ReturnType<typeof vi.fn>;
    adminConnecte:
      ReturnType<typeof vi.fn>;
    estAdminGlobal:
      ReturnType<typeof vi.fn>;
    initialiser:
      ReturnType<typeof vi.fn>;
    deconnecterJoueur:
      ReturnType<typeof vi.fn>;
    deconnecterAdmin:
      ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    nombreInvitationsSignal =
      signal(0);

    facade = {
      nombreInvitationsRecues:
        nombreInvitationsSignal
          .asReadonly(),

      joueurConnecte:
        vi.fn(() => false),

      adminConnecte:
        vi.fn(() => false),

      estAdminGlobal:
        vi.fn(() => false),

      initialiser:
        vi.fn(),

      deconnecterJoueur:
        vi.fn(),

      deconnecterAdmin:
        vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([]),
        {
          provide:
          AppShellFacadeService,
          useValue:
          facade
        }
      ]
    })
      .overrideComponent(App, {
        set: {
          providers: [
            {
              provide:
              AppShellFacadeService,
              useValue:
              facade
            }
          ]
        }
      })
      .compileComponents();
  });

  it('doit créer l application', () => {
    const fixture =
      TestBed.createComponent(App);

    expect(fixture.componentInstance)
      .toBeTruthy();
  });

  it(
    'doit initialiser la façade',
    () => {
      const fixture =
        TestBed.createComponent(App);

      fixture.detectChanges();

      expect(facade.initialiser)
        .toHaveBeenCalledTimes(1);
    }
  );

  it(
    'doit afficher le titre Padel Marius',
    () => {
      const fixture =
        TestBed.createComponent(App);

      fixture.detectChanges();

      const compiled =
        fixture.nativeElement as HTMLElement;

      expect(
        compiled
          .querySelector('h1')
          ?.textContent
      ).toContain('Padel Marius');
    }
  );

  it(
    'doit utiliser la toolbar Angular Material',
    () => {
      const fixture =
        TestBed.createComponent(App);

      fixture.detectChanges();

      const compiled =
        fixture.nativeElement as HTMLElement;

      expect(
        compiled.querySelector('mat-toolbar')
      ).toBeTruthy();
    }
  );

  it(
    'doit afficher le compteur du joueur connecté',
    () => {
      facade.joueurConnecte
        .mockReturnValue(true);

      nombreInvitationsSignal.set(2);

      const fixture =
        TestBed.createComponent(App);

      fixture.detectChanges();

      const compiled =
        fixture.nativeElement as HTMLElement;

      expect(compiled.textContent)
        .toContain('Invitations reçues');

      const badge =
        compiled.querySelector(
          '.mat-badge-content'
        );

      expect(badge?.textContent)
        .toContain('2');
    }
  );

  it(
    'doit masquer le traitement de veille à l admin SITE',
    () => {
      facade.adminConnecte
        .mockReturnValue(true);

      facade.estAdminGlobal
        .mockReturnValue(false);

      const fixture =
        TestBed.createComponent(App);

      fixture.detectChanges();

      const texte =
        fixture.nativeElement
          .textContent as string;

      expect(texte)
        .not.toContain(
          'Traitement de veille'
        );
    }
  );

  it(
    'doit afficher le traitement de veille à l admin GLOBAL',
    () => {
      facade.adminConnecte
        .mockReturnValue(true);

      facade.estAdminGlobal
        .mockReturnValue(true);

      const fixture =
        TestBed.createComponent(App);

      fixture.detectChanges();

      const texte =
        fixture.nativeElement
          .textContent as string;

      expect(texte)
        .toContain(
          'Traitement de veille'
        );
    }
  );

  it(
    'doit déléguer la déconnexion joueur à la façade',
    () => {
      facade.joueurConnecte
        .mockReturnValue(true);

      const fixture =
        TestBed.createComponent(App);

      fixture.detectChanges();

      const boutons =
        Array.from(
          fixture.nativeElement
            .querySelectorAll('button')
        ) as HTMLButtonElement[];

      const bouton =
        boutons.find(element =>
          element.textContent?.includes(
            'Déconnecter joueur'
          )
        );

      bouton?.click();

      expect(
        facade.deconnecterJoueur
      ).toHaveBeenCalled();
    }
  );

  it(
    'doit déléguer la déconnexion admin à la façade',
    () => {
      facade.adminConnecte
        .mockReturnValue(true);

      const fixture =
        TestBed.createComponent(App);

      fixture.detectChanges();

      const boutons =
        Array.from(
          fixture.nativeElement
            .querySelectorAll('button')
        ) as HTMLButtonElement[];

      const bouton =
        boutons.find(element =>
          element.textContent?.includes(
            'Déconnecter admin'
          )
        );

      bouton?.click();

      expect(
        facade.deconnecterAdmin
      ).toHaveBeenCalled();
    }
  );
});
