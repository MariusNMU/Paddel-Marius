import {
  signal,
  WritableSignal
} from '@angular/core';
import {
  ComponentFixture,
  TestBed
} from '@angular/core/testing';
import { AuthJoueurResponse } from '../../models/auth.model';
import {
  MatchPublicResponse,
  RejoindreMatchPublicResponse
} from '../../models/match-public.model';
import { ParametresMetierResponse } from '../../models/parametres-metier.model';
import { SiteResponse } from '../../models/site.model';
import { MatchesPublicsFacadeService } from '../../services/matches-publics-facade.service';
import { JourRapide } from '../../shared/date-ui.util';
import { MatchesPublicsComponent } from './matches-publics.component';

describe('MatchesPublicsComponent', () => {
  let fixture: ComponentFixture<MatchesPublicsComponent>;
  let component: MatchesPublicsComponent;

  let facade: {
    initialiser: ReturnType<typeof vi.fn>;
    selectionnerJour: ReturnType<typeof vi.fn>;
    rechercherMatchesPublics: ReturnType<typeof vi.fn>;
    rejoindreEtPayer: ReturnType<typeof vi.fn>;
    modifierSiteId: ReturnType<typeof vi.fn>;
    modifierDate: ReturnType<typeof vi.fn>;
    joueurConnecte: ReturnType<typeof vi.fn>;

    sites: WritableSignal<SiteResponse[]>;
    parametresMetier:
      WritableSignal<ParametresMetierResponse | null>;
    joursRapides: WritableSignal<JourRapide[]>;
    siteId: WritableSignal<number | null>;
    date: WritableSignal<string>;
    matches: WritableSignal<MatchPublicResponse[]>;
    dernierPaiement:
      WritableSignal<RejoindreMatchPublicResponse | null>;
    chargementSites: WritableSignal<boolean>;
    chargementRecherche: WritableSignal<boolean>;
    chargementPaiement: WritableSignal<boolean>;
    rechercheEffectuee: WritableSignal<boolean>;
    messageErreur: WritableSignal<string>;
    messageSucces: WritableSignal<string>;
  };

  const joueur: AuthJoueurResponse = {
    membreId: 1,
    matricule: 'TEST001',
    nom: 'Test',
    prenom: 'Joueur',
    categorieMembre: 'GLOBAL',
    siteRattachementId: null,
    nomSiteRattachement: null,
    actif: true
  };

  const sites: SiteResponse[] = [{
    siteId: 1,
    code: 'ALP',
    nom: 'Site Alpha',
    adresse: 'Rue du Test 1'
  }];

  const parametresMetier: ParametresMetierResponse = {
    dureeMatchMinutes: 90,
    pauseEntreMatchesMinutes: 15,
    nombreJoueursMaximum: 4,
    prixTotalMatch: 60,
    montantParticipationStandard: 15,
    soldeInitialJoueur: 100
  };

  const matchPublic: MatchPublicResponse = {
    matchId: 10,
    siteId: 1,
    nomSite: 'Site Alpha',
    terrainId: 20,
    numeroTerrain: 'T1',
    dateHeureDebut: '2026-06-20T09:00:00',
    dateHeureFin: '2026-06-20T10:30:00',
    nombreParticipantsActifs: 2,
    placesDisponibles: 2,
    prixTotal: 60,
    montantParticipation: 15,
    peutRejoindre: true,
    motifNonEligibilite: null
  };

  beforeEach(async () => {
    facade = {
      initialiser: vi.fn(),
      selectionnerJour: vi.fn(),
      rechercherMatchesPublics: vi.fn(),
      rejoindreEtPayer: vi.fn(),
      modifierSiteId: vi.fn(),
      modifierDate: vi.fn(),
      joueurConnecte: vi.fn(() => joueur),

      sites: signal(sites),
      parametresMetier: signal(parametresMetier),
      joursRapides: signal([]),
      siteId: signal(1),
      date: signal('2026-06-20'),
      matches: signal([]),
      dernierPaiement: signal(null),
      chargementSites: signal(false),
      chargementRecherche: signal(false),
      chargementPaiement: signal(false),
      rechercheEffectuee: signal(false),
      messageErreur: signal(''),
      messageSucces: signal('')
    };

    await TestBed.configureTestingModule({
      imports: [MatchesPublicsComponent],
      providers: [{
        provide: MatchesPublicsFacadeService,
        useValue: facade
      }]
    }).compileComponents();

    fixture = TestBed.createComponent(
      MatchesPublicsComponent
    );
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('doit créer le composant', () => {
    expect(component).toBeTruthy();
  });

  it('doit initialiser la façade', () => {
    expect(facade.initialiser).toHaveBeenCalled();
  });

  it('doit déléguer la recherche à la façade', () => {
    component.rechercherMatchesPublics();

    expect(facade.rechercherMatchesPublics)
      .toHaveBeenCalled();
  });

  it('doit déléguer le paiement à la façade', () => {
    component.rejoindreEtPayer(matchPublic);

    expect(facade.rejoindreEtPayer)
      .toHaveBeenCalledWith(matchPublic);
  });

  it('doit déléguer le choix rapide de la date', () => {
    component.selectionnerJour('2026-06-21');

    expect(facade.selectionnerJour)
      .toHaveBeenCalledWith('2026-06-21');
  });

  it('doit masquer le bouton pour un match non éligible', () => {
    facade.matches.set([{
      ...matchPublic,
      peutRejoindre: false,
      motifNonEligibilite:
        'Tu participes déjà à ce match.'
    }]);

    fixture.detectChanges();

    const contenu = fixture.nativeElement.textContent;

    expect(contenu).toContain(
      'Tu participes déjà à ce match.'
    );
    expect(contenu).not.toContain(
      'Rejoindre et payer 15.00 €'
    );
  });
});
