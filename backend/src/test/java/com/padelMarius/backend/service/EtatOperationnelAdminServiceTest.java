package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.etatoperationnel.EtatOperationnelAdminResponse;
import com.padelMarius.backend.dto.etatoperationnel.EtatTerrainOperationnel;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.Fermeture;
import com.padelMarius.backend.entity.ModeCreation;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.PorteeFermeture;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.entity.Terrain;
import com.padelMarius.backend.entity.VisibiliteMatch;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.FermetureRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import com.padelMarius.backend.repository.SiteRepository;
import com.padelMarius.backend.repository.TerrainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EtatOperationnelAdminServiceTest {

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private TerrainRepository terrainRepository;

    @Mock
    private PadelMatchRepository padelMatchRepository;

    @Mock
    private FermetureRepository fermetureRepository;

    @Mock
    private ParticipationRepository participationRepository;

    private EtatOperationnelAdminService etatOperationnelAdminService;

    @BeforeEach
    void setUp() {
        etatOperationnelAdminService =
                new EtatOperationnelAdminService(
                        siteRepository,
                        terrainRepository,
                        padelMatchRepository,
                        fermetureRepository,
                        participationRepository
                );
    }

    @Test
    void consulterEtatOperationnel_shouldMapTerrainsMatchesAndParticipants() {
        LocalDate date = LocalDate.of(2026, 7, 20);
        Site site = creerSite(1001L, true);
        Terrain terrainT2 = creerTerrain(2002L, "T2", true, site);
        Terrain terrainT1 = creerTerrain(2001L, "T1", true, site);

        PadelMatch matchActif = creerMatch(
                3001L,
                terrainT1,
                LocalDateTime.of(2026, 7, 20, 10, 0),
                EtatCycleMatch.A_VENIR
        );

        PadelMatch matchAnnule = creerMatch(
                3002L,
                terrainT2,
                LocalDateTime.of(2026, 7, 20, 12, 0),
                EtatCycleMatch.ANNULE
        );

        when(siteRepository.findById(1001L))
                .thenReturn(Optional.of(site));

        when(terrainRepository.findBySiteId(1001L))
                .thenReturn(List.of(terrainT2, terrainT1));

        when(padelMatchRepository
                .findByTerrainInAndDateHeureDebutGreaterThanEqualAndDateHeureDebutBeforeOrderByDateHeureDebutAsc(
                        List.of(terrainT1, terrainT2),
                        date.atStartOfDay(),
                        date.plusDays(1).atStartOfDay()
                ))
                .thenReturn(List.of(matchActif, matchAnnule));

        when(fermetureRepository.findByDateFermeture(date))
                .thenReturn(List.of());

        when(participationRepository
                .countByMatchIdAndStatutParticipationNot(
                        3001L,
                        StatutParticipation.LIBEREE
                ))
                .thenReturn(3L);

        when(participationRepository
                .countByMatchIdAndStatutParticipationNot(
                        3002L,
                        StatutParticipation.LIBEREE
                ))
                .thenReturn(2L);

        EtatOperationnelAdminResponse response =
                etatOperationnelAdminService
                        .consulterEtatOperationnel(
                                date,
                                1001L
                        );

        assertThat(response.date()).isEqualTo(date);
        assertThat(response.siteId()).isEqualTo(1001L);
        assertThat(response.nomSite()).isEqualTo("Padel Bruxelles");
        assertThat(response.siteActif()).isTrue();
        assertThat(response.ferme()).isFalse();
        assertThat(response.motifFermeture()).isNull();

        assertThat(response.terrains())
                .extracting(terrain -> terrain.numeroTerrain())
                .containsExactly("T1", "T2");

        assertThat(response.terrains().getFirst().etatTerrain())
                .isEqualTo(EtatTerrainOperationnel.RESERVE);

        assertThat(response.terrains().getFirst().matches())
                .hasSize(1);

        assertThat(response.terrains().getFirst()
                .matches().getFirst().nombreParticipants())
                .isEqualTo(3);

        assertThat(response.terrains().get(1).etatTerrain())
                .isEqualTo(EtatTerrainOperationnel.DISPONIBLE);

        assertThat(response.terrains().get(1)
                .matches().getFirst().etatCycle())
                .isEqualTo(EtatCycleMatch.ANNULE);
    }

    @Test
    void consulterEtatOperationnel_shouldApplyLocalClosureAndKeepInactiveTerrain() {
        LocalDate date = LocalDate.of(2026, 8, 15);
        Site site = creerSite(1001L, true);
        Terrain terrainActif = creerTerrain(2001L, "T1", true, site);
        Terrain terrainInactif = creerTerrain(2002L, "T2", false, site);

        Fermeture fermeture = Fermeture.builder()
                .dateFermeture(date)
                .portee(PorteeFermeture.LOCALE)
                .site(site)
                .motif("Entretien annuel")
                .build();

        when(siteRepository.findById(1001L))
                .thenReturn(Optional.of(site));

        when(terrainRepository.findBySiteId(1001L))
                .thenReturn(List.of(terrainActif, terrainInactif));

        when(padelMatchRepository
                .findByTerrainInAndDateHeureDebutGreaterThanEqualAndDateHeureDebutBeforeOrderByDateHeureDebutAsc(
                        List.of(terrainActif, terrainInactif),
                        date.atStartOfDay(),
                        date.plusDays(1).atStartOfDay()
                ))
                .thenReturn(List.of());

        when(fermetureRepository.findByDateFermeture(date))
                .thenReturn(List.of(fermeture));

        EtatOperationnelAdminResponse response =
                etatOperationnelAdminService
                        .consulterEtatOperationnel(
                                date,
                                1001L
                        );

        assertThat(response.ferme()).isTrue();
        assertThat(response.motifFermeture())
                .isEqualTo("Entretien annuel");
        assertThat(response.terrains().getFirst().etatTerrain())
                .isEqualTo(EtatTerrainOperationnel.FERME);
        assertThat(response.terrains().get(1).etatTerrain())
                .isEqualTo(EtatTerrainOperationnel.INACTIF);
    }

    @Test
    void consulterEtatOperationnel_shouldRejectUnknownSite() {
        when(siteRepository.findById(999L))
                .thenReturn(Optional.empty());

        RessourceIntrouvableException exception = assertThrows(
                RessourceIntrouvableException.class,
                () -> etatOperationnelAdminService
                        .consulterEtatOperationnel(
                                LocalDate.of(2026, 7, 20),
                                999L
                        )
        );

        assertThat(exception.getMessage())
                .isEqualTo("Site introuvable avec l'id 999");

        verify(siteRepository).findById(999L);
        verifyNoInteractions(terrainRepository);
        verifyNoInteractions(padelMatchRepository);
        verifyNoInteractions(fermetureRepository);
        verifyNoInteractions(participationRepository);
    }

    private Site creerSite(
            Long id,
            boolean actif
    ) {
        Site site = Site.builder()
                .code("BRU")
                .nom("Padel Bruxelles")
                .adresse("Rue du Padel 1")
                .actif(actif)
                .build();

        ReflectionTestUtils.setField(site, "id", id);

        return site;
    }

    private Terrain creerTerrain(
            Long id,
            String numero,
            boolean actif,
            Site site
    ) {
        Terrain terrain = Terrain.builder()
                .numero(numero)
                .actif(actif)
                .site(site)
                .build();

        ReflectionTestUtils.setField(terrain, "id", id);

        return terrain;
    }

    private PadelMatch creerMatch(
            Long id,
            Terrain terrain,
            LocalDateTime debut,
            EtatCycleMatch etatCycle
    ) {
        PadelMatch match = PadelMatch.builder()
                .terrain(terrain)
                .dateHeureDebut(debut)
                .dateHeureFin(debut.plusMinutes(90))
                .modeCreation(ModeCreation.PUBLIC)
                .visibiliteCourante(VisibiliteMatch.PUBLIC)
                .prixTotal(new BigDecimal("60.00"))
                .dateCreation(debut.minusDays(5))
                .etatCycle(etatCycle)
                .build();

        ReflectionTestUtils.setField(match, "id", id);

        return match;
    }
}
