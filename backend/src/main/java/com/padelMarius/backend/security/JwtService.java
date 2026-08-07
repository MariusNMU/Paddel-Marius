package com.padelMarius.backend.security;

import com.padelMarius.backend.entity.Administrateur;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.exception.AuthentificationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
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

    private static final String PREFIXE_BEARER = "Bearer ";

    private final SecretKey cleSignature;
    private final Duration dureeValidite;
    private final Clock clock;

    public JwtService(
            @Value("${padel.jwt.secret:padel-marius-dev-secret-change-me-2026}") String secret,
            @Value("${padel.jwt.expiration-minutes:60}") long expirationMinutes,
            Clock clock
    ) {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalArgumentException("Le secret JWT doit être configuré.");
        }

        if (expirationMinutes <= 0) {
            throw new IllegalArgumentException("La durée de validité JWT doit être positive.");
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

        this.dureeValidite = Duration.ofMinutes(expirationMinutes);
        this.clock = clock;
    }

    public TokenGenere genererTokenJoueur(Membre membre) {
        if (membre == null || !StringUtils.hasText(membre.getMatricule())) {
            throw new AuthentificationException("Impossible de générer un token joueur sans matricule.");
        }

        Site site = membre.getSiteRattachement();

        return genererToken(
                membre.getMatricule(),
                TYPE_UTILISATEUR_JOUEUR,
                membre.getCategorieMembre() == null ? null : membre.getCategorieMembre().name(),
                site == null ? null : site.getId()
        );
    }

    public TokenGenere genererTokenAdmin(Administrateur administrateur) {
        if (administrateur == null || !StringUtils.hasText(administrateur.getEmailOuLogin())) {
            throw new AuthentificationException("Impossible de générer un token administrateur sans login.");
        }

        Site site = administrateur.getSite();

        return genererToken(
                administrateur.getEmailOuLogin(),
                TYPE_UTILISATEUR_ADMIN,
                administrateur.getRoleAdministrateur() == null ? null : administrateur.getRoleAdministrateur().name(),
                site == null ? null : site.getId()
        );
    }

    public JwtUtilisateur extraireUtilisateurDepuisAuthorization(String authorizationHeader) {
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
        String typeUtilisateur = lireString(claims.get("typeUtilisateur"));
        String role = lireString(claims.get("role"));
        Long siteId = lireLong(claims.get("siteId"));

        if (!StringUtils.hasText(sujet) || !StringUtils.hasText(typeUtilisateur)) {
            throw tokenInvalide();
        }

        return new JwtUtilisateur(
                sujet,
                typeUtilisateur,
                role,
                siteId
        );
    }

    private TokenGenere genererToken(
            String sujet,
            String typeUtilisateur,
            String role,
            Long siteId
    ) {
        Instant maintenant = Instant.now(clock);
        Instant expiration = maintenant.plus(dureeValidite);

        JwtBuilder builder = Jwts.builder()
                .subject(sujet)
                .claim("typeUtilisateur", typeUtilisateur)
                .issuedAt(Date.from(maintenant))
                .expiration(Date.from(expiration));

        if (StringUtils.hasText(role)) {
            builder.claim("role", role);
        }

        if (siteId != null) {
            builder.claim("siteId", siteId);
        }

        String token = builder
                .signWith(cleSignature, Jwts.SIG.HS256)
                .compact();

        return new TokenGenere(
                token,
                LocalDateTime.ofInstant(expiration, clock.getZone())
        );
    }

    private String lireString(Object valeur) {
        if (valeur == null) {
            return null;
        }

        return String.valueOf(valeur);
    }

    private Long lireLong(Object valeur) {
        if (valeur == null) {
            return null;
        }

        if (valeur instanceof Number nombre) {
            return nombre.longValue();
        }

        try {
            return Long.parseLong(String.valueOf(valeur));
        } catch (NumberFormatException exception) {
            throw tokenInvalide();
        }
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
