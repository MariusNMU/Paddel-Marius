package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.membre.InscriptionMembreRequest;
import com.padelMarius.backend.dto.membre.MembreResponse;
import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.service.MembreInscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MembreController.class)
class MembreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MembreInscriptionService membreInscriptionService;

    @Test
    void inscrireMembre_shouldReturnCreatedMember() throws Exception {
        String json = """
                {
                  "nom": "Durand",
                  "prenom": "Alice",
                  "categorieMembre": "GLOBAL",
                  "siteRattachementId": null
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
                true
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
                  "siteRattachementId": null
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
                  "siteRattachementId": null
                }
                """;

        mockMvc.perform(post("/api/membres/inscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}