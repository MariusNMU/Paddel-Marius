package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.traitement.TraitementEcheanceResponse;
import com.padelMarius.backend.entity.Dette;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.ModeCreation;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.Participation;
import com.padelMarius.backend.entity.Penalite;
import com.padelMarius.backend.entity.RoleParticipation;
import com.padelMarius.backend.entity.StatutDette;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.entity.StatutPenalite;
import com.padelMarius.backend.repository.DetteRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.PaiementRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import com.padelMarius.backend.repository.PenaliteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraitementEcheanceServiceTest {

    @Mock
    private PadelMatchRepository padelMatchRepository;

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private DetteRepository detteRepository;

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private PenaliteRepository penaliteRepository;

    @Mock
    private DetteService detteService;

    @Mock
    private Clock clock;

    private TraitementEcheanceService service;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(Instant.parse("2026-05-14T10:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("Europe/Brussels"));

        service = new TraitementEcheanceService(
                padelMatchRepository,
                paiementRepository,
                detteRepository,
                participationRepository,
                penaliteRepository,
                detteService,
                clock
        );
    }

    @Test
    void shouldCreateDebtWhenMatchReachedStartTimeAndIsNotFullyPaid() {
        PadelMatch match = creerMatch(100L, ModeCreation.PUBLIC, new BigDecimal("60.00"));

        stubRechercheMatchesArrivesAEcheance(match);
        when(detteRepository.findByMatchIdForUpdate(100L)).thenReturn(Optional.empty());
        when(paiementRepository.findByParticipation_Match_IdAndNaturePaiementAndStatutPaiement(
                eq(100L),
                any(),
                any()
        )).thenReturn(List.of());

        TraitementEcheanceResponse response = service.traiterMatchesArrivesAEcheance();

        assertEquals(1, response.matchesAnalyses());
        assertEquals(1, response.matchesDemarres());
        assertEquals(1, response.dettesCreees());
        assertEquals(EtatCycleMatch.DEMARRE, match.getEtatCycle());

        verify(detteService).genererDettePourMatch(100L);
        verify(padelMatchRepository).save(match);
        verifyNoInteractions(participationRepository);
        verifyNoInteractions(penaliteRepository);
    }

    @Test
    void traiterMatchesArrivesAEcheance_shouldFinishStartedMatchesWhenEndTimeIsReached() {
        PadelMatch matchDemarre = PadelMatch.builder()
                .dateHeureDebut(LocalDateTime.of(2026, 5, 14, 8, 0))
                .dateHeureFin(LocalDateTime.of(2026, 5, 14, 9, 30))
                .prixTotal(new BigDecimal("60.00"))
                .modeCreation(ModeCreation.PUBLIC)
                .etatCycle(EtatCycleMatch.DEMARRE)
                .build();

        ReflectionTestUtils.setField(matchDemarre, "id", 200L);

        when(padelMatchRepository.findArrivesAEcheanceForUpdate(
                eq(EtatCycleMatch.A_VENIR),
                any(LocalDateTime.class)
        )).thenReturn(List.of());

        when(padelMatchRepository.findATerminerForUpdate(
                eq(EtatCycleMatch.DEMARRE),
                any(LocalDateTime.class)
        )).thenReturn(List.of(matchDemarre));

        TraitementEcheanceResponse response = service.traiterMatchesArrivesAEcheance();

        assertEquals(1, response.matchesAnalyses());
        assertEquals(0, response.matchesDemarres());
        assertEquals(0, response.dettesCreees());
        assertEquals(EtatCycleMatch.TERMINE, matchDemarre.getEtatCycle());

        verify(padelMatchRepository).save(matchDemarre);
        verifyNoInteractions(detteService);
        verifyNoInteractions(participationRepository);
        verifyNoInteractions(penaliteRepository);
    }

    @Test
    void traiterMatchesArrivesAEcheance_shouldCreatePenaltyForOrganizerWhenPrivateOriginMatchStillIncomplete() {
        PadelMatch match = creerMatch(100L, ModeCreation.PRIVE, BigDecimal.ZERO);
        Membre organisateur = creerMembre(20L, "G0001");
        Membre joueur = creerMembre(21L, "G0002");

        Participation participationOrganisateur = creerParticipation(
                match,
                organisateur,
                RoleParticipation.ORGANISATEUR,
                StatutParticipation.CONFIRMEE
        );
        Participation participationJoueur = creerParticipation(
                match,
                joueur,
                RoleParticipation.JOUEUR,
                StatutParticipation.CONFIRMEE
        );

        stubRechercheMatchesArrivesAEcheance(match);
        stubDetteNonCreee(100L);
        when(participationRepository.findByMatchId(100L))
                .thenReturn(List.of(participationOrganisateur, participationJoueur));
        when(penaliteRepository.findByMatchSourceId(100L)).thenReturn(List.of());

        service.traiterMatchesArrivesAEcheance();

        ArgumentCaptor<Penalite> penaliteCaptor = ArgumentCaptor.forClass(Penalite.class);
        verify(penaliteRepository).save(penaliteCaptor.capture());

        Penalite penalite = penaliteCaptor.getValue();
        LocalDateTime maintenant = LocalDateTime.of(2026, 5, 14, 12, 0);

        assertEquals(organisateur, penalite.getMembre());
        assertEquals(match, penalite.getMatchSource());
        assertEquals("RESERVATION_PRIVEE_INCOMPLETE", penalite.getTypePenalite());
        assertEquals("Match privé incomplet au moment du match.", penalite.getMotif());
        assertEquals(maintenant, penalite.getDateDebut());
        assertEquals(maintenant.plusDays(7), penalite.getDateFin());
        assertEquals(StatutPenalite.ACTIVE, penalite.getStatutPenalite());
    }

    @Test
    void traiterMatchesArrivesAEcheance_shouldNotCreatePenaltyWhenPrivateOriginMatchHasFourConfirmedPlayers() {
        PadelMatch match = creerMatch(100L, ModeCreation.PRIVE, BigDecimal.ZERO);

        List<Participation> participations = List.of(
                creerParticipation(match, creerMembre(20L, "G0001"), RoleParticipation.ORGANISATEUR, StatutParticipation.CONFIRMEE),
                creerParticipation(match, creerMembre(21L, "G0002"), RoleParticipation.JOUEUR, StatutParticipation.CONFIRMEE),
                creerParticipation(match, creerMembre(22L, "G0003"), RoleParticipation.JOUEUR, StatutParticipation.CONFIRMEE),
                creerParticipation(match, creerMembre(23L, "G0004"), RoleParticipation.JOUEUR, StatutParticipation.CONFIRMEE)
        );

        stubRechercheMatchesArrivesAEcheance(match);
        stubDetteNonCreee(100L);
        when(participationRepository.findByMatchId(100L)).thenReturn(participations);

        service.traiterMatchesArrivesAEcheance();

        verify(penaliteRepository, never()).save(any(Penalite.class));
    }

    @Test
    void traiterMatchesArrivesAEcheance_shouldNotCreatePenaltyForPublicOriginMatch() {
        PadelMatch match = creerMatch(100L, ModeCreation.PUBLIC, BigDecimal.ZERO);

        stubRechercheMatchesArrivesAEcheance(match);
        stubDetteNonCreee(100L);

        service.traiterMatchesArrivesAEcheance();

        verifyNoInteractions(participationRepository);
        verifyNoInteractions(penaliteRepository);
    }

    @Test
    void traitement_echeance_doit_ignorer_une_dette_deja_existante() {
        PadelMatch match = creerMatch(
                100L,
                ModeCreation.PUBLIC,
                new BigDecimal("60.00")
        );

        Membre responsable = creerMembre(20L, "G0001");

        Dette detteExistante = Dette.builder()
                .match(match)
                .membreResponsable(responsable)
                .montantInitial(new BigDecimal("60.00"))
                .montantRestant(BigDecimal.ZERO)
                .dateCreation(LocalDateTime.of(2026, 5, 13, 12, 0))
                .dateReglement(LocalDateTime.of(2026, 5, 14, 9, 0))
                .statutDette(StatutDette.REGLEE)
                .build();

        stubRechercheMatchesArrivesAEcheance(match);

        when(detteRepository.findByMatchIdForUpdate(100L))
                .thenReturn(Optional.of(detteExistante));

        TraitementEcheanceResponse response =
                service.traiterMatchesArrivesAEcheance();

        assertEquals(0, response.dettesCreees());
        assertEquals(EtatCycleMatch.DEMARRE, match.getEtatCycle());

        verifyNoInteractions(detteService);
    }

    private void stubRechercheMatchesArrivesAEcheance(PadelMatch match) {
        when(padelMatchRepository.findArrivesAEcheanceForUpdate(
                eq(EtatCycleMatch.A_VENIR),
                any(LocalDateTime.class)
        )).thenReturn(List.of(match));

        when(padelMatchRepository.findATerminerForUpdate(
                eq(EtatCycleMatch.DEMARRE),
                any(LocalDateTime.class)
        )).thenReturn(List.of());
    }

    private void stubDetteNonCreee(Long matchId) {
        when(detteRepository.findByMatchIdForUpdate(matchId)).thenReturn(Optional.empty());
        when(paiementRepository.findByParticipation_Match_IdAndNaturePaiementAndStatutPaiement(
                eq(matchId),
                any(),
                any()
        )).thenReturn(List.of());
    }

    private PadelMatch creerMatch(Long id, ModeCreation modeCreation, BigDecimal prixTotal) {
        PadelMatch match = PadelMatch.builder()
                .dateHeureDebut(LocalDateTime.of(2026, 5, 14, 11, 0))
                .dateHeureFin(LocalDateTime.of(2026, 5, 14, 12, 30))
                .prixTotal(prixTotal)
                .modeCreation(modeCreation)
                .etatCycle(EtatCycleMatch.A_VENIR)
                .build();

        ReflectionTestUtils.setField(match, "id", id);

        return match;
    }

    private Membre creerMembre(Long id, String matricule) {
        Membre membre = Membre.builder()
                .matricule(matricule)
                .nom("Nom " + matricule)
                .prenom("Prenom " + matricule)
                .motDePasseHash("$2y$10$w7Hmtss9GA8U9RAxfZeb3.JmBalmCw64iEo6pY5YEgNky9FM7OriK")
                .actif(true)
                .build();

        ReflectionTestUtils.setField(membre, "id", id);

        return membre;
    }

    private Participation creerParticipation(
            PadelMatch match,
            Membre membre,
            RoleParticipation roleParticipation,
            StatutParticipation statutParticipation
    ) {
        return Participation.builder()
                .match(match)
                .membre(membre)
                .roleParticipation(roleParticipation)
                .statutParticipation(statutParticipation)
                .build();
    }
}
