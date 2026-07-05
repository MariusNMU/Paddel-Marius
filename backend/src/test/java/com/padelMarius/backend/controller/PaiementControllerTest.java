package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.paiement.PaiementResponse;
import com.padelMarius.backend.dto.paiement.PayerParticipationRequest;
import com.padelMarius.backend.entity.NaturePaiement;
import com.padelMarius.backend.entity.StatutPaiement;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.exception.AutorisationException;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.service.JoueurAuthorizationService;
import com.padelMarius.backend.service.PaiementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaiementController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class PaiementControllerTest {

    private static final String AUTHORIZATION =
            "Bearer jwt-joueur";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaiementService paiementService;

    @MockitoBean
    private JoueurAuthorizationService joueurAuthorizationService;

    @Test
    void shouldReturnCreated_whenPayingParticipation() throws Exception {
        PaiementResponse response = new PaiementResponse(
                400L,
                300L,
                21L,
                "G0002",
                NaturePaiement.PARTICIPATION,
                new BigDecimal("15.00"),
                new BigDecimal("0.00"),
                new BigDecimal("15.00"),
                StatutPaiement.PAYE,
                StatutParticipation.CONFIRMEE,
                LocalDateTime.of(2026, 5, 7, 12, 0),
                LocalDateTime.of(2026, 5, 7, 12, 0)
        );

        when(paiementService.payerParticipation(
                eq(300L),
                any(PayerParticipationRequest.class)
        )).thenReturn(response);

        mockMvc.perform(post("/api/participations/300/paiements")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "montant": 15.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paiementId").value(400))
                .andExpect(jsonPath("$.participationId").value(300))
                .andExpect(jsonPath("$.membreId").value(21))
                .andExpect(jsonPath("$.matriculeMembre").value("G0002"))
                .andExpect(jsonPath("$.naturePaiement").value("PARTICIPATION"))
                .andExpect(jsonPath("$.montant").value(15.00))
                .andExpect(jsonPath("$.montantDettesReglees").value(0.00))
                .andExpect(jsonPath("$.montantTotalDebite").value(15.00))
                .andExpect(jsonPath("$.statutPaiement").value("PAYE"))
                .andExpect(jsonPath("$.statutParticipation").value("CONFIRMEE"))
                .andExpect(jsonPath("$.dateHeurePaiement").value("2026-05-07T12:00:00"))
                .andExpect(jsonPath("$.dateConfirmationParticipation").value("2026-05-07T12:00:00"));
    }

    @Test
    void shouldReturnCreated_whenPayingStandardParticipationWithoutRequestBody() throws Exception {
        PaiementResponse response = new PaiementResponse(
                400L,
                300L,
                21L,
                "G0002",
                NaturePaiement.PARTICIPATION,
                new BigDecimal("15.00"),
                new BigDecimal("0.00"),
                new BigDecimal("15.00"),
                StatutPaiement.PAYE,
                StatutParticipation.CONFIRMEE,
                LocalDateTime.of(2026, 5, 7, 12, 0),
                LocalDateTime.of(2026, 5, 7, 12, 0)
        );

        when(paiementService.payerParticipationStandard(300L))
                .thenReturn(response);

        mockMvc.perform(post("/api/participations/300/paiements/standard")
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paiementId").value(400))
                .andExpect(jsonPath("$.participationId").value(300))
                .andExpect(jsonPath("$.membreId").value(21))
                .andExpect(jsonPath("$.matriculeMembre").value("G0002"))
                .andExpect(jsonPath("$.naturePaiement").value("PARTICIPATION"))
                .andExpect(jsonPath("$.montant").value(15.00))
                .andExpect(jsonPath("$.montantTotalDebite").value(15.00))
                .andExpect(jsonPath("$.statutPaiement").value("PAYE"))
                .andExpect(jsonPath("$.statutParticipation").value("CONFIRMEE"));
    }

    @Test
    void shouldReturnForbiddenWhenParticipationBelongsToOtherPlayer()
            throws Exception {

        doThrow(new AutorisationException(
                "Cette participation n'appartient pas "
                        + "au joueur connecté."
        )).when(joueurAuthorizationService)
                .verifierParticipationDuJoueur(
                        AUTHORIZATION,
                        300L
                );

        mockMvc.perform(
                        post("/api/participations/300/paiements")
                                .header(
                                        "Authorization",
                                        AUTHORIZATION
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "montant": 15.00
                                        }
                                        """)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code")
                        .value("ACCES_REFUSE"));

        verifyNoInteractions(paiementService);
    }

    @Test
    void shouldReturnPaymentHistoryForMember() throws Exception {
        when(paiementService.consulterHistoriquePaiements("G1001"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/membres/G1001/paiements")
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404_whenParticipationDoesNotExist() throws Exception {
        when(paiementService.payerParticipation(
                eq(999L),
                any(PayerParticipationRequest.class)
        )).thenThrow(new RessourceIntrouvableException("Participation introuvable avec l'id 999"));

        mockMvc.perform(post("/api/participations/999/paiements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "montant": 15.00
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESSOURCE_INTROUVABLE"))
                .andExpect(jsonPath("$.message").value("Participation introuvable avec l'id 999"));
    }

    @Test
    void shouldReturn409_whenBusinessRuleBlocksPayment() throws Exception {
        when(paiementService.payerParticipation(
                eq(300L),
                any(PayerParticipationRequest.class)
        )).thenThrow(new ConfigurationMetierException("Cette participation possède déjà un paiement."));

        mockMvc.perform(post("/api/participations/300/paiements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "montant": 15.00
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFIGURATION_METIER_INVALIDE"))
                .andExpect(jsonPath("$.message").value("Cette participation possède déjà un paiement."));
    }

    @Test
    void shouldReturn400_whenRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/participations/300/paiements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "montant": null
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(paiementService, never()).payerParticipation(
                eq(300L),
                any(PayerParticipationRequest.class)
        );
    }
}
