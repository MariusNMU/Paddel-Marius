package com.padelMarius.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenCookieServiceTest {

    @Test
    void shouldCreateHttpOnlyStrictCookieForSevenDays() {
        RefreshTokenCookieService service =
                new RefreshTokenCookieService(7, false);

        ResponseCookie cookie = service.creer("refresh-token");

        assertThat(cookie.getName())
                .isEqualTo(RefreshTokenCookieService.NOM_COOKIE);
        assertThat(cookie.getValue()).isEqualTo("refresh-token");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isFalse();
        assertThat(cookie.getSameSite()).isEqualTo("Strict");
        assertThat(cookie.getPath()).isEqualTo("/api/auth");
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofDays(7));
    }

    @Test
    void shouldExpireCookieOnLogout() {
        RefreshTokenCookieService service =
                new RefreshTokenCookieService(7, false);

        ResponseCookie cookie = service.supprimer();

        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ZERO);
        assertThat(cookie.isHttpOnly()).isTrue();
    }
}
