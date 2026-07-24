import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, EMPTY, finalize, tap } from 'rxjs';
import {
  CreneauDisponibiliteResponse,
  DisponibilitesResponse
} from '../models/disponibilite.model';
import { ParametresMetierResponse } from '../models/parametres-metier.model';
import { SiteResponse } from '../models/site.model';
import { extraireMessageErreur } from '../shared/api-error.util';
import {
  dateDuJourPourInput,
  genererJoursRapides,
  JourRapide
} from '../shared/date-ui.util';
import { AuthContextService } from './auth-context.service';
import { DisponibiliteApiService } from './disponibilite-api.service';
import { ParametresMetierApiService } from './parametres-metier-api.service';
import { SiteApiService } from './site-api.service';

@Injectable()
export class DisponibilitesFacadeService {
  private readonly sitesSignal = signal<SiteResponse[]>([]);
  private readonly parametresMetierSignal =
    signal<ParametresMetierResponse | null>(null);
  private readonly joursRapidesSignal = signal<JourRapide[]>([]);

  private readonly siteIdSignal = signal<number | null>(null);
  private readonly dateSignal = signal('');

  private readonly chargementSitesSignal = signal(false);
  private readonly chargementParametresMetierSignal = signal(false);
  private readonly chargementRechercheSignal = signal(false);

  private readonly messageErreurSignal = signal('');
  private readonly disponibilitesSignal =
    signal<DisponibilitesResponse | null>(null);

  readonly sites = this.sitesSignal.asReadonly();
  readonly parametresMetier =
    this.parametresMetierSignal.asReadonly();
  readonly joursRapides = this.joursRapidesSignal.asReadonly();

  readonly siteId = this.siteIdSignal.asReadonly();
  readonly date = this.dateSignal.asReadonly();

  readonly chargementSites =
    this.chargementSitesSignal.asReadonly();
  readonly chargementParametresMetier =
    this.chargementParametresMetierSignal.asReadonly();
  readonly chargementRecherche =
    this.chargementRechercheSignal.asReadonly();

  readonly messageErreur =
    this.messageErreurSignal.asReadonly();
  readonly disponibilites =
    this.disponibilitesSignal.asReadonly();

  constructor(
    private readonly disponibiliteApiService:
    DisponibiliteApiService,
    private readonly siteApiService: SiteApiService,
    private readonly parametresMetierApiService:
    ParametresMetierApiService,
    private readonly authContextService: AuthContextService,
    private readonly router: Router
  ) {
  }

  initialiser(): void {
    this.reinitialiserParcours();
    this.chargerSites();
    this.chargerParametresMetier();
  }

  modifierSiteId(siteId: number | null): void {
    this.siteIdSignal.set(siteId);
    this.reinitialiserResultat();
  }

  modifierDate(date: string): void {
    this.dateSignal.set(date);
    this.reinitialiserResultat();
  }

  selectionnerJour(date: string): void {
    this.modifierDate(date);
  }

  siteSelectionne(): SiteResponse | undefined {
    return this.sitesSignal().find(
      site => site.siteId === Number(this.siteIdSignal())
    );
  }

  dureeMatchLibelle(): string {
    const parametres = this.parametresMetierSignal();

    if (!parametres) {
      return 'Non chargée';
    }

    const minutes = parametres.dureeMatchMinutes;
    const heures = Math.floor(minutes / 60);
    const minutesRestantes = minutes % 60;

    if (minutesRestantes === 0) {
      return `${heures}h`;
    }

    return `${heures}h${String(minutesRestantes).padStart(2, '0')}`;
  }

  consulterDisponibilites(): void {
    this.reinitialiserResultat();

    const siteId = this.siteIdSignal();
    const date = this.dateSignal();

    if (!siteId || !date) {
      this.messageErreurSignal.set(
        'Le site et la date sont obligatoires.'
      );
      return;
    }

    this.chargementRechercheSignal.set(true);

    this.disponibiliteApiService
      .consulterDisponibilites(siteId, date)
      .pipe(
        tap(response => {
          this.disponibilitesSignal.set(response);
        }),
        catchError(error => {
          this.messageErreurSignal.set(
            extraireMessageErreur(error)
          );
          return EMPTY;
        }),
        finalize(() => {
          this.chargementRechercheSignal.set(false);
        })
      )
      .subscribe();
  }

  peutCreerMatchSurSiteSelectionne(): boolean {
    const joueur = this.authContextService.joueur();
    const site = this.siteSelectionne();

    if (!joueur || !site) {
      return false;
    }

    if (joueur.categorieMembre !== 'SITE') {
      return true;
    }

    return joueur.siteRattachementId !== null
      && joueur.siteRattachementId === site.siteId;
  }

  allerCreerMatch(
    creneau: CreneauDisponibiliteResponse
  ): void {
    if (!this.peutCreerMatchSurSiteSelectionne()) {
      this.messageErreurSignal.set(
        'Un membre SITE ne peut réserver que sur son site de rattachement.'
      );
      return;
    }

    this.router.navigate(['/joueur/creer-match'], {
      queryParams: {
        terrainId: creneau.terrainId,
        dateHeureDebut: creneau.dateHeureDebut
      }
    });
  }

  private chargerSites(): void {
    this.chargementSitesSignal.set(true);

    this.siteApiService.listerSitesActifs().pipe(
      tap(sites => {
        this.sitesSignal.set(sites);

        const siteIdActuel = this.siteIdSignal();
        const siteSelectionExiste =
          siteIdActuel !== null
          && sites.some(
            site => site.siteId === siteIdActuel
          );

        if (!siteSelectionExiste) {
          this.siteIdSignal.set(
            sites.length > 0
              ? sites[0].siteId
              : null
          );
        }
      }),
      catchError(error => {
        this.messageErreurSignal.set(
          extraireMessageErreur(error)
        );
        this.sitesSignal.set([]);
        this.siteIdSignal.set(null);
        return EMPTY;
      }),
      finalize(() => {
        this.chargementSitesSignal.set(false);
      })
    ).subscribe();
  }

  private chargerParametresMetier(): void {
    this.chargementParametresMetierSignal.set(true);

    this.parametresMetierApiService
      .consulterParametresMetier()
      .pipe(
        tap(parametres => {
          this.parametresMetierSignal.set(parametres);
        }),
        catchError(error => {
          this.messageErreurSignal.set(
            extraireMessageErreur(error)
          );
          this.parametresMetierSignal.set(null);
          return EMPTY;
        }),
        finalize(() => {
          this.chargementParametresMetierSignal.set(false);
        })
      )
      .subscribe();
  }

  private reinitialiserParcours(): void {
    this.sitesSignal.set([]);
    this.parametresMetierSignal.set(null);
    this.joursRapidesSignal.set(genererJoursRapides(7));

    this.siteIdSignal.set(null);
    this.dateSignal.set(dateDuJourPourInput());

    this.chargementSitesSignal.set(false);
    this.chargementParametresMetierSignal.set(false);
    this.chargementRechercheSignal.set(false);

    this.messageErreurSignal.set('');
    this.disponibilitesSignal.set(null);
  }

  private reinitialiserResultat(): void {
    this.messageErreurSignal.set('');
    this.disponibilitesSignal.set(null);
  }
}
