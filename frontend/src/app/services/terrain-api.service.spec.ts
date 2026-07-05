import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { TerrainResponse } from '../models/terrain.model';
import { TerrainApiService } from './terrain-api.service';

describe('TerrainApiService', () => {
  let service: TerrainApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        TerrainApiService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(TerrainApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('doit appeler l API de consultation des terrains actifs', () => {
    const response: TerrainResponse[] = [
      {
        terrainId: 1101,
        numeroTerrain: 'T1',
        siteId: 1001,
        nomSite: 'Padel Bruxelles'
      }
    ];

    service.listerTerrainsActifs().subscribe(resultat => {
      expect(resultat).toEqual(response);
    });

    const request = httpMock.expectOne('/api/terrains');

    expect(request.request.method).toBe('GET');

    request.flush(response);
  });
});
