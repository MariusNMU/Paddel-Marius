package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.demo.CategorieMembreDemoResponse;
import com.padelMarius.backend.dto.demo.CompteAdministrateurDemoResponse;
import com.padelMarius.backend.dto.demo.CompteJoueurDemoResponse;
import com.padelMarius.backend.dto.demo.PresentationDemoResponse;
import com.padelMarius.backend.dto.site.SiteResponse;
import com.padelMarius.backend.service.PresentationDemoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PresentationDemoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
@TestPropertySource(properties = "padel.demo.enabled=true")
class PresentationDemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PresentationDemoService presentationDemoService;

    @Test
    void shouldReturnDemoPresentation() throws Exception {
        PresentationDemoResponse response =
                new PresentationDemoResponse(
                        List.of(new CategorieMembreDemoResponse(
                                "G",
                                "GLOBAL",
                                "Peut réserver jusqu'à 21 jours avant."
                        )),
                        List.of(new SiteResponse(
                                1001L,
                                "BRU",
                                "Padel Bruxelles",
                                "Rue du Padel 1"
                        )),
                        List.of(new CompteJoueurDemoResponse(
                                "G1001",
                                "password",
                                "joueur GLOBAL actif"
                        )),
                        List.of(new CompteAdministrateurDemoResponse(
                                "admin-global",
                                "secret",
                                "administrateur GLOBAL"
                        ))
                );

        when(presentationDemoService.consulterPresentation())
                .thenReturn(response);

        mockMvc.perform(get("/api/demo/presentation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.categoriesMembres[0].prefixe"
                ).value("G"))
                .andExpect(jsonPath(
                        "$.sites[0].siteId"
                ).value(1001))
                .andExpect(jsonPath(
                        "$.joueurs[0].matricule"
                ).value("G1001"))
                .andExpect(jsonPath(
                        "$.joueurs[0].motDePasse"
                ).value("password"))
                .andExpect(jsonPath(
                        "$.joueurs[0].motDePasseHash"
                ).doesNotExist())
                .andExpect(jsonPath(
                        "$.administrateurs[0].login"
                ).value("admin-global"))
                .andExpect(jsonPath(
                        "$.administrateurs[0].motDePasseHash"
                ).doesNotExist());
    }
}
