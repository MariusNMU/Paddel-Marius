package com.padelMarius.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RefreshTokenCookieService {

    public static final String NOM_COOKIE = "padel_refresh";

    private static final String CHEMIN_COOKIE = "/api/auth";
    private static final String SAME_SITE = "Strict";

    private final Duration dureeValidite;
    private final boolean secure;

    public RefreshTokenCookieService(
            @Value("${padel.jwt.refresh-expiration-days:7}") long expirationDays,
            @Value("${padel.jwt.refresh-cookie-secure:false}") boolean secure
    ) {
        if (expirationDays <= 0) {
            throw new IllegalArgumentException(
                    "La durée du cookie de refresh doit être positive."
            );
        }

        this.dureeValidite = Duration.ofDays(expirationDays);
        this.secure = secure;
    }

    public ResponseCookie creer(String token) {
        return cookie(token)
                .maxAge(dureeValidite)
                .build();
    }

    public ResponseCookie supprimer() {
        return cookie("")
                .maxAge(Duration.ZERO)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder cookie(String valeur) {
        return ResponseCookie.from(NOM_COOKIE, valeur)
                .httpOnly(true)
                .secure(secure)
                .sameSite(SAME_SITE)
                .path(CHEMIN_COOKIE);
    }
}
