import {
  signal,
  type WritableSignal
} from '@angular/core';
import {
  ComponentFixture,
  TestBed
} from '@angular/core/testing';
import { AuthJoueurResponse } from '../../models/auth.model';
import { DetteResponse } from '../../models/dette.model';
import { MesDettesFacadeService } from '../../services/mes-dettes-facade.service';
import { MesDettesComponent } from './mes-dettes.component';

describe('MesDettesComponent', () => {
  let fixture:
    ComponentFixture<MesDettesComponent>;

  let component: MesDettesComponent;

  let facade: {
    initialiser:
      ReturnType<typeof vi.fn>;
    chargerDettes:
      ReturnType<typeof vi.fn>;
    payerDette:
      ReturnType<typeof vi.fn>;
    modifierMontantPaiement:
      ReturnType<typeof vi.fn>;
    montantPaiement:
      ReturnType<typeof vi.fn>;
    joueur:
      WritableSignal<AuthJoueurResponse | null>;
    dettes:
      WritableSignal<DetteResponse[]>;
    chargement:
      WritableSignal<boolean>;
    rechercheEffectuee:
      WritableSignal<boolean>;
    paiementEnCoursDetteId:
      WritableSignal<number | null>;
    messageErreur:
      WritableSignal<string>;
    messageSucces:
      WritableSignal<string>;
    totalMontantRestant:
      WritableSignal<number>;
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

  const dette: DetteResponse = {
    detteId: 4001,
    matchId: 3001,
    membreResponsableId: 2001,
    matriculeResponsable: 'G1001',
    montantInitial: 45,
    montantRestant: 45,
    statutDette: 'OUVERTE',
    dateCreation:
      '2026-06-01T10:00:00',
    dateReglement: null
  };

  beforeEach(async () => {
    facade = {
      initialiser: vi.fn(),
      chargerDettes: vi.fn(),
      payerDette: vi.fn(),
      modifierMontantPaiement: vi.fn(),
      montantPaiement: vi.fn(() => 45),
      joueur:
        signal<AuthJoueurResponse | null>(
          null
        ),
      dettes:
        signal<DetteResponse[]>([]),
      chargement: signal(false),
      rechercheEffectuee: signal(false),
      paiementEnCoursDetteId:
        signal<number | null>(null),
      messageErreur: signal(''),
      messageSucces: signal(''),
      totalMontantRestant: signal(0)
    };

    await TestBed
      .configureTestingModule({
        imports: [
          MesDettesComponent
        ]
      })
      .overrideComponent(
        MesDettesComponent,
        {
          set: {
            providers: [
              {
                provide:
                MesDettesFacadeService,
                useValue: facade
              }
            ]
          }
        }
      )
      .compileComponents();

    fixture = TestBed.createComponent(
      MesDettesComponent
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
    'doit afficher l absence de joueur connecté',
    () => {
      const contenu =
        fixture.nativeElement
          .textContent as string;

      expect(contenu)
        .toContain(
          'Aucun joueur connecté'
        );
    }
  );

  it(
    'doit afficher le joueur, la dette et le total',
    () => {
      facade.joueur.set(joueur);
      facade.dettes.set([dette]);
      facade.rechercheEffectuee.set(true);
      facade.totalMontantRestant.set(45);

      fixture.detectChanges();

      const contenu =
        fixture.nativeElement
          .textContent as string;

      expect(contenu).toContain('G1001');
      expect(contenu).toContain('Marie');
      expect(contenu).toContain('Dupont');
      expect(contenu).toContain('Dette 4001');
      expect(contenu).toContain('45.00');
    }
  );

  it(
    'doit déléguer l actualisation à la façade',
    () => {
      facade.joueur.set(joueur);
      fixture.detectChanges();

      const boutons = Array.from(
        fixture.nativeElement
          .querySelectorAll('button')
      ) as HTMLButtonElement[];

      const boutonActualiser =
        boutons.find(
          bouton =>
            bouton.textContent
              ?.includes(
                'Actualiser mes dettes'
              )
        );

      boutonActualiser?.click();

      expect(facade.chargerDettes)
        .toHaveBeenCalled();
    }
  );

  it(
    'doit déléguer la saisie du montant à la façade',
    () => {
      facade.joueur.set(joueur);
      facade.dettes.set([dette]);

      fixture.detectChanges();

      const champMontant =
        fixture.nativeElement
          .querySelector(
            '#montantDette4001'
          ) as HTMLInputElement;

      champMontant.value = '40';
      champMontant.dispatchEvent(
        new Event('input')
      );

      expect(
        facade.modifierMontantPaiement
      ).toHaveBeenCalledWith(
        4001,
        40
      );
    }
  );

  it(
    'doit déléguer le paiement à la façade',
    () => {
      facade.joueur.set(joueur);
      facade.dettes.set([dette]);

      fixture.detectChanges();

      const boutons = Array.from(
        fixture.nativeElement
          .querySelectorAll('button')
      ) as HTMLButtonElement[];

      const boutonPaiement =
        boutons.find(
          bouton =>
            bouton.textContent
              ?.includes(
                'Payer cette dette'
              )
        );

      boutonPaiement?.click();

      expect(facade.payerDette)
        .toHaveBeenCalledWith(dette);
    }
  );

  it(
    'doit afficher les messages de la façade',
    () => {
      facade.joueur.set(joueur);

      facade.messageErreur.set(
        'Erreur backend dettes.'
      );

      facade.messageSucces.set(
        'Paiement réussi.'
      );

      fixture.detectChanges();

      const contenu =
        fixture.nativeElement
          .textContent as string;

      expect(contenu)
        .toContain(
          'Erreur backend dettes.'
        );

      expect(contenu)
        .toContain(
          'Paiement réussi.'
        );
    }
  );
});
