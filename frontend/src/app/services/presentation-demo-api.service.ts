import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PresentationDemoResponse } from '../models/donnees-demonstration.model';

@Injectable({
  providedIn: 'root'
})
export class PresentationDemoApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {
  }

  consulterPresentation(): Observable<PresentationDemoResponse> {
    return this.http.get<PresentationDemoResponse>(
      `${this.apiUrl}/api/demo/presentation`
    );
  }
}
