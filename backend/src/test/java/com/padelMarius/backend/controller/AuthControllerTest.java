package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.auth.AuthAdminResponse;
import com.padelMarius.backend.dto.auth.AuthJoueurResponse;
import com.padelMarius.backend.dto.auth.ConnexionAdminRequest;
import com.padelMarius.backend.dto.auth.ConnexionJoueurRequest;
import com.padelMarius.backend.dto.auth.RafraichissementTokenResponse;
import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.RoleAdministrateur;
import com.padelMarius.backend.exception.AuthentificationException;
import com.padelMarius.backend.security.RefreshTokenCookieService;
import com.padelMarius.backend.service.AuthService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        ApiExceptionHandler.class,
        RefreshTokenCookieService.class
})
class AuthControllerTest {

    private static final String MESSAGE_IDENTIFIANTS_INVALIDES =
            "Identifiant ou mot de passe invalide.";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void shouldReturnPlayerAndHttpOnlyRefreshCookie_whenLoginSucceeds()
            throws Exception {
        AuthJoueurResponse response = new AuthJoueurResponse(
                10L,
                "G0001",
                "Dupont",
                "Marie",
                CategorieMembre.GLOBAL,
                null,
                null,
                true,
                new BigDecimal("100.00"),
                "access-joueur",
                LocalDateTime.of(2026, 5, 30, 12, 0)
        );

        when(authService.authentifierJoueur(
                any(ConnexionJoueurRequest.class)
        )).thenReturn(new AuthService.ResultatAuthentification<>(
                response,
                "refresh-joueur"
        ));

        mockMvc.perform(post("/api/auth/joueur")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matricule": "G0001",
                                  "motDePasse": "password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.membreId").value(10))
                .andExpect(jsonPath("$.matricule").value("G0001"))
                .andExpect(jsonPath("$.token").value("access-joueur"))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        allOf(
                                containsString("padel_refresh=refresh-joueur"),
                                containsString("Path=/api/auth"),
                                containsString("HttpOnly"),
                                containsString("SameSite=Strict"),
                                containsString("Max-Age=604800")
                        )
                ));
    }

    @Test
    void shouldReturnAdminAndRefreshCookie_whenLoginSucceeds()
            throws Exception {
        AuthAdminResponse response = new AuthAdminResponse(
                20L,
                "admin-global",
                "Admin",
                "Global",
                RoleAdministrateur.GLOBAL,
                null,
                null,
                true,
                "access-admin",
                LocalDateTime.of(2026, 5, 30, 12, 0)
        );

        when(authService.authentifierAdmin(
                any(ConnexionAdminRequest.class)
        )).thenReturn(new AuthService.ResultatAuthentification<>(
                response,
                "refresh-admin"
        ));

        mockMvc.perform(post("/api/auth/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "admin-global",
                                  "motDePasse": "secret"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("admin-global"))
                .andExpect(jsonPath("$.token").value("access-admin"))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("padel_refresh=refresh-admin")
                ));
    }

    @Test
    void shouldRefreshAccessTokenAndRotateCookie() throws Exception {
        when(authService.rafraichir("refresh-valide"))
                .thenReturn(new AuthService.ResultatAuthentification<>(
                        new RafraichissementTokenResponse(
                                "nouvel-access",
                                LocalDateTime.of(2026, 5, 30, 13, 0)
                        ),
                        "nouveau-refresh"
                ));

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie(
                                RefreshTokenCookieService.NOM_COOKIE,
                                "refresh-valide"
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token")
                        .value("nouvel-access"))
                .andExpect(jsonPath("$.expirationToken")
                        .value("2026-05-30T13:00:00"))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        containsString("padel_refresh=nouveau-refresh")
                ));
    }

    @Test
    void shouldClearRefreshCookieOnLogout() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie(
                                RefreshTokenCookieService.NOM_COOKIE,
                                "refresh-logout"
                        )))
                .andExpect(status().isNoContent())
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        allOf(
                                containsString("padel_refresh="),
                                containsString("Max-Age=0"),
                                containsString("HttpOnly")
                        )
                ));

        verify(authService).deconnecter("refresh-logout");
    }

    @Test
    void shouldReturn401_whenRefreshCookieIsMissing() throws Exception {
        when(authService.rafraichir(null))
                .thenThrow(new AuthentificationException(
                        "Refresh token obligatoire."
                ));

        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code")
                        .value("AUTHENTIFICATION_INVALIDE"));
    }

    @Test
    void shouldReturn401_whenPlayerCredentialsAreInvalid() throws Exception {
        when(authService.authentifierJoueur(
                any(ConnexionJoueurRequest.class)
        )).thenThrow(new AuthentificationException(
                MESSAGE_IDENTIFIANTS_INVALIDES
        ));

        mockMvc.perform(post("/api/auth/joueur")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matricule": "G9999",
                                  "motDePasse": "mauvais"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code")
                        .value("AUTHENTIFICATION_INVALIDE"))
                .andExpect(jsonPath("$.message")
                        .value(MESSAGE_IDENTIFIANTS_INVALIDES));
    }

    @Test
    void shouldReturn401_whenAdminCredentialsAreInvalid() throws Exception {
        when(authService.authentifierAdmin(
                any(ConnexionAdminRequest.class)
        )).thenThrow(new AuthentificationException(
                MESSAGE_IDENTIFIANTS_INVALIDES
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
                .andExpect(jsonPath("$.code")
                        .value("AUTHENTIFICATION_INVALIDE"));
    }

    @Test
    void shouldReturn400_whenPlayerCredentialsAreMissing() throws Exception {
        mockMvc.perform(post("/api/auth/joueur")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matricule": "",
                                  "motDePasse": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_INVALIDE"));

        verify(authService, never()).authentifierJoueur(
                any(ConnexionJoueurRequest.class)
        );
    }
}
