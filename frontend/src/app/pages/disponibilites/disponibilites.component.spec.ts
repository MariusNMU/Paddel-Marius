import { signal, WritableSignal } from '@angular/core';
import {
  ComponentFixture,
  TestBed
} from '@angular/core/testing';
import {
  CreneauDisponibiliteResponse,
  DisponibilitesResponse
} from '../../models/disponibilite.model';
import { SiteResponse } from '../../models/site.model';
import { DisponibilitesFacadeService } from '../../services/disponibilites-facade.service';
import { JourRapide } from '../../shared/date-ui.util';
import { DisponibilitesComponent } from './disponibilites.component';

describe('DisponibilitesComponent', () => {
  let fixture: ComponentFixture<DisponibilitesComponent>;
  let component: DisponibilitesComponent;

  let facade: {
    initialiser: ReturnType<typeof vi.fn>;
    modifierSiteId: ReturnType<typeof vi.fn>;
    modifierDate: ReturnType<typeof vi.fn>;
    selectionnerJour: ReturnType<typeof vi.fn>;
    consulterDisponibilites: ReturnType<typeof vi.fn>;
    allerCreerMatch: ReturnType<typeof vi.fn>;
    peutCreerMatchSurSiteSelectionne:
      ReturnType<typeof vi.fn>;
    siteSelectionne: ReturnType<typeof vi.fn>;
    dureeMatchLibelle: ReturnType<typeof vi.fn>;

    sites: WritableSignal<SiteResponse[]>;
    joursRapides: WritableSignal<JourRapide[]>;
    siteId: WritableSignal<number | null>;
    date: WritableSignal<string>;
    chargementSites: WritableSignal<boolean>;
    chargementParametresMetier:
      WritableSignal<boolean>;
    chargementRecherche: WritableSignal<boolean>;
    messageErreur: WritableSignal<string>;
    disponibilites:
      WritableSignal<DisponibilitesResponse | null>;
  };

  const site: SiteResponse = {
    siteId: 1,
    code: 'ALP',
    nom: 'Site Alpha',
    adresse: 'Rue du Test 1'
  };

  const creneau: CreneauDisponibiliteResponse = {
    terrainId: 20,
    numeroTerrain: 'T2',
    dateHeureDebut: '2026-06-20T09:00:00',
    dateHeureFin: '2026-06-20T10:30:00'
  };

  const disponibilites: DisponibilitesResponse = {
    siteId: 1,
    nomSite: 'Site Alpha',
    date: '2026-06-20',
    ferme: false,
    motifFermeture: null,
    creneaux: [creneau]
  };

  beforeEach(async () => {
    facade = {
      initialiser: vi.fn(),
      modifierSiteId: vi.fn(),
      modifierDate: vi.fn(),
      selectionnerJour: vi.fn(),
      consulterDisponibilites: vi.fn(),
      allerCreerMatch: vi.fn(),
      peutCreerMatchSurSiteSelectionne:
        vi.fn(() => true),
      siteSelectionne: vi.fn(() => site),
      dureeMatchLibelle: vi.fn(() => '1h30'),

      sites: signal([site]),
      joursRapides: signal([]),
      siteId: signal(1),
      date: signal('2026-06-20'),
      chargementSites: signal(false),
      chargementParametresMetier: signal(false),
      chargementRecherche: signal(false),
      messageErreur: signal(''),
      disponibilites: signal(null)
    };

    await TestBed.configureTestingModule({
      imports: [DisponibilitesComponent]
    })
      .overrideComponent(DisponibilitesComponent, {
        set: {
          providers: [
            {
              provide: DisponibilitesFacadeService,
              useValue: facade
            }
          ]
        }
      })
      .compileComponents();

    fixture = TestBed.createComponent(
      DisponibilitesComponent
    );

    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('doit créer le composant et initialiser la façade', () => {
    expect(component).toBeTruthy();
    expect(facade.initialiser).toHaveBeenCalled();
  });

  it('doit déléguer la recherche à la façade', () => {
    component.consulterDisponibilites();

    expect(facade.consulterDisponibilites)
      .toHaveBeenCalled();
  });

  it('doit déléguer le choix rapide de la date', () => {
    component.selectionnerJour('2026-06-21');

    expect(facade.selectionnerJour)
      .toHaveBeenCalledWith('2026-06-21');
  });

  it('doit déléguer la navigation du créneau', () => {
    component.allerCreerMatch(creneau);

    expect(facade.allerCreerMatch)
      .toHaveBeenCalledWith(creneau);
  });

  it('doit afficher les disponibilités de la façade', () => {
    facade.disponibilites.set(disponibilites);

    fixture.detectChanges();

    const contenu = fixture.nativeElement.textContent;

    expect(contenu).toContain('Terrain T2');
    expect(contenu).not.toContain('(20)');
    expect(contenu).toContain('20/06/2026');
    expect(contenu).toContain('09:00');
    expect(contenu).toContain('10:30');
    expect(contenu).toContain('1h30');
  });

  it('doit masquer l’action de réservation hors site', () => {
    facade.peutCreerMatchSurSiteSelectionne
      .mockReturnValue(false);

    facade.disponibilites.set({
      ...disponibilites,
      siteId: 2,
      nomSite: 'Site Beta'
    });

    fixture.detectChanges();

    const contenu: string =
      fixture.nativeElement.textContent;

    const boutons: HTMLButtonElement[] = Array.from(
      fixture.nativeElement.querySelectorAll('button')
    );

    const boutonReservation = boutons.find(
      bouton => bouton.textContent?.includes(
        'Utiliser ce créneau'
      )
    );

    expect(contenu).toContain(
      'Un membre SITE ne peut réserver que sur son site de rattachement.'
    );

    expect(boutonReservation).toBeUndefined();
  });
});
