import {
  Injectable,
  signal
} from '@angular/core';
import {
  catchError,
  EMPTY,
  finalize,
  tap,
  timeout
} from 'rxjs';
import {
  CategorieMembre,
  InscriptionMembreRequest,
  MembreResponse
} from '../models/membre.model';
import { SiteResponse } from '../models/site.model';
import { extraireMessageErreur } from '../shared/api-error.util';
import { MembreApiService } from './membre-api.service';
import { SiteApiService } from './site-api.service';

@Injectable()
export class InscriptionJoueurFacadeService {
  private readonly sitesSignal =
    signal<SiteResponse[]>([]);

  private readonly nomSignal =
    signal('');

  private readonly prenomSignal =
    signal('');

  private readonly motDePasseSignal =
    signal('');

  private readonly confirmationMotDePasseSignal =
    signal('');

  private readonly categorieMembreSignal =
    signal<CategorieMembre>('GLOBAL');

  private readonly siteRattachementIdSignal =
    signal<number | null>(null);

  private readonly chargementSitesSignal =
    signal(false);

  private readonly chargementSignal =
    signal(false);

  private readonly messageErreurSignal =
    signal('');

  private readonly membreCreeSignal =
    signal<MembreResponse | null>(null);

  readonly sites =
    this.sitesSignal.asReadonly();

  readonly nom =
    this.nomSignal.asReadonly();

  readonly prenom =
    this.prenomSignal.asReadonly();

  readonly motDePasse =
    this.motDePasseSignal.asReadonly();

  readonly confirmationMotDePasse =
    this.confirmationMotDePasseSignal
      .asReadonly();

  readonly categorieMembre =
    this.categorieMembreSignal.asReadonly();

  readonly siteRattachementId =
    this.siteRattachementIdSignal.asReadonly();

  readonly chargementSites =
    this.chargementSitesSignal.asReadonly();

  readonly chargement =
    this.chargementSignal.asReadonly();

  readonly messageErreur =
    this.messageErreurSignal.asReadonly();

  readonly membreCree =
    this.membreCreeSignal.asReadonly();

  constructor(
    private readonly membreApiService:
    MembreApiService,
    private readonly siteApiService:
    SiteApiService
  ) {
  }

  initialiser(): void {
    this.reinitialiserParcours();
    this.chargerSites();
  }

  modifierNom(nom: string): void {
    this.nomSignal.set(nom);
    this.reinitialiserResultat();
  }

  modifierPrenom(prenom: string): void {
    this.prenomSignal.set(prenom);
    this.reinitialiserResultat();
  }

  modifierMotDePasse(
    motDePasse: string
  ): void {
    this.motDePasseSignal.set(
      motDePasse
    );

    this.reinitialiserResultat();
  }

  modifierConfirmationMotDePasse(
    confirmationMotDePasse: string
  ): void {
    this.confirmationMotDePasseSignal.set(
      confirmationMotDePasse
    );

    this.reinitialiserResultat();
  }

  modifierCategorieMembre(
    categorieMembre: CategorieMembre
  ): void {
    this.categorieMembreSignal.set(
      categorieMembre
    );

    this.reinitialiserResultat();
    this.mettreAJourSiteRattachement();
  }

  modifierSiteRattachementId(
    siteRattachementId: number | null
  ): void {
    if (siteRattachementId === null) {
      this.siteRattachementIdSignal.set(null);
      this.reinitialiserResultat();
      return;
    }

    const valeurNumerique =
      Number(siteRattachementId);

    this.siteRattachementIdSignal.set(
      Number.isFinite(valeurNumerique)
        ? valeurNumerique
        : null
    );

    this.reinitialiserResultat();
  }

  nomSiteSelectionne(): string {
    const site = this.sitesSignal().find(
      element =>
        element.siteId
        === Number(
          this.siteRattachementIdSignal()
        )
    );

    if (!site) {
      return 'Site inconnu';
    }

    return `${site.nom} (${site.siteId})`;
  }

