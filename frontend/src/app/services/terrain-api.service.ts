import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { TerrainResponse } from '../models/terrain.model';

@Injectable({
  providedIn: 'root'
})
export class TerrainApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {
  }

  listerTerrainsActifs(): Observable<TerrainResponse[]> {
    return this.http.get<TerrainResponse[]>(
      `${this.apiUrl}/api/terrains`
    );
  }
}
