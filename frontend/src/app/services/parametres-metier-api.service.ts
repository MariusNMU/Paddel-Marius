import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ParametresMetierResponse } from '../models/parametres-metier.model';

@Injectable({
  providedIn: 'root'
})
export class ParametresMetierApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {
  }

  consulterParametresMetier(): Observable<ParametresMetierResponse> {
    return this.http.get<ParametresMetierResponse>(
      `${this.apiUrl}/api/parametres-metier`
    );
  }
}
