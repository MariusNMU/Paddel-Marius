package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.matchpublic.MatchPublicResponse;
import com.padelMarius.backend.dto.matchpublic.RejoindreMatchPublicRequest;
import com.padelMarius.backend.dto.matchpublic.RejoindreMatchPublicResponse;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.service.MatchPublicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MatchPublicController.class)
@Import(ApiExceptionHandler.class)
class MatchPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MatchPublicService matchPublicService;

    @Test
    void listerMatchesPublics_shouldReturnPublicMatches() throws Exception {
        MatchPublicResponse response = new MatchPublicResponse(
                3001L,
                1001L,
                "Padel Bruxelles",
                1101L,
                "T1",
                LocalDateTime.of(2026, 6, 20, 9, 0),
                LocalDateTime.of(2026, 6, 20, 10, 30),
                2,
                2,
                new BigDecimal("60.00"),
                new BigDecimal("15.00")
        );

        when(matchPublicService.listerMatchesPublicsDisponibles(
                1001L,
                LocalDate.of(2026, 6, 20)
        )).thenReturn(List.of(response));

        mockMvc.perform(get("/api/matches/publics")
                        .param("siteId", "1001")
                        .param("date", "2026-06-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matchId").value(3001))
                .andExpect(jsonPath("$[0].siteId").value(1001))
                .andExpect(jsonPath("$[0].nomSite").value("Padel Bruxelles"))
                .andExpect(jsonPath("$[0].placesDisponibles").value(2))
                .andExpect(jsonPath("$[0].montantParticipation").value(15.00));
    }

    @Test
    void rejoindreEtPayer_shouldReturnCreatedResponse() throws Exception {
        RejoindreMatchPublicResponse response = new RejoindreMatchPublicResponse(
                3001L,
                3105L,
                6008L,
                "G1001",
                new BigDecimal("15.00"),
                StatutParticipation.CONFIRMEE,
                new BigDecimal("85.00")
        );

        when(matchPublicService.rejoindreEtPayer(
                any(Long.class),
                any(RejoindreMatchPublicRequest.class)
        )).thenReturn(response);

        mockMvc.perform(post("/api/matches/3001/participants/public/payer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matriculeJoueur": "G1001"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.matchId").value(3001))
                .andExpect(jsonPath("$.participationId").value(3105))
                .andExpect(jsonPath("$.paiementId").value(6008))
                .andExpect(jsonPath("$.matriculeJoueur").value("G1001"))
                .andExpect(jsonPath("$.montantPaye").value(15.00))
                .andExpect(jsonPath("$.statutParticipation").value("CONFIRMEE"))
                .andExpect(jsonPath("$.soldeRestant").value(85.00));
    }

    @Test
    void rejoindreEtPayer_shouldReturnBadRequest_whenMatriculeIsBlank() throws Exception {
        mockMvc.perform(post("/api/matches/3001/participants/public/payer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matriculeJoueur": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejoindreEtPayer_shouldReturnConflict_whenBusinessRuleBlocks() throws Exception {
        when(matchPublicService.rejoindreEtPayer(
                any(Long.class),
                any(RejoindreMatchPublicRequest.class)
        )).thenThrow(new ConfigurationMetierException(
                "Le match contient deja 4 participants."
        ));

        mockMvc.perform(post("/api/matches/3001/participants/public/payer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matriculeJoueur": "G1001"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFIGURATION_METIER_INVALIDE"))
                .andExpect(jsonPath("$.message").value("Le match contient deja 4 participants."));
    }
}