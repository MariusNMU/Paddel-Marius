package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.auth.AuthAdminResponse;
import com.padelMarius.backend.dto.auth.AuthJoueurResponse;
import com.padelMarius.backend.dto.auth.ConnexionAdminRequest;
import com.padelMarius.backend.dto.auth.ConnexionJoueurRequest;
import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.RoleAdministrateur;
import com.padelMarius.backend.exception.AuthentificationException;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(ApiExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void shouldReturnOk_whenPlayerAuthenticationSucceeds() throws Exception {
        AuthJoueurResponse response = new AuthJoueurResponse(
                10L,
                "G0001",
                "Dupont",
                "Marie",
                CategorieMembre.GLOBAL,
                null,
                null,
                true
        );

        when(authService.authentifierJoueur(any(ConnexionJoueurRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/joueur")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matricule": "G0001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.membreId").value(10))
                .andExpect(jsonPath("$.matricule").value("G0001"))
                .andExpect(jsonPath("$.nom").value("Dupont"))
                .andExpect(jsonPath("$.prenom").value("Marie"))
                .andExpect(jsonPath("$.categorieMembre").value("GLOBAL"))
                .andExpect(jsonPath("$.actif").value(true));
    }

    @Test
    void shouldReturnOk_whenAdminAuthenticationSucceeds() throws Exception {
        AuthAdminResponse response = new AuthAdminResponse(
                20L,
                "admin-global",
                "Admin",
                "Global",
                RoleAdministrateur.GLOBAL,
                null,
                null,
                true
        );

        when(authService.authentifierAdmin(any(ConnexionAdminRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "admin-global",
                                  "motDePasse": "secret"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.administrateurId").value(20))
                .andExpect(jsonPath("$.login").value("admin-global"))
                .andExpect(jsonPath("$.nom").value("Admin"))
                .andExpect(jsonPath("$.prenom").value("Global"))
                .andExpect(jsonPath("$.roleAdministrateur").value("GLOBAL"))
                .andExpect(jsonPath("$.actif").value(true));
    }

    @Test
    void shouldReturn404_whenPlayerMatriculeDoesNotExist() throws Exception {
        when(authService.authentifierJoueur(any(ConnexionJoueurRequest.class)))
                .thenThrow(new RessourceIntrouvableException(
                        "Membre introuvable avec le matricule G9999"
                ));

        mockMvc.perform(post("/api/auth/joueur")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matricule": "G9999"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESSOURCE_INTROUVABLE"))
                .andExpect(jsonPath("$.message").value(
                        "Membre introuvable avec le matricule G9999"
                ));
    }

    @Test
    void shouldReturn409_whenPlayerIsInactive() throws Exception {
        when(authService.authentifierJoueur(any(ConnexionJoueurRequest.class)))
                .thenThrow(new ConfigurationMetierException(
                        "Le membre est inactif."
                ));

        mockMvc.perform(post("/api/auth/joueur")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matricule": "G0001"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFIGURATION_METIER_INVALIDE"))
                .andExpect(jsonPath("$.message").value("Le membre est inactif."));
    }

    @Test
    void shouldReturn401_whenAdminCredentialsAreInvalid() throws Exception {
        when(authService.authentifierAdmin(any(ConnexionAdminRequest.class)))
                .thenThrow(new AuthentificationException(
                        "Identifiants administrateur invalides."
                ));

        mockMvc.perform(post("/api/auth/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "admin-global",
                                  "motDePasse": "mauvais"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTIFICATION_INVALIDE"))
                .andExpect(jsonPath("$.message").value(
                        "Identifiants administrateur invalides."
                ));
    }

    @Test
    void shouldReturn400_whenPlayerMatriculeIsMissing() throws Exception {
        mockMvc.perform(post("/api/auth/joueur")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matricule": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .authentifierJoueur(any(ConnexionJoueurRequest.class));
    }

    @Test
    void shouldReturn400_whenAdminPasswordIsMissing() throws Exception {
        mockMvc.perform(post("/api/auth/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "admin-global",
                                  "motDePasse": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .authentifierAdmin(any(ConnexionAdminRequest.class));
    }
}