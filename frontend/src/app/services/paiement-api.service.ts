import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  HistoriquePaiementResponse,
  PaiementResponse,
  PayerParticipationRequest
} from '../models/paiement.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PaiementApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {
  }

  payerParticipation(participationId: number): Observable<PaiementResponse> {
    const request: PayerParticipationRequest = {
      montant: 15
    };

    return this.http.post<PaiementResponse>(
      `${this.apiUrl}/api/participations/${participationId}/paiements`,
      request
    );
  }

  consulterHistoriquePaiements(matricule: string): Observable<HistoriquePaiementResponse[]> {
    return this.http.get<HistoriquePaiementResponse[]>(
      `${this.apiUrl}/api/membres/${matricule}/paiements`
    );
  }
}
