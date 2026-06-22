package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.participation.AjouterParticipantPriveRequest;
import com.padelMarius.backend.dto.participation.InscriptionPubliqueRequest;
import com.padelMarius.backend.dto.participation.ParticipationResponse;
import com.padelMarius.backend.entity.ModeEntreeParticipation;
import com.padelMarius.backend.entity.RoleParticipation;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.service.JoueurAuthorizationService;
import com.padelMarius.backend.service.ParticipationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParticipationController.class)
@Import(ApiExceptionHandler.class)
class ParticipationControllerTest {

    private static final String AUTHORIZATION =
            "Bearer jwt-joueur";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParticipationService participationService;

    @MockitoBean
    private JoueurAuthorizationService joueurAuthorizationService;

    @Test
    void shouldReturnCreated_whenAddingPrivateParticipant() throws Exception {
        ParticipationResponse response = new ParticipationResponse(
                300L,
                100L,
                21L,
                "G0002",
                RoleParticipation.JOUEUR,
                ModeEntreeParticipation.INVITATION_PRIVEE,
                StatutParticipation.EN_ATTENTE_PAIEMENT,
                LocalDateTime.of(2026, 5, 1, 10, 0)
        );

        when(participationService.ajouterParticipantPrive(
                eq(100L),
                any(AjouterParticipantPriveRequest.class)
        )).thenReturn(response);

        mockMvc.perform(post("/api/matches/100/participants/prive")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matriculeJoueur": "G0002"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.participationId").value(300))
                .andExpect(jsonPath("$.matchId").value(100))
                .andExpect(jsonPath("$.membreId").value(21))
                .andExpect(jsonPath("$.matriculeJoueur").value("G0002"))
                .andExpect(jsonPath("$.roleParticipation").value("JOUEUR"))
                .andExpect(jsonPath("$.modeEntree").value("INVITATION_PRIVEE"))
                .andExpect(jsonPath("$.statutParticipation").value("EN_ATTENTE_PAIEMENT"))
                .andExpect(jsonPath("$.dateAffectation").value("2026-05-01T10:00:00"));
    }

    @Test
    void shouldReturnCreated_whenRegisteringPublicParticipant() throws Exception {
        ParticipationResponse response = new ParticipationResponse(
                301L,
                100L,
                21L,
                "L0001",
                RoleParticipation.JOUEUR,
                ModeEntreeParticipation.INSCRIPTION_PUBLIQUE,
                StatutParticipation.EN_ATTENTE_PAIEMENT,
                LocalDateTime.of(2026, 5, 1, 10, 0)
        );

        when(participationService.inscrireParticipantPublic(
                eq(100L),
                any(InscriptionPubliqueRequest.class)
        )).thenReturn(response);

        mockMvc.perform(post("/api/matches/100/participants/public")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matriculeJoueur": "L0001"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.participationId").value(301))
                .andExpect(jsonPath("$.matchId").value(100))
                .andExpect(jsonPath("$.membreId").value(21))
                .andExpect(jsonPath("$.matriculeJoueur").value("L0001"))
                .andExpect(jsonPath("$.roleParticipation").value("JOUEUR"))
                .andExpect(jsonPath("$.modeEntree").value("INSCRIPTION_PUBLIQUE"))
                .andExpect(jsonPath("$.statutParticipation").value("EN_ATTENTE_PAIEMENT"));
    }

    @Test
    void shouldReturn404_whenMatchDoesNotExist() throws Exception {
        when(participationService.ajouterParticipantPrive(
                eq(999L),
                any(AjouterParticipantPriveRequest.class)
        )).thenThrow(new RessourceIntrouvableException("Match introuvable avec l'id 999"));

        mockMvc.perform(post("/api/matches/999/participants/prive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matriculeJoueur": "G0002"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESSOURCE_INTROUVABLE"))
                .andExpect(jsonPath("$.message").value("Match introuvable avec l'id 999"));
    }

    @Test
    void shouldReturn409_whenBusinessRuleBlocksParticipation() throws Exception {
        when(participationService.inscrireParticipantPublic(
                eq(100L),
                any(InscriptionPubliqueRequest.class)
        )).thenThrow(new ConfigurationMetierException("Le match contient deja 4 participants."));

        mockMvc.perform(post("/api/matches/100/participants/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matriculeJoueur": "L0001"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFIGURATION_METIER_INVALIDE"))
                .andExpect(jsonPath("$.message").value("Le match contient deja 4 participants."));
    }

    @Test
    void shouldReturn400_whenRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/matches/100/participants/prive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matriculeJoueur": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(participationService, never()).ajouterParticipantPrive(
                eq(100L),
                any(AjouterParticipantPriveRequest.class)
        );
    }
}
