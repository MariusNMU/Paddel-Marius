package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.Site;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SiteRepositoryTest {

    @Autowired
    private SiteRepository siteRepository;

    @Test
    void shouldFindOnlyActiveSitesOrderedByName() {
        siteRepository.save(Site.builder()
                .code("BET")
                .nom("Site Beta")
                .adresse("Rue du Test 2")
                .actif(true)
                .build());

        siteRepository.save(Site.builder()
                .code("ALP")
                .nom("Site Alpha")
                .adresse("Rue du Test 1")
                .actif(true)
                .build());

        siteRepository.save(Site.builder()
                .code("GAM")
                .nom("Site Gamma")
                .adresse("Rue du Test 3")
                .actif(false)
                .build());

        List<Site> sites = siteRepository.findByActifTrueOrderByNomAsc();

        assertThat(sites)
                .extracting(Site::getNom)
                .containsExactly(
                        "Site Alpha",
                        "Site Beta"
                );
    }
}