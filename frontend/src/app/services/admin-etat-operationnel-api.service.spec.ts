import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import {
  EtatOperationnelAdminResponse,
  OccupationHebdomadaireAdminResponse
} from '../models/etat-operationnel.model';
import { SiteResponse } from '../models/site.model';
import {
  AdminEtatOperationnelApiService
} from './admin-etat-operationnel-api.service';

describe(
  'AdminEtatOperationnelApiService',
  () => {
    let service:
      AdminEtatOperationnelApiService;

    let httpMock:
      HttpTestingController;

    beforeEach(() => {
      TestBed.configureTestingModule({
        providers: [
          AdminEtatOperationnelApiService,
          provideHttpClient(),
          provideHttpClientTesting()
        ]
      });

      service = TestBed.inject(
        AdminEtatOperationnelApiService
      );

      httpMock = TestBed.inject(
        HttpTestingController
      );
    });

    afterEach(() => {
      httpMock.verify();
    });

    it(
      'doit appeler l API admin qui liste tous les sites',
      () => {
        const response: SiteResponse[] = [
          {
            siteId: 1001,
            code: 'BRU',
            nom: 'Padel Bruxelles',
            adresse: 'Rue du Padel 1'
          }
        ];

        service.listerTousSites()
          .subscribe(resultat => {
            expect(resultat).toEqual(response);
          });

        const request = httpMock.expectOne(
          '/api/admin/sites'
        );

        expect(request.request.method)
          .toBe('GET');

        request.flush(response);
      }
    );

    it(
      'doit appeler l API avec la date et le site',
      () => {
        const response:
          EtatOperationnelAdminResponse = {
          date: '2026-07-20',
          siteId: 1001,
          nomSite: 'Padel Bruxelles',
          siteActif: true,
          ferme: false,
          motifFermeture: null,
          terrains: []
        };

        service.consulterEtatOperationnel(
          '2026-07-20',
          1001
        ).subscribe(resultat => {
          expect(resultat).toEqual(response);
        });

        const request = httpMock.expectOne(
          requete =>
            requete.url
            === '/api/admin/etat-operationnel'
            && requete.params.get('date')
            === '2026-07-20'
            && requete.params.get('siteId')
            === '1001'
        );

        expect(request.request.method)
          .toBe('GET');

        request.flush(response);
      }
    );

    it(
      'doit appeler l API d occupation hebdomadaire',
      () => {
        const response:
          OccupationHebdomadaireAdminResponse = {
          dateDebut: '2026-07-20',
          dateFin: '2026-07-26',
          siteId: 1001,
          nomSite: 'Padel Bruxelles',
          siteActif: true,
          jours: []
        };

        service.consulterOccupationHebdomadaire(
          '2026-07-22',
          1001
        ).subscribe(resultat => {
          expect(resultat).toEqual(response);
        });

        const request = httpMock.expectOne(
          requete =>
            requete.url
            === '/api/admin/etat-operationnel/semaine'
            && requete.params.get('date')
            === '2026-07-22'
            && requete.params.get('siteId')
            === '1001'
        );

        expect(request.request.method)
          .toBe('GET');

        request.flush(response);
      }
    );
  }
);
