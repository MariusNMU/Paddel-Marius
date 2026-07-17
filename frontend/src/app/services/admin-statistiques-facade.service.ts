import { Injectable, signal } from '@angular/core';
import {
  catchError,
  EMPTY,
  finalize,
  tap
} from 'rxjs';
import { AuthAdminResponse } from '../models/auth.model';
import { SiteResponse } from '../models/site.model';
import { StatistiquesAdminResponse } from '../models/statistique.model';
import { extraireMessageErreur } from '../shared/api-error.util';
import { AdminStatsApiService } from './admin-stats-api.service';
import { AuthContextService } from './auth-context.service';
import { SiteApiService } from './site-api.service';

export type PeriodeStatistiques =
  'moisCourant' | 'prochainsJours' | 'demo';

function dateIso(date: Date): string {
  const annee = date.getFullYear();
  const mois = String(
    date.getMonth() + 1
  ).padStart(2, '0');
  const jour = String(
    date.getDate()
  ).padStart(2, '0');

  return `${annee}-${mois}-${jour}`;
}

function dateIsoDansJours(
  decalageJours: number
): string {
  const date = new Date();
  date.setDate(
    date.getDate() + decalageJours
  );

  return dateIso(date);
}

function premierJourMoisCourant(): string {
  const date = new Date();
  date.setDate(1);

  return dateIso(date);
}

function dernierJourMoisCourant(): string {
  const date = new Date();
  date.setMonth(
    date.getMonth() + 1,
    0
  );

  return dateIso(date);
}

@Injectable()
export class AdminStatistiquesFacadeService {
  private readonly adminSignal =
    signal<AuthAdminResponse | null>(null);

  private readonly sitesSignal =
    signal<SiteResponse[]>([]);

  private readonly dateDebutSignal = signal('');
  private readonly dateFinSignal = signal('');
  private readonly siteIdSignal =
    signal<number | null>(null);

  private readonly chargementSitesSignal =
    signal(false);

  private readonly chargementSignal =
    signal(false);

  private readonly messageErreurSignal =
    signal('');

  private readonly statistiquesSignal =
    signal<StatistiquesAdminResponse | null>(null);

  readonly admin =
    this.adminSignal.asReadonly();

  readonly sites =
    this.sitesSignal.asReadonly();

  readonly dateDebut =
    this.dateDebutSignal.asReadonly();

  readonly dateFin =
    this.dateFinSignal.asReadonly();

  readonly siteId =
    this.siteIdSignal.asReadonly();

  readonly chargementSites =
    this.chargementSitesSignal.asReadonly();

  readonly chargement =
    this.chargementSignal.asReadonly();

  readonly messageErreur =
    this.messageErreurSignal.asReadonly();

  readonly statistiques =
    this.statistiquesSignal.asReadonly();

  constructor(
    private readonly adminStatsApiService:
    AdminStatsApiService,
    private readonly siteApiService:
    SiteApiService,
    private readonly authContextService:
    AuthContextService
  ) {
  }

  initialiser(): void {
    this.reinitialiserParcours();
    this.appliquerPeriodeDemo();

    const admin =
      this.authContextService.admin();

    this.adminSignal.set(admin);

    if (!admin) {
      this.messageErreurSignal.set(
        'Tu dois te connecter comme admin avant de consulter les statistiques.'
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

  estAdminGlobal(): boolean {
    return this.adminSignal()
      ?.roleAdministrateur === 'GLOBAL';
  }

  modifierDateDebut(
    dateDebut: string
  ): void {
    this.dateDebutSignal.set(dateDebut);
    this.reinitialiserResultat();
  }

  modifierDateFin(
    dateFin: string
  ): void {
    this.dateFinSignal.set(dateFin);
    this.reinitialiserResultat();
  }

  modifierSiteId(
    siteId: number | null
  ): void {
    const admin = this.adminSignal();

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

  selectionnerPeriode(
    periode: PeriodeStatistiques
  ): void {
    if (periode === 'moisCourant') {
      this.dateDebutSignal.set(
        premierJourMoisCourant()
      );

      this.dateFinSignal.set(
        dernierJourMoisCourant()
      );
    }

    if (periode === 'prochainsJours') {
      this.dateDebutSignal.set(
        dateIsoDansJours(0)
      );

      this.dateFinSignal.set(
        dateIsoDansJours(7)
      );
    }

    if (periode === 'demo') {
      this.appliquerPeriodeDemo();
    }

    this.reinitialiserResultat();
  }

  chargerStatistiques(): void {
    this.reinitialiserResultat();

    const admin = this.adminSignal();
    const dateDebut =
      this.dateDebutSignal();
    const dateFin =
      this.dateFinSignal();

    if (!admin) {
      this.messageErreurSignal.set(
        'Tu dois te connecter comme admin avant de consulter les statistiques.'
      );
      return;
    }

    if (!dateDebut || !dateFin) {
      this.messageErreurSignal.set(
        'Les dates de début et de fin sont obligatoires.'
      );
      return;
    }

    if (dateFin < dateDebut) {
      this.messageErreurSignal.set(
        'La date de fin doit être supérieure ou égale à la date de début.'
      );
      return;
    }

    const siteId =
      admin.roleAdministrateur === 'SITE'
        ? admin.siteId
        : this.siteIdSignal();

    if (
      admin.roleAdministrateur === 'SITE'
      && siteId === null
    ) {
      this.messageErreurSignal.set(
        'Aucun site n’est associé à cet administrateur.'
      );
      return;
    }

    const siteIdParam =
      siteId === null
        ? undefined
        : Number(siteId);

    this.chargementSignal.set(true);

    this.adminStatsApiService
      .consulterStatistiques(
        dateDebut,
        dateFin,
        siteIdParam
      )
      .pipe(
        tap(statistiques => {
          this.statistiquesSignal.set(
            statistiques
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
        })
      )
      .subscribe();
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
            siteIdActuel === null
            || sites.some(
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
        })
      )
      .subscribe();
  }

  private appliquerPeriodeDemo(): void {
    this.dateDebutSignal.set(
      dateIsoDansJours(-14)
    );

    this.dateFinSignal.set(
      dateIsoDansJours(14)
    );
  }

  private reinitialiserResultat(): void {
    this.messageErreurSignal.set('');
    this.statistiquesSignal.set(null);
  }

  private reinitialiserParcours(): void {
    this.adminSignal.set(null);
    this.sitesSignal.set([]);
    this.dateDebutSignal.set('');
    this.dateFinSignal.set('');
    this.siteIdSignal.set(null);

    this.chargementSitesSignal.set(false);
    this.chargementSignal.set(false);
    this.messageErreurSignal.set('');
    this.statistiquesSignal.set(null);
  }
}
