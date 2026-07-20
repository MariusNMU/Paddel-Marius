package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.membre.InscriptionMembreRequest;
import com.padelMarius.backend.dto.membre.MembreResponse;
import com.padelMarius.backend.dto.membre.SoldeJoueurResponse;
import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.service.MembreInscriptionService;
import com.padelMarius.backend.service.MembreSoldeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MembreController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class MembreControllerTest {

    private static final String AUTHORIZATION =
            "Bearer jwt-joueur";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MembreInscriptionService membreInscriptionService;

    @MockitoBean
    private MembreSoldeService membreSoldeService;

    @Test
    void inscrireMembre_shouldReturnCreatedMember() throws Exception {
        String json = """
                {
                  "nom": "Durand",
                  "prenom": "Alice",
                  "categorieMembre": "GLOBAL",
                  "siteRattachementId": null,
                  "motDePasse": "MotDePasse2026!",
                  "confirmationMotDePasse": "MotDePasse2026!"
                }
                """;

        MembreResponse response = new MembreResponse(
                10L,
                "G1003",
                "Durand",
                "Alice",
                CategorieMembre.GLOBAL,
                null,
                null,
                true,
                new BigDecimal("100.00")
        );

        when(membreInscriptionService.inscrireMembre(any(InscriptionMembreRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/membres/inscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.membreId").value(10))
                .andExpect(jsonPath("$.matricule").value("G1003"))
                .andExpect(jsonPath("$.nom").value("Durand"))
                .andExpect(jsonPath("$.prenom").value("Alice"))
                .andExpect(jsonPath("$.categorieMembre").value("GLOBAL"))
                .andExpect(jsonPath("$.actif").value(true));
    }

    @Test
    void inscrireMembre_shouldReturnBadRequest_whenNomIsBlank() throws Exception {
        String json = """
                {
                  "nom": "",
                  "prenom": "Alice",
                  "categorieMembre": "GLOBAL",
                  "siteRattachementId": null,
                  "motDePasse": "MotDePasse2026!",
                  "confirmationMotDePasse": "MotDePasse2026!"
                }
                """;

        mockMvc.perform(post("/api/membres/inscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void inscrireMembre_shouldReturnBadRequest_whenCategorieIsMissing() throws Exception {
        String json = """
                {
                  "nom": "Durand",
                  "prenom": "Alice",
                  "siteRattachementId": null,
                  "motDePasse": "MotDePasse2026!",
                  "confirmationMotDePasse": "MotDePasse2026!"
                }
                """;

        mockMvc.perform(post("/api/membres/inscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void inscrireMembre_shouldReturnBadRequest_whenPasswordIsTooShort()
            throws Exception {
        String json = """
                {
                  "nom": "Durand",
                  "prenom": "Alice",
                  "categorieMembre": "GLOBAL",
                  "siteRattachementId": null,
                  "motDePasse": "court",
                  "confirmationMotDePasse": "MotDePasse2026!"
                }
                """;

        mockMvc.perform(post("/api/membres/inscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_INVALIDE"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Le champ 'motDePasse' est invalide : "
                                        + "Le mot de passe doit contenir "
                                        + "entre 12 et 72 caractères."
                        ));
    }

    @Test
    void consulterSolde_shouldReturnMemberBalance() throws Exception {
        when(membreSoldeService.consulterSolde("G1001"))
                .thenReturn(new SoldeJoueurResponse(
                        10L,
                        "G1001",
                        new BigDecimal("85.00")
                ));

        mockMvc.perform(get("/api/membres/G1001/solde")
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.membreId").value(10))
                .andExpect(jsonPath("$.matricule").value("G1001"))
                .andExpect(jsonPath("$.soldeCredit").value(85.00));
    }
}
