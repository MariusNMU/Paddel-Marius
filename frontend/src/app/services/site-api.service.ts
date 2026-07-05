import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { SiteResponse } from '../models/site.model';

@Injectable({
  providedIn: 'root'
})
export class SiteApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private readonly http: HttpClient) {
  }

  listerSitesActifs(): Observable<SiteResponse[]> {
    return this.http.get<SiteResponse[]>(
      `${this.apiUrl}/api/sites`
    );
  }
}
