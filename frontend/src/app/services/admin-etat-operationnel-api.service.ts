import {
  HttpClient,
  HttpParams
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  EtatOperationnelAdminResponse
} from '../models/etat-operationnel.model';
import { SiteResponse } from '../models/site.model';

@Injectable({
  providedIn: 'root'
})
export class AdminEtatOperationnelApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(
    private readonly http: HttpClient
  ) {
  }

  listerTousSites(): Observable<SiteResponse[]> {
    return this.http.get<SiteResponse[]>(
      `${this.apiUrl}/api/admin/sites`
    );
  }

  consulterEtatOperationnel(
    date: string,
    siteId: number
  ): Observable<EtatOperationnelAdminResponse> {
    const params = new HttpParams()
      .set('date', date)
      .set('siteId', siteId);

    return this.http.get<EtatOperationnelAdminResponse>(
      `${this.apiUrl}/api/admin/etat-operationnel`,
      { params }
    );
  }
}
