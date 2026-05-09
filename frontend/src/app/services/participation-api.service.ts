import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AjouterParticipantPriveRequest,
  InscriptionPubliqueRequest,
  ParticipationResponse
} from '../models/participation.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ParticipationApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {
  }

  ajouterParticipantPrive(
    matchId: number,
    request: AjouterParticipantPriveRequest
  ): Observable<ParticipationResponse> {
    return this.http.post<ParticipationResponse>(
      `${this.apiUrl}/api/matches/${matchId}/participants/prive`,
      request
    );
  }

  inscrireParticipantPublic(
    matchId: number,
    request: InscriptionPubliqueRequest
  ): Observable<ParticipationResponse> {
    return this.http.post<ParticipationResponse>(
      `${this.apiUrl}/api/matches/${matchId}/participants/public`,
      request
    );
  }
}
