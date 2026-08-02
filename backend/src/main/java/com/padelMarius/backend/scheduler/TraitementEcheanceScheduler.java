package com.padelMarius.backend.scheduler;

import com.padelMarius.backend.dto.traitement.TraitementEcheanceResponse;
import com.padelMarius.backend.service.TraitementEcheanceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "padel.traitement-echeance.planification-active",
        havingValue = "true",
        matchIfMissing = true
)
public class TraitementEcheanceScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            TraitementEcheanceScheduler.class
    );

    private final TraitementEcheanceService traitementEcheanceService;

    @Scheduled(
            initialDelayString = "${padel.traitement-echeance.delai-initial-ms:60000}",
            fixedDelayString = "${padel.traitement-echeance.intervalle-ms:60000}"
    )
    public void traiterEcheances() {
        try {
            TraitementEcheanceResponse resultat = traitementEcheanceService
                    .traiterMatchesArrivesAEcheance();

            LOGGER.info(
                    "Traitement automatique des échéances terminé : "
                            + "{} match(s) analysé(s), "
                            + "{} démarré(s), {} dette(s) créée(s).",
                    resultat.matchesAnalyses(),
                    resultat.matchesDemarres(),
                    resultat.dettesCreees()
            );
        } catch (RuntimeException exception) {
            LOGGER.error(
                    "Le traitement automatique des échéances a échoué.",
                    exception
            );
        }
    }
}
