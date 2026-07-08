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
    actif: true
  };

  const admin: AuthAdminResponse = {
    administrateurId: 2101,
    login: 'admin-global',
    nom: 'Admin',
    prenom: 'Global',
    roleAdministrateur: 'GLOBAL',
    siteId: null,
    nomSite: null,
    actif: true
  };

  function creerService(): AuthContextService {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({});
    return TestBed.inject(AuthContextService);
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
  });

  it('doit définir un joueur connecté et le sauvegarder dans localStorage', () => {
    const service = creerService();

    service.definirJoueur(joueur);

    expect(service.joueur()).toEqual(joueur);
    expect(service.joueurConnecte()).toBe(true);
    expect(JSON.parse(localStorage.getItem('padel-joueur') ?? '{}')).toEqual(joueur);
  });

  it('doit définir un admin connecté et le sauvegarder dans localStorage', () => {
    const service = creerService();

    service.definirAdmin(admin);

    expect(service.admin()).toEqual(admin);
    expect(service.adminConnecte()).toBe(true);
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
    expect(localStorage.getItem('padel-joueur')).toBeNull();
    expect(localStorage.getItem('padel-admin')).toBeNull();
  });

  it('doit déconnecter le joueur', () => {
    const service = creerService();

    service.definirJoueur(joueur);
    service.deconnecterJoueur();

    expect(service.joueur()).toBeNull();
    expect(service.joueurConnecte()).toBe(false);
    expect(localStorage.getItem('padel-joueur')).toBeNull();
  });

  it('doit déconnecter l admin', () => {
    const service = creerService();

    service.definirAdmin(admin);
    service.deconnecterAdmin();

    expect(service.admin()).toBeNull();
    expect(service.adminConnecte()).toBe(false);
    expect(localStorage.getItem('padel-admin')).toBeNull();
  });

  it('doit relire un joueur déjà stocké dans localStorage', () => {
    localStorage.setItem('padel-joueur', JSON.stringify(joueur));

    const service = creerService();

    expect(service.joueur()).toEqual(joueur);
    expect(service.joueurConnecte()).toBe(true);
  });

  it('doit nettoyer une valeur localStorage invalide', () => {
    localStorage.setItem('padel-joueur', 'valeur-json-invalide');

    const service = creerService();

    expect(service.joueur()).toBeNull();
    expect(localStorage.getItem('padel-joueur')).toBeNull();
  });
});
