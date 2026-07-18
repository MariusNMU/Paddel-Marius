import {
  signal,
  type WritableSignal
} from '@angular/core';
import {
  ComponentFixture,
  TestBed
} from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthJoueurResponse } from '../../models/auth.model';
import { ParametresMetierResponse } from '../../models/parametres-metier.model';
import { SoldeJoueurResponse } from '../../models/solde-joueur.model';
import { MonSoldeFacadeService } from '../../services/mon-solde-facade.service';
import { MonSoldeComponent } from './mon-solde.component';

describe('MonSoldeComponent', () => {
  let fixture:
    ComponentFixture<MonSoldeComponent>;

  let component: MonSoldeComponent;

  let facade: {
    initialiser:
      ReturnType<typeof vi.fn>;
    chargerSolde:
      ReturnType<typeof vi.fn>;
    joueur:
      WritableSignal<AuthJoueurResponse | null>;
    parametresMetier:
      WritableSignal<
        ParametresMetierResponse | null
      >;
    solde:
      WritableSignal<
        SoldeJoueurResponse | null
      >;
    chargement:
      WritableSignal<boolean>;
    chargementParametres:
      WritableSignal<boolean>;
    messageErreur:
      WritableSignal<string>;
  };

  const joueur: AuthJoueurResponse = {
    membreId: 2001,
    matricule: 'G1001',
    nom: 'Dupont',
    prenom: 'Marie',
    categorieMembre: 'GLOBAL',
    siteRattachementId: null,
    nomSiteRattachement: null,
    actif: true
  };

  const parametres:
    ParametresMetierResponse = {
    dureeMatchMinutes: 90,
    pauseEntreMatchesMinutes: 15,
    nombreJoueursMaximum: 4,
    prixTotalMatch: 60,
    montantParticipationStandard: 15,
    soldeInitialJoueur: 100
  };

  const solde: SoldeJoueurResponse = {
    membreId: 2001,
    matricule: 'G1001',
    soldeCredit: 70
  };

  beforeEach(async () => {
    facade = {
      initialiser: vi.fn(),
      chargerSolde: vi.fn(),
      joueur:
        signal<AuthJoueurResponse | null>(
          null
        ),
      parametresMetier:
        signal<
          ParametresMetierResponse | null
        >(parametres),
      solde:
        signal<SoldeJoueurResponse | null>(
          null
        ),
      chargement: signal(false),
      chargementParametres: signal(false),
      messageErreur: signal('')
    };

    await TestBed
      .configureTestingModule({
        imports: [
          MonSoldeComponent
        ],
        providers: [
          provideRouter([])
        ]
      })
      .overrideComponent(
        MonSoldeComponent,
        {
          set: {
            providers: [
              {
                provide:
                MonSoldeFacadeService,
                useValue: facade
              }
            ]
          }
        }
      )
      .compileComponents();

    fixture = TestBed.createComponent(
      MonSoldeComponent
    );

    component =
      fixture.componentInstance;

    fixture.detectChanges();
  });

  it(
    'doit créer le composant et initialiser la façade',
    () => {
      expect(component).toBeTruthy();

      expect(facade.initialiser)
        .toHaveBeenCalled();
    }
  );

  it(
    'doit afficher les règles et l absence de joueur',
    () => {
      const contenu =
        fixture.nativeElement
          .textContent as string;

      expect(contenu)
        .toContain(
          'Aucun joueur connecté'
        );

      expect(contenu).toContain('100.00');
      expect(contenu).toContain('15.00');
    }
  );

  it(
    'doit afficher le joueur et son solde',
    () => {
      facade.joueur.set(joueur);
      facade.solde.set(solde);

      fixture.detectChanges();

      const contenu =
        fixture.nativeElement
          .textContent as string;

      expect(contenu).toContain('G1001');
      expect(contenu).toContain('Dupont');
      expect(contenu).toContain('Marie');
      expect(contenu).toContain('70.00');
      expect(contenu)
        .toContain('Solde disponible');
    }
  );

  it(
    'doit déléguer l actualisation à la façade',
    () => {
      facade.joueur.set(joueur);
      fixture.detectChanges();

      const bouton =
        fixture.nativeElement
          .querySelector(
            'button'
          ) as HTMLButtonElement;

      bouton.click();

      expect(facade.chargerSolde)
        .toHaveBeenCalled();
    }
  );

  it(
    'doit afficher l erreur de la façade',
    () => {
      facade.messageErreur.set(
        'Solde indisponible.'
      );

      fixture.detectChanges();

      const contenu =
        fixture.nativeElement
          .textContent as string;

      expect(contenu)
        .toContain('Solde indisponible.');
    }
  );

  it(
    'doit désactiver le bouton pendant le chargement',
    () => {
      facade.joueur.set(joueur);
      facade.chargement.set(true);

      fixture.detectChanges();

      const bouton =
        fixture.nativeElement
          .querySelector(
            'button'
          ) as HTMLButtonElement;

      expect(bouton.disabled).toBe(true);

      expect(bouton.textContent)
        .toContain('Chargement...');
    }
  );
});
