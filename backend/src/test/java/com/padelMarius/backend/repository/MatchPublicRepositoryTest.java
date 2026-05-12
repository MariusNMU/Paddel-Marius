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

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class MatchPublicRepositoryTest {

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private TerrainRepository terrainRepository;

    @Autowired
    private PadelMatchRepository padelMatchRepository;

    @Test
    void findByVisibiliteCouranteAndEtatCycleAndDateRange_shouldReturnOnlyPublicFutureMatches() {
        Site site = siteRepository.save(Site.builder()
                .code("PUB")
                .nom("Padel Public")
                .adresse("Adresse public")
                .actif(true)
                .build());

        Terrain terrain = terrainRepository.save(Terrain.builder()
                .site(site)
                .numero("T-PUB")
                .actif(true)
                .build());

        PadelMatch matchPublicAVenir = padelMatchRepository.save(creerMatch(
                terrain,
                LocalDateTime.of(2026, 6, 20, 9, 0),
                VisibiliteMatch.PUBLIC,
                EtatCycleMatch.A_VENIR
        ));

        padelMatchRepository.save(creerMatch(
                terrain,
                LocalDateTime.of(2026, 6, 20, 11, 0),
                VisibiliteMatch.PRIVE,
                EtatCycleMatch.A_VENIR
        ));

        padelMatchRepository.save(creerMatch(
                terrain,
                LocalDateTime.of(2026, 6, 20, 13, 0),
                VisibiliteMatch.PUBLIC,
                EtatCycleMatch.TERMINE
        ));

        List<PadelMatch> resultats = padelMatchRepository
                .findByVisibiliteCouranteAndEtatCycleAndDateHeureDebutGreaterThanEqualAndDateHeureDebutBefore(
                        VisibiliteMatch.PUBLIC,
                        EtatCycleMatch.A_VENIR,
                        LocalDateTime.of(2026, 6, 20, 0, 0),
                        LocalDateTime.of(2026, 6, 21, 0, 0)
                );

        assertEquals(1, resultats.size());
        assertEquals(matchPublicAVenir.getId(), resultats.getFirst().getId());
    }

    private PadelMatch creerMatch(
            Terrain terrain,
            LocalDateTime dateHeureDebut,
            VisibiliteMatch visibiliteMatch,
            EtatCycleMatch etatCycleMatch
    ) {
        return PadelMatch.builder()
                .terrain(terrain)
                .dateHeureDebut(dateHeureDebut)
                .dateHeureFin(dateHeureDebut.plusMinutes(90))
                .modeCreation(ModeCreation.PUBLIC)
                .visibiliteCourante(visibiliteMatch)
                .prixTotal(new BigDecimal("60.00"))
                .dateCreation(LocalDateTime.of(2026, 5, 1, 10, 0))
                .etatCycle(etatCycleMatch)
                .build();
    }
}