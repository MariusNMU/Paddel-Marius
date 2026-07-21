import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { DisponibilitesResponse } from '../models/disponibilite.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DisponibiliteApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {
  }

  consulterDisponibilites(siteId: number, date: string): Observable<DisponibilitesResponse> {
    const params = new HttpParams()
      .set('siteId', siteId)
      .set('date', date);

    return this.http.get<DisponibilitesResponse>(
      `${this.apiUrl}/api/disponibilites`,
      { params }
    );
  }
}
