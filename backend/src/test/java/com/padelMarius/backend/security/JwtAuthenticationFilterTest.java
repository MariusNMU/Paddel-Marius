package com.padelMarius.backend.security;

import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.Membre;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    private static final String SECRET =
            "secret-de-test-suffisamment-long-pour-jwt-mvp";

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-05-30T10:00:00Z"),
            ZoneId.of("UTC")
    );

    @AfterEach
    void cleanSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldPopulateSecurityContext_whenBearerTokenIsValid()
            throws Exception {
        JwtService jwtService = new JwtService(SECRET, 120, clock);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                jwtService,
                new SecurityErrorWriter(new ObjectMapper())
        );

        Membre membre = Membre.builder()
                .matricule("G1001")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .build();

        JwtService.TokenGenere token = jwtService.genererTokenJoueur(membre);

        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/api/disponibilites"
        );
        request.addHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + token.valeur()
        );

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("G1001");
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder(
                        "ROLE_JOUEUR",
                        "ROLE_JOUEUR_GLOBAL"
                );
    }

    @Test
    void shouldReturnUnauthorized_whenBearerTokenIsInvalid()
            throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                new JwtService(SECRET, 120, clock),
                new SecurityErrorWriter(new ObjectMapper())
        );

        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/api/disponibilites"
        );
        request.addHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer token-invalide"
        );

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString())
                .contains("AUTHENTIFICATION_INVALIDE")
                .contains("Token JWT invalide.");
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();
    }

    @Test
    void shouldRejectInvalidBearerToken_evenWhenEndpointIsPublic()
            throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                new JwtService(SECRET, 120, clock),
                new SecurityErrorWriter(new ObjectMapper())
        );

        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST",
                "/api/auth/joueur"
        );
        request.addHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer token-invalide"
        );

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString())
                .contains("AUTHENTIFICATION_INVALIDE")
                .contains("Token JWT invalide.");
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();
    }
}
