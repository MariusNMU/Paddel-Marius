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
import { adminAuthInterceptor } from './admin-auth.interceptor';

describe('adminAuthInterceptor', () => {
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;
  let authContextService: AuthContextService;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(
          withInterceptors([adminAuthInterceptor])
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
    'doit envoyer uniquement le JWT admin sur un endpoint admin',
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

      httpClient.get('/api/admin/membres').subscribe();

      const request = httpTestingController.expectOne(
        '/api/admin/membres'
      );

      expect(
        request.request.headers.get('Authorization')
      ).toBe('Bearer jwt-admin');

      expect(
        request.request.headers.has('X-Admin-Login')
      ).toBe(false);

      request.flush([]);
    }
  );

  it(
    'doit envoyer le JWT joueur sur un endpoint joueur',
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
});
