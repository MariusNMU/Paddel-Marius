import { TestBed } from '@angular/core/testing';
import { AuthAdminResponse, AuthJoueurResponse } from '../models/auth.model';
import { AuthContextService } from './auth-context.service';

describe('AuthContextService', () => {
  const joueur: AuthJoueurResponse = {
    membreId: 2001,
    matricule: 'G1001',
    nom: 'Dupont',
    prenom: 'Marie',
    categorieMembre: 'GLOBAL',
    siteRattachementId: null,
    nomSiteRattachement: null,
    actif: true,
    token: 'jwt-joueur',
    expirationToken: '2099-12-31T23:59:59'
  };

  const admin: AuthAdminResponse = {
    administrateurId: 2101,
    login: 'admin-global',
    nom: 'Admin',
    prenom: 'Global',
    roleAdministrateur: 'GLOBAL',
    siteId: null,
    nomSite: null,
    actif: true,
    token: 'jwt-admin',
    expirationToken: '2099-12-31T23:59:59'
  };

  function creerService(): AuthContextService {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({});
    return TestBed.inject(AuthContextService);
  }

  function notifierChangementStockage(cle: string | null): void {
    window.dispatchEvent(
      new StorageEvent('storage', {
        key: cle
      })
    );
  }

  beforeEach(() => {
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('doit démarrer sans joueur ni admin connecté', () => {
    const service = creerService();

    expect(service.joueur()).toBeNull();
    expect(service.admin()).toBeNull();
    expect(service.joueurConnecte()).toBe(false);
    expect(service.adminConnecte()).toBe(false);
    expect(service.token()).toBeNull();
  });

  it('doit définir un joueur connecté et le sauvegarder dans localStorage', () => {
    const service = creerService();

    service.definirJoueur(joueur);

    expect(service.joueur()).toEqual(joueur);
    expect(service.joueurConnecte()).toBe(true);
    expect(service.token()).toBe('jwt-joueur');
    expect(JSON.parse(localStorage.getItem('padel-joueur') ?? '{}')).toEqual(joueur);
  });

  it('doit définir un admin connecté et le sauvegarder dans localStorage', () => {
    const service = creerService();

    service.definirAdmin(admin);

    expect(service.admin()).toEqual(admin);
    expect(service.adminConnecte()).toBe(true);
    expect(service.token()).toBe('jwt-admin');
    expect(JSON.parse(localStorage.getItem('padel-admin') ?? '{}')).toEqual(admin);
  });

  it('doit déconnecter l admin quand un joueur se connecte', () => {
    const service = creerService();

    service.definirAdmin(admin);
    service.definirJoueur(joueur);

    expect(service.joueur()).toEqual(joueur);
    expect(service.admin()).toBeNull();
    expect(service.joueurConnecte()).toBe(true);
    expect(service.adminConnecte()).toBe(false);
    expect(service.token()).toBe('jwt-joueur');
    expect(JSON.parse(localStorage.getItem('padel-joueur') ?? '{}')).toEqual(joueur);
    expect(localStorage.getItem('padel-admin')).toBeNull();
  });

  it('doit déconnecter le joueur quand un admin se connecte', () => {
    const service = creerService();

    service.definirJoueur(joueur);
    service.definirAdmin(admin);

    expect(service.joueur()).toBeNull();
    expect(service.admin()).toEqual(admin);
    expect(service.joueurConnecte()).toBe(false);
    expect(service.adminConnecte()).toBe(true);
    expect(service.token()).toBe('jwt-admin');
    expect(localStorage.getItem('padel-joueur')).toBeNull();
    expect(JSON.parse(localStorage.getItem('padel-admin') ?? '{}')).toEqual(admin);
  });

  it('doit nettoyer le stockage si joueur et admin existent en même temps', () => {
    localStorage.setItem('padel-joueur', JSON.stringify(joueur));
    localStorage.setItem('padel-admin', JSON.stringify(admin));

    const service = creerService();

    expect(service.joueur()).toBeNull();
    expect(service.admin()).toBeNull();
    expect(service.joueurConnecte()).toBe(false);
    expect(service.adminConnecte()).toBe(false);
    expect(service.token()).toBeNull();
    expect(localStorage.getItem('padel-joueur')).toBeNull();
    expect(localStorage.getItem('padel-admin')).toBeNull();
  });

  it('doit synchroniser la connexion d un joueur depuis un autre onglet', () => {
    const service = creerService();
    service.definirAdmin(admin);

    localStorage.removeItem('padel-admin');
    notifierChangementStockage('padel-admin');

    localStorage.setItem('padel-joueur', JSON.stringify(joueur));
    notifierChangementStockage('padel-joueur');

    expect(service.joueur()).toEqual(joueur);
    expect(service.admin()).toBeNull();
    expect(service.joueurConnecte()).toBe(true);
    expect(service.adminConnecte()).toBe(false);
    expect(service.token()).toBe('jwt-joueur');
  });

  it('doit synchroniser la connexion d un admin depuis un autre onglet', () => {
    const service = creerService();
    service.definirJoueur(joueur);

    localStorage.removeItem('padel-joueur');
    notifierChangementStockage('padel-joueur');

    localStorage.setItem('padel-admin', JSON.stringify(admin));
    notifierChangementStockage('padel-admin');

    expect(service.joueur()).toBeNull();
    expect(service.admin()).toEqual(admin);
    expect(service.joueurConnecte()).toBe(false);
    expect(service.adminConnecte()).toBe(true);
    expect(service.token()).toBe('jwt-admin');
  });

  it('doit synchroniser une déconnexion effectuée dans un autre onglet', () => {
    const service = creerService();
    service.definirJoueur(joueur);

    localStorage.removeItem('padel-joueur');
    notifierChangementStockage('padel-joueur');

    expect(service.joueur()).toBeNull();
    expect(service.joueurConnecte()).toBe(false);
    expect(service.token()).toBeNull();
  });

  it('doit déconnecter le joueur', () => {
    const service = creerService();

    service.definirJoueur(joueur);
    service.deconnecterJoueur();

    expect(service.joueur()).toBeNull();
    expect(service.joueurConnecte()).toBe(false);
    expect(service.token()).toBeNull();
    expect(localStorage.getItem('padel-joueur')).toBeNull();
  });

  it('doit déconnecter l admin', () => {
    const service = creerService();

    service.definirAdmin(admin);
    service.deconnecterAdmin();

    expect(service.admin()).toBeNull();
    expect(service.adminConnecte()).toBe(false);
    expect(service.token()).toBeNull();
    expect(localStorage.getItem('padel-admin')).toBeNull();
  });

  it('doit relire un joueur déjà stocké dans localStorage', () => {
    localStorage.setItem('padel-joueur', JSON.stringify(joueur));

    const service = creerService();

    expect(service.joueur()).toEqual(joueur);
    expect(service.joueurConnecte()).toBe(true);
    expect(service.token()).toBe('jwt-joueur');
  });

  it('doit conserver une session joueur expirée pour permettre le refresh', () => {
    localStorage.setItem('padel-joueur', JSON.stringify({
      ...joueur,
      expirationToken: '2020-01-01T00:00:00'
    }));

    const service = creerService();

    expect(service.joueur()).not.toBeNull();
    expect(service.joueurConnecte()).toBe(true);
    expect(service.token()).toBe('jwt-joueur');
  });

  it('doit conserver une session admin expirée pour permettre le refresh', () => {
    localStorage.setItem('padel-admin', JSON.stringify({
      ...admin,
      expirationToken: '2020-01-01T00:00:00'
    }));

    const service = creerService();

    expect(service.admin()).not.toBeNull();
    expect(service.adminConnecte()).toBe(true);
    expect(service.token()).toBe('jwt-admin');
  });

  it('doit remplacer le token du joueur après refresh', () => {
    const service = creerService();
    service.definirJoueur(joueur);

    service.mettreAJourToken({
      token: 'jwt-joueur-renouvele',
      expirationToken: '2100-01-01T00:59:59'
    });

    expect(service.token()).toBe('jwt-joueur-renouvele');
    expect(service.joueur()?.expirationToken).toBe(
      '2100-01-01T00:59:59'
    );
    expect(
      JSON.parse(localStorage.getItem('padel-joueur') ?? '{}').token
    ).toBe('jwt-joueur-renouvele');
  });

  it('doit remplacer le token de l admin après refresh', () => {
    const service = creerService();
    service.definirAdmin(admin);

    service.mettreAJourToken({
      token: 'jwt-admin-renouvele',
      expirationToken: '2100-01-01T00:59:59'
    });

    expect(service.token()).toBe('jwt-admin-renouvele');
    expect(service.admin()?.expirationToken).toBe(
      '2100-01-01T00:59:59'
    );
  });

  it('doit effacer toutes les sessions si le refresh échoue', () => {
    const service = creerService();
    service.definirJoueur(joueur);

    service.deconnecterTout();

    expect(service.joueur()).toBeNull();
    expect(service.admin()).toBeNull();
    expect(service.token()).toBeNull();
    expect(localStorage.getItem('padel-joueur')).toBeNull();
    expect(localStorage.getItem('padel-admin')).toBeNull();
  });

  it('doit supprimer une session stockée sans expiration', () => {
    const joueurSansExpiration = { ...joueur };
    delete joueurSansExpiration.expirationToken;
    localStorage.setItem('padel-joueur', JSON.stringify(joueurSansExpiration));

    const service = creerService();

    expect(service.joueur()).toBeNull();
    expect(localStorage.getItem('padel-joueur')).toBeNull();
  });

  it('doit nettoyer une valeur localStorage invalide', () => {
    localStorage.setItem('padel-joueur', 'valeur-json-invalide');

    const service = creerService();

    expect(service.joueur()).toBeNull();
    expect(localStorage.getItem('padel-joueur')).toBeNull();
  });
});
