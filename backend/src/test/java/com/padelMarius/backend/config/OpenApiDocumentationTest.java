package com.padelMarius.backend.config;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldExposeOpenApiJsonDocumentation() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title")
                        .value("Padel Marius API"))
                .andExpect(jsonPath("$.info.version")
                        .value("1.0.0"));
    }

    @Test
    void shouldExposeOnlySupportedParticipationAndDebtEndpoints()
            throws Exception {

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())

                .andExpect(jsonPath(
                        "$['paths']"
                                + "['/api/matches/{matchId}/invitations/privees']"
                                + "['post']"
                ).exists())

                .andExpect(jsonPath(
                        "$['paths']"
                                + "['/api/matches/{matchId}/participants/public/payer']"
                                + "['post']"
                ).exists())

                .andExpect(jsonPath(
                        "$['paths']"
                                + "['/api/matches/{matchId}/participants/prive']"
                ).doesNotExist())

                .andExpect(jsonPath(
                        "$['paths']"
                                + "['/api/matches/{matchId}/participants/public']"
                ).doesNotExist())

                .andExpect(jsonPath(
                        "$['paths']"
                                + "['/api/matches/{matchId}/dettes/generer']"
                ).doesNotExist());
    }

    @Test
    void shouldExposeStableMatchPaymentAndDeadlineSchemas() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode documentation = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );
        JsonNode schemas = documentation.path("components").path("schemas");
        JsonNode matchProperties = schemas
                .path("MatchResponse")
                .path("properties");

        assertThat(matchProperties.has("matriculeOrganisateur")).isTrue();
        assertThat(matchProperties.has("participationOrganisateurId")).isTrue();

        JsonNode paiementProperties = schemas
                .path("PaiementResponse")
                .path("properties");

        assertThat(paiementProperties.has("participationId")).isTrue();
        assertThat(paiementProperties.has("matriculeMembre")).isTrue();
        assertThat(paiementProperties.has("montantDettesReglees")).isTrue();
        assertThat(paiementProperties.has("montantTotalDebite")).isTrue();
        assertThat(paiementProperties.has("dateConfirmationParticipation"))
                .isTrue();

        JsonNode statutPaiement = resoudreSchema(
                documentation,
                paiementProperties.path("statutPaiement")
        );

        assertThat(valeursEnum(statutPaiement))
                .containsExactlyInAnyOrder(
                        "EN_ATTENTE",
                        "PAYE",
                        "ANNULE"
                );

        JsonNode traitementEcheanceProperties = schemas
                .path("TraitementEcheanceResponse")
                .path("properties");

        assertThat(traitementEcheanceProperties.has("matchesAnalyses"))
                .isTrue();
        assertThat(traitementEcheanceProperties.has("matchesDemarres"))
                .isTrue();
        assertThat(traitementEcheanceProperties.has("matchesTermines"))
                .isTrue();
        assertThat(traitementEcheanceProperties.has("dettesCreees"))
                .isTrue();
    }

    @Test
    void shouldExposeSwaggerUi() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsStringIgnoringCase("swagger")
                ));
    }

    private JsonNode resoudreSchema(
            JsonNode documentation,
            JsonNode schema
    ) {
        String reference = schema.path("$ref").asText();

        if (reference.isBlank()) {
            return schema;
        }

        return documentation.at(reference.substring(1));
    }

    private List<String> valeursEnum(JsonNode schema) {
        List<String> valeurs = new ArrayList<>();

        schema.path("enum").forEach(valeur ->
                valeurs.add(valeur.asText())
        );

        return valeurs;
    }
}
