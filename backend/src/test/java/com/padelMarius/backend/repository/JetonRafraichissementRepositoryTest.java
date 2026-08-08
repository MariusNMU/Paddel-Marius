package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.JetonRafraichissement;
import com.padelMarius.backend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class JetonRafraichissementRepositoryTest {

    @Autowired
    private JetonRafraichissementRepository repository;

    @Test
    void shouldPersistAndLockRefreshTokenIdentity() {
        JetonRafraichissement jeton = repository.saveAndFlush(
                nouveauJeton(
                        "550e8400-e29b-41d4-a716-446655440000",
                        LocalDateTime.of(2026, 8, 15, 12, 0)
                )
        );

        JetonRafraichissement verrouille = repository
                .findByIdentifiantForUpdate(jeton.getIdentifiant())
                .orElseThrow();

        assertThat(verrouille.getSujet()).isEqualTo("G1001");
        assertThat(verrouille.isRevoque()).isFalse();
    }

    @Test
    void shouldDeleteOnlyExpiredRefreshTokens() {
        repository.save(nouveauJeton(
                "550e8400-e29b-41d4-a716-446655440001",
                LocalDateTime.of(2026, 8, 7, 12, 0)
        ));
        repository.save(nouveauJeton(
                "550e8400-e29b-41d4-a716-446655440002",
                LocalDateTime.of(2026, 8, 15, 12, 0)
        ));
        repository.flush();

        long supprimes = repository.deleteByDateExpirationBefore(
                LocalDateTime.of(2026, 8, 8, 12, 0)
        );

        assertThat(supprimes).isEqualTo(1);
        assertThat(repository.count()).isEqualTo(1);
    }

    private JetonRafraichissement nouveauJeton(
            String identifiant,
            LocalDateTime expiration
    ) {
        return new JetonRafraichissement(
                identifiant,
                expiration,
                "G1001",
                JwtService.TYPE_UTILISATEUR_JOUEUR
        );
    }
}
