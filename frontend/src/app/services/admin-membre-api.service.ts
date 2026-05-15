import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { MembreAdminResponse } from '../models/admin-membre.model';

@Injectable({
  providedIn: 'root'
})
export class AdminMembreApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {
  }

  listerTousLesMembres(): Observable<MembreAdminResponse[]> {
    return this.http.get<MembreAdminResponse[]>(
      `${this.apiUrl}/api/admin/membres`
    );
  }

  listerMembresParSite(siteId: number): Observable<MembreAdminResponse[]> {
    return this.http.get<MembreAdminResponse[]>(
      `${this.apiUrl}/api/admin/sites/${siteId}/membres`
    );
  }
}
