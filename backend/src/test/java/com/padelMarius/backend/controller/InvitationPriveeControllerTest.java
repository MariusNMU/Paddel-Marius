package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.invitation.DeclinerInvitationRequest;
import com.padelMarius.backend.dto.invitation.InvitationPriveeResponse;
import com.padelMarius.backend.dto.invitation.InviterJoueurPriveRequest;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.service.InvitationPriveeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvitationPriveeController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class InvitationPriveeControllerTest {

    private static final String AUTHORIZATION =
            "Bearer jwt-joueur";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InvitationPriveeService invitationPriveeService;

    @Test
    void inviterJoueur_shouldReturnCreated() throws Exception {
        when(invitationPriveeService.inviterJoueur(
                eq(100L),
                any(InviterJoueurPriveRequest.class)
        )).thenReturn(creerInvitationResponse());

        mockMvc.perform(post("/api/matches/100/invitations/privees")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matriculeOrganisateur": "G0001",
                                  "matriculeInvite": "G0002"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.participationId").value(301))
                .andExpect(jsonPath("$.matchId").value(100))
                .andExpect(jsonPath("$.matriculeOrganisateur").value("G0001"))
                .andExpect(jsonPath("$.matriculeInvite").value("G0002"))
                .andExpect(jsonPath("$.statutParticipation").value("EN_ATTENTE_PAIEMENT"));
    }

    @Test
    void listerInvitationsRecues_shouldReturnInvitations() throws Exception {
        when(invitationPriveeService.listerInvitationsRecues("G0002"))
                .thenReturn(List.of(creerInvitationResponse()));

        mockMvc.perform(get("/api/membres/G0002/invitations/recues")
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].participationId").value(301))
                .andExpect(jsonPath("$[0].matchId").value(100))
                .andExpect(jsonPath("$[0].matriculeInvite").value("G0002"))
                .andExpect(jsonPath("$[0].statutParticipation").value("EN_ATTENTE_PAIEMENT"));
    }

    @Test
    void compterInvitationsRecues_shouldReturnCount() throws Exception {
        when(invitationPriveeService.compterInvitationsRecues("G0002"))
                .thenReturn(2);

        mockMvc.perform(get("/api/membres/G0002/invitations/recues/count")
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(2));
    }

    @Test
    void declinerInvitation_shouldReturnOk() throws Exception {
        InvitationPriveeResponse response = new InvitationPriveeResponse(
                301L,
                100L,
                1L,
                "Padel Central",
                10L,
                "T1",
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 5, 20, 10, 30),
                20L,
                "G0001",
                "Nom G0001",
                "Prenom G0001",
                21L,
                "G0002",
                "Nom G0002",
                "Prenom G0002",
                StatutParticipation.LIBEREE
        );

        when(invitationPriveeService.declinerInvitation(
                eq(301L),
                any(DeclinerInvitationRequest.class)
        )).thenReturn(response);

        mockMvc.perform(post("/api/participations/301/decliner")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matriculeJoueur": "G0002"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participationId").value(301))
                .andExpect(jsonPath("$.statutParticipation").value("LIBEREE"));
    }

    @Test
    void inviterJoueur_shouldReturnConflictWhenBusinessRuleBlocks() throws Exception {
        when(invitationPriveeService.inviterJoueur(
                eq(100L),
                any(InviterJoueurPriveRequest.class)
        )).thenThrow(new ConfigurationMetierException(
                "Seul l'organisateur du match peut inviter des joueurs."
        ));

        mockMvc.perform(post("/api/matches/100/invitations/privees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matriculeOrganisateur": "G9999",
                                  "matriculeInvite": "G0002"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFIGURATION_METIER_INVALIDE"))
                .andExpect(jsonPath("$.message").value("Seul l'organisateur du match peut inviter des joueurs."));
    }

    private InvitationPriveeResponse creerInvitationResponse() {
        return new InvitationPriveeResponse(
                301L,
                100L,
                1L,
                "Padel Central",
                10L,
                "T1",
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 5, 20, 10, 30),
                20L,
                "G0001",
                "Nom G0001",
                "Prenom G0001",
                21L,
                "G0002",
                "Nom G0002",
                "Prenom G0002",
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );
    }
}
