import {
  HttpErrorResponse
} from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import {
  NEVER,
  of,
  throwError
} from 'rxjs';
import {
  InscriptionMembreRequest,
  MembreResponse
} from '../models/membre.model';
import { SiteResponse } from '../models/site.model';
import { InscriptionJoueurFacadeService } from './inscription-joueur-facade.service';
import { MembreApiService } from './membre-api.service';
import { SiteApiService } from './site-api.service';

describe(
  'InscriptionJoueurFacadeService',
  () => {
    let service:
      InscriptionJoueurFacadeService;

    let membreApiService: {
      inscrireMembre:
        ReturnType<typeof vi.fn>;
    };

    let siteApiService: {
      listerSitesActifs:
        ReturnType<typeof vi.fn>;
    };

    const sites: SiteResponse[] = [
      {
        siteId: 1001,
        code: 'BRU',
        nom: 'Padel Bruxelles',
        adresse: 'Rue de Bruxelles'
      },
      {
        siteId: 1002,
        code: 'NAM',
        nom: 'Padel Namur',
        adresse: 'Rue de Namur'
      }
    ];

    const membreCree: MembreResponse = {
      membreId: 2001,
      matricule: 'S1003',
      nom: 'Dupont',
      prenom: 'Marie',
      categorieMembre: 'SITE',
      siteRattachementId: 1002,
      nomSiteRattachement:
        'Padel Namur',
      actif: true,
      soldeCredit: 100
    };

    beforeEach(() => {
      vi.useFakeTimers();

      siteApiService = {
        listerSitesActifs:
          vi.fn(() => of(sites))
      };

      membreApiService = {
        inscrireMembre:
          vi.fn(() => of(membreCree))
      };

      TestBed.configureTestingModule({
        providers: [
          InscriptionJoueurFacadeService,
          {
            provide: MembreApiService,
            useValue: membreApiService
          },
          {
            provide: SiteApiService,
            useValue: siteApiService
          }
        ]
      });

      service = TestBed.inject(
        InscriptionJoueurFacadeService
      );
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it(
      'doit charger les sites à l initialisation',
      () => {
        service.initialiser();

        expect(
          siteApiService
            .listerSitesActifs
        ).toHaveBeenCalled();

        expect(service.sites())
          .toEqual(sites);

        expect(service.categorieMembre())
          .toBe('GLOBAL');

        expect(
          service.siteRattachementId()
        ).toBeNull();

        expect(service.chargementSites())
          .toBe(false);
      }
    );

    it(
      'doit sélectionner un site uniquement pour la catégorie SITE',
      () => {
        service.initialiser();

        service.modifierCategorieMembre(
          'SITE'
        );

        expect(
          service.siteRattachementId()
        ).toBe(1001);

        expect(service.nomSiteSelectionne())
          .toBe(
            'Padel Bruxelles (1001)'
          );

        service.modifierSiteRattachementId(
          1002
        );

        expect(
          service.siteRattachementId()
        ).toBe(1002);

        service.modifierCategorieMembre(
          'LIBRE'
        );

        expect(
          service.siteRattachementId()
        ).toBeNull();
      }
    );

    it(
      'doit exposer une erreur de chargement des sites',
      () => {
        siteApiService
          .listerSitesActifs
          .mockReturnValue(
            throwError(
              () =>
                new HttpErrorResponse({
                  status: 500,
                  error: {
                    message:
                      'Sites indisponibles.'
                  }
                })
            )
          );

        service.initialiser();

        expect(service.messageErreur())
          .toBe('Sites indisponibles.');

        expect(service.sites())
          .toEqual([]);

        expect(
          service.siteRattachementId()
        ).toBeNull();

        expect(service.chargementSites())
          .toBe(false);
      }
    );

    it(
      'doit refuser un nom ou un prénom vide',
      () => {
        service.initialiser();

        service.modifierNom('  ');
        service.modifierPrenom('Marie');

        service.envoyerDemande();

        expect(service.messageErreur())
          .toBe(
            'Le nom et le prénom sont obligatoires.'
          );

        expect(
          membreApiService.inscrireMembre
        ).not.toHaveBeenCalled();
      }
    );

    it(
      'doit refuser un mot de passe trop court',
      () => {
        service.initialiser();
        service.modifierNom('Dupont');
        service.modifierPrenom('Marie');

        service.modifierMotDePasse(
          'court'
        );

        service
          .modifierConfirmationMotDePasse(
            'court'
          );

        service.envoyerDemande();

        expect(service.messageErreur())
          .toBe(
            'Le mot de passe doit contenir entre 12 et 72 caractères.'
          );

        expect(
          membreApiService.inscrireMembre
        ).not.toHaveBeenCalled();
      }
    );

    it(
      'doit refuser deux mots de passe différents',
      () => {
        service.initialiser();
        service.modifierNom('Dupont');
        service.modifierPrenom('Marie');

        service.modifierMotDePasse(
          'MotDePasse2026!'
        );

        service
          .modifierConfirmationMotDePasse(
            'AutreMotDePasse2026!'
          );

        service.envoyerDemande();

        expect(service.messageErreur())
          .toBe(
            'Les mots de passe ne correspondent pas.'
          );

        expect(
          membreApiService.inscrireMembre
        ).not.toHaveBeenCalled();
      }
    );

    it(
      'doit refuser un site invalide pour une inscription SITE',
      () => {
        service.initialiser();

        service.modifierNom('Dupont');
        service.modifierPrenom('Marie');

        service.modifierMotDePasse(
          'MotDePasse2026!'
        );

        service
          .modifierConfirmationMotDePasse(
            'MotDePasse2026!'
          );

        service.modifierCategorieMembre(
          'SITE'
        );

        service.modifierSiteRattachementId(
          9999
        );

        service.envoyerDemande();

        expect(service.messageErreur())
          .toBe(
            'Sélectionne un site valide pour une inscription SITE.'
          );

        expect(
          membreApiService.inscrireMembre
        ).not.toHaveBeenCalled();
      }
    );

    it(
      'doit nettoyer les données et inscrire le membre',
      () => {
        service.initialiser();

        service.modifierNom(
          '  Dupont  '
        );

        service.modifierPrenom(
          '  Marie  '
        );

        service.modifierMotDePasse(
          'MotDePasse2026!'
        );

        service
          .modifierConfirmationMotDePasse(
            'MotDePasse2026!'
          );

        service.modifierCategorieMembre(
          'SITE'
        );

        service.modifierSiteRattachementId(
          1002
        );

        service.envoyerDemande();

        const requestAttendue:
          InscriptionMembreRequest = {
          nom: 'Dupont',
          prenom: 'Marie',
          categorieMembre: 'SITE',
          siteRattachementId: 1002,
          motDePasse: 'MotDePasse2026!',
          confirmationMotDePasse:
            'MotDePasse2026!'
        };

        expect(
          membreApiService.inscrireMembre
        ).toHaveBeenCalledWith(
          requestAttendue
        );

        expect(service.membreCree())
          .toEqual(membreCree);

        expect(service.messageErreur())
          .toBe('');

        expect(service.chargement())
          .toBe(false);
      }
    );

    it(
      'doit envoyer un site nul pour une catégorie GLOBAL',
      () => {
        service.initialiser();

        service.modifierNom('Dupont');
        service.modifierPrenom('Marie');

        service.modifierMotDePasse(
          'MotDePasse2026!'
        );

        service
          .modifierConfirmationMotDePasse(
            'MotDePasse2026!'
          );

        service.envoyerDemande();

        expect(
          membreApiService.inscrireMembre
        ).toHaveBeenCalledWith({
          nom: 'Dupont',
          prenom: 'Marie',
          categorieMembre: 'GLOBAL',
          siteRattachementId: null,
          motDePasse: 'MotDePasse2026!',
          confirmationMotDePasse:
            'MotDePasse2026!'
        });
      }
    );

    it(
      'doit exposer une erreur d inscription',
      () => {
        membreApiService
          .inscrireMembre
          .mockReturnValue(
            throwError(
              () =>
                new HttpErrorResponse({
                  status: 409,
                  error: {
                    message:
                      'Inscription impossible.'
                  }
                })
            )
          );

        service.initialiser();

        service.modifierNom('Dupont');
        service.modifierPrenom('Marie');

        service.modifierMotDePasse(
          'MotDePasse2026!'
        );

        service
          .modifierConfirmationMotDePasse(
            'MotDePasse2026!'
          );

        service.envoyerDemande();

        expect(service.messageErreur())
          .toBe(
            'Inscription impossible.'
          );

        expect(service.membreCree())
          .toBeNull();

        expect(service.chargement())
          .toBe(false);
      }
    );

    it(
      'doit terminer le chargement après expiration du délai',
      () => {
        membreApiService
          .inscrireMembre
          .mockReturnValue(NEVER);

        service.initialiser();

        service.modifierNom('Dupont');
        service.modifierPrenom('Marie');

        service.modifierMotDePasse(
          'MotDePasse2026!'
        );

        service
          .modifierConfirmationMotDePasse(
            'MotDePasse2026!'
          );

        service.envoyerDemande();

        expect(service.chargement())
          .toBe(true);

        vi.advanceTimersByTime(10001);

        expect(service.messageErreur())
          .toBe(
            'Une erreur est survenue. Vérifie les données saisies puis réessaie.'
          );

        expect(service.chargement())
          .toBe(false);
      }
    );
  }
);
