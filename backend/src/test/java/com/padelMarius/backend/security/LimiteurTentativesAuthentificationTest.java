package com.padelMarius.backend.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class LimiteurTentativesAuthentificationTest {

    @Test
    void shouldRejectRequestsAfterConfiguredMaximum() {
        MutableClock clock = new MutableClock(
                Instant.parse("2026-05-20T10:00:00Z")
        );
        LimiteurTentativesAuthentification limiteur =
                new LimiteurTentativesAuthentification(2, 10, clock);

        assertThat(limiteur.autoriser(
                "127.0.0.1",
                "/api/auth/joueur"
        ).autorise()).isTrue();
        assertThat(limiteur.autoriser(
                "127.0.0.1",
                "/api/auth/joueur"
        ).autorise()).isTrue();

        LimiteurTentativesAuthentification.Decision decision =
                limiteur.autoriser(
                        "127.0.0.1",
                        "/api/auth/joueur"
                );

        assertThat(decision.autorise()).isFalse();
        assertThat(decision.secondesAvantNouvelEssai()).isEqualTo(600);
    }

    @Test
    void shouldKeepClientAndEndpointCountersIndependent() {
        MutableClock clock = new MutableClock(
                Instant.parse("2026-05-20T10:00:00Z")
        );
        LimiteurTentativesAuthentification limiteur =
                new LimiteurTentativesAuthentification(1, 10, clock);

        assertThat(limiteur.autoriser(
                "127.0.0.1",
                "/api/auth/joueur"
        ).autorise()).isTrue();
        assertThat(limiteur.autoriser(
                "127.0.0.1",
                "/api/auth/joueur"
        ).autorise()).isFalse();

        assertThat(limiteur.autoriser(
                "127.0.0.1",
                "/api/auth/refresh"
        ).autorise()).isTrue();
        assertThat(limiteur.autoriser(
                "127.0.0.2",
                "/api/auth/joueur"
        ).autorise()).isTrue();
    }

    @Test
    void shouldOpenANewWindowAfterDelay() {
        MutableClock clock = new MutableClock(
                Instant.parse("2026-05-20T10:00:00Z")
        );
        LimiteurTentativesAuthentification limiteur =
                new LimiteurTentativesAuthentification(1, 10, clock);

        limiteur.autoriser("127.0.0.1", "/api/auth/admin");
        assertThat(limiteur.autoriser(
                "127.0.0.1",
                "/api/auth/admin"
        ).autorise()).isFalse();

        clock.avancerDeSecondes(600);

        assertThat(limiteur.autoriser(
                "127.0.0.1",
                "/api/auth/admin"
        ).autorise()).isTrue();
    }

    private static final class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void avancerDeSecondes(long secondes) {
            instant = instant.plusSeconds(secondes);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
