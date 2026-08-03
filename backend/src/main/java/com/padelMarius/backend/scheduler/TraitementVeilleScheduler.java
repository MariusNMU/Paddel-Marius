package com.padelMarius.backend.scheduler;

import com.padelMarius.backend.dto.traitement.TraitementVeilleResponse;
import com.padelMarius.backend.service.TraitementVeilleService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "padel.traitement-veille.planification-active",
        havingValue = "true",
        matchIfMissing = true
)
public class TraitementVeilleScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            TraitementVeilleScheduler.class
    );

    private final TraitementVeilleService traitementVeilleService;
    private final Clock clock;

    @Scheduled(
            initialDelayString = "${padel.traitement-veille.delai-initial-ms:30000}",
            fixedDelayString = "${padel.traitement-veille.intervalle-ms:3600000}"
    )
    public void traiterVeilleDuJour() {
        LocalDate dateTraitement = LocalDate.now(clock);

        try {
            TraitementVeilleResponse resultat = traitementVeilleService
                    .traiterVeille(dateTraitement);

            LOGGER.info(
                    "Traitement automatique J-1 terminé : "
                            + "{} match(s) analysé(s), "
                            + "{} passé(s) public(s), "
                            + "{} participation(s) libérée(s).",
                    resultat.matchesAnalyses(),
                    resultat.matchesPassesPublics(),
                    resultat.participationsLiberees()
            );
        } catch (RuntimeException exception) {
            LOGGER.error(
                    "Le traitement automatique J-1 a échoué pour la date {}.",
                    dateTraitement,
                    exception
            );
        }
    }
}
