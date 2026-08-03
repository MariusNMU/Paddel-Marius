import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { PaiementResponse } from '../models/paiement.model';
import { PaiementApiService } from './paiement-api.service';

describe('PaiementApiService', () => {
  let service: PaiementApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        PaiementApiService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(PaiementApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('doit utiliser l endpoint unique de paiement avec le montant standard', () => {
    const response: PaiementResponse = {
      paiementId: 4101,
      participationId: 3101,
      membreId: 2001,
      matriculeMembre: 'G1001',
      montant: 15,
      montantDettesReglees: 0,
      montantTotalDebite: 15,
      naturePaiement: 'PARTICIPATION',
      statutPaiement: 'PAYE',
      statutParticipation: 'CONFIRMEE',
      dateHeurePaiement: '2026-06-01T12:00:00',
      dateConfirmationParticipation: '2026-06-01T12:00:00'
    };

    service.payerParticipation(3101).subscribe(resultat => {
      expect(resultat).toEqual(response);
    });

    const request = httpMock.expectOne(
      '/api/participations/3101/paiements'
    );

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ montant: 15 });

    request.flush(response);
  });
});
