package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.terrain.TerrainResponse;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.Terrain;
import com.padelMarius.backend.repository.TerrainRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TerrainConsultationServiceTest {

    @Mock
    private TerrainRepository terrainRepository;

    @InjectMocks
    private TerrainConsultationService terrainConsultationService;

    @Test
    void listerTerrainsActifs_shouldMapTerrainsWithSiteInformation() {
        Site bruxelles = Site.builder()
                .code("BRU")
                .nom("Padel Bruxelles")
                .adresse("Rue du Test 1, 1000 Bruxelles")
                .actif(true)
                .build();
        ReflectionTestUtils.setField(bruxelles, "id", 1001L);

        Terrain terrain = Terrain.builder()
                .numero("T1")
                .actif(true)
                .site(bruxelles)
                .build();
        ReflectionTestUtils.setField(terrain, "id", 1101L);

        when(terrainRepository.findByActifTrueAndSiteActifTrueOrderBySiteNomAscNumeroAsc())
                .thenReturn(List.of(terrain));

        List<TerrainResponse> responses = terrainConsultationService.listerTerrainsActifs();

        assertEquals(1, responses.size());
        assertEquals(1101L, responses.get(0).terrainId());
        assertEquals("T1", responses.get(0).numeroTerrain());
        assertEquals(1001L, responses.get(0).siteId());
        assertEquals("Padel Bruxelles", responses.get(0).nomSite());
    }
}