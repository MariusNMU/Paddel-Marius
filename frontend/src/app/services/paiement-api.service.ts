import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PaiementResponse, PayerParticipationRequest } from '../models/paiement.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PaiementApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {
  }

  payerParticipation(
    participationId: number,
    request: PayerParticipationRequest
  ): Observable<PaiementResponse> {
    return this.http.post<PaiementResponse>(
      `${this.apiUrl}/api/participations/${participationId}/paiements`,
      request
    );
  }
}
