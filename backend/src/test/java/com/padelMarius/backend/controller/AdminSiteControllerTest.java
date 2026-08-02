package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.site.SiteResponse;
import com.padelMarius.backend.service.SiteConsultationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminSiteController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminSiteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SiteConsultationService siteConsultationService;

    @Test
    void shouldReturnActiveAndInactiveSitesForGlobalAdmin() throws Exception {
        when(siteConsultationService.listerTousSites())
                .thenReturn(List.of(
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
                ));

        mockMvc.perform(get("/api/admin/sites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].siteId").value(1))
                .andExpect(jsonPath("$[0].nom").value("Site Alpha"))
                .andExpect(jsonPath("$[1].siteId").value(2))
                .andExpect(jsonPath("$[1].nom").value("Site Beta"));
    }
}
