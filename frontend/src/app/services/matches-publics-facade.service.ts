import { Injectable, signal } from '@angular/core';
import { catchError, EMPTY, finalize, tap } from 'rxjs';
import { AuthJoueurResponse } from '../models/auth.model';
import {
  MatchPublicResponse,
  RejoindreMatchPublicResponse
} from '../models/match-public.model';
import { ParametresMetierResponse } from '../models/parametres-metier.model';
import { SiteResponse } from '../models/site.model';
import { extraireMessageErreur } from '../shared/api-error.util';
import {
  JourRapide,
  dateDuJourPourInput,
  genererJoursRapides
} from '../shared/date-ui.util';
import { AuthContextService } from './auth-context.service';
import { MatchPublicApiService } from './match-public-api.service';
import { ParametresMetierApiService } from './parametres-metier-api.service';
import { SiteApiService } from './site-api.service';

@Injectable()
export class MatchesPublicsFacadeService {
  private readonly sitesSignal = signal<SiteResponse[]>([]);
  private readonly parametresMetierSignal =
    signal<ParametresMetierResponse | null>(null);
  private readonly joursRapidesSignal = signal<JourRapide[]>([]);

  private readonly siteIdSignal = signal<number | null>(null);
  private readonly dateSignal = signal('');

  private readonly matchesSignal = signal<MatchPublicResponse[]>([]);
  private readonly dernierPaiementSignal =
    signal<RejoindreMatchPublicResponse | null>(null);

  private readonly chargementSitesSignal = signal(false);
  private readonly chargementParametresMetierSignal = signal(false);
  private readonly chargementRechercheSignal = signal(false);
  private readonly chargementPaiementSignal = signal(false);
  private readonly rechercheEffectueeSignal = signal(false);

  private readonly messageErreurSignal = signal('');
  private readonly messageSuccesSignal = signal('');

  readonly sites = this.sitesSignal.asReadonly();
  readonly parametresMetier =
    this.parametresMetierSignal.asReadonly();
  readonly joursRapides = this.joursRapidesSignal.asReadonly();

  readonly siteId = this.siteIdSignal.asReadonly();
  readonly date = this.dateSignal.asReadonly();

  readonly matches = this.matchesSignal.asReadonly();
  readonly dernierPaiement =
    this.dernierPaiementSignal.asReadonly();

  readonly chargementSites =
    this.chargementSitesSignal.asReadonly();
  readonly chargementParametresMetier =
    this.chargementParametresMetierSignal.asReadonly();
  readonly chargementRecherche =
    this.chargementRechercheSignal.asReadonly();
  readonly chargementPaiement =
    this.chargementPaiementSignal.asReadonly();
  readonly rechercheEffectuee =
    this.rechercheEffectueeSignal.asReadonly();

  readonly messageErreur =
    this.messageErreurSignal.asReadonly();
  readonly messageSucces =
    this.messageSuccesSignal.asReadonly();

  constructor(
    private readonly matchPublicApiService: MatchPublicApiService,
    private readonly siteApiService: SiteApiService,
    private readonly parametresMetierApiService:
    ParametresMetierApiService,
    private readonly authContextService: AuthContextService
  ) {
  }

  initialiser(): void {
    this.reinitialiserParcours();

    this.dateSignal.set(dateDuJourPourInput());
    this.joursRapidesSignal.set(genererJoursRapides(7));

    this.chargerParametresMetier();
    this.chargerSites();
  }

  joueurConnecte(): AuthJoueurResponse | null {
    return this.authContextService.joueur();
  }

  modifierSiteId(siteId: number | null): void {
    this.siteIdSignal.set(siteId);
  }

  modifierDate(date: string): void {
    this.dateSignal.set(date);
  }

  selectionnerJour(date: string): void {
    this.dateSignal.set(date);
    this.messageErreurSignal.set('');
    this.messageSuccesSignal.set('');
    this.dernierPaiementSignal.set(null);
    this.matchesSignal.set([]);
    this.rechercheEffectueeSignal.set(false);
  }

