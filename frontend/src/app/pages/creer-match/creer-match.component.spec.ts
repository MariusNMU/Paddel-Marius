import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';
import { AuthJoueurResponse } from '../../models/auth.model';
import { MatchResponse } from '../../models/match.model';
import { ParametresMetierResponse } from '../../models/parametres-metier.model';
import { TerrainResponse } from '../../models/terrain.model';
import { AuthContextService } from '../../services/auth-context.service';
import { InvitationApiService } from '../../services/invitation-api.service';
import { MatchApiService } from '../../services/match-api.service';
import { ParametresMetierApiService } from '../../services/parametres-metier-api.service';
import { TerrainApiService } from '../../services/terrain-api.service';
import { CreerMatchComponent } from './creer-match.component';

describe('CreerMatchComponent', () => {
  let fixture: ComponentFixture<CreerMatchComponent>;
  let component: CreerMatchComponent;

  let matchApiService: {
    creerMatch: ReturnType<typeof vi.fn>;
  };

  let invitationApiService: {
    inviterJoueur: ReturnType<typeof vi.fn>;
  };

  let terrainApiService: {
    listerTerrainsActifs: ReturnType<typeof vi.fn>;
  };

  let parametresMetierApiService: {
    consulterParametresMetier: ReturnType<typeof vi.fn>;
  };

  let authContextService: {
    joueur: ReturnType<typeof vi.fn>;
  };

  const joueur: AuthJoueurResponse = {
    membreId: 2001,
    matricule: 'TEST001',
    nom: 'Dupont',
    prenom: 'Marie',
    categorieMembre: 'GLOBAL',
    siteRattachementId: null,
    nomSiteRattachement: null,
    actif: true
  };

  const terrains: TerrainResponse[] = [
    {
      terrainId: 10,
      numeroTerrain: 'T1',
      siteId: 1,
      nomSite: 'Site Alpha'
    },
    {
      terrainId: 20,
      numeroTerrain: 'T1',
      siteId: 2,
      nomSite: 'Site Beta'
    }
  ];

  const parametresMetier: ParametresMetierResponse = {
    dureeMatchMinutes: 90,
    pauseEntreMatchesMinutes: 15,
    nombreJoueursMaximum: 4,
    prixTotalMatch: 60,
    montantParticipationStandard: 15,
    soldeInitialJoueur: 100
  };

  beforeEach(async () => {
    matchApiService = {
      creerMatch: vi.fn()
    };

    invitationApiService = {
      inviterJoueur: vi.fn()
    };

    terrainApiService = {
      listerTerrainsActifs: vi.fn(() => of(terrains))
    };

    parametresMetierApiService = {
      consulterParametresMetier: vi.fn(() => of(parametresMetier))
    };

    authContextService = {
      joueur: vi.fn(() => joueur)
    };

    await TestBed.configureTestingModule({
      imports: [CreerMatchComponent],
      providers: [
        { provide: MatchApiService, useValue: matchApiService },
        { provide: InvitationApiService, useValue: invitationApiService },
        { provide: TerrainApiService, useValue: terrainApiService },
        { provide: ParametresMetierApiService, useValue: parametresMetierApiService },
        { provide: AuthContextService, useValue: authContextService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: convertToParamMap({})
            }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CreerMatchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('doit charger les terrains depuis l API backend', () => {
    expect(terrainApiService.listerTerrainsActifs).toHaveBeenCalled();
    expect(component.terrains).toEqual(terrains);
    expect(component.terrainId).toBe(10);
  });

  it('doit charger les paramètres métier depuis le backend', () => {
    expect(parametresMetierApiService.consulterParametresMetier).toHaveBeenCalled();
    expect(component.parametresMetier).toEqual(parametresMetier);
    expect(component.dureeMatchLibelle()).toBe('1h30');
  });

  it('ne doit pas choisir un mode de creation par defaut', () => {
    expect(component.modeCreation).toBe('');
  });

  it('doit pre remplir le matricule depuis le joueur connecte', () => {
    expect(component.matriculeOrganisateur).toBe('TEST001');
  });

  it('doit appeler l API de creation avec le terrain charge par API', () => {
    const response: MatchResponse = {
      matchId: 3001,
      terrainId: 10,
      numeroTerrain: 'T1',
      siteId: 1,
      nomSite: 'Site Alpha',
      dateHeureDebut: '2026-06-20T09:00:00',
      dateHeureFin: '2026-06-20T10:30:00',
      modeCreation: 'PUBLIC',
      visibiliteCourante: 'PUBLIC',
      prixTotal: 60,
      etatCycle: 'A_VENIR'
    };

    matchApiService.creerMatch.mockReturnValue(of(response));

    component.dateHeureDebut = '2026-06-20T09:00';
    component.modeCreation = 'PUBLIC';

    component.creerMatch();

    expect(matchApiService.creerMatch).toHaveBeenCalledWith({
      terrainId: 10,
      matriculeOrganisateur: 'TEST001',
      dateHeureDebut: '2026-06-20T09:00',
      modeCreation: 'PUBLIC'
    });
    expect(component.matchCree).toEqual(response);
  });
});
