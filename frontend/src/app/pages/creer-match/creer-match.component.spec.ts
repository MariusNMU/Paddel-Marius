import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { MatchResponse } from '../../models/match.model';
import { ParametresMetierResponse } from '../../models/parametres-metier.model';
import { TerrainResponse } from '../../models/terrain.model';
import { CreerMatchFacadeService } from '../../services/creer-match-facade.service';
import { CreerMatchComponent } from './creer-match.component';

describe('CreerMatchComponent', () => {
  let fixture: ComponentFixture<CreerMatchComponent>;
  let component: CreerMatchComponent;
  let facade: {
    initialiser: ReturnType<typeof vi.fn>;
    creerMatch: ReturnType<typeof vi.fn>;
    inviterJoueur: ReturnType<typeof vi.fn>;
    modifierTerrainId: ReturnType<typeof vi.fn>;
    modifierMatriculeOrganisateur: ReturnType<typeof vi.fn>;
    modifierDateHeureDebut: ReturnType<typeof vi.fn>;
    modifierModeCreation: ReturnType<typeof vi.fn>;
    modifierMatriculeInvite: ReturnType<typeof vi.fn>;
    dureeMatchLibelle: ReturnType<typeof vi.fn>;
    terrainSelectionne: ReturnType<typeof vi.fn>;
    terrains: ReturnType<typeof signal<TerrainResponse[]>>;
    parametresMetier:
      ReturnType<typeof signal<ParametresMetierResponse | null>>;
    terrainId: ReturnType<typeof signal<number | null>>;
    matriculeOrganisateur: ReturnType<typeof signal<string>>;
    dateHeureDebut: ReturnType<typeof signal<string>>;
    modeCreation: ReturnType<typeof signal<'PUBLIC' | 'PRIVE' | ''>>;
    chargementTerrains: ReturnType<typeof signal<boolean>>;
    chargementCreation: ReturnType<typeof signal<boolean>>;
    chargementInvitation: ReturnType<typeof signal<boolean>>;
    messageErreur: ReturnType<typeof signal<string>>;
    matchCree: ReturnType<typeof signal<MatchResponse | null>>;
    matriculeInvite: ReturnType<typeof signal<string>>;
    invites: ReturnType<typeof signal<never[]>>;
    messageInvitation: ReturnType<typeof signal<string>>;
  };

  const terrains: TerrainResponse[] = [
    {
      terrainId: 20,
      numeroTerrain: 'T2',
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
    facade = {
      initialiser: vi.fn(),
      creerMatch: vi.fn(),
      inviterJoueur: vi.fn(),
      modifierTerrainId: vi.fn(),
      modifierMatriculeOrganisateur: vi.fn(),
      modifierDateHeureDebut: vi.fn(),
      modifierModeCreation: vi.fn(),
      modifierMatriculeInvite: vi.fn(),
      dureeMatchLibelle: vi.fn(() => '1h30'),
      terrainSelectionne: vi.fn(() => terrains[0]),
      terrains: signal(terrains),
      parametresMetier: signal(parametresMetier),
      terrainId: signal(20),
      matriculeOrganisateur: signal('TEST001'),
      dateHeureDebut: signal('2026-06-20T09:00'),
      modeCreation: signal('PUBLIC'),
      chargementTerrains: signal(false),
      chargementCreation: signal(false),
      chargementInvitation: signal(false),
      messageErreur: signal(''),
      matchCree: signal<MatchResponse | null>(null),
      matriculeInvite: signal(''),
      invites: signal([]),
      messageInvitation: signal('')
    };

    await TestBed.configureTestingModule({
      imports: [CreerMatchComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: convertToParamMap({
                terrainId: '20',
                dateHeureDebut: '2026-06-20T09:00:00'
              })
            }
          }
        }
      ]
    })
      .overrideComponent(CreerMatchComponent, {
        set: {
          providers: [
            {
              provide: CreerMatchFacadeService,
              useValue: facade
            }
          ]
        }
      })
      .compileComponents();

    fixture = TestBed.createComponent(CreerMatchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('doit initialiser la façade avec les paramètres de l URL', () => {
    expect(facade.initialiser).toHaveBeenCalledWith(
      '20',
      '2026-06-20T09:00:00'
    );
  });

  it('doit désactiver la création sans terrain valide', () => {
    facade.terrainId.set(null);

    fixture.detectChanges();

    const bouton: HTMLButtonElement =
      fixture.nativeElement.querySelector(
        'button[type="submit"]'
      );

    expect(bouton.disabled).toBe(true);
  });

  it('doit déléguer la création du match à la façade', () => {
    component.creerMatch();

    expect(facade.creerMatch).toHaveBeenCalled();
  });

  it('doit expliquer l engagement financier de l organisateur', () => {
    const contenu = fixture.nativeElement.textContent;

    expect(contenu).toContain(
      "Engagement financier de l'organisateur"
    );
    expect(contenu).toContain(
      'tu deviens automatiquement le premier participant'
    );
    expect(contenu).toContain('15.00 €');
    expect(contenu).toContain('60.00 €');
    expect(contenu).toContain(
      'tu devras payer le solde restant'
    );
    expect(contenu).toContain(
      'Une dette ouverte bloque toute nouvelle création de match'
    );
    expect(contenu).toContain(
      'ajoutée au montant total débité'
    );
  });

  it('doit déléguer l invitation à la façade', () => {
    component.inviterJoueur();

    expect(facade.inviterJoueur).toHaveBeenCalled();
  });
});
