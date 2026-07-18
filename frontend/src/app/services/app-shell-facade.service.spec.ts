import {
  computed,
  signal,
  type Signal,
  type WritableSignal
} from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
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
import { AuthContextService } from './auth-context.service';
import { InvitationApiService } from './invitation-api.service';

describe('AppShellFacadeService', () => {
  let service: AppShellFacadeService;

  let joueurSignal:
    WritableSignal<AuthJoueurResponse | null>;

  let adminSignal:
    WritableSignal<AuthAdminResponse | null>;

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

  let router: {
    navigate: ReturnType<typeof vi.fn>;
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

    router = {
      navigate:
        vi.fn(() => Promise.resolve(true))
    };

    TestBed.configureTestingModule({
      providers: [
        AppShellFacadeService,
        {
          provide: AuthContextService,
          useValue: authContextService
        },
        {
          provide: InvitationApiService,
          useValue: invitationApiService
        },
        {
          provide: Router,
          useValue: router
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
    'doit déconnecter le joueur et naviguer vers l accueil',
    () => {
      service.initialiser();

      service.deconnecterJoueur();

      expect(
        authContextService
          .deconnecterJoueur
      ).toHaveBeenCalled();

      expect(
        service.nombreInvitationsRecues()
      ).toBe(0);

      expect(router.navigate)
        .toHaveBeenCalledWith([
          '/accueil'
        ]);
    }
  );

  it(
    'doit déconnecter l admin et naviguer vers l accueil',
    () => {
      adminSignal.set(adminGlobal);
      TestBed.tick();

      service.deconnecterAdmin();

      expect(
        authContextService
          .deconnecterAdmin
      ).toHaveBeenCalled();

      expect(router.navigate)
        .toHaveBeenCalledWith([
          '/accueil'
        ]);
    }
  );
});
