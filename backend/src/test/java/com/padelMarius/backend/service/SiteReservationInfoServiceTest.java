package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.site.SiteReservationInfoResponse;
import com.padelMarius.backend.entity.HoraireAnnuelSite;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.Terrain;
import com.padelMarius.backend.repository.HoraireAnnuelSiteRepository;
import com.padelMarius.backend.repository.SiteRepository;
import com.padelMarius.backend.repository.TerrainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SiteReservationInfoServiceTest {

    private SiteRepository siteRepository;
    private TerrainRepository terrainRepository;
    private HoraireAnnuelSiteRepository horaireAnnuelSiteRepository;
    private SiteReservationInfoService service;

    @BeforeEach
    void setUp() {
        siteRepository = mock(SiteRepository.class);
        terrainRepository = mock(TerrainRepository.class);
        horaireAnnuelSiteRepository = mock(HoraireAnnuelSiteRepository.class);

        Clock clock = Clock.fixed(
                LocalDate.of(2026, 5, 14)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant(),
                ZoneId.systemDefault()
        );

        service = new SiteReservationInfoService(
                siteRepository,
                terrainRepository,
                horaireAnnuelSiteRepository,
                clock
        );
    }

    @Test
    void listerSitesAvecInfosReservation_shouldReturnActiveSitesWithHoursAndTerrains() {
        Site bruxelles = creerSite(1001L, "BRU", "Padel Bruxelles");
        Terrain t1 = creerTerrain(1101L, "T1", bruxelles);
        Terrain t3 = creerTerrain(1103L, "T3", bruxelles);

        HoraireAnnuelSite horaire = HoraireAnnuelSite.builder()
                .site(bruxelles)
                .anneeCivile(2026)
                .heureDebutReservation(LocalTime.of(8, 0))
                .heureFinReservation(LocalTime.of(22, 0))
                .build();

        when(siteRepository.findAll(any(Sort.class))).thenReturn(List.of(bruxelles));
        when(horaireAnnuelSiteRepository.findBySiteAndAnneeCivile(bruxelles, 2026))
                .thenReturn(Optional.of(horaire));
        when(terrainRepository.findBySiteAndActifTrue(bruxelles))
                .thenReturn(List.of(t3, t1));

        List<SiteReservationInfoResponse> resultats =
                service.listerSitesAvecInfosReservation(2026);

        assertEquals(1, resultats.size());
        assertEquals(1001L, resultats.getFirst().siteId());
        assertEquals("Padel Bruxelles", resultats.getFirst().nomSite());
        assertEquals(LocalTime.of(8, 0), resultats.getFirst().heureDebutReservation());
        assertEquals(LocalTime.of(22, 0), resultats.getFirst().heureFinReservation());
        assertEquals(2, resultats.getFirst().terrains().size());
        assertEquals("T1", resultats.getFirst().terrains().getFirst().numeroTerrain());

        verify(siteRepository).findAll(any(Sort.class));
    }

    private Site creerSite(Long id, String code, String nom) {
        Site site = Site.builder()
                .code(code)
                .nom(nom)
                .adresse("Adresse " + nom)
                .actif(true)
                .build();

        ReflectionTestUtils.setField(site, "id", id);
        return site;
    }

    private Terrain creerTerrain(Long id, String numero, Site site) {
        Terrain terrain = Terrain.builder()
                .numero(numero)
                .site(site)
                .actif(true)
                .build();

        ReflectionTestUtils.setField(terrain, "id", id);
        return terrain;
    }
}