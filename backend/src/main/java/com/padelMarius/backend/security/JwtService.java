package com.padelMarius.backend.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.padelMarius.backend.entity.Administrateur;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.exception.AuthentificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.IOException;

@Service
public class JwtService {

    public static final String TYPE_UTILISATEUR_JOUEUR = "JOUEUR";
    public static final String TYPE_UTILISATEUR_ADMIN = "ADMIN";

    private static final String ALGORITHME_HMAC = "HmacSHA256";
    private static final String ALGORITHME_JWT = "HS256";
    private static final String PREFIXE_BEARER = "Bearer ";

    private final String secret;
    private final Duration dureeValidite;
    private final ObjectMapper objectMapper;
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

        this.secret = secret;
        this.dureeValidite = Duration.ofMinutes(expirationMinutes);
        this.objectMapper = new ObjectMapper();
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

        String[] morceaux = token.split("\\.");

        if (morceaux.length != 3) {
            throw tokenInvalide();
        }

        String headerBase64 = morceaux[0];
        String payloadBase64 = morceaux[1];
        String signatureFournie = morceaux[2];

        String contenuSigne = headerBase64 + "." + payloadBase64;
        String signatureAttendue = signer(contenuSigne);

        boolean signatureValide = MessageDigest.isEqual(
                signatureAttendue.getBytes(StandardCharsets.UTF_8),
                signatureFournie.getBytes(StandardCharsets.UTF_8)
        );

        if (!signatureValide) {
            throw tokenInvalide();
        }

        Map<String, Object> payload = lirePayload(payloadBase64);

        Long expirationEpochSecond = lireLong(payload.get("exp"));

        if (expirationEpochSecond == null) {
            throw tokenInvalide();
        }

        Instant maintenant = Instant.now(clock);

        if (!maintenant.isBefore(Instant.ofEpochSecond(expirationEpochSecond))) {
            throw new AuthentificationException("Token JWT expiré.");
        }

        String sujet = lireString(payload.get("sub"));
        String typeUtilisateur = lireString(payload.get("typeUtilisateur"));
        String role = lireString(payload.get("role"));
        Long siteId = lireLong(payload.get("siteId"));

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

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", ALGORITHME_JWT);
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", sujet);
        payload.put("typeUtilisateur", typeUtilisateur);
        payload.put("role", role);
        payload.put("siteId", siteId);
        payload.put("iat", maintenant.getEpochSecond());
        payload.put("exp", expiration.getEpochSecond());

        String headerBase64 = encoderJsonBase64(header);
        String payloadBase64 = encoderJsonBase64(payload);
        String contenuSigne = headerBase64 + "." + payloadBase64;
        String signature = signer(contenuSigne);

        return new TokenGenere(
                contenuSigne + "." + signature,
                LocalDateTime.ofInstant(expiration, clock.getZone())
        );
    }

    private String encoderJsonBase64(Map<String, Object> valeur) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(valeur);

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(json);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Impossible de sérialiser le JWT.", exception);
        }
    }

    private Map<String, Object> lirePayload(String payloadBase64) {
        try {
            byte[] json = Base64.getUrlDecoder().decode(payloadBase64);

            return objectMapper.readValue(
                    json,
                    new TypeReference<Map<String, Object>>() {
                    }
            );
        } catch (IllegalArgumentException | IOException exception) {
            throw tokenInvalide();
        }
    }

    private String signer(String contenu) {
        try {
            Mac mac = Mac.getInstance(ALGORITHME_HMAC);
            SecretKeySpec cle = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    ALGORITHME_HMAC
            );

            mac.init(cle);

            byte[] signature = mac.doFinal(contenu.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(signature);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Impossible de signer le JWT.", exception);
        }
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
