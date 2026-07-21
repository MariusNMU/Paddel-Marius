import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CreerMatchRequest, MatchResponse } from '../models/match.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MatchApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {
  }

  creerMatch(request: CreerMatchRequest): Observable<MatchResponse> {
    return this.http.post<MatchResponse>(
      `${this.apiUrl}/api/matches`,
      request
    );
  }
}
