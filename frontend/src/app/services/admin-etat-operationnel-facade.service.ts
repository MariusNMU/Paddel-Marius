import {
  effect,
  Injectable,
  signal
} from '@angular/core';
import {
  catchError,
  EMPTY,
  finalize,
  Subject,
  takeUntil,
  tap
} from 'rxjs';
import { AuthAdminResponse } from '../models/auth.model';
import {
  EtatOperationnelAdminResponse
} from '../models/etat-operationnel.model';
import { SiteResponse } from '../models/site.model';
import {
  extraireMessageErreur
} from '../shared/api-error.util';
import {
  AdminEtatOperationnelApiService
} from './admin-etat-operationnel-api.service';
import {
  AuthContextService
} from './auth-context.service';
import { SiteApiService } from './site-api.service';

function dateIsoAujourdhui(): string {
  const date = new Date();
  const annee = date.getFullYear();

  const mois = String(
    date.getMonth() + 1
  ).padStart(2, '0');

  const jour = String(
    date.getDate()
  ).padStart(2, '0');

  return `${annee}-${mois}-${jour}`;
}

@Injectable()
export class AdminEtatOperationnelFacadeService {
  private readonly sitesSignal =
    signal<SiteResponse[]>([]);

  private readonly dateSignal =
    signal(dateIsoAujourdhui());

  private readonly siteIdSignal =
    signal<number | null>(null);

  private readonly chargementSitesSignal =
    signal(false);

  private readonly chargementSignal =
    signal(false);

  private readonly messageErreurSignal =
    signal('');

  private readonly etatOperationnelSignal =
    signal<EtatOperationnelAdminResponse | null>(
      null
    );

  private readonly changementSession$ =
    new Subject<void>();

  private parcoursInitialise = false;

  private adminObserve:
    AuthAdminResponse | null | undefined =
    undefined;

  get admin() {
    return this.authContextService.admin;
  }

  readonly sites =
    this.sitesSignal.asReadonly();

  readonly date =
    this.dateSignal.asReadonly();

  readonly siteId =
    this.siteIdSignal.asReadonly();

  readonly chargementSites =
    this.chargementSitesSignal.asReadonly();

  readonly chargement =
    this.chargementSignal.asReadonly();

  readonly messageErreur =
    this.messageErreurSignal.asReadonly();

  readonly etatOperationnel =
    this.etatOperationnelSignal.asReadonly();

  constructor(
    private readonly etatOperationnelApiService:
    AdminEtatOperationnelApiService,
    private readonly siteApiService:
    SiteApiService,
    private readonly authContextService:
    AuthContextService
  ) {
    effect(() => {
      const admin =
        this.authContextService.admin();

      if (
        !this.parcoursInitialise
        || admin === this.adminObserve
      ) {
        return;
      }

      this.adminObserve = admin;
      this.synchroniserAvecAdmin(admin);
    });
  }

  initialiser(): void {
    const admin =
      this.authContextService.admin();

    this.parcoursInitialise = true;
    this.adminObserve = admin;

    this.synchroniserAvecAdmin(admin);
  }

  estAdminGlobal(): boolean {
    return this.admin()
      ?.roleAdministrateur === 'GLOBAL';
  }

  modifierDate(date: string): void {
    this.dateSignal.set(date);
    this.reinitialiserResultat();
  }

  modifierSiteId(
    siteId: number | null
  ): void {
    const admin = this.admin();

    if (
      admin?.roleAdministrateur === 'SITE'
    ) {
      this.siteIdSignal.set(admin.siteId);
      this.reinitialiserResultat();
      return;
    }

    this.siteIdSignal.set(
      siteId === null
        ? null
        : Number(siteId)
    );

    this.reinitialiserResultat();
  }

  chargerEtatOperationnel(): void {
    this.reinitialiserResultat();

    const admin = this.admin();
    const date = this.dateSignal();

    if (!admin) {
      this.messageErreurSignal.set(
        'Tu dois te connecter comme admin avant de consulter cet état.'
      );
      return;
    }

    if (!date) {
      this.messageErreurSignal.set(
        'La date est obligatoire.'
      );
      return;
    }

    const siteId =
      admin.roleAdministrateur === 'SITE'
        ? admin.siteId
        : this.siteIdSignal();

    if (siteId === null) {
      this.messageErreurSignal.set(
        admin.roleAdministrateur === 'SITE'
          ? 'Aucun site n’est associé à cet administrateur.'
          : 'Sélectionne un site.'
      );
      return;
    }

    this.chargementSignal.set(true);

    this.etatOperationnelApiService
      .consulterEtatOperationnel(
        date,
        Number(siteId)
      )
      .pipe(
        tap(etatOperationnel => {
          this.etatOperationnelSignal.set(
            etatOperationnel
          );
        }),
        catchError(error => {
          this.messageErreurSignal.set(
            extraireMessageErreur(error)
          );

          return EMPTY;
        }),
        finalize(() => {
          this.chargementSignal.set(false);
        }),
        takeUntil(this.changementSession$)
      )
      .subscribe();
  }

  private synchroniserAvecAdmin(
    admin: AuthAdminResponse | null
  ): void {
    this.changementSession$.next();
    this.sitesSignal.set([]);
    this.siteIdSignal.set(null);
    this.dateSignal.set(
      dateIsoAujourdhui()
    );
    this.chargementSitesSignal.set(false);
    this.chargementSignal.set(false);
    this.reinitialiserResultat();

    if (!admin) {
      this.messageErreurSignal.set(
        'Tu dois te connecter comme admin avant de consulter cet état.'
      );
      return;
    }

    if (
      admin.roleAdministrateur === 'SITE'
    ) {
      this.siteIdSignal.set(admin.siteId);

      if (admin.siteId === null) {
        this.messageErreurSignal.set(
          'Aucun site n’est associé à cet administrateur.'
        );
      }

      return;
    }

    this.chargerSites();
  }

  private chargerSites(): void {
    this.chargementSitesSignal.set(true);

    this.siteApiService
      .listerSitesActifs()
      .pipe(
        tap(sites => {
          this.sitesSignal.set(sites);

          if (
            this.siteIdSignal() === null
            && sites.length > 0
          ) {
            this.siteIdSignal.set(
              sites[0].siteId
            );
          }
        }),
        catchError(error => {
          this.sitesSignal.set([]);
          this.siteIdSignal.set(null);
          this.messageErreurSignal.set(
            extraireMessageErreur(error)
          );

          return EMPTY;
        }),
        finalize(() => {
          this.chargementSitesSignal.set(
            false
          );
        }),
        takeUntil(this.changementSession$)
      )
      .subscribe();
  }

  private reinitialiserResultat(): void {
    this.messageErreurSignal.set('');
    this.etatOperationnelSignal.set(null);
  }
}
