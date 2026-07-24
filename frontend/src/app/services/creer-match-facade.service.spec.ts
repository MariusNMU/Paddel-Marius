import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AuthJoueurResponse } from '../models/auth.model';
import { InvitationPriveeResponse } from '../models/invitation.model';
import { MatchResponse } from '../models/match.model';
import { ParametresMetierResponse } from '../models/parametres-metier.model';
import { TerrainResponse } from '../models/terrain.model';
import { AuthContextService } from './auth-context.service';
import { CreerMatchFacadeService } from './creer-match-facade.service';
import { InvitationApiService } from './invitation-api.service';
import { MatchApiService } from './match-api.service';
import { ParametresMetierApiService } from './parametres-metier-api.service';
import { TerrainApiService } from './terrain-api.service';

describe('CreerMatchFacadeService', () => {
  let service: CreerMatchFacadeService;

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

  const matchCree: MatchResponse = {
    matchId: 3001,
    terrainId: 20,
    numeroTerrain: 'T2',
    siteId: 2,
    nomSite: 'Site Beta',
    dateHeureDebut: '2026-06-20T09:00:00',
    dateHeureFin: '2026-06-20T10:30:00',
    modeCreation: 'PRIVE',
    visibiliteCourante: 'PRIVE',
    prixTotal: 60,
    etatCycle: 'A_VENIR'
  };

  const invitation: InvitationPriveeResponse = {
    participationId: 4001,
    matchId: 3001,
    siteId: 2,
    nomSite: 'Site Beta',
    terrainId: 20,
    numeroTerrain: 'T2',
    dateHeureDebut: '2026-06-20T09:00:00',
    dateHeureFin: '2026-06-20T10:30:00',
    organisateurId: 2001,
    matriculeOrganisateur: 'TEST001',
    nomOrganisateur: 'Dupont',
    prenomOrganisateur: 'Marie',
    joueurInviteId: 2002,
    matriculeInvite: 'TEST002',
    nomInvite: 'Martin',
    prenomInvite: 'Paul',
    statutParticipation: 'EN_ATTENTE_PAIEMENT'
  };

  beforeEach(() => {
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
      consulterParametresMetier: vi.fn(
        () => of(parametresMetier)
      )
    };

    authContextService = {
      joueur: vi.fn(() => joueur)
    };

    TestBed.configureTestingModule({
      providers: [
        CreerMatchFacadeService,
        {
          provide: MatchApiService,
          useValue: matchApiService
        },
        {
          provide: InvitationApiService,
          useValue: invitationApiService
        },
        {
          provide: TerrainApiService,
          useValue: terrainApiService
        },
        {
          provide: ParametresMetierApiService,
          useValue: parametresMetierApiService
        },
        {
          provide: AuthContextService,
          useValue: authContextService
        }
      ]
    });

    service = TestBed.inject(CreerMatchFacadeService);
  });

  it('doit initialiser le parcours depuis l URL et le joueur connecté', () => {
    service.initialiser(
      '20',
      '2026-06-20T09:00:00'
    );

    expect(terrainApiService.listerTerrainsActifs)
      .toHaveBeenCalled();
    expect(parametresMetierApiService.consulterParametresMetier)
      .toHaveBeenCalled();

    expect(service.terrains()).toEqual(terrains);
    expect(service.parametresMetier()).toEqual(parametresMetier);
    expect(service.terrainId()).toBe(20);
    expect(service.matriculeOrganisateur()).toBe('TEST001');
    expect(service.dateHeureDebut()).toBe('2026-06-20T09:00');
    expect(service.dureeMatchLibelle()).toBe('1h30');
  });

  it('doit limiter les terrains d un membre SITE à son site', () => {
    authContextService.joueur.mockReturnValue({
      ...joueur,
      matricule: 'S1001',
      categorieMembre: 'SITE',
      siteRattachementId: 1,
      nomSiteRattachement: 'Site Alpha'
    });

    service.initialiser(null, null);

    expect(service.terrains()).toEqual([terrains[0]]);
    expect(service.terrainId()).toBe(10);
  });

  it('doit refuser un terrain URL appartenant à un autre site', () => {
    authContextService.joueur.mockReturnValue({
      ...joueur,
      matricule: 'S1001',
      categorieMembre: 'SITE',
      siteRattachementId: 1,
      nomSiteRattachement: 'Site Alpha'
    });

    service.initialiser(
      '20',
      '2026-06-20T09:00:00'
    );

    expect(service.terrains()).toEqual([terrains[0]]);
    expect(service.terrainId()).toBeNull();
    expect(service.messageErreur()).toBe(
      'Le terrain demandé n’est plus disponible. '
      + 'Choisis un autre terrain.'
    );
  });

  it('doit choisir le premier terrain sans terrain demandé dans l URL', () => {
    service.initialiser(null, null);

    expect(service.terrainId()).toBe(10);
    expect(service.messageErreur()).toBe('');
  });

  it('doit refuser un terrain demandé absent des terrains actifs', () => {
    service.initialiser(
      '999',
      '2026-06-20T09:00:00'
    );

    expect(service.terrainId()).toBeNull();
    expect(service.messageErreur()).toBe(
      'Le terrain demandé n’est plus disponible. '
      + 'Choisis un autre terrain.'
    );
  });

  it('doit créer un match avec les valeurs du parcours', () => {
    matchApiService.creerMatch.mockReturnValue(of(matchCree));

    service.initialiser(
      '20',
      '2026-06-20T09:00:00'
    );
    service.modifierModeCreation('PRIVE');

    service.creerMatch();

    expect(matchApiService.creerMatch).toHaveBeenCalledWith({
      terrainId: 20,
      matriculeOrganisateur: 'TEST001',
      dateHeureDebut: '2026-06-20T09:00',
      modeCreation: 'PRIVE'
    });

    expect(service.matchCree()).toEqual(matchCree);
    expect(service.chargementCreation()).toBe(false);
    expect(service.messageErreur()).toBe('');
  });

  it('doit refuser un formulaire incomplet', () => {
    service.initialiser(
      '20',
      '2026-06-20T09:00:00'
    );

    service.creerMatch();

    expect(service.messageErreur()).toBe(
      'Tous les champs sont obligatoires.'
    );
    expect(matchApiService.creerMatch).not.toHaveBeenCalled();
  });

  it('doit exposer une erreur de création retournée par le backend', () => {
    matchApiService.creerMatch.mockReturnValue(
      throwError(() => new HttpErrorResponse({
        status: 409,
        error: {
          message: 'Le terrain est déjà réservé.'
        }
      }))
    );

    service.initialiser(
      '20',
      '2026-06-20T09:00:00'
    );
    service.modifierModeCreation('PUBLIC');

    service.creerMatch();

    expect(service.messageErreur()).toBe(
      'Le terrain est déjà réservé.'
    );
    expect(service.matchCree()).toBeNull();
    expect(service.chargementCreation()).toBe(false);
  });

  it('doit inviter un joueur dans le match privé créé', () => {
    matchApiService.creerMatch.mockReturnValue(of(matchCree));
    invitationApiService.inviterJoueur.mockReturnValue(
      of(invitation)
    );

    service.initialiser(
      '20',
      '2026-06-20T09:00:00'
    );
    service.modifierModeCreation('PRIVE');
    service.creerMatch();

    service.modifierMatriculeInvite(' TEST002 ');
    service.inviterJoueur();

    expect(invitationApiService.inviterJoueur)
      .toHaveBeenCalledWith(
        3001,
        {
          matriculeOrganisateur: 'TEST001',
          matriculeInvite: 'TEST002'
        }
      );

    expect(service.invites()).toEqual([invitation]);
    expect(service.matriculeInvite()).toBe('');
    expect(service.messageInvitation()).toContain(
      'Paul Martin (TEST002) invité.'
    );
    expect(service.chargementInvitation()).toBe(false);
  });
});
