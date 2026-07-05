package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.site.SiteResponse;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.repository.SiteRepository;
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
class SiteConsultationServiceTest {

    @Mock
    private SiteRepository siteRepository;

    @InjectMocks
    private SiteConsultationService siteConsultationService;

    @Test
    void listerSitesActifs_shouldMapSitesToResponses() {
        Site site = Site.builder()
                .code("ALP")
                .nom("Site Alpha")
                .adresse("Rue du Test 1")
                .actif(true)
                .build();
        ReflectionTestUtils.setField(site, "id", 1L);

        when(siteRepository.findByActifTrueOrderByNomAsc())
                .thenReturn(List.of(site));

        List<SiteResponse> responses = siteConsultationService.listerSitesActifs();

        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).siteId());
        assertEquals("ALP", responses.get(0).code());
        assertEquals("Site Alpha", responses.get(0).nom());
        assertEquals("Rue du Test 1", responses.get(0).adresse());
    }
}