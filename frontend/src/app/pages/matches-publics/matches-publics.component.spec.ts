import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AuthJoueurResponse } from '../../models/auth.model';
import {
  MatchPublicResponse,
  RejoindreMatchPublicResponse
} from '../../models/match-public.model';
import { AuthContextService } from '../../services/auth-context.service';
import { MatchPublicApiService } from '../../services/match-public-api.service';
import { MatchesPublicsComponent } from './matches-publics.component';

describe('MatchesPublicsComponent', () => {
  let fixture: ComponentFixture<MatchesPublicsComponent>;
  let component: MatchesPublicsComponent;

  let matchPublicApiService: {
    listerMatchesPublics: ReturnType<typeof vi.fn>;
    rejoindreEtPayer: ReturnType<typeof vi.fn>;
  };

  let authContextService: {
    joueur: ReturnType<typeof vi.fn>;
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

  const matchPublic: MatchPublicResponse = {
    matchId: 3001,
    siteId: 1001,
    nomSite: 'Padel Bruxelles',
    terrainId: 1101,
    numeroTerrain: 'T1',
    dateHeureDebut: '2026-06-20T09:00:00',
    dateHeureFin: '2026-06-20T10:30:00',
    nombreParticipantsActifs: 2,
    placesDisponibles: 2,
    prixTotal: 60,
    montantParticipation: 15
  };

  beforeEach(async () => {
    matchPublicApiService = {
      listerMatchesPublics: vi.fn(),
      rejoindreEtPayer: vi.fn()
    };

    authContextService = {
      joueur: vi.fn(() => joueur)
    };

    await TestBed.configureTestingModule({
      imports: [MatchesPublicsComponent],
      providers: [
        { provide: MatchPublicApiService, useValue: matchPublicApiService },
        { provide: AuthContextService, useValue: authContextService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(MatchesPublicsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('doit créer le composant', () => {
    expect(component).toBeTruthy();
  });

  it('doit lister les matches publics pour un site et une date', () => {
    matchPublicApiService.listerMatchesPublics.mockReturnValue(of([matchPublic]));

    component.siteId = 1001;
    component.date = '2026-06-20';

    component.rechercherMatchesPublics();

    expect(matchPublicApiService.listerMatchesPublics).toHaveBeenCalledWith(1001, '2026-06-20');
    expect(component.matches).toEqual([matchPublic]);
    expect(component.rechercheEffectuee).toBe(true);
    expect(component.chargement).toBe(false);
  });

  it('doit afficher une erreur si le site ou la date est manquant', () => {
    component.siteId = 0;
    component.date = '';

    component.rechercherMatchesPublics();

    expect(component.messageErreur).toBe('Le site et la date sont obligatoires.');
    expect(matchPublicApiService.listerMatchesPublics).not.toHaveBeenCalled();
  });

  it('doit afficher une erreur si la recherche échoue', () => {
    matchPublicApiService.listerMatchesPublics.mockReturnValue(
      throwError(() => new HttpErrorResponse({
        status: 500,
        error: {
          message: 'Erreur backend matches publics.'
        }
      }))
    );

    component.rechercherMatchesPublics();

    expect(component.messageErreur).toBe('Erreur backend matches publics.');
    expect(component.matches).toEqual([]);
    expect(component.chargement).toBe(false);
  });

  it('doit refuser de rejoindre un match si aucun joueur n est connecté', () => {
    authContextService.joueur.mockReturnValue(null);

    component.rejoindreEtPayer(matchPublic);

    expect(component.messageErreur).toBe('Connecte-toi comme joueur pour rejoindre un match public.');
    expect(matchPublicApiService.rejoindreEtPayer).not.toHaveBeenCalled();
  });

  it('doit appeler l API pour rejoindre et payer un match public', () => {
    const paiement: RejoindreMatchPublicResponse = {
      matchId: 3001,
      participationId: 3103,
      paiementId: 6008,
      matriculeJoueur: 'G1001',
      montantPaye: 15,
      statutParticipation: 'CONFIRMEE',
      soldeRestant: 85
    };

    authContextService.joueur.mockReturnValue(joueur);
    matchPublicApiService.rejoindreEtPayer.mockReturnValue(of(paiement));
    matchPublicApiService.listerMatchesPublics.mockReturnValue(of([]));

    component.rejoindreEtPayer(matchPublic);

    expect(matchPublicApiService.rejoindreEtPayer).toHaveBeenCalledWith(3001, {
      matriculeJoueur: 'G1001'
    });

    expect(matchPublicApiService.listerMatchesPublics).toHaveBeenCalled();
    expect(component.chargementPaiement).toBe(false);
  });

  it('doit changer la date avec le choix rapide', () => {
    component.matches = [matchPublic];
    component.messageErreur = 'ancienne erreur';
    component.messageSucces = 'ancien succès';
    component.rechercheEffectuee = true;

    component.selectionnerJour('2026-06-21');

    expect(component.date).toBe('2026-06-21');
    expect(component.matches).toEqual([]);
    expect(component.messageErreur).toBe('');
    expect(component.messageSucces).toBe('');
    expect(component.rechercheEffectuee).toBe(false);
  });
});
