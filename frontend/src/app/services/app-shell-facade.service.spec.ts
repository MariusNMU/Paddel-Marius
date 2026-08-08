import {
  computed,
  signal,
  type Signal,
  type WritableSignal
} from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  of,
  Subject,
  throwError
} from 'rxjs';
import {
  AuthAdminResponse,
  AuthJoueurResponse
} from '../models/auth.model';
import { AppShellFacadeService } from './app-shell-facade.service';
import { AuthFacadeService } from './auth-facade.service';
import { AuthContextService } from './auth-context.service';
import { InvitationApiService } from './invitation-api.service';

describe('AppShellFacadeService', () => {
  let service: AppShellFacadeService;

  let joueurSignal:
    WritableSignal<AuthJoueurResponse | null>;

  let adminSignal:
    WritableSignal<AuthAdminResponse | null>;

  let messageErreurDeconnexionSignal:
    WritableSignal<string | null>;

  let authContextService: {
    joueur: Signal<AuthJoueurResponse | null>;
    admin: Signal<AuthAdminResponse | null>;
    joueurConnecte: Signal<boolean>;
    adminConnecte: Signal<boolean>;
    deconnecterJoueur:
      ReturnType<typeof vi.fn>;
    deconnecterAdmin:
      ReturnType<typeof vi.fn>;
  };

  let invitationApiService: {
    compterInvitationsRecues:
      ReturnType<typeof vi.fn>;
  };

  let authFacadeService: {
    messageErreurDeconnexion:
      Signal<string | null>;
    deconnecterJoueur:
      ReturnType<typeof vi.fn>;
    deconnecterAdmin:
      ReturnType<typeof vi.fn>;
  };

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

  const joueurDeux: AuthJoueurResponse = {
    membreId: 2,
    matricule: 'TEST002',
    nom: 'Deux',
    prenom: 'Joueur',
    categorieMembre: 'SITE',
    siteRattachementId: 1001,
    nomSiteRattachement:
      'Padel Bruxelles',
    actif: true
  };

  const adminGlobal: AuthAdminResponse = {
    administrateurId: 1,
    login: 'admin-global',
    nom: 'Admin',
    prenom: 'Global',
    roleAdministrateur: 'GLOBAL',
    siteId: null,
    nomSite: null,
    actif: true
  };

  const adminSite: AuthAdminResponse = {
    administrateurId: 2,
    login: 'admin-bruxelles',
    nom: 'Admin',
    prenom: 'Bruxelles',
    roleAdministrateur: 'SITE',
    siteId: 1001,
    nomSite: 'Padel Bruxelles',
    actif: true
  };

  beforeEach(() => {
    joueurSignal =
      signal<AuthJoueurResponse | null>(
        joueur
      );

    adminSignal =
      signal<AuthAdminResponse | null>(
        null
      );

    messageErreurDeconnexionSignal =
      signal<string | null>(null);

    authContextService = {
      joueur: joueurSignal.asReadonly(),
      admin: adminSignal.asReadonly(),

      joueurConnecte: computed(
        () => joueurSignal() !== null
      ),

      adminConnecte: computed(
        () => adminSignal() !== null
      ),

      deconnecterJoueur: vi.fn(() => {
        joueurSignal.set(null);
      }),

      deconnecterAdmin: vi.fn(() => {
        adminSignal.set(null);
      })
    };

    invitationApiService = {
      compterInvitationsRecues:
        vi.fn((matricule: string) => {
          return of(
            matricule === 'TEST002'
              ? 1
              : 3
          );
        })
    };

    authFacadeService = {
      messageErreurDeconnexion:
        messageErreurDeconnexionSignal
          .asReadonly(),

      deconnecterJoueur: vi.fn(() => {
        joueurSignal.set(null);
      }),

      deconnecterAdmin: vi.fn(() => {
        adminSignal.set(null);
      })
    };

    TestBed.configureTestingModule({
      providers: [
        AppShellFacadeService,
        {
          provide: AuthContextService,
          useValue: authContextService
        },
        {
          provide: AuthFacadeService,
          useValue: authFacadeService
        },
        {
          provide: InvitationApiService,
          useValue: invitationApiService
        }
      ]
    });

    service = TestBed.inject(
      AppShellFacadeService
    );
  });

  it(
    'doit charger le compteur du joueur connecté',
    () => {
      service.initialiser();

      expect(
        invitationApiService
          .compterInvitationsRecues
      ).toHaveBeenCalledWith('TEST001');

      expect(
        service.nombreInvitationsRecues()
      ).toBe(3);

      expect(service.joueurConnecte())
        .toBe(true);
    }
  );

  it(
    'doit suivre la connexion et la déconnexion du joueur',
    () => {
      joueurSignal.set(null);
      TestBed.tick();

      service.initialiser();

      expect(
        invitationApiService
          .compterInvitationsRecues
      ).not.toHaveBeenCalled();

      expect(
        service.nombreInvitationsRecues()
      ).toBe(0);

      joueurSignal.set(joueurDeux);
      TestBed.tick();

      expect(
        invitationApiService
          .compterInvitationsRecues
      ).toHaveBeenCalledWith('TEST002');

      expect(
        service.nombreInvitationsRecues()
      ).toBe(1);

      joueurSignal.set(null);
      TestBed.tick();

      expect(
        service.nombreInvitationsRecues()
      ).toBe(0);
    }
  );

  it(
    'doit remettre le compteur à zéro en cas d erreur API',
    () => {
      invitationApiService
        .compterInvitationsRecues
        .mockReturnValue(
          throwError(
            () => new Error(
              'Erreur compteur'
            )
          )
        );

      service.initialiser();

      expect(
        service.nombreInvitationsRecues()
      ).toBe(0);
    }
  );

  it(
    'doit ignorer la réponse d une ancienne session',
    () => {
      const premiereReponse =
        new Subject<number>();

      invitationApiService
        .compterInvitationsRecues
        .mockReset();

      invitationApiService
        .compterInvitationsRecues
        .mockReturnValueOnce(
          premiereReponse.asObservable()
        )
        .mockReturnValueOnce(of(1));

      service.initialiser();

      joueurSignal.set(joueurDeux);
      TestBed.tick();

      expect(
        service.nombreInvitationsRecues()
      ).toBe(1);

      premiereReponse.next(99);

      expect(
        service.nombreInvitationsRecues()
      ).toBe(1);
    }
  );

  it(
    'doit identifier uniquement l admin GLOBAL',
    () => {
      adminSignal.set(adminSite);
      TestBed.tick();

      expect(service.adminConnecte())
        .toBe(true);

      expect(service.estAdminGlobal())
        .toBe(false);

      adminSignal.set(adminGlobal);
      TestBed.tick();

      expect(service.estAdminGlobal())
        .toBe(true);
    }
  );

  it(
    'doit déléguer la déconnexion du joueur à la façade auth',
    () => {
      service.initialiser();

      service.deconnecterJoueur();

      expect(
        authFacadeService
          .deconnecterJoueur
      ).toHaveBeenCalled();

      expect(
        service.nombreInvitationsRecues()
      ).toBe(0);

    }
  );

  it(
    'doit déléguer la déconnexion admin à la façade auth',
    () => {
      adminSignal.set(adminGlobal);
      TestBed.tick();

      service.deconnecterAdmin();

      expect(
        authFacadeService
          .deconnecterAdmin
      ).toHaveBeenCalled();
    }
  );

  it(
    'doit exposer l avertissement de déconnexion',
    () => {
      messageErreurDeconnexionSignal.set(
        'Serveur indisponible'
      );

      expect(
        service.messageErreurDeconnexion()
      ).toBe('Serveur indisponible');
    }
  );
});
