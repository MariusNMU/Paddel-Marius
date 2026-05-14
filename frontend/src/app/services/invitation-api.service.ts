import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  DeclinerInvitationRequest,
  InvitationPriveeResponse,
  InviterJoueurPriveRequest
} from '../models/invitation.model';

@Injectable({
  providedIn: 'root'
})
export class InvitationApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {
  }

  inviterJoueur(matchId: number, request: InviterJoueurPriveRequest): Observable<InvitationPriveeResponse> {
    return this.http.post<InvitationPriveeResponse>(
      `${this.apiUrl}/api/matches/${matchId}/invitations/privees`,
      request
    );
  }

  listerInvitationsRecues(matricule: string): Observable<InvitationPriveeResponse[]> {
    return this.http.get<InvitationPriveeResponse[]>(
      `${this.apiUrl}/api/membres/${matricule}/invitations/recues`
    );
  }

  compterInvitationsRecues(matricule: string): Observable<number> {
    return this.http.get<number>(
      `${this.apiUrl}/api/membres/${matricule}/invitations/recues/count`
    );
  }

  declinerInvitation(participationId: number, request: DeclinerInvitationRequest): Observable<InvitationPriveeResponse> {
    return this.http.post<InvitationPriveeResponse>(
      `${this.apiUrl}/api/participations/${participationId}/decliner`,
      request
    );
  }
}
