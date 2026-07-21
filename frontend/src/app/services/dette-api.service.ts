import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  DetteResponse,
  PaiementDetteResponse,
  PayerDetteRequest
} from '../models/dette.model';

@Injectable({
  providedIn: 'root'
})
export class DetteApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {
  }

  consulterDettesOuvertes(
    matricule: string
  ): Observable<DetteResponse[]> {
    return this.http.get<DetteResponse[]>(
      `${this.apiUrl}/api/membres/${matricule}/dettes/ouvertes`
    );
  }

  payerDette(
    detteId: number,
    request: PayerDetteRequest
  ): Observable<PaiementDetteResponse> {
    return this.http.post<PaiementDetteResponse>(
      `${this.apiUrl}/api/dettes/${detteId}/paiements`,
      request
    );
  }
}
