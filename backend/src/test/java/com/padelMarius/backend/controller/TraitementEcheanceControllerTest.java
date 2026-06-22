package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.traitement.TraitementEcheanceResponse;
import com.padelMarius.backend.exception.AuthentificationException;
import com.padelMarius.backend.service.AdminAuthorizationService;
import com.padelMarius.backend.service.TraitementEcheanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TraitementEcheanceController.class)
@Import(ApiExceptionHandler.class)
class TraitementEcheanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TraitementEcheanceService traitementEcheanceService;

    @MockitoBean
    private AdminAuthorizationService adminAuthorizationService;

    @Test
    void shouldReturnOk_whenGlobalAdminRunsProcessing() throws Exception {
        TraitementEcheanceResponse response = new TraitementEcheanceResponse(
                LocalDateTime.of(2026, 5, 17, 17, 0),
                2,
                1,
                1
        );

        when(traitementEcheanceService.traiterMatchesArrivesAEcheance())
                .thenReturn(response);

        mockMvc.perform(post("/api/admin/matches/traitement-echeance")
                        .header(
                                "Authorization",
                                "Bearer jwt-admin-global"
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchesAnalyses").value(2))
                .andExpect(jsonPath("$.matchesDemarres").value(1))
                .andExpect(jsonPath("$.dettesCreees").value(1));

        verify(adminAuthorizationService).verifierAdminGlobal(
                "Bearer jwt-admin-global"
        );
    }

    @Test
    void shouldReturn401_whenOnlyLegacyAdminHeaderIsProvided() throws Exception {
        org.mockito.Mockito.doThrow(new AuthentificationException(
                        "Token JWT obligatoire."
                )).when(adminAuthorizationService)
                .verifierAdminGlobal(null);

        mockMvc.perform(post("/api/admin/matches/traitement-echeance")
                        .header(
                                "X-Admin-Login",
                                "admin-global"
                        ))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTIFICATION_INVALIDE"))
                .andExpect(jsonPath("$.message").value(
                        "Token JWT obligatoire."
                ));
    }
}
