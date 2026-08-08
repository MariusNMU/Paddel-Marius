package com.padelMarius.backend.security;

import com.padelMarius.backend.entity.JetonRafraichissement;
import com.padelMarius.backend.exception.AuthentificationException;
import com.padelMarius.backend.repository.JetonRafraichissementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JetonRafraichissementServiceTest {

    private static final LocalDateTime MAINTENANT =
            LocalDateTime.of(2026, 8, 8, 12, 0);

    @Mock
    private JetonRafraichissementRepository repository;

    private JetonRafraichissementService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-08-08T10:00:00Z"),
                ZoneId.of("Europe/Brussels")
        );
        service = new JetonRafraichissementService(repository, clock);
    }

    @Test
    void enregistrer_shouldPersistOnlyServerSideTokenIdentity() {
        JwtService.TokenGenere token = new JwtService.TokenGenere(
                "jwt-brut-non-stocke",
                MAINTENANT.plusDays(7),
                "550e8400-e29b-41d4-a716-446655440000"
        );

        service.enregistrer(
                token,
                "G1001",
                JwtService.TYPE_UTILISATEUR_JOUEUR
        );

        verify(repository).deleteByDateExpirationBefore(MAINTENANT);

        ArgumentCaptor<JetonRafraichissement> captor =
                ArgumentCaptor.forClass(JetonRafraichissement.class);
        verify(repository).save(captor.capture());

        JetonRafraichissement enregistre = captor.getValue();
        assertThat(enregistre.getIdentifiant())
                .isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(enregistre.getSujet()).isEqualTo("G1001");
        assertThat(enregistre.getTypeUtilisateur()).isEqualTo("JOUEUR");
        assertThat(enregistre.isRevoque()).isFalse();
    }

    @Test
    void consommer_shouldRevokeActiveMatchingToken() {
        JetonRafraichissement jeton = joueurActif();
        when(repository.findByIdentifiantForUpdate("token-id"))
                .thenReturn(Optional.of(jeton));

        service.consommer(new JwtUtilisateur(
                "G1001",
                JwtService.TYPE_UTILISATEUR_JOUEUR,
                "token-id"
        ));

        assertThat(jeton.isRevoque()).isTrue();
        assertThat(jeton.getDateRevocation()).isEqualTo(MAINTENANT);
    }

    @Test
    void consommer_shouldRejectReplayOfRevokedToken() {
        JetonRafraichissement jeton = joueurActif();
        jeton.revoquer(MAINTENANT.minusMinutes(1));
        when(repository.findByIdentifiantForUpdate("token-id"))
                .thenReturn(Optional.of(jeton));

        assertThatThrownBy(() -> service.consommer(new JwtUtilisateur(
                "G1001",
                JwtService.TYPE_UTILISATEUR_JOUEUR,
                "token-id"
        )))
                .isInstanceOf(AuthentificationException.class)
                .hasMessage("Refresh token invalide.");
    }

    @Test
    void consommer_shouldRejectTokenWhoseIdentityDoesNotMatchDatabase() {
        when(repository.findByIdentifiantForUpdate("token-id"))
                .thenReturn(Optional.of(joueurActif()));

        assertThatThrownBy(() -> service.consommer(new JwtUtilisateur(
                "admin-global",
                JwtService.TYPE_UTILISATEUR_ADMIN,
                "token-id"
        )))
                .isInstanceOf(AuthentificationException.class)
                .hasMessage("Refresh token invalide.");
    }

    @Test
    void revoquerSiPresent_shouldRemainIdempotent() {
        when(repository.findByIdentifiantForUpdate("absent"))
                .thenReturn(Optional.empty());

        service.revoquerSiPresent("absent");

        verify(repository).findByIdentifiantForUpdate("absent");
    }

    private JetonRafraichissement joueurActif() {
        return new JetonRafraichissement(
                "token-id",
                MAINTENANT.plusDays(7),
                "G1001",
                JwtService.TYPE_UTILISATEUR_JOUEUR
        );
    }
}
