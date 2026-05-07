package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.dette.DetteResponse;
import com.padelMarius.backend.dto.dette.PaiementDetteResponse;
import com.padelMarius.backend.dto.dette.PayerDetteRequest;
import com.padelMarius.backend.entity.NaturePaiement;
import com.padelMarius.backend.entity.StatutDette;
import com.padelMarius.backend.entity.StatutPaiement;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.service.DetteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DetteController.class)
@Import(ApiExceptionHandler.class)
class DetteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DetteService detteService;

    @Test
    void shouldReturnCreated_whenGeneratingDebtForMatch() throws Exception {
        DetteResponse response = new DetteResponse(
                500L,
                100L,
                20L,
                "G0001",
                new BigDecimal("30.00"),
                new BigDecimal("30.00"),
                StatutDette.OUVERTE,
                LocalDateTime.of(2026, 5, 7, 12, 0),
                null
        );

        when(detteService.genererDettePourMatch(100L)).thenReturn(response);

        mockMvc.perform(post("/api/matches/100/dettes/generer"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.detteId").value(500))
                .andExpect(jsonPath("$.matchId").value(100))
                .andExpect(jsonPath("$.membreResponsableId").value(20))
                .andExpect(jsonPath("$.matriculeResponsable").value("G0001"))
                .andExpect(jsonPath("$.montantInitial").value(30.00))
                .andExpect(jsonPath("$.montantRestant").value(30.00))
                .andExpect(jsonPath("$.statutDette").value("OUVERTE"))
                .andExpect(jsonPath("$.dateCreation").value("2026-05-07T12:00:00"));
    }

    @Test
    void shouldReturnOpenDebtsForMember() throws Exception {
        DetteResponse response = new DetteResponse(
                500L,
                100L,
                20L,
                "G0001",
                new BigDecimal("30.00"),
                new BigDecimal("30.00"),
                StatutDette.OUVERTE,
                LocalDateTime.of(2026, 5, 7, 12, 0),
                null
        );

        when(detteService.consulterDettesOuvertes("G0001")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/membres/G0001/dettes/ouvertes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].detteId").value(500))
                .andExpect(jsonPath("$[0].matchId").value(100))
                .andExpect(jsonPath("$[0].membreResponsableId").value(20))
                .andExpect(jsonPath("$[0].matriculeResponsable").value("G0001"))
                .andExpect(jsonPath("$[0].montantRestant").value(30.00))
                .andExpect(jsonPath("$[0].statutDette").value("OUVERTE"));
    }

    @Test
    void shouldReturnCreated_whenPayingDebt() throws Exception {
        PaiementDetteResponse response = new PaiementDetteResponse(
                700L,
                500L,
                20L,
                "G0001",
                NaturePaiement.REGLEMENT_DETTE,
                new BigDecimal("30.00"),
                StatutPaiement.PAYE,
                StatutDette.REGLEE,
                LocalDateTime.of(2026, 5, 7, 12, 0),
                LocalDateTime.of(2026, 5, 7, 12, 0)
        );

        when(detteService.payerDette(
                eq(500L),
                any(PayerDetteRequest.class)
        )).thenReturn(response);

        mockMvc.perform(post("/api/dettes/500/paiements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "montant": 30.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paiementId").value(700))
                .andExpect(jsonPath("$.detteId").value(500))
                .andExpect(jsonPath("$.membreId").value(20))
                .andExpect(jsonPath("$.matriculeMembre").value("G0001"))
                .andExpect(jsonPath("$.naturePaiement").value("REGLEMENT_DETTE"))
                .andExpect(jsonPath("$.montant").value(30.00))
                .andExpect(jsonPath("$.statutPaiement").value("PAYE"))
                .andExpect(jsonPath("$.statutDette").value("REGLEE"))
                .andExpect(jsonPath("$.dateHeurePaiement").value("2026-05-07T12:00:00"))
                .andExpect(jsonPath("$.dateReglementDette").value("2026-05-07T12:00:00"));
    }

    @Test
    void shouldReturn404_whenMatchDoesNotExistDuringDebtGeneration() throws Exception {
        when(detteService.genererDettePourMatch(999L))
                .thenThrow(new RessourceIntrouvableException("Match introuvable avec l'id 999"));

        mockMvc.perform(post("/api/matches/999/dettes/generer"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESSOURCE_INTROUVABLE"))
                .andExpect(jsonPath("$.message").value("Match introuvable avec l'id 999"));
    }

    @Test
    void shouldReturn409_whenBusinessRuleBlocksDebtGeneration() throws Exception {
        when(detteService.genererDettePourMatch(100L))
                .thenThrow(new ConfigurationMetierException("Une dette existe déjà pour ce match."));

        mockMvc.perform(post("/api/matches/100/dettes/generer"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFIGURATION_METIER_INVALIDE"))
                .andExpect(jsonPath("$.message").value("Une dette existe déjà pour ce match."));
    }

    @Test
    void shouldReturn400_whenPayDebtRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/dettes/500/paiements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "montant": null
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(detteService, never()).payerDette(
                eq(500L),
                any(PayerDetteRequest.class)
        );
    }
}