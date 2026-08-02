package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.reservation.ReservationJoueurResponse;
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
import com.padelMarius.backend.repository.ParticipationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembreReservationServiceTest {

    @Mock
    private MembreRepository membreRepository;

    @Mock
    private ParticipationRepository participationRepository;

    private MembreReservationService membreReservationService;

    @BeforeEach
    void setUp() {
        membreReservationService = new MembreReservationService(
                membreRepository,
                participationRepository
        );
    }

    @Test
    void consulterReservations_shouldReturnMappedReservations() {
        Membre membre = creerMembre(1L, "G1001");
        Participation participation = creerParticipation(
                10L,
                creerMatch(
                        100L,
                        LocalDateTime.of(2026, 8, 5, 9, 0)
                ),
                membre,
                StatutParticipation.CONFIRMEE
        );

        when(membreRepository.findByMatricule("G1001"))
                .thenReturn(Optional.of(membre));
        when(participationRepository.findByMembreId(1L))
                .thenReturn(List.of(participation));

        List<ReservationJoueurResponse> reservations =
                membreReservationService.consulterReservations(" G1001 ");

        assertThat(reservations).hasSize(1);
        assertThat(reservations.getFirst().participationId()).isEqualTo(10L);
        assertThat(reservations.getFirst().matchId()).isEqualTo(100L);
        assertThat(reservations.getFirst().nomSite()).isEqualTo("Padel Bruxelles");
        assertThat(reservations.getFirst().numeroTerrain()).isEqualTo("T1");
        assertThat(reservations.getFirst().statutParticipation())
                .isEqualTo(StatutParticipation.CONFIRMEE);
    }

    @Test
    void consulterReservations_shouldSortByMatchStartTime() {
        Membre membre = creerMembre(1L, "G1001");
        Participation plusTard = creerParticipation(
                11L,
                creerMatch(
                        101L,
                        LocalDateTime.of(2026, 8, 6, 18, 0)
                ),
                membre,
                StatutParticipation.CONFIRMEE
        );
        Participation plusTot = creerParticipation(
                10L,
                creerMatch(
                        100L,
                        LocalDateTime.of(2026, 8, 5, 9, 0)
                ),
                membre,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        when(membreRepository.findByMatricule("G1001"))
                .thenReturn(Optional.of(membre));
        when(participationRepository.findByMembreId(1L))
                .thenReturn(List.of(plusTard, plusTot));

        List<ReservationJoueurResponse> reservations =
                membreReservationService.consulterReservations("G1001");

        assertThat(reservations)
                .extracting(ReservationJoueurResponse::matchId)
                .containsExactly(100L, 101L);
    }

    @Test
    void consulterReservations_shouldExcludeReleasedParticipation() {
        Membre membre = creerMembre(1L, "G1001");
        Participation liberee = creerParticipation(
                10L,
                creerMatch(
                        100L,
                        LocalDateTime.of(2026, 8, 5, 9, 0)
                ),
                membre,
                StatutParticipation.LIBEREE
        );

        when(membreRepository.findByMatricule("G1001"))
                .thenReturn(Optional.of(membre));
        when(participationRepository.findByMembreId(1L))
                .thenReturn(List.of(liberee));

        assertThat(membreReservationService.consulterReservations("G1001"))
                .isEmpty();
    }

    @Test
    void consulterReservations_shouldExcludeParticipationWithoutMatch() {
        Membre membre = creerMembre(1L, "G1001");
        Participation sansMatch = creerParticipation(
                10L,
                null,
                membre,
                StatutParticipation.CONFIRMEE
        );

        when(membreRepository.findByMatricule("G1001"))
                .thenReturn(Optional.of(membre));
        when(participationRepository.findByMembreId(1L))
                .thenReturn(List.of(sansMatch));

        assertThat(membreReservationService.consulterReservations("G1001"))
                .isEmpty();
    }

    @Test
    void consulterReservations_shouldRejectBlankMatricule() {
        assertThatThrownBy(() ->
                membreReservationService.consulterReservations("   ")
        )
                .isInstanceOf(ConfigurationMetierException.class)
                .hasMessage("Le matricule est obligatoire.");

        verify(membreRepository, never()).findByMatricule("   ");
    }

    @Test
    void consulterReservations_shouldRejectUnknownMember() {
        when(membreRepository.findByMatricule("G9999"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                membreReservationService.consulterReservations("G9999")
        )
                .isInstanceOf(RessourceIntrouvableException.class)
                .hasMessage("Membre introuvable avec le matricule G9999");

        verify(participationRepository, never()).findByMembreId(1L);
    }

    private Membre creerMembre(Long id, String matricule) {
        Membre membre = Membre.builder()
                .matricule(matricule)
                .nom("Dupont")
                .prenom("Marie")
                .motDePasseHash("hash-test")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .soldeCredit(new BigDecimal("100.00"))
                .build();

        ReflectionTestUtils.setField(membre, "id", id);
        return membre;
    }

    private PadelMatch creerMatch(
            Long id,
            LocalDateTime dateHeureDebut
    ) {
        Site site = Site.builder()
                .code("BRU")
                .nom("Padel Bruxelles")
                .adresse("Rue du Padel 1")
                .actif(true)
                .build();
        ReflectionTestUtils.setField(site, "id", 1001L);

        Terrain terrain = Terrain.builder()
                .site(site)
                .numero("T1")
                .actif(true)
                .build();
        ReflectionTestUtils.setField(terrain, "id", 1101L);

        PadelMatch match = PadelMatch.builder()
                .terrain(terrain)
                .dateHeureDebut(dateHeureDebut)
                .dateHeureFin(dateHeureDebut.plusMinutes(90))
                .modeCreation(ModeCreation.PUBLIC)
                .visibiliteCourante(VisibiliteMatch.PUBLIC)
                .prixTotal(new BigDecimal("60.00"))
                .dateCreation(dateHeureDebut.minusDays(1))
                .etatCycle(EtatCycleMatch.A_VENIR)
                .build();

        ReflectionTestUtils.setField(match, "id", id);
        return match;
    }

    private Participation creerParticipation(
            Long id,
            PadelMatch match,
            Membre membre,
            StatutParticipation statut
    ) {
        Participation participation = Participation.builder()
                .match(match)
                .membre(membre)
                .roleParticipation(RoleParticipation.JOUEUR)
                .modeEntree(ModeEntreeParticipation.INSCRIPTION_PUBLIQUE)
                .statutParticipation(statut)
                .dateAffectation(LocalDateTime.of(2026, 8, 1, 10, 0))
                .build();

        ReflectionTestUtils.setField(participation, "id", id);
        return participation;
    }
}
