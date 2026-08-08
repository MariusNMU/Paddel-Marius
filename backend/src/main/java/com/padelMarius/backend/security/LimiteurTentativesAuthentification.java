package com.padelMarius.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class LimiteurTentativesAuthentification {

    private final int maximumTentatives;
    private final Duration dureeFenetre;
    private final Clock clock;
    private final ConcurrentHashMap<CleLimite, FenetreTentatives> fenetres =
            new ConcurrentHashMap<>();

    @Autowired
    public LimiteurTentativesAuthentification(
            @Value("${padel.security.auth-rate-limit.max-attempts:5}")
            int maximumTentatives,
            @Value("${padel.security.auth-rate-limit.window-minutes:10}")
            long dureeFenetreMinutes,
            Clock clock
    ) {
        if (maximumTentatives <= 0) {
            throw new IllegalArgumentException(
                    "Le nombre maximal de tentatives doit être positif."
            );
        }

        if (dureeFenetreMinutes <= 0) {
            throw new IllegalArgumentException(
                    "La durée de limitation doit être positive."
            );
        }

        this.maximumTentatives = maximumTentatives;
        this.dureeFenetre = Duration.ofMinutes(dureeFenetreMinutes);
        this.clock = clock;
    }

    public Decision autoriser(
            String adresseClient,
            String endpoint
    ) {
        Instant maintenant = Instant.now(clock);
        nettoyerFenetresExpirees(maintenant);

        CleLimite cle = new CleLimite(
                normaliser(adresseClient),
                normaliser(endpoint)
        );
        AtomicReference<Decision> decision = new AtomicReference<>();

        fenetres.compute(cle, (cleIgnoree, fenetreExistante) -> {
            if (fenetreExistante == null
                    || !maintenant.isBefore(fenetreExistante.fin())) {
                decision.set(Decision.autorisee());
                return new FenetreTentatives(
                        maintenant.plus(dureeFenetre),
                        1
                );
            }

            if (fenetreExistante.nombreTentatives()
                    >= maximumTentatives) {
                decision.set(Decision.refusee(
                        secondesRestantes(
                                maintenant,
                                fenetreExistante.fin()
                        )
                ));
                return fenetreExistante;
            }

            decision.set(Decision.autorisee());
            return new FenetreTentatives(
                    fenetreExistante.fin(),
                    fenetreExistante.nombreTentatives() + 1
            );
        });

        return decision.get();
    }

    private void nettoyerFenetresExpirees(Instant maintenant) {
        fenetres.entrySet().removeIf(entree ->
                !maintenant.isBefore(entree.getValue().fin())
        );
    }

    private long secondesRestantes(
            Instant maintenant,
            Instant fin
    ) {
        long millisecondes = Duration.between(
                maintenant,
                fin
        ).toMillis();

        return Math.max(1, (millisecondes + 999) / 1000);
    }

    private String normaliser(String valeur) {
        return StringUtils.hasText(valeur)
                ? valeur.trim()
                : "inconnu";
    }

    private record CleLimite(
            String adresseClient,
            String endpoint
    ) {
    }

    private record FenetreTentatives(
            Instant fin,
            int nombreTentatives
    ) {
    }

    public record Decision(
            boolean autorise,
            long secondesAvantNouvelEssai
    ) {

        private static Decision autorisee() {
            return new Decision(true, 0);
        }

        private static Decision refusee(long secondes) {
            return new Decision(false, secondes);
        }
    }
}
