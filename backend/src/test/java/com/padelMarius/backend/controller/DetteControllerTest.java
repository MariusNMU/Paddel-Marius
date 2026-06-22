package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.dette.DetteResponse;
import com.padelMarius.backend.dto.dette.PaiementDetteResponse;
import com.padelMarius.backend.dto.dette.PayerDetteRequest;
import com.padelMarius.backend.entity.NaturePaiement;
import com.padelMarius.backend.entity.StatutDette;
import com.padelMarius.backend.entity.StatutPaiement;
import com.padelMarius.backend.service.DetteService;
import com.padelMarius.backend.service.JoueurAuthorizationService;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DetteController.class)
@Import(ApiExceptionHandler.class)
class DetteControllerTest {

    private static final String AUTHORIZATION =
            "Bearer jwt-joueur";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DetteService detteService;

    @MockitoBean
    private JoueurAuthorizationService joueurAuthorizationService;

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

        when(detteService.consulterDettesOuvertes("G0001"))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/membres/G0001/dettes/ouvertes")
                                .header(
                                        "Authorization",
                                        AUTHORIZATION
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].detteId").value(500))
                .andExpect(jsonPath("$[0].matchId").value(100))
                .andExpect(jsonPath("$[0].membreResponsableId").value(20))
                .andExpect(jsonPath("$[0].matriculeResponsable").value("G0001"))
                .andExpect(jsonPath("$[0].montantRestant").value(30.00))
                .andExpect(jsonPath("$[0].statutDette").value("OUVERTE"));

        verify(joueurAuthorizationService)
                .verifierAccesMatricule(
                        AUTHORIZATION,
                        "G0001"
                );

        verify(detteService)
                .consulterDettesOuvertes("G0001");
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

        mockMvc.perform(
                        post("/api/dettes/500/paiements")
                                .header(
                                        "Authorization",
                                        AUTHORIZATION
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "montant": 30.00
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paiementId").value(700))
                .andExpect(jsonPath("$.detteId").value(500))
                .andExpect(jsonPath("$.membreId").value(20))
                .andExpect(jsonPath("$.matriculeMembre").value("G0001"))
                .andExpect(jsonPath("$.naturePaiement")
                        .value("REGLEMENT_DETTE"))
                .andExpect(jsonPath("$.montant").value(30.00))
                .andExpect(jsonPath("$.statutPaiement").value("PAYE"))
                .andExpect(jsonPath("$.statutDette").value("REGLEE"));

        verify(joueurAuthorizationService)
                .verifierDetteDuJoueur(
                        AUTHORIZATION,
                        500L
                );

        verify(detteService).payerDette(
                eq(500L),
                any(PayerDetteRequest.class)
        );
    }

    @Test
    void shouldReturn400_whenPayDebtRequestIsInvalid()
            throws Exception {

        mockMvc.perform(
                        post("/api/dettes/500/paiements")
                                .header(
                                        "Authorization",
                                        AUTHORIZATION
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "montant": null
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(joueurAuthorizationService);

        verify(detteService, never()).payerDette(
                eq(500L),
                any(PayerDetteRequest.class)
        );
    }
}