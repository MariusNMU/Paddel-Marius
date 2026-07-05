package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.match.CreerMatchRequest;
import com.padelMarius.backend.dto.match.MatchResponse;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.ModeCreation;
import com.padelMarius.backend.entity.VisibiliteMatch;
import com.padelMarius.backend.exception.AutorisationException;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.service.JoueurAuthorizationService;
import com.padelMarius.backend.service.MatchCreationService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatchController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class MatchControllerTest {

    private static final String AUTHORIZATION =
            "Bearer jwt-joueur";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MatchCreationService matchCreationService;

    @MockitoBean
    private JoueurAuthorizationService joueurAuthorizationService;

    @Test
    void shouldCreateMatch() throws Exception {
        MatchResponse response = new MatchResponse(
                100L,
                10L,
                1L,
                "G0001",
                LocalDateTime.of(2026, 5, 20, 9, 0),
                LocalDateTime.of(2026, 5, 20, 10, 30),
                ModeCreation.PRIVE,
                VisibiliteMatch.PRIVE,
                new BigDecimal("60.00"),
                EtatCycleMatch.A_VENIR,
                200L
        );

        when(matchCreationService.creerMatch(any(CreerMatchRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/matches")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "terrainId": 10,
                                  "matriculeOrganisateur": "G0001",
                                  "dateHeureDebut": "2026-05-20T09:00:00",
                                  "modeCreation": "PRIVE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.matchId").value(100))
                .andExpect(jsonPath("$.terrainId").value(10))
                .andExpect(jsonPath("$.siteId").value(1))
                .andExpect(jsonPath("$.matriculeOrganisateur").value("G0001"))
                .andExpect(jsonPath("$.dateHeureDebut").value("2026-05-20T09:00:00"))
                .andExpect(jsonPath("$.dateHeureFin").value("2026-05-20T10:30:00"))
                .andExpect(jsonPath("$.modeCreation").value("PRIVE"))
                .andExpect(jsonPath("$.visibiliteCourante").value("PRIVE"))
                .andExpect(jsonPath("$.prixTotal").value(60.00))
                .andExpect(jsonPath("$.etatCycle").value("A_VENIR"))
                .andExpect(jsonPath("$.participationOrganisateurId").value(200));
    }

    @Test
    void shouldReturnForbiddenWhenTokenDoesNotMatchOrganizer()
            throws Exception {

        doThrow(new AutorisationException(
                "Un joueur ne peut agir que pour son propre compte."
        )).when(joueurAuthorizationService)
                .verifierAccesMatricule(
                        "Bearer jwt-autre-joueur",
                        "G0001"
                );

        mockMvc.perform(
                        post("/api/matches")
                                .header(
                                        "Authorization",
                                        "Bearer jwt-autre-joueur"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "terrainId": 10,
                                          "matriculeOrganisateur": "G0001",
                                          "dateHeureDebut": "2026-05-20T09:00:00",
                                          "modeCreation": "PRIVE"
                                        }
                                        """)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code")
                        .value("ACCES_REFUSE"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Un joueur ne peut agir "
                                        + "que pour son propre compte."
                        ));

        verifyNoInteractions(matchCreationService);
    }

    @Test
    void shouldReturnNotFoundWhenTerrainDoesNotExist() throws Exception {
        when(matchCreationService.creerMatch(any(CreerMatchRequest.class)))
                .thenThrow(new RessourceIntrouvableException("Terrain introuvable avec l'id 999"));

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "terrainId": 999,
                                  "matriculeOrganisateur": "G0001",
                                  "dateHeureDebut": "2026-05-20T09:00:00",
                                  "modeCreation": "PRIVE"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESSOURCE_INTROUVABLE"))
                .andExpect(jsonPath("$.message").value("Terrain introuvable avec l'id 999"));
    }

    @Test
    void shouldReturnConflictWhenBusinessRuleBlocksCreation() throws Exception {
        when(matchCreationService.creerMatch(any(CreerMatchRequest.class)))
                .thenThrow(new ConfigurationMetierException(
                        "L'organisateur a une dette ouverte et ne peut pas créer un nouveau match."
                ));

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "terrainId": 10,
                                  "matriculeOrganisateur": "G0001",
                                  "dateHeureDebut": "2026-05-20T09:00:00",
                                  "modeCreation": "PUBLIC"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFIGURATION_METIER_INVALIDE"))
                .andExpect(jsonPath("$.message").value(
                        "L'organisateur a une dette ouverte et ne peut pas créer un nouveau match."
                ));
    }

    @Test
    void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "terrainId": null,
                                  "matriculeOrganisateur": "",
                                  "dateHeureDebut": null,
                                  "modeCreation": null
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(matchCreationService, never()).creerMatch(any(CreerMatchRequest.class));
    }
}
