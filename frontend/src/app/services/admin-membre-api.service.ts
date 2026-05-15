import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { MembreResponse } from '../models/membre.model';

@Injectable({
  providedIn: 'root'
})
export class AdminMembreApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {
  }

  listerTousLesMembres(): Observable<MembreResponse[]> {
    return this.http.get<MembreResponse[]>(
      `${this.apiUrl}/api/admin/membres`
    );
  }

  listerMembresParSite(siteId: number): Observable<MembreResponse[]> {
    return this.http.get<MembreResponse[]>(
      `${this.apiUrl}/api/admin/membres?siteId=${siteId}`
    );
  }
}
