package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.invitation.DeclinerInvitationRequest;
import com.padelMarius.backend.dto.invitation.InvitationPriveeResponse;
import com.padelMarius.backend.dto.invitation.InviterJoueurPriveRequest;
import com.padelMarius.backend.dto.participation.AjouterParticipantPriveRequest;
import com.padelMarius.backend.dto.participation.ParticipationResponse;
import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.ModeCreation;
import com.padelMarius.backend.entity.ModeEntreeParticipation;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.Participation;
import com.padelMarius.backend.entity.RoleParticipation;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.entity.Terrain;
import com.padelMarius.backend.entity.VisibiliteMatch;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.repository.MembreRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvitationPriveeServiceTest {

    private static final LocalDateTime MAINTENANT_FIXE =
            LocalDateTime.of(2026, 5, 19, 8, 0);

    @Mock
    private PadelMatchRepository padelMatchRepository;

    @Mock
    private MembreRepository membreRepository;

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private ParticipationService participationService;

    private InvitationPriveeService invitationPriveeService;

    @BeforeEach
    void setUp() {
        Clock clockFixe = Clock.fixed(
                MAINTENANT_FIXE
                        .atZone(ZoneId.systemDefault())
                        .toInstant(),
                ZoneId.systemDefault()
        );

        invitationPriveeService = new InvitationPriveeService(
                padelMatchRepository,
                membreRepository,
                participationRepository,
                participationService,
                clockFixe
        );
    }

    @Test
    void inviterJoueur_shouldCreatePrivateInvitation() {
        DonneesInvitation donnees = creerDonneesInvitation();
        Participation participationInvite = creerParticipation(
                301L,
                donnees.match(),
                donnees.invite(),
                RoleParticipation.JOUEUR,
                ModeEntreeParticipation.INVITATION_PRIVEE,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        when(padelMatchRepository.findById(100L)).thenReturn(Optional.of(donnees.match()));
        when(participationRepository.findByMatchId(100L))
                .thenReturn(List.of(donnees.participationOrganisateur()));
        when(participationService.ajouterParticipantPrive(
                eq(100L),
                any(AjouterParticipantPriveRequest.class)
        )).thenReturn(new ParticipationResponse(
                301L,
                100L,
                21L,
                "G0002",
                RoleParticipation.JOUEUR,
                ModeEntreeParticipation.INVITATION_PRIVEE,
                StatutParticipation.EN_ATTENTE_PAIEMENT,
                LocalDateTime.of(2026, 5, 1, 10, 0)
        ));
        when(participationRepository.findById(301L)).thenReturn(Optional.of(participationInvite));

        InvitationPriveeResponse response = invitationPriveeService.inviterJoueur(
                100L,
                new InviterJoueurPriveRequest("G0001", "G0002")
        );

        assertEquals(301L, response.participationId());
        assertEquals(100L, response.matchId());
        assertEquals("G0001", response.matriculeOrganisateur());
        assertEquals("G0002", response.matriculeInvite());
        assertEquals(StatutParticipation.EN_ATTENTE_PAIEMENT, response.statutParticipation());

        verify(participationService).ajouterParticipantPrive(
                eq(100L),
                any(AjouterParticipantPriveRequest.class)
        );
    }

    @Test
    void inviterJoueur_shouldRejectWhenRequesterIsNotOrganizer() {
        DonneesInvitation donnees = creerDonneesInvitation();

        when(padelMatchRepository.findById(100L)).thenReturn(Optional.of(donnees.match()));
        when(participationRepository.findByMatchId(100L))
                .thenReturn(List.of(donnees.participationOrganisateur()));

        ConfigurationMetierException exception = assertThrows(
                ConfigurationMetierException.class,
                () -> invitationPriveeService.inviterJoueur(
                        100L,
                        new InviterJoueurPriveRequest("G9999", "G0002")
                )
        );

        assertEquals("Seul l'organisateur du match peut inviter des joueurs.", exception.getMessage());
        verify(participationService, never()).ajouterParticipantPrive(any(), any());
    }

    @Test
    void inviterJoueur_shouldRejectWhenMatchIsPublic() {
        DonneesInvitation donnees = creerDonneesInvitation();
        donnees.match().setVisibiliteCourante(VisibiliteMatch.PUBLIC);

        when(padelMatchRepository.findById(100L)).thenReturn(Optional.of(donnees.match()));

        ConfigurationMetierException exception = assertThrows(
                ConfigurationMetierException.class,
                () -> invitationPriveeService.inviterJoueur(
                        100L,
                        new InviterJoueurPriveRequest("G0001", "G0002")
                )
        );

        assertEquals("Le match est déjà public. Les joueurs doivent rejoindre le match public.", exception.getMessage());
        verify(participationService, never()).ajouterParticipantPrive(any(), any());
    }

    @Test
    void listerInvitationsRecues_shouldReturnPendingPrivateInvitations() {
        DonneesInvitation donnees = creerDonneesInvitation();
        Participation participationInvite = creerParticipation(
                301L,
                donnees.match(),
                donnees.invite(),
                RoleParticipation.JOUEUR,
                ModeEntreeParticipation.INVITATION_PRIVEE,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        when(membreRepository.findByMatricule("G0002")).thenReturn(Optional.of(donnees.invite()));
        when(participationRepository.findByMembreIdAndModeEntreeAndStatutParticipation(
                21L,
                ModeEntreeParticipation.INVITATION_PRIVEE,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        )).thenReturn(List.of(participationInvite));
        when(participationRepository.findByMatchId(100L))
                .thenReturn(List.of(donnees.participationOrganisateur(), participationInvite));

        List<InvitationPriveeResponse> responses = invitationPriveeService.listerInvitationsRecues("G0002");

        assertEquals(1, responses.size());
        assertEquals(301L, responses.get(0).participationId());
        assertEquals("G0001", responses.get(0).matriculeOrganisateur());
        assertEquals("G0002", responses.get(0).matriculeInvite());
        assertEquals(StatutParticipation.EN_ATTENTE_PAIEMENT, responses.get(0).statutParticipation());
    }

    @Test
    void declinerInvitation_shouldLiberateInvitation() {
        DonneesInvitation donnees = creerDonneesInvitation();
        Participation participationInvite = creerParticipation(
                301L,
                donnees.match(),
                donnees.invite(),
                RoleParticipation.JOUEUR,
                ModeEntreeParticipation.INVITATION_PRIVEE,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        when(participationRepository.findByIdForUpdate(301L))
                .thenReturn(Optional.of(participationInvite));
        when(participationRepository.save(participationInvite)).thenReturn(participationInvite);
        when(participationRepository.findByMatchId(100L))
                .thenReturn(List.of(donnees.participationOrganisateur(), participationInvite));

        InvitationPriveeResponse response = invitationPriveeService.declinerInvitation(
                301L,
                new DeclinerInvitationRequest("G0002")
        );

        assertEquals(301L, response.participationId());
        assertEquals(StatutParticipation.LIBEREE, response.statutParticipation());
        assertEquals(StatutParticipation.LIBEREE, participationInvite.getStatutParticipation());
        assertEquals(MAINTENANT_FIXE, participationInvite.getDateLiberation());

        verify(participationRepository).findByIdForUpdate(301L);
        verify(participationRepository).save(participationInvite);
    }

    @Test
    void declinerInvitation_shouldRejectWhenInvitationBelongsToAnotherPlayer() {
        DonneesInvitation donnees = creerDonneesInvitation();
        Participation participationInvite = creerParticipation(
                301L,
                donnees.match(),
                donnees.invite(),
                RoleParticipation.JOUEUR,
                ModeEntreeParticipation.INVITATION_PRIVEE,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        when(participationRepository.findByIdForUpdate(301L))
                .thenReturn(Optional.of(participationInvite));

        ConfigurationMetierException exception = assertThrows(
                ConfigurationMetierException.class,
                () -> invitationPriveeService.declinerInvitation(
                        301L,
                        new DeclinerInvitationRequest("G0003")
                )
        );

        assertEquals("Cette invitation ne concerne pas le joueur connecté.", exception.getMessage());
        verify(participationRepository, never()).save(any(Participation.class));
    }

    private DonneesInvitation creerDonneesInvitation() {
        Site site = Site.builder()
                .code("SITE01")
                .nom("Padel Central")
                .adresse("Rue du Test 1")
                .actif(true)
                .build();
        ReflectionTestUtils.setField(site, "id", 1L);

        Terrain terrain = Terrain.builder()
                .site(site)
                .numero("T1")
                .actif(true)
                .build();
        ReflectionTestUtils.setField(terrain, "id", 10L);

        PadelMatch match = PadelMatch.builder()
                .terrain(terrain)
                .dateHeureDebut(LocalDateTime.of(2026, 5, 20, 9, 0))
                .dateHeureFin(LocalDateTime.of(2026, 5, 20, 10, 30))
                .modeCreation(ModeCreation.PRIVE)
                .visibiliteCourante(VisibiliteMatch.PRIVE)
                .prixTotal(new BigDecimal("60.00"))
                .dateCreation(LocalDateTime.of(2026, 5, 1, 10, 0))
                .etatCycle(EtatCycleMatch.A_VENIR)
                .build();
        ReflectionTestUtils.setField(match, "id", 100L);

        Membre organisateur = creerMembre(20L, "G0001");
        Membre invite = creerMembre(21L, "G0002");
        Participation participationOrganisateur = creerParticipation(
                300L,
                match,
                organisateur,
                RoleParticipation.ORGANISATEUR,
                ModeEntreeParticipation.CREATION,
                StatutParticipation.CONFIRMEE
        );

        return new DonneesInvitation(match, organisateur, invite, participationOrganisateur);
    }

    private Membre creerMembre(Long id, String matricule) {
        Membre membre = Membre.builder()
                .matricule(matricule)
                .nom("Nom " + matricule)
                .prenom("Prenom " + matricule)
                .motDePasseHash("$2y$10$w7Hmtss9GA8U9RAxfZeb3.JmBalmCw64iEo6pY5YEgNky9FM7OriK")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .build();
        ReflectionTestUtils.setField(membre, "id", id);
        return membre;
    }

    private Participation creerParticipation(
            Long id,
            PadelMatch match,
            Membre membre,
            RoleParticipation roleParticipation,
            ModeEntreeParticipation modeEntree,
            StatutParticipation statutParticipation
    ) {
        Participation participation = Participation.builder()
                .match(match)
                .membre(membre)
                .roleParticipation(roleParticipation)
                .modeEntree(modeEntree)
                .statutParticipation(statutParticipation)
                .dateAffectation(LocalDateTime.of(2026, 5, 1, 10, 0))
                .build();
        ReflectionTestUtils.setField(participation, "id", id);
        return participation;
    }

    private record DonneesInvitation(
            PadelMatch match,
            Membre organisateur,
            Membre invite,
            Participation participationOrganisateur
    ) {
    }
}
