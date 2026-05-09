import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { StatistiquesAdminResponse } from '../models/statistique.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AdminStatsApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {
  }

  consulterStatistiques(
    dateDebut: string,
    dateFin: string,
    siteId?: number
  ): Observable<StatistiquesAdminResponse> {
    let params = new HttpParams()
      .set('dateDebut', dateDebut)
      .set('dateFin', dateFin);

    if (siteId !== undefined && siteId !== null) {
      params = params.set('siteId', siteId);
    }

    return this.http.get<StatistiquesAdminResponse>(
      `${this.apiUrl}/api/admin/statistiques`,
      { params }
    );
  }
}
