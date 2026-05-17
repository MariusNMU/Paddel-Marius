import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of } from 'rxjs';
import { App } from './app';
import { AuthContextService } from './services/auth-context.service';
import { InvitationApiService } from './services/invitation-api.service';

describe('App', () => {
  let authContextService: {
    joueur: ReturnType<typeof vi.fn>;
    joueurConnecte: ReturnType<typeof vi.fn>;
    adminConnecte: ReturnType<typeof vi.fn>;
    deconnecterJoueur: ReturnType<typeof vi.fn>;
    deconnecterAdmin: ReturnType<typeof vi.fn>;
  };

  let invitationApiService: {
    compterInvitationsRecues: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    authContextService = {
      joueur: vi.fn(() => null),
      joueurConnecte: vi.fn(() => false),
      adminConnecte: vi.fn(() => false),
      deconnecterJoueur: vi.fn(),
      deconnecterAdmin: vi.fn()
    };

    invitationApiService = {
      compterInvitationsRecues: vi.fn(() => of(0))
    };

    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([]),
        { provide: AuthContextService, useValue: authContextService },
        { provide: InvitationApiService, useValue: invitationApiService }
      ]
    }).compileComponents();
  });

  it('doit créer l application', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;

    expect(app).toBeTruthy();
  });

  it('doit afficher le titre Padel Marius', async () => {
    const fixture = TestBed.createComponent(App);

    fixture.detectChanges();
    await fixture.whenStable();

    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('h1')?.textContent).toContain('Padel Marius');
  });

  it('doit garder le compteur d invitations à zéro si aucun joueur n est connecté', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;

    expect(app.nombreInvitationsRecues).toBe(0);
    expect(invitationApiService.compterInvitationsRecues).not.toHaveBeenCalled();
  });

  it('doit déconnecter le joueur et revenir à l accueil', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;

    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    app.deconnecterJoueur();

    expect(authContextService.deconnecterJoueur).toHaveBeenCalled();
    expect(app.nombreInvitationsRecues).toBe(0);
    expect(navigateSpy).toHaveBeenCalledWith(['/accueil']);
  });

  it('doit déconnecter l admin et revenir à l accueil', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;

    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    app.deconnecterAdmin();

    expect(authContextService.deconnecterAdmin).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/accueil']);
  });
});
