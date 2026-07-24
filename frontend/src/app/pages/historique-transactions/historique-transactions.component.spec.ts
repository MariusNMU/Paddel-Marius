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
import { HistoriquePaiementResponse } from '../../models/paiement.model';
import { HistoriqueTransactionsFacadeService } from '../../services/historique-transactions-facade.service';
import { HistoriqueTransactionsComponent } from './historique-transactions.component';

describe(
  'HistoriqueTransactionsComponent',
  () => {
    let fixture:
      ComponentFixture<
        HistoriqueTransactionsComponent
      >;

    let component:
      HistoriqueTransactionsComponent;

    let facade: {
      initialiser:
        ReturnType<typeof vi.fn>;
      chargerHistorique:
        ReturnType<typeof vi.fn>;
      joueur:
        WritableSignal<
          AuthJoueurResponse | null
        >;
      transactions:
        WritableSignal<
          HistoriquePaiementResponse[]
        >;
      chargement:
        WritableSignal<boolean>;
      rechercheEffectuee:
        WritableSignal<boolean>;
      messageErreur:
        WritableSignal<string>;
      totalPaye:
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

    const transaction:
      HistoriquePaiementResponse = {
      paiementId: 5001,
      membreId: 2001,
      matriculeMembre: 'G1001',
      naturePaiement: 'PARTICIPATION',
      montant: 15,
      statutPaiement: 'PAYE',
      dateHeurePaiement:
        '2026-06-01T12:00:00',
      participationId: 3101,
      detteId: null,
      matchId: 3001
    };

    beforeEach(async () => {
      facade = {
        initialiser: vi.fn(),
        chargerHistorique: vi.fn(),
        joueur:
          signal<
            AuthJoueurResponse | null
          >(null),
        transactions:
          signal<
            HistoriquePaiementResponse[]
          >([]),
        chargement: signal(false),
        rechercheEffectuee:
          signal(false),
        messageErreur: signal(''),
        totalPaye: signal(0)
      };

      await TestBed
        .configureTestingModule({
          imports: [
            HistoriqueTransactionsComponent
          ],
          providers: [
            provideRouter([])
          ]
        })
        .overrideComponent(
          HistoriqueTransactionsComponent,
          {
            set: {
              providers: [
                {
                  provide:
                  HistoriqueTransactionsFacadeService,
                  useValue: facade
                }
              ]
            }
          }
        )
        .compileComponents();

      fixture = TestBed.createComponent(
        HistoriqueTransactionsComponent
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
      'doit afficher le joueur, les transactions et le total',
      () => {
        facade.joueur.set(joueur);

        facade.transactions.set([
          transaction
        ]);

        facade.rechercheEffectuee.set(
          true
        );

        facade.totalPaye.set(15);

        fixture.detectChanges();

        const contenu =
          fixture.nativeElement
            .textContent as string;

        expect(contenu).toContain('G1001');
        expect(contenu).toContain('Dupont');
        expect(contenu).toContain('Marie');
        expect(contenu).toContain('15.00');
        expect(contenu).toContain(
          '01/06/2026, 12:00'
        );
        expect(contenu).not.toContain('3001');
        expect(contenu).not.toContain('3101');
        expect(contenu)
          .toContain(
            'Nombre de transactions'
          );
      }
    );

    it(
      'doit afficher une réponse vide',
      () => {
        facade.joueur.set(joueur);

        facade.rechercheEffectuee.set(
          true
        );

        fixture.detectChanges();

        const contenu =
          fixture.nativeElement
            .textContent as string;

        expect(contenu)
          .toContain(
            'Aucune transaction trouvée'
          );
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

        expect(facade.chargerHistorique)
          .toHaveBeenCalled();
      }
    );

    it(
      'doit afficher l erreur de la façade',
      () => {
        facade.messageErreur.set(
          'Historique indisponible.'
        );

        fixture.detectChanges();

        const contenu =
          fixture.nativeElement
            .textContent as string;

        expect(contenu)
          .toContain(
            'Historique indisponible.'
          );
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

        expect(bouton.disabled)
          .toBe(true);

        expect(bouton.textContent)
          .toContain('Chargement...');
      }
    );
  }
);
