import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { SiteReservationInfoResponse } from '../models/site-reservation-info.model';

@Injectable({
  providedIn: 'root'
})
export class SiteReservationInfoApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {
  }

  listerSitesAvecInfosReservation(annee: number = new Date().getFullYear()): Observable<SiteReservationInfoResponse[]> {
    return this.http.get<SiteReservationInfoResponse[]>(
      `${this.apiUrl}/api/sites/reservation-infos?annee=${annee}`
    );
  }
}
