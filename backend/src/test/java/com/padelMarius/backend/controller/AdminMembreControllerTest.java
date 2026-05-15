package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.admin.MembreAdminResponse;
import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.service.AdminMembreService;
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
    private AdminMembreService adminMembreService;

    @Test
    void listerTousLesMembres_shouldReturnMembers() throws Exception {
        MembreAdminResponse response = new MembreAdminResponse(
                2001L,
                "G1001",
                "Dupont",
                "Marie",
                CategorieMembre.GLOBAL,
                null,
                null,
                true,
                new BigDecimal("100.00")
        );

        when(adminMembreService.listerTousLesMembres())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/admin/membres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].membreId").value(2001))
                .andExpect(jsonPath("$[0].matricule").value("G1001"))
                .andExpect(jsonPath("$[0].categorieMembre").value("GLOBAL"))
                .andExpect(jsonPath("$[0].actif").value(true));
    }

    @Test
    void listerMembresParSite_shouldReturnMembersForSite() throws Exception {
        MembreAdminResponse response = new MembreAdminResponse(
                2004L,
                "S1002",
                "Bernard",
                "Luc",
                CategorieMembre.SITE,
                1002L,
                "Padel Namur",
                true,
                new BigDecimal("100.00")
        );

        when(adminMembreService.listerMembresParSite(1002L))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/admin/sites/1002/membres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].membreId").value(2004))
                .andExpect(jsonPath("$[0].matricule").value("S1002"))
                .andExpect(jsonPath("$[0].siteRattachementId").value(1002))
                .andExpect(jsonPath("$[0].nomSiteRattachement").value("Padel Namur"));
    }
}