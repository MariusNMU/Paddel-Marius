package com.padelMarius.backend.security;


import com.padelMarius.backend.entity.Administrateur;
import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.RoleAdministrateur;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.exception.AuthentificationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET =
            "secret-de-test-suffisamment-long-pour-jwt-mvp";


    private final Clock clock = Clock.fixed(
            Instant.parse("2026-05-30T10:00:00Z"),
            ZoneId.of("UTC")
    );

    @Test
    void constructor_shouldRejectSecretShorterThanHs256Requirement() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new JwtService("trop-court", 60, clock)
        );

        assertEquals(
                "Le secret JWT doit contenir au moins 32 octets.",
                exception.getMessage()
        );
    }

    @Test
    void genererTokenAdmin_shouldReturnValidAdminToken() {
        JwtService jwtService = new JwtService(
                SECRET,
                120,
                clock
        );

        Site site = creerSite(1001L);

        Administrateur administrateur = Administrateur.builder()
                .emailOuLogin("admin-bruxelles")
                .roleAdministrateur(RoleAdministrateur.SITE)
                .site(site)
                .actif(true)
                .build();

        JwtService.TokenGenere token = jwtService.genererTokenAdmin(administrateur);

        JwtUtilisateur utilisateur = jwtService.extraireUtilisateurDepuisAuthorization(
                "Bearer " + token.valeur()
        );

        assertEquals("admin-bruxelles", utilisateur.sujet());
        assertEquals(JwtService.TYPE_UTILISATEUR_ADMIN, utilisateur.typeUtilisateur());
        assertFalse(lireClaims(token.valeur()).containsKey("role"));
        assertFalse(lireClaims(token.valeur()).containsKey("siteId"));
    }

    @Test
    void genererTokenJoueur_shouldReturnValidPlayerToken() {
        JwtService jwtService = new JwtService(
                SECRET,
                120,

                clock
        );

        Membre membre = Membre.builder()
                .matricule("G1001")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .build();

        JwtService.TokenGenere token = jwtService.genererTokenJoueur(membre);

        JwtUtilisateur utilisateur = jwtService.extraireUtilisateurDepuisAuthorization(
                "Bearer " + token.valeur()
        );

        assertEquals("G1001", utilisateur.sujet());
        assertEquals(JwtService.TYPE_UTILISATEUR_JOUEUR, utilisateur.typeUtilisateur());
        assertFalse(lireClaims(token.valeur()).containsKey("role"));
        assertFalse(lireClaims(token.valeur()).containsKey("siteId"));
    }

    @Test
    void validerToken_shouldRejectTamperedToken() {
        JwtService jwtService = new JwtService(
                SECRET,
                120,

                clock
        );

        Membre membre = Membre.builder()
                .matricule("G1001")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .build();

        JwtService.TokenGenere token = jwtService.genererTokenJoueur(membre);

        String tokenModifie = token.valeur().substring(0, token.valeur().length() - 2) + "xx";

        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> jwtService.validerToken(tokenModifie)
        );

        assertEquals("Token JWT invalide.", exception.getMessage());
    }

    @Test
    void validerToken_shouldRejectExpiredToken() {
        JwtService jwtService = new JwtService(
                SECRET,
                1,

                clock
        );

        Membre membre = Membre.builder()
                .matricule("G1001")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .build();

        JwtService.TokenGenere token = jwtService.genererTokenJoueur(membre);

        Clock clockApresExpiration = Clock.fixed(
                Instant.parse("2026-05-30T10:02:00Z"),
                ZoneId.of("UTC")
        );

        JwtService jwtServiceApresExpiration = new JwtService(
                SECRET,
                1,

                clockApresExpiration
        );

        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> jwtServiceApresExpiration.validerToken(token.valeur())
        );

        assertEquals("Token JWT expiré.", exception.getMessage());
    }

    @Test
    void refreshToken_shouldLastSevenDaysAndContainMinimalIdentity() {
        JwtService jwtService = new JwtService(
                SECRET,
                60,
                7,
                clock
        );

        Membre membre = Membre.builder()
                .matricule("G1001")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .build();

        JwtService.TokenGenere token =
                jwtService.genererRefreshTokenJoueur(membre);
        JwtUtilisateur utilisateur =
                jwtService.validerRefreshToken(token.valeur());

        assertEquals("G1001", utilisateur.sujet());
        assertEquals(
                JwtService.TYPE_UTILISATEUR_JOUEUR,
                utilisateur.typeUtilisateur()
        );
        assertFalse(lireClaims(token.valeur()).containsKey("role"));
        assertFalse(lireClaims(token.valeur()).containsKey("siteId"));
        assertEquals(
                java.time.LocalDateTime.of(2026, 6, 6, 10, 0),
                token.expiration()
        );
    }

    @Test
    void accessAndRefreshTokens_shouldNotBeInterchangeable() {
        JwtService jwtService = new JwtService(
                SECRET,
                60,
                7,
                clock
        );
        Membre membre = Membre.builder()
                .matricule("G1001")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .build();

        String accessToken = jwtService
                .genererTokenJoueur(membre)
                .valeur();
        String refreshToken = jwtService
                .genererRefreshTokenJoueur(membre)
                .valeur();

        assertThrows(
                AuthentificationException.class,
                () -> jwtService.validerRefreshToken(accessToken)
        );
        assertThrows(
                AuthentificationException.class,
                () -> jwtService.validerToken(refreshToken)
        );
    }

    @Test
    void application_shouldConfigureJwtExpirationAtSixtyMinutes()
            throws IOException {
        Properties properties = new Properties();

        try (InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream("application.properties")) {
            assertNotNull(inputStream);
            properties.load(inputStream);
        }

        assertEquals(
                "60",
                properties.getProperty(
                        "padel.jwt.expiration-minutes"
                )
        );
        assertEquals(
                "7",
                properties.getProperty(
                        "padel.jwt.refresh-expiration-days"
                )
        );
    }

    private Site creerSite(Long id) {
        Site site = Site.builder()
                .code("BRU")
                .nom("Padel Bruxelles")
                .adresse("Rue du Padel 1")
                .actif(true)
                .build();

        ReflectionTestUtils.setField(site, "id", id);

        return site;
    }

    private Claims lireClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(
                        SECRET.getBytes(StandardCharsets.UTF_8)
                ))
                .clock(() -> java.util.Date.from(Instant.now(clock)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
