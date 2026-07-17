import { effect, Injectable, signal } from '@angular/core';
import {
  catchError,
  EMPTY,
  finalize,
  Observable,
  Subject,
  takeUntil,
  tap
} from 'rxjs';
import { AuthAdminResponse } from '../models/auth.model';
import { MembreResponse } from '../models/membre.model';
import { SiteResponse } from '../models/site.model';
import { extraireMessageErreur } from '../shared/api-error.util';
import { AdminMembreApiService } from './admin-membre-api.service';
import { AuthContextService } from './auth-context.service';
import { SiteApiService } from './site-api.service';

@Injectable()
export class AdminMembresFacadeService {
  private readonly sitesSignal =
    signal<SiteResponse[]>([]);
  private readonly siteIdSignal =
    signal<number | null>(null);
  private readonly membresSignal =
    signal<MembreResponse[]>([]);

  private readonly chargementSitesSignal =
    signal(false);
  private readonly chargementMembresSignal =
    signal(false);
  private readonly messageErreurSignal =
    signal('');
  private readonly titreResultatSignal =
    signal('Membres');

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
  readonly siteId =
    this.siteIdSignal.asReadonly();
  readonly membres =
    this.membresSignal.asReadonly();

  readonly chargementSites =
    this.chargementSitesSignal.asReadonly();
  readonly chargementMembres =
    this.chargementMembresSignal.asReadonly();
  readonly messageErreur =
    this.messageErreurSignal.asReadonly();
  readonly titreResultat =
    this.titreResultatSignal.asReadonly();

  constructor(
    private readonly adminMembreApiService:
    AdminMembreApiService,
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

  modifierSiteId(
    siteId: number | null
  ): void {
    const admin = this.admin();

    if (
      admin?.roleAdministrateur === 'SITE'
    ) {
      this.siteIdSignal.set(admin.siteId);
      return;
    }

    this.siteIdSignal.set(
      siteId === null
        ? null
        : Number(siteId)
    );

    this.messageErreurSignal.set('');
  }

  afficherTousLesMembres(): void {
    if (!this.estAdminGlobal()) {
      this.messageErreurSignal.set(
        'Cette action est réservée aux administrateurs globaux.'
      );
      return;
    }

    this.chargerMembres(
      this.adminMembreApiService
        .listerTousLesMembres(),
      'Tous les membres'
    );
  }

  afficherMembresDuSiteSelectionne(): void {
    this.messageErreurSignal.set('');
    this.membresSignal.set([]);

    const admin = this.admin();

    if (!admin) {
      this.messageErreurSignal.set(
        'Connecte-toi comme administrateur pour consulter les membres.'
      );
      return;
    }

    if (
      admin.roleAdministrateur === 'SITE'
    ) {
      if (admin.siteId === null) {
        this.messageErreurSignal.set(
          'Aucun site n’est associé à cet administrateur.'
        );
        return;
      }

      this.siteIdSignal.set(admin.siteId);

      this.chargerMembresParSite(
        admin.siteId,
        admin.nomSite
        ?? `site ${admin.siteId}`
      );
      return;
    }

    const siteId = this.siteIdSignal();

    if (siteId === null) {
      this.messageErreurSignal.set(
        'Sélectionne un site avant de filtrer les membres.'
      );
      return;
    }

    const siteSelectionne =
      this.sitesSignal().find(
        site => site.siteId === siteId
      );

    if (!siteSelectionne) {
      this.messageErreurSignal.set(
        'Sélectionne un site valide avant de filtrer les membres.'
      );
      return;
    }

    this.chargerMembresParSite(
      siteSelectionne.siteId,
      siteSelectionne.nom
    );
  }

  private synchroniserAvecAdmin(
    admin: AuthAdminResponse | null
  ): void {
    this.changementSession$.next();
    this.reinitialiserParcours();

    if (!admin) {
      this.messageErreurSignal.set(
        'Connecte-toi comme administrateur pour consulter les membres.'
      );
      return;
    }

    if (
      admin.roleAdministrateur === 'GLOBAL'
    ) {
      this.afficherTousLesMembres();
      this.chargerSites();
      return;
    }

    if (admin.siteId === null) {
      this.messageErreurSignal.set(
        'Aucun site n’est associé à cet administrateur.'
      );
      return;
    }

    this.siteIdSignal.set(admin.siteId);

    this.chargerMembresParSite(
      admin.siteId,
      admin.nomSite
      ?? `site ${admin.siteId}`
    );
  }

  private chargerSites(): void {
    this.chargementSitesSignal.set(true);

    this.siteApiService
      .listerSitesActifs()
      .pipe(
        tap(sites => {
          this.sitesSignal.set(sites);

          const siteIdActuel =
            this.siteIdSignal();

          const siteSelectionExiste =
            siteIdActuel !== null
            && sites.some(
              site =>
                site.siteId === siteIdActuel
            );

          if (!siteSelectionExiste) {
            this.siteIdSignal.set(null);
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
          this.chargementSitesSignal.set(
            false
          );
        }),
        takeUntil(this.changementSession$)
      )
      .subscribe();
  }

  private chargerMembresParSite(
    siteId: number,
    nomSite: string
  ): void {
    this.chargerMembres(
      this.adminMembreApiService
        .listerMembresParSite(siteId),
      `Membres rattachés au site ${nomSite}`
    );
  }

  private chargerMembres(
    membres$: Observable<MembreResponse[]>,
    titreResultat: string
  ): void {
    this.messageErreurSignal.set('');
    this.membresSignal.set([]);
    this.titreResultatSignal.set(
      titreResultat
    );
    this.chargementMembresSignal.set(
      true
    );

    membres$
      .pipe(
        tap(membres => {
          this.membresSignal.set(membres);
        }),
        catchError(error => {
          this.messageErreurSignal.set(
            extraireMessageErreur(error)
          );

          this.membresSignal.set([]);

          return EMPTY;
        }),
        finalize(() => {
          this.chargementMembresSignal.set(
            false
          );
        }),
        takeUntil(this.changementSession$)
      )
      .subscribe();
  }

  private reinitialiserParcours(): void {
    this.sitesSignal.set([]);
    this.siteIdSignal.set(null);
    this.membresSignal.set([]);

    this.chargementSitesSignal.set(false);
    this.chargementMembresSignal.set(false);
    this.messageErreurSignal.set('');
    this.titreResultatSignal.set(
      'Membres'
    );
  }
}
