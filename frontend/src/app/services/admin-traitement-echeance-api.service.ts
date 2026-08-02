import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  TraitementEcheanceResponse
} from '../models/traitement-echeance.model';

@Injectable({
  providedIn: 'root'
})
export class AdminTraitementEcheanceApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(
    private readonly http: HttpClient
  ) {
  }

  traiterEcheance():
    Observable<TraitementEcheanceResponse> {
    return this.http.post<TraitementEcheanceResponse>(
      `${this.apiUrl}/api/admin/matches/traitement-echeance`,
      {}
    );
  }
}
