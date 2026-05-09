import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { TraitementVeilleResponse } from '../models/traitement-veille.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AdminTraitementVeilleApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {
  }

  traiterVeille(date: string): Observable<TraitementVeilleResponse> {
    const params = new HttpParams().set('date', date);

    return this.http.post<TraitementVeilleResponse>(
      `${this.apiUrl}/api/admin/matches/traitement-veille`,
      {},
      { params }
    );
  }
}
