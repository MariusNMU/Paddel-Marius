import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import {
  TraitementEcheanceResponse
} from '../models/traitement-echeance.model';
import {
  AdminTraitementEcheanceApiService
} from './admin-traitement-echeance-api.service';

describe(
  'AdminTraitementEcheanceApiService',
  () => {
    let service:
      AdminTraitementEcheanceApiService;

    let httpMock:
      HttpTestingController;

    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [
          AdminTraitementEcheanceApiService,
          provideHttpClient(),
          provideHttpClientTesting()
        ]
      });

      service = TestBed.inject(
        AdminTraitementEcheanceApiService
      );

      httpMock = TestBed.inject(
        HttpTestingController
      );
    });

    afterEach(() => {
      httpMock.verify();
    });

    it(
      'doit appeler l endpoint de traitement d échéance',
      () => {
        const response:
          TraitementEcheanceResponse = {
          dateHeureTraitement:
            '2026-07-20T17:00:00',
          matchesAnalyses: 3,
          matchesDemarres: 2,
          matchesTermines: 1,
          dettesCreees: 1
        };

        service.traiterEcheance()
          .subscribe(resultat => {
            expect(resultat)
              .toEqual(response);
          });

        const request =
          httpMock.expectOne(
            '/api/admin/matches/traitement-echeance'
          );

        expect(request.request.method)
          .toBe('POST');

        expect(request.request.body)
          .toEqual({});

        request.flush(response);
      }
    );
  }
);
