import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthAdminResponse, AuthJoueurResponse } from '../models/auth.model';
import { AuthApiService } from './auth-api.service';

describe('AuthApiService', () => {
  let service: AuthApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthApiService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(AuthApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('doit appeler l API de connexion joueur', () => {
    const response: AuthJoueurResponse = {
      membreId: 2001,
      matricule: 'G1001',
      nom: 'Dupont',
      prenom: 'Marie',
      categorieMembre: 'GLOBAL',
      siteRattachementId: null,
      nomSiteRattachement: null,
      actif: true
    };

    service.connecterJoueur({
      matricule: 'G1001',
      motDePasse: 'password'
    }).subscribe(resultat => {
      expect(resultat).toEqual(response);
    });

    const request = httpMock.expectOne('/api/auth/joueur');

    expect(request.request.method).toBe('POST');
    expect(request.request.withCredentials).toBe(true);
    expect(request.request.body).toEqual({
      matricule: 'G1001',
      motDePasse: 'password'
    });

    request.flush(response);
  });

  it('doit appeler l API de connexion admin', () => {
    const response: AuthAdminResponse = {
      administrateurId: 2101,
      login: 'admin-global',
      nom: 'Admin',
      prenom: 'Global',
      roleAdministrateur: 'GLOBAL',
      siteId: null,
      nomSite: null,
      actif: true
    };

    service.connecterAdmin({
      login: 'admin-global',
      motDePasse: 'secret'
    }).subscribe(resultat => {
      expect(resultat).toEqual(response);
    });

    const request = httpMock.expectOne('/api/auth/admin');

    expect(request.request.method).toBe('POST');
    expect(request.request.withCredentials).toBe(true);
    expect(request.request.body).toEqual({
      login: 'admin-global',
      motDePasse: 'secret'
    });

    request.flush(response);
  });

  it('doit demander un nouveau token avec le cookie HttpOnly', () => {
    const response = {
      token: 'nouvel-access',
      expirationToken: '2099-12-31T23:59:59'
    };

    service.rafraichir().subscribe(resultat => {
      expect(resultat).toEqual(response);
    });

    const request = httpMock.expectOne('/api/auth/refresh');

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toBeNull();
    expect(request.request.withCredentials).toBe(true);

    request.flush(response);
  });

  it('doit demander la suppression du cookie au logout', () => {
    service.deconnecter().subscribe();

    const request = httpMock.expectOne('/api/auth/logout');

    expect(request.request.method).toBe('POST');
    expect(request.request.withCredentials).toBe(true);

    request.flush(null);
  });
});
