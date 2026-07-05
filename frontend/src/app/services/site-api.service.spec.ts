import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SiteResponse } from '../models/site.model';
import { SiteApiService } from './site-api.service';

describe('SiteApiService', () => {
  let service: SiteApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        SiteApiService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(SiteApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('doit appeler l API de consultation des sites actifs', () => {
    const response: SiteResponse[] = [
      {
        siteId: 1,
        code: 'ALP',
        nom: 'Site Alpha',
        adresse: 'Rue du Test 1'
      }
    ];

    service.listerSitesActifs().subscribe(resultat => {
      expect(resultat).toEqual(response);
    });

    const request = httpMock.expectOne('/api/sites');

    expect(request.request.method).toBe('GET');

    request.flush(response);
  });
});
