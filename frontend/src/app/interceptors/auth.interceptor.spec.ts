import {
  HttpClient,
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthContextService } from '../services/auth-context.service';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;
  let authContextService: AuthContextService;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(
          withInterceptors([authInterceptor])
        ),
        provideHttpClientTesting()
      ]
    });

    httpClient = TestBed.inject(HttpClient);
    httpTestingController = TestBed.inject(
      HttpTestingController
    );
    authContextService = TestBed.inject(
      AuthContextService
    );
  });

  afterEach(() => {
    httpTestingController.verify();
    localStorage.clear();
  });

  it(
    'doit envoyer le token actif sans déduire le type depuis l URL',
    () => {
      authContextService.definirAdmin({
        administrateurId: 2101,
        login: 'admin-global',
        nom: 'Admin',
        prenom: 'Global',
        roleAdministrateur: 'GLOBAL',
        siteId: null,
        nomSite: null,
        actif: true,
        token: 'jwt-admin'
      });

      httpClient
        .get('/api/membres/G1001/solde')
        .subscribe();

      const request = httpTestingController.expectOne(
        '/api/membres/G1001/solde'
      );

      expect(
        request.request.headers.get('Authorization')
      ).toBe('Bearer jwt-admin');

      expect(
        request.request.headers.has('X-Admin-Login')
      ).toBe(false);

      request.flush({});
    }
  );

  it(
    'doit envoyer le token joueur exposé par le contexte',
    () => {
      authContextService.definirJoueur({
        membreId: 2001,
        matricule: 'G1001',
        nom: 'Dupont',
        prenom: 'Marie',
        categorieMembre: 'GLOBAL',
        siteRattachementId: null,
        nomSiteRattachement: null,
        actif: true,
        token: 'jwt-joueur'
      });

      httpClient
        .get('/api/membres/G1001/solde')
        .subscribe();

      const request = httpTestingController.expectOne(
        '/api/membres/G1001/solde'
      );

      expect(
        request.request.headers.get('Authorization')
      ).toBe('Bearer jwt-joueur');

      expect(
        request.request.headers.has('X-Admin-Login')
      ).toBe(false);

      request.flush({});
    }
  );

  it(
    'ne doit pas modifier une requête extérieure à l API',
    () => {
      authContextService.definirJoueur({
        membreId: 2001,
        matricule: 'G1001',
        nom: 'Dupont',
        prenom: 'Marie',
        categorieMembre: 'GLOBAL',
        siteRattachementId: null,
        nomSiteRattachement: null,
        actif: true,
        token: 'jwt-joueur'
      });

      httpClient
        .get('https://example.org/status')
        .subscribe();

      const request = httpTestingController.expectOne(
        'https://example.org/status'
      );

      expect(
        request.request.headers.has('Authorization')
      ).toBe(false);

      expect(
        request.request.headers.has('X-Admin-Login')
      ).toBe(false);

      request.flush({});
    }
  );

  it(
    'ne doit pas transmettre le JWT à une URL externe contenant api',
    () => {
      authContextService.definirJoueur({
        membreId: 2001,
        matricule: 'G1001',
        nom: 'Dupont',
        prenom: 'Marie',
        categorieMembre: 'GLOBAL',
        siteRattachementId: null,
        nomSiteRattachement: null,
        actif: true,
        token: 'jwt-joueur'
      });

      httpClient
        .post('https://example.org/api/collect', {})
        .subscribe();

      const request = httpTestingController.expectOne(
        'https://example.org/api/collect'
      );

      expect(
        request.request.headers.has('Authorization')
      ).toBe(false);

      request.flush({});
    }
  );
});