  rechercherMatchesPublics(): void {
    this.messageErreurSignal.set('');
    this.messageSuccesSignal.set('');
    this.dernierPaiementSignal.set(null);
    this.rechercheEffectueeSignal.set(true);

    const siteId = this.siteIdSignal();
    const date = this.dateSignal();

    if (siteId === null || !date) {
      this.messageErreurSignal.set(
        'Le site et la date sont obligatoires.'
      );
      this.matchesSignal.set([]);
      return;
    }

    this.chargerMatchesPublics(siteId, date);
  }

  rejoindreEtPayer(match: MatchPublicResponse): void {
    this.messageErreurSignal.set('');
    this.messageSuccesSignal.set('');
    this.dernierPaiementSignal.set(null);

    if (!match.peutRejoindre) {
      this.messageErreurSignal.set(
        match.motifNonEligibilite
        ?? 'Ce match ne peut pas être rejoint.'
      );
      return;
    }

    const joueur = this.joueurConnecte();

    if (!joueur) {
      this.messageErreurSignal.set(
        'Connecte-toi comme joueur pour rejoindre un match public.'
      );
      return;
    }

    this.chargementPaiementSignal.set(true);

    this.matchPublicApiService.rejoindreEtPayer(
      match.matchId,
      {
        matriculeJoueur: joueur.matricule
      }
    ).pipe(
      tap(response => {
        this.dernierPaiementSignal.set(response);
        this.messageSuccesSignal.set(
          `Le joueur ${response.matriculeJoueur} a rejoint `
          + `le match public et payé `
          + `${response.montantPaye.toFixed(2)} €.`
        );

        this.actualiserApresPaiement();
      }),
      catchError(error => {
        this.messageErreurSignal.set(
          extraireMessageErreur(error)
        );
        return EMPTY;
      }),
      finalize(() => {
        this.chargementPaiementSignal.set(false);
      })
    ).subscribe();
  }

  private actualiserApresPaiement(): void {
    const siteId = this.siteIdSignal();
    const date = this.dateSignal();

    if (siteId === null || !date) {
      return;
    }

    this.chargerMatchesPublics(siteId, date);
  }

  private chargerMatchesPublics(
    siteId: number,
    date: string
  ): void {
    this.chargementRechercheSignal.set(true);

    this.matchPublicApiService
      .listerMatchesPublics(siteId, date)
      .pipe(
        tap(matches => {
          this.matchesSignal.set(matches);
        }),
        catchError(error => {
          this.messageErreurSignal.set(
            extraireMessageErreur(error)
          );
          this.matchesSignal.set([]);
          return EMPTY;
        }),
        finalize(() => {
          this.chargementRechercheSignal.set(false);
        })
      )
      .subscribe();
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

  private chargerSites(): void {
    this.chargementSitesSignal.set(true);

    this.siteApiService.listerSitesActifs().pipe(
      tap(sites => {
        this.sitesSignal.set(sites);

        const siteIdActuel = this.siteIdSignal();
        const siteSelectionExiste =
          siteIdActuel !== null
          && sites.some(site => site.siteId === siteIdActuel);

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

  private reinitialiserParcours(): void {
    this.sitesSignal.set([]);
    this.parametresMetierSignal.set(null);
    this.joursRapidesSignal.set([]);

    this.siteIdSignal.set(null);
    this.dateSignal.set('');

    this.matchesSignal.set([]);
    this.dernierPaiementSignal.set(null);

    this.chargementSitesSignal.set(false);
    this.chargementParametresMetierSignal.set(false);
    this.chargementRechercheSignal.set(false);
    this.chargementPaiementSignal.set(false);
    this.rechercheEffectueeSignal.set(false);

    this.messageErreurSignal.set('');
    this.messageSuccesSignal.set('');
  }
}
