package com.padelMarius.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "padel.demo.enabled=true")
@AutoConfigureMockMvc
@Sql(scripts = "/data.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldPermitAuthenticationEndpointWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/joueur")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matricule": "G1001",
                                  "motDePasse": "password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void shouldPermitDemoPresentationWithoutToken() throws Exception {
        mockMvc.perform(get("/api/demo/presentation"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldPermitAuthenticationEndpointWhenInvalidBearerTokenIsPresent()
            throws Exception {
        mockMvc.perform(post("/api/auth/joueur")
                        .header(AUTHORIZATION, "Bearer token-invalide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matricule": "G1001",
                                  "motDePasse": "password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void shouldRejectPlayerEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/disponibilites")
                        .param("siteId", "1001")
                        .param("date", "2026-05-20"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code")
                        .value("AUTHENTIFICATION_INVALIDE"));
    }

    @Test
    void shouldRejectAdminEndpointWithPlayerToken() throws Exception {
        String playerToken = authenticatePlayerAndReadToken();

        mockMvc.perform(get("/api/admin/statistiques")
                        .header(AUTHORIZATION, bearer(playerToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCES_REFUSE"));
    }

    @Test
    void shouldRejectPlayerAccessToAnotherPlayerBalance() throws Exception {
        String playerToken = authenticatePlayerAndReadToken();

        mockMvc.perform(get("/api/membres/S1001/solde")
                        .header(AUTHORIZATION, bearer(playerToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCES_REFUSE"));
    }

    @Test
    void shouldReturnNotFoundWhenProtectedParticipationDoesNotExist()
            throws Exception {
        String playerToken = authenticatePlayerAndReadToken();

        mockMvc.perform(post("/api/participations/999999/paiements")
                        .header(AUTHORIZATION, bearer(playerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "montant": 15.00
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESSOURCE_INTROUVABLE"));
    }

    @Test
    void shouldReturnForbiddenWhenProtectedParticipationBelongsToAnotherPlayer()
            throws Exception {
        String playerToken = authenticatePlayerAndReadToken();

        mockMvc.perform(post("/api/participations/3202/paiements")
                        .header(AUTHORIZATION, bearer(playerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "montant": 15.00
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCES_REFUSE"));
    }

    @Test
    void shouldReturnNotFoundWhenProtectedDebtDoesNotExist()
            throws Exception {
        String playerToken = authenticatePlayerAndReadToken();

        mockMvc.perform(post("/api/dettes/999999/paiements")
                        .header(AUTHORIZATION, bearer(playerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "montant": 30.00
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESSOURCE_INTROUVABLE"));
    }

    @Test
    void shouldReturnForbiddenWhenProtectedDebtBelongsToAnotherPlayer()
            throws Exception {
        String playerToken = authenticatePlayerAndReadToken();

        mockMvc.perform(post("/api/dettes/4001/paiements")
                        .header(AUTHORIZATION, bearer(playerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "montant": 30.00
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCES_REFUSE"));
    }

    @Test
    void shouldRejectSiteAdminAccessToGlobalStatistics() throws Exception {
        String adminToken = authenticateAdminAndReadToken("admin-bruxelles");

        mockMvc.perform(get("/api/admin/statistiques")
                        .header(AUTHORIZATION, bearer(adminToken))
                        .param("dateDebut", "2026-05-01")
                        .param("dateFin", "2026-05-31"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCES_REFUSE"));
    }

    @Test
    void shouldAllowSiteAdminAccessToOwnSiteStatistics() throws Exception {
        String adminToken = authenticateAdminAndReadToken("admin-bruxelles");

        mockMvc.perform(get("/api/admin/statistiques")
                        .header(AUTHORIZATION, bearer(adminToken))
                        .param("dateDebut", "2026-05-01")
                        .param("dateFin", "2026-05-31")
                        .param("siteId", "1001"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowSiteAdminAccessToOwnSiteOperationalState() throws Exception {
        String adminToken = authenticateAdminAndReadToken("admin-bruxelles");

        mockMvc.perform(get("/api/admin/etat-operationnel")
                        .header(AUTHORIZATION, bearer(adminToken))
                        .param("date", "2026-05-20")
                        .param("siteId", "1001"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectSiteAdminAccessToAnotherSiteOperationalState() throws Exception {
        String adminToken = authenticateAdminAndReadToken("admin-bruxelles");

        mockMvc.perform(get("/api/admin/etat-operationnel")
                        .header(AUTHORIZATION, bearer(adminToken))
                        .param("date", "2026-05-20")
                        .param("siteId", "1002"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCES_REFUSE"));
    }

    @Test
    void shouldAllowSiteAdminAccessToOwnSiteWeeklyOccupation() throws Exception {
        String adminToken = authenticateAdminAndReadToken("admin-bruxelles");

        mockMvc.perform(get("/api/admin/etat-operationnel/semaine")
                        .header(AUTHORIZATION, bearer(adminToken))
                        .param("date", "2026-05-20")
                        .param("siteId", "1001"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectSiteAdminAccessToAnotherSiteWeeklyOccupation() throws Exception {
        String adminToken = authenticateAdminAndReadToken("admin-bruxelles");

        mockMvc.perform(get("/api/admin/etat-operationnel/semaine")
                        .header(AUTHORIZATION, bearer(adminToken))
                        .param("date", "2026-05-20")
                        .param("siteId", "1002"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCES_REFUSE"));
    }

    private String authenticatePlayerAndReadToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/joueur")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matricule": "G1001",
                                  "motDePasse": "password"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse()
                .getContentAsString());

        String token = body.get("token").asText();
        assertThat(token).isNotBlank();

        return token;
    }

    private String authenticateAdminAndReadToken(String login) throws Exception {
        String motDePasse = switch (login) {
            case "admin-global" -> "secret";
            case "admin-bruxelles", "admin-namur" -> "secret-site";
            default -> "password";
        };

        MvcResult result = mockMvc.perform(post("/api/auth/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "%s",
                                  "motDePasse": "%s"
                                }
                                """.formatted(login, motDePasse)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse()
                .getContentAsString());

        String token = body.get("token").asText();
        assertThat(token).isNotBlank();

        return token;
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
