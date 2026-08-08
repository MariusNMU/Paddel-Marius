package com.padelMarius.backend.security;

import com.padelMarius.backend.entity.Administrateur;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.exception.AuthentificationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

@Service
public class JwtService {

    public static final String TYPE_UTILISATEUR_JOUEUR = "JOUEUR";
    public static final String TYPE_UTILISATEUR_ADMIN = "ADMIN";

    public static final String TYPE_TOKEN_ACCES = "ACCES";
    public static final String TYPE_TOKEN_RAFRAICHISSEMENT =
            "RAFRAICHISSEMENT";

    private static final String PREFIXE_BEARER = "Bearer ";
    private static final String CLAIM_TYPE_UTILISATEUR = "typeUtilisateur";
    private static final String CLAIM_TYPE_TOKEN = "typeToken";

    private final SecretKey cleSignature;
    private final Duration dureeValiditeAcces;
    private final Duration dureeValiditeRafraichissement;
    private final Clock clock;

    @Autowired
    public JwtService(
            @Value("${padel.jwt.secret:padel-marius-dev-secret-change-me-2026}") String secret,
            @Value("${padel.jwt.expiration-minutes:60}") long expirationMinutes,
            @Value("${padel.jwt.refresh-expiration-days:7}") long refreshExpirationDays,
            Clock clock
    ) {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalArgumentException(
                    "Le secret JWT doit être configuré."
            );
        }

        if (expirationMinutes <= 0) {
            throw new IllegalArgumentException(
                    "La durée de validité JWT doit être positive."
            );
        }

        if (refreshExpirationDays <= 0) {
            throw new IllegalArgumentException(
                    "La durée du refresh token doit être positive."
            );
        }

        try {
            this.cleSignature = Keys.hmacShaKeyFor(
                    secret.getBytes(StandardCharsets.UTF_8)
            );
        } catch (WeakKeyException exception) {
            throw new IllegalArgumentException(
                    "Le secret JWT doit contenir au moins 32 octets.",
                    exception
            );
        }

        this.dureeValiditeAcces = Duration.ofMinutes(expirationMinutes);
        this.dureeValiditeRafraichissement =
                Duration.ofDays(refreshExpirationDays);
        this.clock = clock;
    }

    public JwtService(
            String secret,
            long expirationMinutes,
            Clock clock
    ) {
        this(secret, expirationMinutes, 7, clock);
    }

    public TokenGenere genererTokenJoueur(Membre membre) {
        verifierJoueur(membre);

        return genererToken(
                membre.getMatricule(),
                TYPE_UTILISATEUR_JOUEUR,
                TYPE_TOKEN_ACCES,
                dureeValiditeAcces
        );
    }

    public TokenGenere genererTokenAdmin(Administrateur administrateur) {
        verifierAdmin(administrateur);

        return genererToken(
                administrateur.getEmailOuLogin(),
                TYPE_UTILISATEUR_ADMIN,
                TYPE_TOKEN_ACCES,
                dureeValiditeAcces
        );
    }

    public TokenGenere genererRefreshTokenJoueur(Membre membre) {
        verifierJoueur(membre);

        return genererToken(
                membre.getMatricule(),
                TYPE_UTILISATEUR_JOUEUR,
                TYPE_TOKEN_RAFRAICHISSEMENT,
                dureeValiditeRafraichissement
        );
    }

    public TokenGenere genererRefreshTokenAdmin(
            Administrateur administrateur
    ) {
        verifierAdmin(administrateur);

        return genererToken(
                administrateur.getEmailOuLogin(),
                TYPE_UTILISATEUR_ADMIN,
                TYPE_TOKEN_RAFRAICHISSEMENT,
                dureeValiditeRafraichissement
        );
    }

    public JwtUtilisateur extraireUtilisateurDepuisAuthorization(
            String authorizationHeader
    ) {
        if (!StringUtils.hasText(authorizationHeader)) {
            throw new AuthentificationException("Token JWT obligatoire.");
        }

        String valeur = authorizationHeader.trim();

        if (!valeur.startsWith(PREFIXE_BEARER)) {
            throw new AuthentificationException("Token JWT obligatoire.");
        }

        String token = valeur.substring(PREFIXE_BEARER.length()).trim();

        if (!StringUtils.hasText(token)) {
            throw new AuthentificationException("Token JWT obligatoire.");
        }

        return validerToken(token);
    }

    public JwtUtilisateur validerToken(String token) {
        return validerToken(token, TYPE_TOKEN_ACCES);
    }

    public JwtUtilisateur validerRefreshToken(String token) {
        return validerToken(token, TYPE_TOKEN_RAFRAICHISSEMENT);
    }

    private JwtUtilisateur validerToken(
            String token,
            String typeTokenAttendu
    ) {
        if (!StringUtils.hasText(token)) {
            throw new AuthentificationException("Token JWT obligatoire.");
        }

        Claims claims;

        try {
            claims = Jwts.parser()
                    .verifyWith(cleSignature)
                    .clock(() -> Date.from(Instant.now(clock)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException exception) {
            throw new AuthentificationException("Token JWT expiré.");
        } catch (JwtException | IllegalArgumentException exception) {
            throw tokenInvalide();
        }

        String sujet = claims.getSubject();
        String typeUtilisateur = lireString(
                claims.get(CLAIM_TYPE_UTILISATEUR)
        );
        String typeToken = lireString(claims.get(CLAIM_TYPE_TOKEN));

        if (!StringUtils.hasText(sujet)
                || !StringUtils.hasText(typeUtilisateur)
                || !typeTokenAttendu.equals(typeToken)) {
            throw tokenInvalide();
        }

        return new JwtUtilisateur(
                sujet,
                typeUtilisateur
        );
    }

    private TokenGenere genererToken(
            String sujet,
            String typeUtilisateur,
            String typeToken,
            Duration dureeValidite
    ) {
        Instant maintenant = Instant.now(clock);
        Instant expiration = maintenant.plus(dureeValidite);

        JwtBuilder builder = Jwts.builder()
                .subject(sujet)
                .claim(CLAIM_TYPE_UTILISATEUR, typeUtilisateur)
                .claim(CLAIM_TYPE_TOKEN, typeToken)
                .issuedAt(Date.from(maintenant))
                .expiration(Date.from(expiration));

        String token = builder
                .signWith(cleSignature, Jwts.SIG.HS256)
                .compact();

        return new TokenGenere(
                token,
                LocalDateTime.ofInstant(expiration, clock.getZone())
        );
    }

    private void verifierJoueur(Membre membre) {
        if (membre == null
                || !StringUtils.hasText(membre.getMatricule())) {
            throw new AuthentificationException(
                    "Impossible de générer un token joueur sans matricule."
            );
        }
    }

    private void verifierAdmin(Administrateur administrateur) {
        if (administrateur == null
                || !StringUtils.hasText(
                        administrateur.getEmailOuLogin()
                )) {
            throw new AuthentificationException(
                    "Impossible de générer un token administrateur sans login."
            );
        }
    }

    private String lireString(Object valeur) {
        return valeur == null ? null : String.valueOf(valeur);
    }

    private AuthentificationException tokenInvalide() {
        return new AuthentificationException("Token JWT invalide.");
    }

    public record TokenGenere(
            String valeur,
            LocalDateTime expiration
    ) {
    }
}
