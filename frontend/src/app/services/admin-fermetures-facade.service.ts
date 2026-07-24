import { Injectable, signal } from '@angular/core';
import { catchError, EMPTY, finalize, tap } from 'rxjs';
import { AuthAdminResponse } from '../models/auth.model';
import {
  CreerFermetureRequest,
  FermetureAdminResponse,
  PorteeFermeture
} from '../models/fermeture.model';
import { SiteResponse } from '../models/site.model';
import { extraireMessageErreur } from '../shared/api-error.util';
import { AdminFermetureApiService } from './admin-fermeture-api.service';
import { AuthContextService } from './auth-context.service';
import { SiteApiService } from './site-api.service';

@Injectable()
export class AdminFermeturesFacadeService {
  private readonly adminSignal = signal<AuthAdminResponse | null>(null);
  private readonly sitesSignal = signal<SiteResponse[]>([]);

  private readonly dateFermetureSignal = signal('');
  private readonly porteeSignal = signal<PorteeFermeture | ''>('');
  private readonly siteIdSignal = signal<number | null>(null);
  private readonly motifSignal = signal('');

  private readonly chargementSitesSignal = signal(false);
  private readonly chargementCreationSignal = signal(false);

  private readonly messageErreurSignal = signal('');
  private readonly fermetureCreeeSignal =
    signal<FermetureAdminResponse | null>(null);

  readonly admin = this.adminSignal.asReadonly();
  readonly sites = this.sitesSignal.asReadonly();

  readonly dateFermeture = this.dateFermetureSignal.asReadonly();
  readonly portee = this.porteeSignal.asReadonly();
  readonly siteId = this.siteIdSignal.asReadonly();
  readonly motif = this.motifSignal.asReadonly();

  readonly chargementSites = this.chargementSitesSignal.asReadonly();
  readonly chargementCreation =
    this.chargementCreationSignal.asReadonly();

  readonly messageErreur = this.messageErreurSignal.asReadonly();
  readonly fermetureCreee = this.fermetureCreeeSignal.asReadonly();

  constructor(
    private readonly adminFermetureApiService:
    AdminFermetureApiService,
    private readonly siteApiService: SiteApiService,
    private readonly authContextService: AuthContextService
  ) {
  }

  initialiser(): void {
    this.reinitialiserParcours();

    const admin = this.authContextService.admin();
    this.adminSignal.set(admin);

    if (!admin) {
      this.messageErreurSignal.set(
        'Connecte-toi comme administrateur pour créer une fermeture.'
      );
      return;
    }

    if (admin.roleAdministrateur === 'SITE') {
      this.porteeSignal.set('LOCALE');
      this.siteIdSignal.set(admin.siteId);

      if (admin.siteId === null) {
        this.messageErreurSignal.set(
          'Le compte administrateur SITE n’est rattaché à aucun site.'
        );
      }

      return;
    }

    this.chargerSites();
  }

  estAdminGlobal(): boolean {
    return this.adminSignal()?.roleAdministrateur === 'GLOBAL';
  }

  modifierDateFermeture(dateFermeture: string): void {
    this.dateFermetureSignal.set(dateFermeture);
    this.reinitialiserResultat();
  }

  modifierPortee(portee: PorteeFermeture | ''): void {
    this.reinitialiserResultat();

    const admin = this.adminSignal();

    if (admin?.roleAdministrateur === 'SITE') {
      this.porteeSignal.set('LOCALE');
      this.siteIdSignal.set(admin.siteId);
      return;
    }

    this.porteeSignal.set(portee);
  }

  modifierSiteId(siteId: number | null): void {
    this.reinitialiserResultat();

    const admin = this.adminSignal();

    if (admin?.roleAdministrateur === 'SITE') {
      this.siteIdSignal.set(admin.siteId);
      return;
    }

    this.siteIdSignal.set(siteId);
  }

  modifierMotif(motif: string): void {
    this.motifSignal.set(motif);
    this.reinitialiserResultat();
  }

  nomSiteSelectionne(): string {
    const siteId = this.siteIdSignal();
    const admin = this.adminSignal();

    if (siteId === null) {
      return 'Site inconnu';
    }

    if (
      admin?.roleAdministrateur === 'SITE'
      && admin.siteId === siteId
    ) {
      return admin.nomSite || 'Site administré';
    }

    const site = this.sitesSignal().find(
      siteActif =>
        siteActif.siteId === Number(siteId)
    );

    return site?.nom || 'Site inconnu';
  }

  creerFermeture(): void {
    this.reinitialiserResultat();

    const admin = this.adminSignal();
    const dateFermeture = this.dateFermetureSignal();
    const portee = this.porteeSignal();

    if (!admin) {
      this.messageErreurSignal.set(
        'Connecte-toi comme administrateur pour créer une fermeture.'
      );
      return;
    }

    if (!dateFermeture) {
      this.messageErreurSignal.set(
        'La date de fermeture est obligatoire.'
      );
      return;
    }

    if (!portee) {
      this.messageErreurSignal.set(
        'La portée de fermeture est obligatoire.'
      );
      return;
    }

    if (
      admin.roleAdministrateur === 'SITE'
      && portee !== 'LOCALE'
    ) {
      this.messageErreurSignal.set(
        'Un administrateur SITE ne peut créer qu’une fermeture locale.'
      );
      return;
    }

    const siteId = admin.roleAdministrateur === 'SITE'
      ? admin.siteId
      : this.siteIdSignal();

    if (portee === 'LOCALE' && siteId === null) {
      this.messageErreurSignal.set(
        'Le site est obligatoire pour une fermeture locale.'
      );
      return;
    }

    const request: CreerFermetureRequest = {
      dateFermeture,
      portee,
      siteId: portee === 'LOCALE' ? Number(siteId) : null,
      motif: this.motifSignal().trim()
    };

    this.chargementCreationSignal.set(true);

    this.adminFermetureApiService.creerFermeture(request).pipe(
      tap(response => {
        this.fermetureCreeeSignal.set(response);
      }),
      catchError(error => {
        this.messageErreurSignal.set(
          extraireMessageErreur(error)
        );
        return EMPTY;
      }),
      finalize(() => {
        this.chargementCreationSignal.set(false);
      })
    ).subscribe();
  }

  private chargerSites(): void {
    this.chargementSitesSignal.set(true);

    this.siteApiService.listerSitesActifs().pipe(
      tap(sites => {
        this.sitesSignal.set(sites);

        const siteIdActuel = this.siteIdSignal();
        const siteSelectionExiste = siteIdActuel !== null
          && sites.some(site => site.siteId === siteIdActuel);

        if (!siteSelectionExiste) {
          this.siteIdSignal.set(
            sites.length > 0 ? sites[0].siteId : null
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
    this.adminSignal.set(null);
    this.sitesSignal.set([]);

    this.dateFermetureSignal.set('');
    this.porteeSignal.set('');
    this.siteIdSignal.set(null);
    this.motifSignal.set('');

    this.chargementSitesSignal.set(false);
    this.chargementCreationSignal.set(false);

    this.messageErreurSignal.set('');
    this.fermetureCreeeSignal.set(null);
  }

  private reinitialiserResultat(): void {
    this.messageErreurSignal.set('');
    this.fermetureCreeeSignal.set(null);
  }
}
