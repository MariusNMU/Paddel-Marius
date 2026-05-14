package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.traitement.TraitementEcheanceResponse;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.repository.DetteRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.PaiementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraitementEcheanceServiceTest {

    @Mock
    private PadelMatchRepository padelMatchRepository;

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private DetteRepository detteRepository;

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
                detteService,
                clock
        );
    }

    @Test
    void shouldCreateDebtWhenMatchReachedStartTimeAndIsNotFullyPaid() {
        PadelMatch match = PadelMatch.builder()
                .dateHeureDebut(LocalDateTime.of(2026, 5, 14, 11, 0))
                .prixTotal(new BigDecimal("60.00"))
                .etatCycle(EtatCycleMatch.A_VENIR)
                .build();

        org.springframework.test.util.ReflectionTestUtils.setField(match, "id", 100L);

        when(padelMatchRepository.findByEtatCycleAndDateHeureDebutLessThanEqual(
                eq(EtatCycleMatch.A_VENIR),
                any(LocalDateTime.class)
        )).thenReturn(List.of(match));

        when(detteRepository.findByMatchId(100L)).thenReturn(Optional.empty());
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
    }
}