  envoyerDemande(): void {
    if (this.chargementSignal()) {
      return;
    }

    this.messageErreurSignal.set('');
    this.membreCreeSignal.set(null);

    const nom = this.nomSignal().trim();
    const prenom = this.prenomSignal().trim();

    const motDePasse =
      this.motDePasseSignal();

    const confirmationMotDePasse =
      this.confirmationMotDePasseSignal();

    if (!nom || !prenom) {
      this.messageErreurSignal.set(
        'Le nom et le prénom sont obligatoires.'
      );
      return;
    }

    if (
      !motDePasse.trim()
      || !confirmationMotDePasse.trim()
    ) {
      this.messageErreurSignal.set(
        'Le mot de passe et sa confirmation sont obligatoires.'
      );
      return;
    }

    if (
      motDePasse.length < 12
      || motDePasse.length > 72
    ) {
      this.messageErreurSignal.set(
        'Le mot de passe doit contenir entre 12 et 72 caractères.'
      );
      return;
    }

    if (
      motDePasse
      !== confirmationMotDePasse
    ) {
      this.messageErreurSignal.set(
        'Les mots de passe ne correspondent pas.'
      );
      return;
    }

    const categorieMembre =
      this.categorieMembreSignal();

    const siteRattachement =
      categorieMembre === 'SITE'
        ? this.sitesSignal().find(
          site =>
            site.siteId
            === Number(
              this.siteRattachementIdSignal()
            )
        )
        : undefined;

    if (
      categorieMembre === 'SITE'
      && !siteRattachement
    ) {
      this.messageErreurSignal.set(
        'Sélectionne un site valide pour une inscription SITE.'
      );
      return;
    }

    const request: InscriptionMembreRequest = {
      nom,
      prenom,
      categorieMembre,
      siteRattachementId:
        categorieMembre === 'SITE'
          ? siteRattachement!.siteId
          : null,
      motDePasse,
      confirmationMotDePasse
    };

    this.chargementSignal.set(true);

    this.membreApiService
      .inscrireMembre(request)
      .pipe(
        timeout(10000),
        tap(membreCree => {
          this.membreCreeSignal.set(
            membreCree
          );

          this.motDePasseSignal.set('');
          this.confirmationMotDePasseSignal
            .set('');
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
        timeout(10000),
        tap(sites => {
          this.sitesSignal.set(sites);
          this.mettreAJourSiteRattachement();
        }),
        catchError(error => {
          this.messageErreurSignal.set(
            extraireMessageErreur(error)
          );

          this.sitesSignal.set([]);
          this.siteRattachementIdSignal.set(
            null
          );

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

  private mettreAJourSiteRattachement(): void {
    if (
      this.categorieMembreSignal()
      !== 'SITE'
    ) {
      this.siteRattachementIdSignal.set(
        null
      );
      return;
    }

    const siteRattachementId =
      this.siteRattachementIdSignal();

    const siteSelectionExiste =
      siteRattachementId !== null
      && this.sitesSignal().some(
        site =>
          site.siteId
          === siteRattachementId
      );

    if (!siteSelectionExiste) {
      this.siteRattachementIdSignal.set(
        this.sitesSignal().length > 0
          ? this.sitesSignal()[0].siteId
          : null
      );
    }
  }

  private reinitialiserParcours(): void {
    this.sitesSignal.set([]);
    this.nomSignal.set('');
    this.prenomSignal.set('');
    this.motDePasseSignal.set('');
    this.confirmationMotDePasseSignal
      .set('');
    this.categorieMembreSignal.set(
      'GLOBAL'
    );

    this.siteRattachementIdSignal.set(
      null
    );

    this.chargementSitesSignal.set(false);
    this.chargementSignal.set(false);
    this.messageErreurSignal.set('');
    this.membreCreeSignal.set(null);
  }

  private reinitialiserResultat(): void {
    this.messageErreurSignal.set('');
    this.membreCreeSignal.set(null);
  }
}
