import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';
import { AuthJoueurResponse } from '../../models/auth.model';
import { MatchResponse } from '../../models/match.model';
import { TerrainResponse } from '../../models/terrain.model';
import { AuthContextService } from '../../services/auth-context.service';
import { InvitationApiService } from '../../services/invitation-api.service';
import { MatchApiService } from '../../services/match-api.service';
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

  const terrains: TerrainResponse[] = [
    {
      terrainId: 1101,
      numeroTerrain: 'T1',
      siteId: 1001,
      nomSite: 'Padel Bruxelles'
    },
    {
      terrainId: 1201,
      numeroTerrain: 'T1',
      siteId: 1002,
      nomSite: 'Padel Namur'
    }
  ];

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

    authContextService = {
      joueur: vi.fn(() => joueur)
    };

    await TestBed.configureTestingModule({
      imports: [CreerMatchComponent],
      providers: [
        { provide: MatchApiService, useValue: matchApiService },
        { provide: InvitationApiService, useValue: invitationApiService },
        { provide: TerrainApiService, useValue: terrainApiService },
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
    expect(component.terrainId).toBe(1101);
  });

  it('ne doit pas choisir un mode de creation par defaut', () => {
    expect(component.modeCreation).toBe('');
  });

  it('doit pre remplir le matricule depuis le joueur connecte', () => {
    expect(component.matriculeOrganisateur).toBe('G1001');
  });

  it('doit appeler l API de creation avec le terrain charge par API', () => {
    const response: MatchResponse = {
      matchId: 3001,
      terrainId: 1101,
      numeroTerrain: 'T1',
      siteId: 1001,
      nomSite: 'Padel Bruxelles',
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
      terrainId: 1101,
      matriculeOrganisateur: 'G1001',
      dateHeureDebut: '2026-06-20T09:00',
      modeCreation: 'PUBLIC'
    });
    expect(component.matchCree).toEqual(response);
  });
});
