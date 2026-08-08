import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthJoueurResponse } from '../../models/auth.model';
import { AuthFacadeService } from '../../services/auth-facade.service';
import { JoueurAuthComponent } from './joueur-auth.component';

describe('JoueurAuthComponent', () => {
  let fixture: ComponentFixture<JoueurAuthComponent>;
  let component: JoueurAuthComponent;

  const joueur: AuthJoueurResponse = {
    membreId: 1,
    matricule: 'TEST001',
    nom: 'Test',
    prenom: 'Joueur',
    categorieMembre: 'GLOBAL',
    siteRattachementId: null,
    nomSiteRattachement: null,
    actif: true
  };

  const joueurSignal = signal<AuthJoueurResponse | null>(null);
  const chargementJoueurSignal = signal(false);
  const messageErreurJoueurSignal = signal<string | null>(null);
  const messageSuccesJoueurSignal = signal<string | null>(null);

  let authFacade: {
    joueur: typeof joueurSignal;
    chargementJoueur: typeof chargementJoueurSignal;
    messageErreurJoueur: typeof messageErreurJoueurSignal;
    messageSuccesJoueur: typeof messageSuccesJoueurSignal;
    preparerConnexionJoueur: ReturnType<typeof vi.fn>;
    connecterJoueur: ReturnType<typeof vi.fn>;
    deconnecterJoueur: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    joueurSignal.set(null);
    chargementJoueurSignal.set(false);
    messageErreurJoueurSignal.set(null);
    messageSuccesJoueurSignal.set(null);

    authFacade = {
      joueur: joueurSignal,
      chargementJoueur: chargementJoueurSignal,
      messageErreurJoueur: messageErreurJoueurSignal,
      messageSuccesJoueur: messageSuccesJoueurSignal,
      preparerConnexionJoueur: vi.fn(),
      connecterJoueur: vi.fn(),
      deconnecterJoueur: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [JoueurAuthComponent],
      providers: [
        provideRouter([]),
        {
          provide: AuthFacadeService,
          useValue: authFacade
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(JoueurAuthComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('doit créer le composant', () => {
    expect(component).toBeTruthy();
  });

  it('doit préparer le parcours de connexion', () => {
    expect(authFacade.preparerConnexionJoueur).toHaveBeenCalled();
  });

  it('ne doit pas préremplir le formulaire', () => {
    expect(component.matricule).toBe('');
    expect(component.motDePasse).toBe('');
  });

  it('doit borner les champs comme le DTO backend', () => {
    const matricule: HTMLInputElement =
      fixture.nativeElement.querySelector('input[name="matricule"]');
    const motDePasse: HTMLInputElement =
      fixture.nativeElement.querySelector('input[name="motDePasse"]');

    expect(matricule.maxLength).toBe(10);
    expect(motDePasse.maxLength).toBe(72);
  });

  it('doit transmettre la demande de connexion à la façade', () => {
    component.matricule = ' TEST001 ';
    component.motDePasse = ' motdepasse-test ';

    component.connecterJoueur();

    expect(authFacade.connecterJoueur).toHaveBeenCalledWith(
      ' TEST001 ',
      ' motdepasse-test '
    );
  });

  it('doit afficher le nom du site sans identifiant technique', () => {
    joueurSignal.set({
      ...joueur,
      categorieMembre: 'SITE',
      siteRattachementId: 42,
      nomSiteRattachement: 'Site Beta'
    });

    fixture.detectChanges();

    const site: HTMLElement =
      fixture.nativeElement.querySelector(
        '[data-testid="site-rattachement"]'
      );

    expect(site).toBeTruthy();
    expect(site.textContent?.trim()).toBe('Site Beta');
    expect(fixture.nativeElement.textContent).not.toContain('(42)');
  });

  it('doit transmettre la déconnexion à la façade', () => {
    joueurSignal.set(joueur);

    component.deconnecter();

    expect(authFacade.deconnecterJoueur).toHaveBeenCalled();
  });
});
