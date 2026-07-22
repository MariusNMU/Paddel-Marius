import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { PresentationDemoResponse } from '../models/donnees-demonstration.model';
import { PresentationDemoApiService } from './presentation-demo-api.service';

describe('PresentationDemoApiService', () => {
  let service: PresentationDemoApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        PresentationDemoApiService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(PresentationDemoApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('doit appeler l API de présentation de la démonstration', () => {
    const response: PresentationDemoResponse = {
      categoriesMembres: [],
      sites: [],
      joueurs: [],
      administrateurs: []
    };

    service.consulterPresentation().subscribe(resultat => {
      expect(resultat).toEqual(response);
    });

    const request = httpMock.expectOne('/api/demo/presentation');

    expect(request.request.method).toBe('GET');

    request.flush(response);
  });
});
