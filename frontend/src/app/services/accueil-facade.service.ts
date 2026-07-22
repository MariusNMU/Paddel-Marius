import { Injectable, signal } from '@angular/core';
import {
  catchError,
  EMPTY,
  finalize,
  tap
} from 'rxjs';
import { PresentationDemoResponse } from '../models/donnees-demonstration.model';
import { PresentationDemoApiService } from './presentation-demo-api.service';

@Injectable()
export class AccueilFacadeService {
  private readonly donneesDemonstrationSignal =
    signal<PresentationDemoResponse | null>(null);

  private readonly chargementSignal =
    signal(false);

  private readonly messageErreurSignal =
    signal('');

  readonly donneesDemonstration =
    this.donneesDemonstrationSignal.asReadonly();

  readonly chargement =
    this.chargementSignal.asReadonly();

  readonly messageErreur =
    this.messageErreurSignal.asReadonly();

  constructor(
    private readonly presentationDemoApiService:
    PresentationDemoApiService
  ) {
  }

  initialiser(): void {
    this.chargerPresentation();
  }

  reessayer(): void {
    this.chargerPresentation();
  }

  private chargerPresentation(): void {
    this.messageErreurSignal.set('');
    this.donneesDemonstrationSignal.set(null);
    this.chargementSignal.set(true);

    this.presentationDemoApiService
      .consulterPresentation()
      .pipe(
        tap(response => {
          this.donneesDemonstrationSignal.set(response);
        }),
        catchError(() => {
          this.messageErreurSignal.set(
            'Les données de démonstration ne sont pas disponibles. '
            + 'Vérifie que le backend est démarré en mode démo.'
          );

          return EMPTY;
        }),
        finalize(() => {
          this.chargementSignal.set(false);
        })
      )
      .subscribe();
  }
}
