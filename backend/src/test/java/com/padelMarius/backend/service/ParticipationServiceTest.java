package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.participation.AjouterParticipantPriveRequest;
import com.padelMarius.backend.dto.participation.InscriptionPubliqueRequest;
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
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.MembreRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParticipationServiceTest {

    @Mock
    private PadelMatchRepository padelMatchRepository;

    @Mock
    private MembreRepository membreRepository;

    @Mock
    private ParticipationRepository participationRepository;

    @InjectMocks
    private ParticipationService participationService;

    @Test
    void ajouterParticipantPrive_shouldCreateParticipation_whenRequestIsValid() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain, VisibiliteMatch.PRIVE, ModeCreation.PRIVE);
        Membre organisateur = creerMembre(20L, "G0001");
        Membre joueur = creerMembre(21L, "G0002");

        Participation participationOrganisateur = creerParticipation(
                200L,
                match,
                organisateur,
                RoleParticipation.ORGANISATEUR,
                ModeEntreeParticipation.CREATION,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        when(padelMatchRepository.findById(100L)).thenReturn(Optional.of(match));
        when(membreRepository.findByMatricule("G0002")).thenReturn(Optional.of(joueur));
        when(participationRepository.findByMatchId(100L)).thenReturn(List.of(participationOrganisateur));
        when(participationRepository.existsByMatchIdAndMembreId(100L, 21L)).thenReturn(false);
        when(participationRepository.findByMembreId(21L)).thenReturn(List.of());
        when(participationRepository.save(any(Participation.class))).thenAnswer(invocation -> {
            Participation participation = invocation.getArgument(0);
            ReflectionTestUtils.setField(participation, "id", 300L);
            return participation;
        });

        ParticipationResponse response = participationService.ajouterParticipantPrive(
                100L,
                new AjouterParticipantPriveRequest("G0002")
        );

        assertEquals(300L, response.participationId());
        assertEquals(100L, response.matchId());
        assertEquals(21L, response.membreId());
        assertEquals("G0002", response.matriculeJoueur());
        assertEquals(RoleParticipation.JOUEUR, response.roleParticipation());
        assertEquals(ModeEntreeParticipation.INVITATION_PRIVEE, response.modeEntree());
        assertEquals(StatutParticipation.EN_ATTENTE_PAIEMENT, response.statutParticipation());
        assertNotNull(response.dateAffectation());

        verify(participationRepository).save(any(Participation.class));
    }

    @Test
    void inscrireParticipantPublic_shouldCreateParticipation_whenRequestIsValid() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain, VisibiliteMatch.PUBLIC, ModeCreation.PUBLIC);
        Membre organisateur = creerMembre(20L, "G0001");
        Membre joueur = creerMembre(21L, "L0001");

        Participation participationOrganisateur = creerParticipation(
                200L,
                match,
                organisateur,
                RoleParticipation.ORGANISATEUR,
                ModeEntreeParticipation.CREATION,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        when(padelMatchRepository.findById(100L)).thenReturn(Optional.of(match));
        when(membreRepository.findByMatricule("L0001")).thenReturn(Optional.of(joueur));
        when(participationRepository.findByMatchId(100L)).thenReturn(List.of(participationOrganisateur));
        when(participationRepository.existsByMatchIdAndMembreId(100L, 21L)).thenReturn(false);
        when(participationRepository.findByMembreId(21L)).thenReturn(List.of());
        when(participationRepository.save(any(Participation.class))).thenAnswer(invocation -> {
            Participation participation = invocation.getArgument(0);
            ReflectionTestUtils.setField(participation, "id", 301L);
            return participation;
        });

        ParticipationResponse response = participationService.inscrireParticipantPublic(
                100L,
                new InscriptionPubliqueRequest("L0001")
        );

        assertEquals(301L, response.participationId());
        assertEquals(100L, response.matchId());
        assertEquals(21L, response.membreId());
        assertEquals("L0001", response.matriculeJoueur());
        assertEquals(RoleParticipation.JOUEUR, response.roleParticipation());
        assertEquals(ModeEntreeParticipation.INSCRIPTION_PUBLIQUE, response.modeEntree());
        assertEquals(StatutParticipation.EN_ATTENTE_PAIEMENT, response.statutParticipation());

        verify(participationRepository).save(any(Participation.class));
    }

    @Test
    void shouldThrowNotFound_whenMatchDoesNotExist() {
        when(padelMatchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                RessourceIntrouvableException.class,
                () -> participationService.ajouterParticipantPrive(
                        999L,
                        new AjouterParticipantPriveRequest("G0002")
                )
        );

        verify(participationRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFound_whenMembreDoesNotExist() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain, VisibiliteMatch.PRIVE, ModeCreation.PRIVE);

        when(padelMatchRepository.findById(100L)).thenReturn(Optional.of(match));
        when(membreRepository.findByMatricule("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(
                RessourceIntrouvableException.class,
                () -> participationService.ajouterParticipantPrive(
                        100L,
                        new AjouterParticipantPriveRequest("UNKNOWN")
                )
        );

        verify(participationRepository, never()).save(any());
    }

    @Test
    void shouldRejectInactiveMember() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain, VisibiliteMatch.PRIVE, ModeCreation.PRIVE);
        Membre joueur = creerMembre(21L, "G0002");
        joueur.setActif(false);

        when(padelMatchRepository.findById(100L)).thenReturn(Optional.of(match));
        when(membreRepository.findByMatricule("G0002")).thenReturn(Optional.of(joueur));

        assertThrows(
                ConfigurationMetierException.class,
                () -> participationService.ajouterParticipantPrive(
                        100L,
                        new AjouterParticipantPriveRequest("G0002")
                )
        );

        verify(participationRepository, never()).save(any());
    }

    @Test
    void shouldRejectFinishedMatch() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain, VisibiliteMatch.PRIVE, ModeCreation.PRIVE);
        match.setEtatCycle(EtatCycleMatch.TERMINE);

        when(padelMatchRepository.findById(100L)).thenReturn(Optional.of(match));

        assertThrows(
                ConfigurationMetierException.class,
                () -> participationService.ajouterParticipantPrive(
                        100L,
                        new AjouterParticipantPriveRequest("G0002")
                )
        );

        verify(participationRepository, never()).save(any());
    }

    @Test
    void shouldRejectFullMatch() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain, VisibiliteMatch.PRIVE, ModeCreation.PRIVE);
        Membre joueur = creerMembre(21L, "G0002");

        when(padelMatchRepository.findById(100L)).thenReturn(Optional.of(match));
        when(membreRepository.findByMatricule("G0002")).thenReturn(Optional.of(joueur));
        when(participationRepository.findByMatchId(100L)).thenReturn(List.of(
                creerParticipation(200L, match, creerMembre(20L, "G0001"), RoleParticipation.ORGANISATEUR, ModeEntreeParticipation.CREATION, StatutParticipation.EN_ATTENTE_PAIEMENT),
                creerParticipation(201L, match, creerMembre(22L, "G0003"), RoleParticipation.JOUEUR, ModeEntreeParticipation.INVITATION_PRIVEE, StatutParticipation.EN_ATTENTE_PAIEMENT),
                creerParticipation(202L, match, creerMembre(23L, "G0004"), RoleParticipation.JOUEUR, ModeEntreeParticipation.INVITATION_PRIVEE, StatutParticipation.EN_ATTENTE_PAIEMENT),
                creerParticipation(203L, match, creerMembre(24L, "G0005"), RoleParticipation.JOUEUR, ModeEntreeParticipation.INVITATION_PRIVEE, StatutParticipation.EN_ATTENTE_PAIEMENT)
        ));

        assertThrows(
                ConfigurationMetierException.class,
                () -> participationService.ajouterParticipantPrive(
                        100L,
                        new AjouterParticipantPriveRequest("G0002")
                )
        );

        verify(participationRepository, never()).save(any());
    }

    @Test
    void shouldRejectAlreadyParticipant() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain, VisibiliteMatch.PRIVE, ModeCreation.PRIVE);
        Membre joueur = creerMembre(21L, "G0002");

        when(padelMatchRepository.findById(100L)).thenReturn(Optional.of(match));
        when(membreRepository.findByMatricule("G0002")).thenReturn(Optional.of(joueur));
        when(participationRepository.findByMatchId(100L)).thenReturn(List.of());
        when(participationRepository.existsByMatchIdAndMembreId(100L, 21L)).thenReturn(true);

        assertThrows(
                ConfigurationMetierException.class,
                () -> participationService.ajouterParticipantPrive(
                        100L,
                        new AjouterParticipantPriveRequest("G0002")
                )
        );

        verify(participationRepository, never()).save(any());
    }

    @Test
    void shouldRejectMemberWithOverlappingMatch() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch nouveauMatch = creerMatch(100L, terrain, VisibiliteMatch.PUBLIC, ModeCreation.PUBLIC);
        Membre joueur = creerMembre(21L, "L0001");

        PadelMatch matchExistant = creerMatch(101L, terrain, VisibiliteMatch.PUBLIC, ModeCreation.PUBLIC);
        matchExistant.setDateHeureDebut(LocalDateTime.of(2026, 5, 20, 10, 0));
        matchExistant.setDateHeureFin(LocalDateTime.of(2026, 5, 20, 11, 30));

        Participation participationExistante = creerParticipation(
                201L,
                matchExistant,
                joueur,
                RoleParticipation.JOUEUR,
                ModeEntreeParticipation.INSCRIPTION_PUBLIQUE,
                StatutParticipation.CONFIRMEE
        );

        when(padelMatchRepository.findById(100L)).thenReturn(Optional.of(nouveauMatch));
        when(membreRepository.findByMatricule("L0001")).thenReturn(Optional.of(joueur));
        when(participationRepository.findByMatchId(100L)).thenReturn(List.of());
        when(participationRepository.existsByMatchIdAndMembreId(100L, 21L)).thenReturn(false);
        when(participationRepository.findByMembreId(21L)).thenReturn(List.of(participationExistante));

        assertThrows(
                ConfigurationMetierException.class,
                () -> participationService.inscrireParticipantPublic(
                        100L,
                        new InscriptionPubliqueRequest("L0001")
                )
        );

        verify(participationRepository, never()).save(any());
    }

    @Test
    void shouldRejectPrivateEndpointWhenMatchIsPublic() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain, VisibiliteMatch.PUBLIC, ModeCreation.PUBLIC);

        when(padelMatchRepository.findById(100L)).thenReturn(Optional.of(match));

        assertThrows(
                ConfigurationMetierException.class,
                () -> participationService.ajouterParticipantPrive(
                        100L,
                        new AjouterParticipantPriveRequest("G0002")
                )
        );

        verify(participationRepository, never()).save(any());
    }

    @Test
    void shouldRejectPublicEndpointWhenMatchIsPrivate() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain, VisibiliteMatch.PRIVE, ModeCreation.PRIVE);

        when(padelMatchRepository.findById(100L)).thenReturn(Optional.of(match));

        assertThrows(
                ConfigurationMetierException.class,
                () -> participationService.inscrireParticipantPublic(
                        100L,
                        new InscriptionPubliqueRequest("L0001")
                )
        );

        verify(participationRepository, never()).save(any());
    }

    private Site creerSite(Long id) {
        Site site = Site.builder()
                .code("SITE-" + id)
                .nom("Site " + id)
                .adresse("Adresse " + id)
                .actif(true)
                .build();

        ReflectionTestUtils.setField(site, "id", id);
        return site;
    }

    private Terrain creerTerrain(Long id, Site site) {
        Terrain terrain = Terrain.builder()
                .site(site)
                .numero("1")
                .actif(true)
                .build();

        ReflectionTestUtils.setField(terrain, "id", id);
        return terrain;
    }

    private Membre creerMembre(Long id, String matricule) {
        Membre membre = Membre.builder()
                .matricule(matricule)
                .nom("Nom")
                .prenom("Prenom")
                .motDePasseHash("$2y$10$w7Hmtss9GA8U9RAxfZeb3.JmBalmCw64iEo6pY5YEgNky9FM7OriK")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .build();

        ReflectionTestUtils.setField(membre, "id", id);
        return membre;
    }

    private PadelMatch creerMatch(
            Long id,
            Terrain terrain,
            VisibiliteMatch visibiliteMatch,
            ModeCreation modeCreation
    ) {
        PadelMatch match = PadelMatch.builder()
                .terrain(terrain)
                .dateHeureDebut(LocalDateTime.of(2026, 5, 20, 9, 0))
                .dateHeureFin(LocalDateTime.of(2026, 5, 20, 10, 30))
                .modeCreation(modeCreation)
                .visibiliteCourante(visibiliteMatch)
                .prixTotal(new BigDecimal("60.00"))
                .dateCreation(LocalDateTime.of(2026, 5, 1, 10, 0))
                .etatCycle(EtatCycleMatch.A_VENIR)
                .build();

        ReflectionTestUtils.setField(match, "id", id);
        return match;
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
}
