import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  CreerFermetureRequest,
  FermetureAdminResponse
} from '../models/fermeture.model';

@Injectable({
  providedIn: 'root'
})
export class AdminFermetureApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {
  }

  creerFermeture(request: CreerFermetureRequest): Observable<FermetureAdminResponse> {
    return this.http.post<FermetureAdminResponse>(
      `${this.apiUrl}/api/admin/fermetures`,
      request
    );
  }
}
