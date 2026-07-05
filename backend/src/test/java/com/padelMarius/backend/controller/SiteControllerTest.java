package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.site.SiteResponse;
import com.padelMarius.backend.service.SiteConsultationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SiteController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class SiteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SiteConsultationService siteConsultationService;

    @Test
    void shouldReturnActiveSites() throws Exception {
        List<SiteResponse> response = List.of(
                new SiteResponse(
                        1L,
                        "ALP",
                        "Site Alpha",
                        "Rue du Test 1"
                ),
                new SiteResponse(
                        2L,
                        "BET",
                        "Site Beta",
                        "Rue du Test 2"
                )
        );

        when(siteConsultationService.listerSitesActifs())
                .thenReturn(response);

        mockMvc.perform(get("/api/sites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].siteId").value(1))
                .andExpect(jsonPath("$[0].code").value("ALP"))
                .andExpect(jsonPath("$[0].nom").value("Site Alpha"))
                .andExpect(jsonPath("$[0].adresse").value("Rue du Test 1"))
                .andExpect(jsonPath("$[1].siteId").value(2))
                .andExpect(jsonPath("$[1].code").value("BET"))
                .andExpect(jsonPath("$[1].nom").value("Site Beta"))
                .andExpect(jsonPath("$[1].adresse").value("Rue du Test 2"));
    }

    @Test
    void shouldReturnEmptyListWhenNoActiveSiteExists() throws Exception {
        when(siteConsultationService.listerSitesActifs())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/sites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
