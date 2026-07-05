import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ParametresMetierResponse } from '../models/parametres-metier.model';
import { ParametresMetierApiService } from './parametres-metier-api.service';

describe('ParametresMetierApiService', () => {
  let service: ParametresMetierApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ParametresMetierApiService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(ParametresMetierApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('doit appeler l API de consultation des paramètres métier', () => {
    const response: ParametresMetierResponse = {
      dureeMatchMinutes: 90,
      pauseEntreMatchesMinutes: 15,
      nombreJoueursMaximum: 4,
      prixTotalMatch: 60,
      montantParticipationStandard: 15,
      soldeInitialJoueur: 100
    };

    service.consulterParametresMetier().subscribe(resultat => {
      expect(resultat).toEqual(response);
    });

    const request = httpMock.expectOne('/api/parametres-metier');

    expect(request.request.method).toBe('GET');

    request.flush(response);
  });
});
