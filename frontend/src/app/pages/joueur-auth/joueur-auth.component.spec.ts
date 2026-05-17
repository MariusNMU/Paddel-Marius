import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthJoueurResponse } from '../../models/auth.model';
import { AuthApiService } from '../../services/auth-api.service';
import { AuthContextService } from '../../services/auth-context.service';
import { JoueurAuthComponent } from './joueur-auth.component';

describe('JoueurAuthComponent', () => {
  let fixture: ComponentFixture<JoueurAuthComponent>;
  let component: JoueurAuthComponent;

  let authApiService: {
    connecterJoueur: ReturnType<typeof vi.fn>;
  };

  let authContextService: {
    joueur: ReturnType<typeof vi.fn>;
    definirJoueur: ReturnType<typeof vi.fn>;
    deconnecterJoueur: ReturnType<typeof vi.fn>;
  };

  const joueur: AuthJoueurResponse = {
    membreId: 2001,
    matricule: 'G1001',
    nom: 'Dupont',
    prenom: 'Marie',
    categorieMembre: 'GLOBAL',
    siteRattachementId: null,
    nomSiteRattachement: null,
    actif: true
  };

  beforeEach(async () => {
    authApiService = {
      connecterJoueur: vi.fn()
    };

    authContextService = {
      joueur: vi.fn(() => null),
      definirJoueur: vi.fn(),
      deconnecterJoueur: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [JoueurAuthComponent],
      providers: [
        provideRouter([]),
        { provide: AuthApiService, useValue: authApiService },
        { provide: AuthContextService, useValue: authContextService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(JoueurAuthComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('doit créer le composant', () => {
    expect(component).toBeTruthy();
  });

  it('doit préremplir le formulaire avec le joueur de démonstration G1001', () => {
    expect(component.matricule).toBe('G1001');
    expect(component.motDePasse).toBe('password');
  });

  it('doit remplir le formulaire avec G1002 via le bouton de démonstration', () => {
    component.messageErreur = 'ancienne erreur';
    component.messageSucces = 'ancien succès';

    component.utiliserG1002();

    expect(component.matricule).toBe('G1002');
    expect(component.motDePasse).toBe('password');
    expect(component.messageErreur).toBe('');
    expect(component.messageSucces).toBe('');
  });

  it('doit refuser la connexion si le matricule est vide', () => {
    component.matricule = '   ';
    component.motDePasse = 'password';

    component.connecterJoueur();

    expect(component.messageErreur).toBe('Le matricule et le mot de passe sont obligatoires.');
    expect(authApiService.connecterJoueur).not.toHaveBeenCalled();
  });

  it('doit connecter un joueur valide', () => {
    authApiService.connecterJoueur.mockReturnValue(of(joueur));

    component.matricule = ' G1001 ';
    component.motDePasse = ' password ';

    component.connecterJoueur();

    expect(authApiService.connecterJoueur).toHaveBeenCalledWith({
      matricule: 'G1001',
      motDePasse: 'password'
    });

    expect(authContextService.definirJoueur).toHaveBeenCalledWith(joueur);
    expect(component.messageSucces).toContain('Joueur connecté');
    expect(component.chargement).toBe(false);
  });

  it('doit afficher une erreur si la connexion joueur échoue', () => {
    authApiService.connecterJoueur.mockReturnValue(
      throwError(() => new HttpErrorResponse({
        status: 401,
        error: {
          message: 'Joueur introuvable ou inactif.'
        }
      }))
    );

    component.connecterJoueur();

    expect(component.messageErreur).toBe('Joueur introuvable ou inactif.');
    expect(component.chargement).toBe(false);
  });

  it('doit déconnecter le joueur et revenir à l accueil', () => {
    authContextService.joueur.mockReturnValue(joueur);

    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    component.deconnecter();

    expect(authContextService.deconnecterJoueur).toHaveBeenCalled();
    expect(component.messageSucces).toContain('Joueur déconnecté');
    expect(navigateSpy).toHaveBeenCalledWith(['/accueil']);
  });
});
