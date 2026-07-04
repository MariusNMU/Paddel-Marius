package com.padelMarius.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.StreamSupport;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/data.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BackendIntegrationHappyFlowTest {

    private static final String MATRICULE_JOUEUR = "G1001";
    private static final String MOT_DE_PASSE_JOUEUR = "password";

    private static final long SITE_BRUXELLES_ID = 1001L;
    private static final long TERRAIN_DEMO_ID = 1103L;

    private static final DateTimeFormatter FORMAT_DATE_HEURE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRunCompleteBackendHappyFlowFromLoginToReservation() throws Exception {
        String token = connecterJoueurEtRecupererToken();

        LocalDate dateMatch = LocalDate.now().plusDays(6);
        LocalDateTime dateHeureDebut = dateMatch.atTime(8, 0);

        verifierDisponibilite(token, dateMatch, dateHeureDebut);

        Long participationOrganisateurId = creerMatch(
                token,
                dateHeureDebut
        );

        payerParticipationOrganisateur(
                token,
                participationOrganisateurId
        );

        verifierReservationVisiblePourJoueur(
                token,
                participationOrganisateurId
        );
    }

    private String connecterJoueurEtRecupererToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/joueur")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matricule": "G1001",
                                  "motDePasse": "password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matricule").value(MATRICULE_JOUEUR))
                .andExpect(jsonPath("$.actif").value(true))
                .andReturn();

        JsonNode body = lireJson(result);
        String token = body.get("token").asText();

        assertThat(token).isNotBlank();

        return token;
    }

    private void verifierDisponibilite(
            String token,
            LocalDate dateMatch,
            LocalDateTime dateHeureDebut
    ) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/disponibilites")
                        .header(AUTHORIZATION, bearer(token))
                        .param("siteId", String.valueOf(SITE_BRUXELLES_ID))
                        .param("date", dateMatch.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.siteId").value(SITE_BRUXELLES_ID))
                .andExpect(jsonPath("$.ferme").value(false))
                .andReturn();

        JsonNode body = lireJson(result);
        JsonNode creneaux = body.get("creneaux");

        assertThat(creneaux).isNotNull();
        assertThat(creneaux.isArray()).isTrue();

        String dateHeureDebutAttendue = formaterDateHeure(dateHeureDebut);

        boolean creneauTrouve = StreamSupport.stream(creneaux.spliterator(), false)
                .anyMatch(creneau ->
                        creneau.get("terrainId").asLong() == TERRAIN_DEMO_ID
                                && dateHeureDebutAttendue.equals(
                                creneau.get("dateHeureDebut").asText()
                        )
                );

        assertThat(creneauTrouve)
                .as("Le créneau %s du terrain %s doit être disponible.",
                        dateHeureDebutAttendue,
                        TERRAIN_DEMO_ID)
                .isTrue();
    }

    private Long creerMatch(
            String token,
            LocalDateTime dateHeureDebut
    ) throws Exception {
        String payload = """
                {
                  "terrainId": %d,
                  "matriculeOrganisateur": "%s",
                  "dateHeureDebut": "%s",
                  "modeCreation": "PUBLIC"
                }
                """.formatted(
                TERRAIN_DEMO_ID,
                MATRICULE_JOUEUR,
                formaterDateHeure(dateHeureDebut)
        );

        MvcResult result = mockMvc.perform(post("/api/matches")
                        .header(AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.terrainId").value(TERRAIN_DEMO_ID))
                .andExpect(jsonPath("$.siteId").value(SITE_BRUXELLES_ID))
                .andExpect(jsonPath("$.matriculeOrganisateur").value(MATRICULE_JOUEUR))
                .andExpect(jsonPath("$.modeCreation").value("PUBLIC"))
                .andExpect(jsonPath("$.visibiliteCourante").value("PUBLIC"))
                .andExpect(jsonPath("$.etatCycle").value("A_VENIR"))
                .andExpect(jsonPath("$.participationOrganisateurId").exists())
                .andReturn();

        JsonNode body = lireJson(result);

        Long participationOrganisateurId =
                body.get("participationOrganisateurId").asLong();

        assertThat(participationOrganisateurId).isPositive();

        return participationOrganisateurId;
    }

    private void payerParticipationOrganisateur(
            String token,
            Long participationOrganisateurId
    ) throws Exception {
        mockMvc.perform(post("/api/participations/{participationId}/paiements",
                        participationOrganisateurId)
                        .header(AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "montant": 15.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.participationId")
                        .value(participationOrganisateurId))
                .andExpect(jsonPath("$.matriculeMembre")
                        .value(MATRICULE_JOUEUR))
                .andExpect(jsonPath("$.naturePaiement")
                        .value("PARTICIPATION"))
                .andExpect(jsonPath("$.statutPaiement")
                        .value("PAYE"))
                .andExpect(jsonPath("$.statutParticipation")
                        .value("CONFIRMEE"))
                .andExpect(jsonPath("$.montant")
                        .value(15.00))
                .andExpect(jsonPath("$.montantTotalDebite")
                        .value(15.00));
    }

    private void verifierReservationVisiblePourJoueur(
            String token,
            Long participationOrganisateurId
    ) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/membres/{matricule}/reservations",
                        MATRICULE_JOUEUR)
                        .header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode reservations = lireJson(result);

        assertThat(reservations.isArray()).isTrue();

        boolean reservationTrouvee = StreamSupport.stream(reservations.spliterator(), false)
                .anyMatch(reservation ->
                        reservation.get("participationId").asLong()
                                == participationOrganisateurId
                                && reservation.get("terrainId").asLong()
                                == TERRAIN_DEMO_ID
                                && "CONFIRMEE".equals(
                                reservation.get("statutParticipation").asText()
                        )
                                && "A_VENIR".equals(
                                reservation.get("etatCycle").asText()
                        )
                );

        assertThat(reservationTrouvee)
                .as("La réservation créée et payée doit être visible dans l'espace joueur.")
                .isTrue();
    }

    private JsonNode lireJson(MvcResult result) throws Exception {
        String json = result.getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return objectMapper.readTree(json);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String formaterDateHeure(LocalDateTime dateHeure) {
        return dateHeure.format(FORMAT_DATE_HEURE);
    }
}
