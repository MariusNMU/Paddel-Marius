package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.membre.MembreResponse;
import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.service.MembreAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminMembreController.class)
class AdminMembreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MembreAdminService membreAdminService;

    @Test
    void listerMembres_shouldReturnAllMembers_whenNoSiteId() throws Exception {
        when(membreAdminService.listerTousLesMembres())
                .thenReturn(List.of(
                        new MembreResponse(
                                1L,
                                "G1001",
                                "Dupont",
                                "Marie",
                                CategorieMembre.GLOBAL,
                                null,
                                null,
                                true,
                                new BigDecimal("100.00")
                        ),
                        new MembreResponse(
                                2L,
                                "S1001",
                                "Martin",
                                "Sophie",
                                CategorieMembre.SITE,
                                1001L,
                                "Padel Bruxelles",
                                true,
                                new BigDecimal("100.00")
                        )
                ));

        mockMvc.perform(get("/api/admin/membres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matricule").value("G1001"))
                .andExpect(jsonPath("$[1].matricule").value("S1001"));
    }

    @Test
    void listerMembres_shouldReturnSiteMembers_whenSiteIdIsProvided() throws Exception {
        when(membreAdminService.listerMembresParSite(1001L))
                .thenReturn(List.of(
                        new MembreResponse(
                                2L,
                                "S1001",
                                "Martin",
                                "Sophie",
                                CategorieMembre.SITE,
                                1001L,
                                "Padel Bruxelles",
                                true,
                                new BigDecimal("100.00")
                        )
                ));

        mockMvc.perform(get("/api/admin/membres")
                        .param("siteId", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matricule").value("S1001"))
                .andExpect(jsonPath("$[0].siteRattachementId").value(1001));
    }
}