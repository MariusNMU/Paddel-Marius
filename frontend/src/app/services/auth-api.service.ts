import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AuthAdminResponse,
  AuthJoueurResponse,
  ConnexionAdminRequest,
  ConnexionJoueurRequest
} from '../models/auth.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {
  }

  connecterJoueur(request: ConnexionJoueurRequest): Observable<AuthJoueurResponse> {
    return this.http.post<AuthJoueurResponse>(
      `${this.apiUrl}/api/auth/joueur`,
      request
    );
  }

  connecterAdmin(request: ConnexionAdminRequest): Observable<AuthAdminResponse> {
    return this.http.post<AuthAdminResponse>(
      `${this.apiUrl}/api/auth/admin`,
      request
    );
  }
}
