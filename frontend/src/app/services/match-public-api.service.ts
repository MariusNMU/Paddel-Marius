import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  MatchPublicResponse,
  RejoindreMatchPublicRequest,
  RejoindreMatchPublicResponse
} from '../models/match-public.model';

@Injectable({
  providedIn: 'root'
})
export class MatchPublicApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {
  }

  listerMatchesPublics(siteId: number, date: string): Observable<MatchPublicResponse[]> {
    return this.http.get<MatchPublicResponse[]>(
      `${this.apiUrl}/api/matches/publics?siteId=${siteId}&date=${date}`
    );
  }

  rejoindreEtPayer(
    matchId: number,
    request: RejoindreMatchPublicRequest
  ): Observable<RejoindreMatchPublicResponse> {
    return this.http.post<RejoindreMatchPublicResponse>(
      `${this.apiUrl}/api/matches/${matchId}/participants/public/payer`,
      request
    );
  }
}
