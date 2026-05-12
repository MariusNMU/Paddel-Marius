import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ReservationJoueurResponse } from '../models/reservation.model';

@Injectable({
  providedIn: 'root'
})
export class ReservationApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {
  }

  consulterMesReservations(matricule: string): Observable<ReservationJoueurResponse[]> {
    return this.http.get<ReservationJoueurResponse[]>(
      `${this.apiUrl}/api/membres/${matricule}/reservations`
    );
  }
}
