package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.ModeCreation;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.Terrain;
import com.padelMarius.backend.entity.VisibiliteMatch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class PadelMatchRepositoryTest {

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private TerrainRepository terrainRepository;

    @Autowired
    private PadelMatchRepository padelMatchRepository;

    @Test
    void shouldFindMatchesInDateRangeForPreMatchProcessing() {
        Site site = creerSite("RNG");
        Terrain terrain = creerTerrain(site, "T1");

        PadelMatch matchDebutInclus = creerMatch(
                terrain,
                LocalDateTime.of(2026, 5, 20, 0, 0)
        );

        PadelMatch matchJourInclus = creerMatch(
                terrain,
                LocalDateTime.of(2026, 5, 20, 9, 0)
        );

        PadelMatch matchAvantPeriode = creerMatch(
                terrain,
                LocalDateTime.of(2026, 5, 19, 23, 59)
        );

        PadelMatch matchFinExclusive = creerMatch(
                terrain,
                LocalDateTime.of(2026, 5, 21, 0, 0)
        );

        LocalDateTime debutPeriode = LocalDateTime.of(2026, 5, 20, 0, 0);
        LocalDateTime finPeriodeExclusive = LocalDateTime.of(2026, 5, 21, 0, 0);

        List<PadelMatch> matchesTrouves =
                padelMatchRepository.findByDateHeureDebutGreaterThanEqualAndDateHeureDebutBefore(
                        debutPeriode,
                        finPeriodeExclusive
                );

        assertThat(matchesTrouves)
                .extracting(PadelMatch::getId)
                .containsExactlyInAnyOrder(
                        matchDebutInclus.getId(),
                        matchJourInclus.getId()
                );

        assertThat(matchesTrouves)
                .extracting(PadelMatch::getId)
                .doesNotContain(
                        matchAvantPeriode.getId(),
                        matchFinExclusive.getId()
                );
    }

    @Test
    void findPourFermetureForUpdate_shouldReturnOnlyFutureMatchesForSelectedTerrains() {
        Site site = siteRepository.save(Site.builder()
                .code("TEST-FERM")
                .nom("Site fermeture")
                .adresse("Adresse fermeture")
                .actif(true)
                .build());

        Terrain terrainConcerne = terrainRepository.save(Terrain.builder()
                .site(site)
                .numero("T-FERM-1")
                .actif(true)
                .build());

        Terrain autreTerrain = terrainRepository.save(Terrain.builder()
                .site(site)
                .numero("T-FERM-2")
                .actif(true)
                .build());

        LocalDateTime dateFermeture = LocalDateTime.of(2026, 8, 15, 9, 0);

        PadelMatch matchConcerne = padelMatchRepository.save(creerMatch(
                terrainConcerne,
                dateFermeture,
                EtatCycleMatch.A_VENIR
        ));

        padelMatchRepository.save(creerMatch(
                autreTerrain,
                dateFermeture,
                EtatCycleMatch.A_VENIR
        ));

        padelMatchRepository.save(creerMatch(
                terrainConcerne,
                LocalDateTime.of(2026, 8, 16, 9, 0),
                EtatCycleMatch.A_VENIR
        ));

        padelMatchRepository.save(creerMatch(
                terrainConcerne,
                dateFermeture.plusHours(2),
                EtatCycleMatch.TERMINE
        ));

        List<PadelMatch> resultats = padelMatchRepository
                .findPourFermetureForUpdate(
                        List.of(terrainConcerne),
                        LocalDateTime.of(2026, 8, 15, 0, 0),
                        LocalDateTime.of(2026, 8, 16, 0, 0),
                        EtatCycleMatch.A_VENIR
                );

        assertEquals(1, resultats.size());
        assertEquals(matchConcerne.getId(), resultats.getFirst().getId());
    }

    @Test
    void shouldFindSiteMatchesForOneDayOrderedByStartTime() {
        Site site = creerSite("ETAT");
        Terrain terrainT1 = creerTerrain(site, "T1");
        Terrain terrainT2 = creerTerrain(site, "T2");
        Terrain autreTerrain = creerTerrain(site, "T3");

        PadelMatch matchMidi = creerMatch(
                terrainT2,
                LocalDateTime.of(2026, 7, 20, 12, 0)
        );

        PadelMatch matchMatin = creerMatch(
                terrainT1,
                LocalDateTime.of(2026, 7, 20, 9, 0)
        );

        creerMatch(
                autreTerrain,
                LocalDateTime.of(2026, 7, 20, 10, 0)
        );

        creerMatch(
                terrainT1,
                LocalDateTime.of(2026, 7, 21, 9, 0)
        );

        List<PadelMatch> matchesTrouves = padelMatchRepository
                .findByTerrainInAndDateHeureDebutGreaterThanEqualAndDateHeureDebutBeforeOrderByDateHeureDebutAsc(
                        List.of(terrainT1, terrainT2),
                        LocalDateTime.of(2026, 7, 20, 0, 0),
                        LocalDateTime.of(2026, 7, 21, 0, 0)
                );

        assertThat(matchesTrouves)
                .extracting(PadelMatch::getId)
                .containsExactly(
                        matchMatin.getId(),
                        matchMidi.getId()
                );
    }

    private Site creerSite(String code) {
        return siteRepository.save(Site.builder()
                .code(code)
                .nom("Padel " + code)
                .adresse("Rue du Test 1, 1000 Bruxelles")
                .actif(true)
                .build());
    }

    private Terrain creerTerrain(Site site, String numero) {
        return terrainRepository.save(Terrain.builder()
                .site(site)
                .numero(numero)
                .actif(true)
                .build());
    }

    private PadelMatch creerMatch(Terrain terrain, LocalDateTime debut) {
        return padelMatchRepository.save(PadelMatch.builder()
                .terrain(terrain)
                .dateHeureDebut(debut)
                .dateHeureFin(debut.plusMinutes(90))
                .modeCreation(ModeCreation.PRIVE)
                .visibiliteCourante(VisibiliteMatch.PRIVE)
                .prixTotal(new BigDecimal("60.00"))
                .dateCreation(LocalDateTime.of(2026, 5, 1, 10, 0))
                .etatCycle(EtatCycleMatch.A_VENIR)
                .build());
    }

    private PadelMatch creerMatch(
            Terrain terrain,
            LocalDateTime dateHeureDebut,
            EtatCycleMatch etatCycle
    ) {
        return PadelMatch.builder()
                .terrain(terrain)
                .dateHeureDebut(dateHeureDebut)
                .dateHeureFin(dateHeureDebut.plusMinutes(90))
                .modeCreation(ModeCreation.PUBLIC)
                .visibiliteCourante(VisibiliteMatch.PUBLIC)
                .prixTotal(new BigDecimal("60.00"))
                .dateCreation(LocalDateTime.of(2026, 5, 1, 10, 0))
                .etatCycle(etatCycle)
                .build();
    }
}
