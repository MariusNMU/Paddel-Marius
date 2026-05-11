import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { InscriptionMembreRequest, MembreResponse } from '../models/membre.model';

@Injectable({
  providedIn: 'root'
})
export class MembreApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {
  }

  inscrireMembre(request: InscriptionMembreRequest): Observable<MembreResponse> {
    return this.http.post<MembreResponse>(
      `${this.apiUrl}/api/membres/inscription`,
      request
    );
  }
}
