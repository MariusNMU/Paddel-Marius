package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.site.SiteReservationInfoResponse;
import com.padelMarius.backend.dto.site.TerrainReservationInfoResponse;
import com.padelMarius.backend.service.SiteReservationInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SiteReservationInfoController.class)
class SiteReservationInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SiteReservationInfoService siteReservationInfoService;

    @Test
    void listerSitesAvecInfosReservation_shouldReturnSites() throws Exception {
        SiteReservationInfoResponse response = new SiteReservationInfoResponse(
                1001L,
                "BRU",
                "Padel Bruxelles",
                LocalTime.of(8, 0),
                LocalTime.of(22, 0),
                List.of(
                        new TerrainReservationInfoResponse(1101L, "T1"),
                        new TerrainReservationInfoResponse(1103L, "T3")
                )
        );

        when(siteReservationInfoService.listerSitesAvecInfosReservation(2026))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/sites/reservation-infos")
                        .param("annee", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].siteId").value(1001))
                .andExpect(jsonPath("$[0].codeSite").value("BRU"))
                .andExpect(jsonPath("$[0].nomSite").value("Padel Bruxelles"))
                .andExpect(jsonPath("$[0].heureDebutReservation").value("08:00:00"))
                .andExpect(jsonPath("$[0].heureFinReservation").value("22:00:00"))
                .andExpect(jsonPath("$[0].terrains[1].numeroTerrain").value("T3"));
    }
}