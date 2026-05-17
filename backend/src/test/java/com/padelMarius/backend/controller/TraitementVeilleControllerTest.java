package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.traitement.TraitementVeilleResponse;
import com.padelMarius.backend.exception.AuthentificationException;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.service.AdminAuthorizationService;
import com.padelMarius.backend.service.TraitementVeilleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TraitementVeilleController.class)
@Import(ApiExceptionHandler.class)
class TraitementVeilleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TraitementVeilleService traitementVeilleService;

    @MockitoBean
    private AdminAuthorizationService adminAuthorizationService;

    @Test
    void shouldReturnOk_whenPreMatchProcessingRuns() throws Exception {
        TraitementVeilleResponse response = new TraitementVeilleResponse(
                LocalDate.of(2026, 5, 19),
                LocalDate.of(2026, 5, 20),
                3,
                1,
                2,
                1
        );

        when(traitementVeilleService.traiterVeille(LocalDate.of(2026, 5, 19)))
                .thenReturn(response);

        mockMvc.perform(post("/api/admin/matches/traitement-veille")
                        .header("X-Admin-Login", "admin-global")
                        .param("date", "2026-05-19"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateTraitement").value("2026-05-19"))
                .andExpect(jsonPath("$.dateMatchTraitee").value("2026-05-20"))
                .andExpect(jsonPath("$.matchesAnalyses").value(3))
                .andExpect(jsonPath("$.matchesPassesPublics").value(1))
                .andExpect(jsonPath("$.participationsLiberees").value(2))
                .andExpect(jsonPath("$.penalitesCreees").value(1));
    }

    @Test
    void shouldReturn409_whenBusinessRuleRejectsProcessing() throws Exception {
        when(traitementVeilleService.traiterVeille(LocalDate.of(2026, 5, 19)))
                .thenThrow(new ConfigurationMetierException(
                        "La date de traitement est obligatoire."
                ));

        mockMvc.perform(post("/api/admin/matches/traitement-veille")
                        .header("X-Admin-Login", "admin-global")
                        .param("date", "2026-05-19"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFIGURATION_METIER_INVALIDE"))
                .andExpect(jsonPath("$.message").value("La date de traitement est obligatoire."));
    }

    @Test
    void shouldReturn400_whenDateParameterIsMissing() throws Exception {
        mockMvc.perform(post("/api/admin/matches/traitement-veille")
                        .header("X-Admin-Login", "admin-global"))
                .andExpect(status().isBadRequest());

        verify(traitementVeilleService, never()).traiterVeille(any(LocalDate.class));
    }

    @Test
    void shouldReturn400_whenDateParameterHasInvalidFormat() throws Exception {
        mockMvc.perform(post("/api/admin/matches/traitement-veille")
                        .header("X-Admin-Login", "admin-global")
                        .param("date", "date-invalide"))
                .andExpect(status().isBadRequest());

        verify(traitementVeilleService, never()).traiterVeille(any(LocalDate.class));
    }

    @Test
    void shouldReturn401_whenAdminHeaderIsMissing() throws Exception {
        org.mockito.Mockito.doThrow(new AuthentificationException(
                "Administrateur requis pour accéder à cette opération."
        )).when(adminAuthorizationService)
                .verifierAdminGlobal(null);

        mockMvc.perform(post("/api/admin/matches/traitement-veille")
                        .param("date", "2026-05-19"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTIFICATION_INVALIDE"))
                .andExpect(jsonPath("$.message").value(
                        "Administrateur requis pour accéder à cette opération."
                ));
    }
}
