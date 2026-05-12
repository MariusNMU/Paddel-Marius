import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { SoldeJoueurResponse } from '../models/solde-joueur.model';

@Injectable({
  providedIn: 'root'
})
export class SoldeJoueurApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {
  }

  consulterSolde(matricule: string): Observable<SoldeJoueurResponse> {
    return this.http.get<SoldeJoueurResponse>(
      `${this.apiUrl}/api/membres/${matricule}/solde`
    );
  }
}
