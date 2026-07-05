import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AuthJoueurResponse } from '../../models/auth.model';
import { InvitationPriveeResponse } from '../../models/invitation.model';
import { AuthContextService } from '../../services/auth-context.service';
import { InvitationApiService } from '../../services/invitation-api.service';
import { PaiementApiService } from '../../services/paiement-api.service';
import { InvitationsRecuesComponent } from './invitations-recues.component';

describe('InvitationsRecuesComponent', () => {
  let fixture: ComponentFixture<InvitationsRecuesComponent>;
  let component: InvitationsRecuesComponent;

  let authContextService: {
    joueur: ReturnType<typeof vi.fn>;
  };

  let invitationApiService: {
    listerInvitationsRecues: ReturnType<typeof vi.fn>;
    declinerInvitation: ReturnType<typeof vi.fn>;
  };

  let paiementApiService: {
    payerParticipationStandard: ReturnType<typeof vi.fn>;
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

  const invitation: InvitationPriveeResponse = {
    participationId: 10,
    matchId: 20,
    siteId: 1,
    nomSite: 'Site Alpha',
    terrainId: 30,
    numeroTerrain: 'T1',
    dateHeureDebut: '2026-06-20T09:00:00',
    dateHeureFin: '2026-06-20T10:30:00',
    organisateurId: 40,
    matriculeOrganisateur: 'ORG001',
    nomOrganisateur: 'Organisateur',
    prenomOrganisateur: 'Test',
    joueurInviteId: 1,
    matriculeInvite: 'TEST001',
    nomInvite: 'Test',
    prenomInvite: 'Joueur',
    statutParticipation: 'EN_ATTENTE_PAIEMENT'
  };

  beforeEach(async () => {
    authContextService = {
      joueur: vi.fn(() => joueur)
    };

    invitationApiService = {
      listerInvitationsRecues: vi.fn(() => of([invitation])),
      declinerInvitation: vi.fn()
    };

    paiementApiService = {
      payerParticipationStandard: vi.fn(() => of({
        paiementId: 100,
        participationId: 10,
        membreId: 1,
        matriculeMembre: 'TEST001',
        montant: 15,
        montantDettesReglees: 0,
        montantTotalDebite: 15,
        naturePaiement: 'PARTICIPATION',
        statutPaiement: 'PAYE',
        statutParticipation: 'CONFIRMEE',
        dateHeurePaiement: '2026-06-20T08:00:00',
        dateConfirmationParticipation: '2026-06-20T08:00:00'
      }))
    };

    await TestBed.configureTestingModule({
      imports: [InvitationsRecuesComponent],
      providers: [
        provideRouter([]),
        { provide: AuthContextService, useValue: authContextService },
        { provide: InvitationApiService, useValue: invitationApiService },
        { provide: PaiementApiService, useValue: paiementApiService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(InvitationsRecuesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('doit charger les invitations du joueur connecté', () => {
    expect(invitationApiService.listerInvitationsRecues)
      .toHaveBeenCalledWith('TEST001');
    expect(component.invitations).toEqual([invitation]);
  });

  it('doit payer une invitation sans envoyer de montant depuis Angular', () => {
    component.confirmerEtPayer(invitation);

    expect(paiementApiService.payerParticipationStandard)
      .toHaveBeenCalledWith(10);
  });
});
