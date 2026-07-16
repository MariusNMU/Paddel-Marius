import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthJoueurResponse } from '../../models/auth.model';
import { PaiementResponse } from '../../models/paiement.model';
import { ReservationJoueurResponse } from '../../models/reservation.model';
import { AuthContextService } from '../../services/auth-context.service';
import { PaiementApiService } from '../../services/paiement-api.service';
import { ReservationApiService } from '../../services/reservation-api.service';
import { MesReservationsComponent } from './mes-reservations.component';

describe('MesReservationsComponent', () => {
  let fixture: ComponentFixture<MesReservationsComponent>;
  let component: MesReservationsComponent;

  let authContextService: {
    joueur: ReturnType<typeof vi.fn>;
  };

  let reservationApiService: {
    consulterMesReservations: ReturnType<typeof vi.fn>;
  };

  let paiementApiService: {
    payerParticipationStandard: ReturnType<typeof vi.fn>;
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

  const reservation: ReservationJoueurResponse = {
    participationId: 3101,
    matchId: 3001,
    siteId: 1001,
    nomSite: 'Padel Bruxelles',
    terrainId: 1101,
    numeroTerrain: 'T1',
    dateHeureDebut: '2026-06-20T09:00:00',
    dateHeureFin: '2026-06-20T10:30:00',
    roleParticipation: 'ORGANISATEUR',
    modeEntree: 'CREATION',
    statutParticipation: 'CONFIRMEE',
    modeCreation: 'PUBLIC',
    visibiliteCourante: 'PUBLIC',
    etatCycle: 'A_VENIR',
    prixTotal: 60
  };

  const paiement: PaiementResponse = {
    paiementId: 4101,
    participationId: 3101,
    membreId: 2001,
    matriculeMembre: 'G1001',
    montant: 15,
    montantDettesReglees: 30,
    montantTotalDebite: 45,
    naturePaiement: 'PARTICIPATION',
    statutPaiement: 'PAYE',
    statutParticipation: 'CONFIRMEE',
    dateHeurePaiement: '2026-06-01T12:00:00',
    dateConfirmationParticipation: '2026-06-01T12:00:00'
  };

  beforeEach(async () => {
    authContextService = {
      joueur: vi.fn(() => null)
    };

    reservationApiService = {
      consulterMesReservations: vi.fn()
    };

    paiementApiService = {
      payerParticipationStandard: vi.fn(() => of(paiement))
    };

    await TestBed.configureTestingModule({
      imports: [MesReservationsComponent],
      providers: [
        provideRouter([]),
        { provide: AuthContextService, useValue: authContextService },
        { provide: ReservationApiService, useValue: reservationApiService },
        { provide: PaiementApiService, useValue: paiementApiService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(MesReservationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('doit créer le composant', () => {
    expect(component).toBeTruthy();
  });

  it('doit afficher une erreur si aucun joueur n est connecté', () => {
    authContextService.joueur.mockReturnValue(null);

    component.chargerReservations();

    expect(component.messageErreur()).toBe('Aucun joueur connecté.');
    expect(reservationApiService.consulterMesReservations).not.toHaveBeenCalled();
  });

  it('doit charger les réservations du joueur connecté', () => {
    authContextService.joueur.mockReturnValue(joueur);
    reservationApiService.consulterMesReservations.mockReturnValue(of([reservation]));

    component.chargerReservations();

    expect(reservationApiService.consulterMesReservations).toHaveBeenCalledWith('G1001');
    expect(component.reservations()).toEqual([reservation]);
    expect(component.rechercheEffectuee()).toBe(true);
    expect(component.chargement()).toBe(false);
    expect(component.messageErreur()).toBe('');
  });

  it('doit afficher une erreur si le chargement des réservations échoue', () => {
    authContextService.joueur.mockReturnValue(joueur);
    reservationApiService.consulterMesReservations.mockReturnValue(
      throwError(() => new HttpErrorResponse({
        status: 500,
        error: {
          message: 'Erreur backend réservations.'
        }
      }))
    );

    component.chargerReservations();

    expect(component.messageErreur()).toBe('Erreur backend réservations.');
    expect(component.reservations()).toEqual([]);
    expect(component.chargement()).toBe(false);
  });

  it('doit payer une participation en attente', () => {
    const reservationEnAttente: ReservationJoueurResponse = {
      ...reservation,
      statutParticipation: 'EN_ATTENTE_PAIEMENT'
    };

    component.reservations.set([reservationEnAttente]);

    component.payerParticipation(reservationEnAttente);

    expect(
      paiementApiService.payerParticipationStandard
    ).toHaveBeenCalledWith(3101);
    expect(component.dernierPaiement()).toEqual(paiement);
    expect(component.messageSucces()).toBe(
      'Participation payée avec succès.'
    );
    expect(component.reservations()[0].statutParticipation)
      .toBe('CONFIRMEE');
    expect(component.paiementEnCoursParticipationId()).toBeNull();
  });

  it('doit afficher la participation, les dettes et le total débité', () => {
    const reservationEnAttente: ReservationJoueurResponse = {
      ...reservation,
      statutParticipation: 'EN_ATTENTE_PAIEMENT'
    };

    component.reservations.set([reservationEnAttente]);
    component.payerParticipation(reservationEnAttente);
    fixture.detectChanges();

    const contenu = fixture.nativeElement.textContent;

    expect(contenu).toContain('Paiement enregistré');
    expect(contenu).toContain('Participation');
    expect(contenu).toContain('15.00');
    expect(contenu).toContain('Dettes réglées');
    expect(contenu).toContain('30.00');
    expect(contenu).toContain('Total débité');
    expect(contenu).toContain('45.00');
  });

  it('doit afficher une erreur si le paiement échoue', () => {
    const reservationEnAttente: ReservationJoueurResponse = {
      ...reservation,
      statutParticipation: 'EN_ATTENTE_PAIEMENT'
    };

    paiementApiService.payerParticipationStandard.mockReturnValue(
      throwError(() => new HttpErrorResponse({
        status: 409,
        error: {
          message: 'Cette participation possède déjà un paiement.'
        }
      }))
    );

    component.reservations.set([reservationEnAttente]);
    component.payerParticipation(reservationEnAttente);

    expect(component.messageErreur()).toBe(
      'Cette participation possède déjà un paiement.'
    );
    expect(component.dernierPaiement()).toBeNull();
    expect(component.paiementEnCoursParticipationId()).toBeNull();
    expect(component.reservations()[0].statutParticipation)
      .toBe('EN_ATTENTE_PAIEMENT');
  });

  it('doit charger automatiquement les réservations au démarrage si un joueur est connecté', () => {
    TestBed.resetTestingModule();

    authContextService = {
      joueur: vi.fn(() => joueur)
    };

    reservationApiService = {
      consulterMesReservations: vi.fn(() => of([reservation]))
    };

    TestBed.configureTestingModule({
      imports: [MesReservationsComponent],
      providers: [
        provideRouter([]),
        { provide: AuthContextService, useValue: authContextService },
        { provide: ReservationApiService, useValue: reservationApiService },
        { provide: PaiementApiService, useValue: paiementApiService }
      ]
    });

    const nouvelleFixture = TestBed.createComponent(MesReservationsComponent);
    const nouveauComposant = nouvelleFixture.componentInstance;

    nouvelleFixture.detectChanges();

    expect(reservationApiService.consulterMesReservations).toHaveBeenCalledWith('G1001');
    expect(nouveauComposant.reservations()).toEqual([reservation]);
  });
});
