package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.Terrain;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TerrainRepositoryTest {

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private TerrainRepository terrainRepository;

    @Test
    void shouldFindOnlyActiveTerrainsOnActiveSitesOrderedBySiteNameThenNumero() {
        Site bruxelles = siteRepository.save(Site.builder()
                .code("BRU")
                .nom("Padel Bruxelles")
                .adresse("Rue du Test 1, 1000 Bruxelles")
                .actif(true)
                .build());

        Site namur = siteRepository.save(Site.builder()
                .code("NAM")
                .nom("Padel Namur")
                .adresse("Rue du Test 2, 5000 Namur")
                .actif(true)
                .build());

        Site liegeInactif = siteRepository.save(Site.builder()
                .code("LIE")
                .nom("Padel Liège")
                .adresse("Rue du Test 3, 4000 Liège")
                .actif(false)
                .build());

        terrainRepository.save(Terrain.builder()
                .numero("T2")
                .actif(true)
                .site(bruxelles)
                .build());

        terrainRepository.save(Terrain.builder()
                .numero("T1")
                .actif(true)
                .site(bruxelles)
                .build());

        terrainRepository.save(Terrain.builder()
                .numero("T3")
                .actif(false)
                .site(bruxelles)
                .build());

        terrainRepository.save(Terrain.builder()
                .numero("T1")
                .actif(true)
                .site(namur)
                .build());

        terrainRepository.save(Terrain.builder()
                .numero("T1")
                .actif(true)
                .site(liegeInactif)
                .build());

        List<Terrain> terrains = terrainRepository
                .findByActifTrueAndSiteActifTrueOrderBySiteNomAscNumeroAsc();

        assertThat(terrains)
                .extracting(terrain -> terrain.getSite().getNom() + " - " + terrain.getNumero())
                .containsExactly(
                        "Padel Bruxelles - T1",
                        "Padel Bruxelles - T2",
                        "Padel Namur - T1"
                );
    }
}