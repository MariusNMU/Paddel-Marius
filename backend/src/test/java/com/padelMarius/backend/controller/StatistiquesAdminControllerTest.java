package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.statistique.StatistiquesAdminResponse;
import com.padelMarius.backend.exception.AuthentificationException;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.service.AdminAuthorizationService;
import com.padelMarius.backend.service.StatistiquesAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatistiquesAdminController.class)
@Import(ApiExceptionHandler.class)
class StatistiquesAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatistiquesAdminService statistiquesAdminService;

    @MockitoBean
    private AdminAuthorizationService adminAuthorizationService;

    @Test
    void shouldReturnOk_whenRequestingGlobalStats() throws Exception {
        StatistiquesAdminResponse response = new StatistiquesAdminResponse(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                null,
                null,
                2,
                1,
                1,
                2,
                new BigDecimal("45.00"),
                1,
                new BigDecimal("30.00"),
                6,
                8,
                new BigDecimal("75.00")
        );

        when(statistiquesAdminService.calculerStatistiques(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                null
        )).thenReturn(response);

        mockMvc.perform(get("/api/admin/statistiques")
                        .header(
                                "Authorization",
                                "Bearer jwt-admin-global"
                        )
                        .param("dateDebut", "2026-05-01")
                        .param("dateFin", "2026-05-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateDebut").value("2026-05-01"))
                .andExpect(jsonPath("$.dateFin").value("2026-05-31"))
                .andExpect(jsonPath("$.nombreMatches").value(2))
                .andExpect(jsonPath("$.nombreMatchesAVenir").value(1))
                .andExpect(jsonPath("$.nombreMatchesTermines").value(1))
                .andExpect(jsonPath("$.nombrePaiements").value(2))
                .andExpect(jsonPath("$.chiffreAffaires").value(45.00))
                .andExpect(jsonPath("$.nombreDettesOuvertes").value(1))
                .andExpect(jsonPath("$.montantDettesOuvertes").value(30.00))
                .andExpect(jsonPath("$.nombreParticipationsActives").value(6))
                .andExpect(jsonPath("$.capaciteTheoriqueJoueurs").value(8))
                .andExpect(jsonPath("$.tauxRemplissage").value(75.00));

        verify(adminAuthorizationService).verifierAccesAdminSite(
                "Bearer jwt-admin-global",
                null
        );
    }

    @Test
    void shouldReturnOk_whenRequestingStatsForOneSite() throws Exception {
        StatistiquesAdminResponse response = new StatistiquesAdminResponse(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                1L,
                "Padel Bruxelles",
                1,
                1,
                0,
                1,
                new BigDecimal("15.00"),
                1,
                new BigDecimal("20.00"),
                2,
                4,
                new BigDecimal("50.00")
        );

        when(statistiquesAdminService.calculerStatistiques(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                1L
        )).thenReturn(response);

        mockMvc.perform(get("/api/admin/statistiques")
                        .header(
                                "Authorization",
                                "Bearer jwt-admin-global"
                        )
                        .param("dateDebut", "2026-05-01")
                        .param("dateFin", "2026-05-31")
                        .param("siteId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.siteId").value(1))
                .andExpect(jsonPath("$.nomSite").value("Padel Bruxelles"))
                .andExpect(jsonPath("$.nombreMatches").value(1))
                .andExpect(jsonPath("$.chiffreAffaires").value(15.00))
                .andExpect(jsonPath("$.tauxRemplissage").value(50.00));
    }

    @Test
    void shouldReturn404_whenSiteDoesNotExist() throws Exception {
        when(statistiquesAdminService.calculerStatistiques(
                eq(LocalDate.of(2026, 5, 1)),
                eq(LocalDate.of(2026, 5, 31)),
                eq(999L)
        )).thenThrow(new RessourceIntrouvableException(
                "Site introuvable avec l'id 999"
        ));

        mockMvc.perform(get("/api/admin/statistiques")
                        .header(
                                "Authorization",
                                "Bearer jwt-admin-global"
                        )
                        .param("dateDebut", "2026-05-01")
                        .param("dateFin", "2026-05-31")
                        .param("siteId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESSOURCE_INTROUVABLE"))
                .andExpect(jsonPath("$.message").value("Site introuvable avec l'id 999"));
    }

    @Test
    void shouldReturn409_whenDateRangeIsInvalid() throws Exception {
        when(statistiquesAdminService.calculerStatistiques(
                eq(LocalDate.of(2026, 5, 31)),
                eq(LocalDate.of(2026, 5, 1)),
                isNull()
        )).thenThrow(new ConfigurationMetierException(
                "La date de fin doit être supérieure ou égale à la date de début."
        ));

        mockMvc.perform(get("/api/admin/statistiques")
                        .header(
                                "Authorization",
                                "Bearer jwt-admin-global"
                        )
                        .param("dateDebut", "2026-05-31")
                        .param("dateFin", "2026-05-01"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFIGURATION_METIER_INVALIDE"))
                .andExpect(jsonPath("$.message").value(
                        "La date de fin doit être supérieure ou égale à la date de début."
                ));
    }

    @Test
    void shouldReturn400_whenDateDebutIsMissing() throws Exception {
        mockMvc.perform(get("/api/admin/statistiques")
                        .header(
                                "Authorization",
                                "Bearer jwt-admin-global"
                        )
                        .param("dateFin", "2026-05-31"))
                .andExpect(status().isBadRequest());

        verify(statistiquesAdminService, never())
                .calculerStatistiques(any(), any(), any());
    }

    @Test
    void shouldReturn400_whenDateFormatIsInvalid() throws Exception {
        mockMvc.perform(get("/api/admin/statistiques")
                        .header(
                                "Authorization",
                                "Bearer jwt-admin-global"
                        )
                        .param("dateDebut", "date-invalide")
                        .param("dateFin", "2026-05-31"))
                .andExpect(status().isBadRequest());

        verify(statistiquesAdminService, never())
                .calculerStatistiques(any(), any(), any());
    }

    @Test
    void shouldReturn401_whenOnlyLegacyAdminHeaderIsProvided() throws Exception {
        when(statistiquesAdminService.calculerStatistiques(
                eq(LocalDate.of(2026, 5, 1)),
                eq(LocalDate.of(2026, 5, 31)),
                isNull()
        )).thenReturn(null);

        org.mockito.Mockito.doThrow(new AuthentificationException(
                "Token JWT obligatoire."
        )).when(adminAuthorizationService)
                .verifierAccesAdminSite(null, null);

        mockMvc.perform(get("/api/admin/statistiques")
                        .header(
                                "X-Admin-Login",
                                "admin-global"
                        )
                        .param("dateDebut", "2026-05-01")
                        .param("dateFin", "2026-05-31"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTIFICATION_INVALIDE"))
                .andExpect(jsonPath("$.message").value(
                        "Token JWT obligatoire."
                ));
    }
}
