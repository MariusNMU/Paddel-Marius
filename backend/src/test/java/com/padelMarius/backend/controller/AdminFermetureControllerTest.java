package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.fermeture.CreerFermetureRequest;
import com.padelMarius.backend.dto.fermeture.FermetureAdminResponse;
import com.padelMarius.backend.entity.PorteeFermeture;
import com.padelMarius.backend.exception.AuthentificationException;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.service.AdminAuthorizationService;
import com.padelMarius.backend.service.AdminFermetureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminFermetureController.class)
@Import(ApiExceptionHandler.class)
class AdminFermetureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminFermetureService adminFermetureService;

    @MockitoBean
    private AdminAuthorizationService adminAuthorizationService;

    @Test
    void creerFermeture_shouldReturnCreated() throws Exception {
        FermetureAdminResponse response = new FermetureAdminResponse(
                50L,
                LocalDate.of(2026, 7, 21),
                PorteeFermeture.GLOBALE,
                null,
                null,
                "Fermeture exceptionnelle",
                2,
                0,
                BigDecimal.ZERO
        );

        when(adminFermetureService.creerFermeture(any(CreerFermetureRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/admin/fermetures")
                        .header("X-Admin-Login", "admin-global")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dateFermeture": "2026-07-21",
                                  "portee": "GLOBALE",
                                  "siteId": null,
                                  "motif": "Fermeture exceptionnelle"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fermetureId").value(50))
                .andExpect(jsonPath("$.dateFermeture").value("2026-07-21"))
                .andExpect(jsonPath("$.portee").value("GLOBALE"))
                .andExpect(jsonPath("$.motif").value("Fermeture exceptionnelle"))
                .andExpect(jsonPath("$.nombreMatchesAnnules").value(2));
    }

    @Test
    void creerFermeture_shouldReturnBadRequest_whenDateIsMissing() throws Exception {
        mockMvc.perform(post("/api/admin/fermetures")
                        .header("X-Admin-Login", "admin-global")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portee": "GLOBALE",
                                  "siteId": null,
                                  "motif": "Fermeture exceptionnelle"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creerFermeture_shouldReturnConflict_whenBusinessRuleBlocksRequest() throws Exception {
        when(adminFermetureService.creerFermeture(any(CreerFermetureRequest.class)))
                .thenThrow(new ConfigurationMetierException(
                        "Une fermeture globale ne doit pas avoir de site."
                ));

        mockMvc.perform(post("/api/admin/fermetures")
                        .header("X-Admin-Login", "admin-global")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dateFermeture": "2026-07-21",
                                  "portee": "GLOBALE",
                                  "siteId": 1001,
                                  "motif": "Erreur"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFIGURATION_METIER_INVALIDE"))
                .andExpect(jsonPath("$.message").value(
                        "Une fermeture globale ne doit pas avoir de site."
                ));
    }

    @Test
    void creerFermeture_shouldReturn401_whenAdminHeaderIsMissing() throws Exception {
        org.mockito.Mockito.doThrow(new AuthentificationException(
                "Administrateur requis pour accéder à cette opération."
        )).when(adminAuthorizationService)
                .verifierAccesFermeture(null, PorteeFermeture.GLOBALE, null);

        mockMvc.perform(post("/api/admin/fermetures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dateFermeture": "2026-07-21",
                                  "portee": "GLOBALE",
                                  "siteId": null,
                                  "motif": "Fermeture exceptionnelle"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTIFICATION_INVALIDE"))
                .andExpect(jsonPath("$.message").value(
                        "Administrateur requis pour accéder à cette opération."
                ));
    }
}
