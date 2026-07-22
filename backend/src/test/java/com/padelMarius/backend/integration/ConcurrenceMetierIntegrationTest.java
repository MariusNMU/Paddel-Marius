package com.padelMarius.backend.integration;

import com.padelMarius.backend.dto.match.CreerMatchRequest;
import com.padelMarius.backend.dto.participation.InscriptionPubliqueRequest;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.ModeCreation;
import com.padelMarius.backend.entity.ModeEntreeParticipation;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.Participation;
import com.padelMarius.backend.entity.RoleParticipation;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.repository.MembreRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.PaiementRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import com.padelMarius.backend.service.MatchCreationService;
import com.padelMarius.backend.service.PaiementService;
import com.padelMarius.backend.service.ParticipationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/data.sql")
@DirtiesContext(
        classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD
)
class ConcurrenceMetierIntegrationTest {

    private static final long MATCH_PUBLIC_ID = 3001L;
    private static final long MATCH_PRIVE_ID = 3002L;
    private static final long TERRAIN_LIBRE_ID = 1103L;

    @Autowired
    private MatchCreationService matchCreationService;

    @Autowired
    private ParticipationService participationService;

    @Autowired
    private PaiementService paiementService;

    @Autowired
    private MembreRepository membreRepository;

    @Autowired
    private PadelMatchRepository padelMatchRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private PaiementRepository paiementRepository;

    @Test
    void deuxInscriptionsSimultaneesNeDoiventPasDepasserQuatreParticipants()
            throws Exception {
        PadelMatch match = padelMatchRepository.findById(MATCH_PUBLIC_ID)
                .orElseThrow();
        Membre troisiemeJoueur =
                membreRepository.findByMatricule("L1001")
                        .orElseThrow();

        participationRepository.saveAndFlush(
                creerParticipation(match, troisiemeJoueur)
        );

        List<ResultatConcurrent> resultats = executerEnParallele(
                List.of(
                        () -> {
                            participationService
                                    .inscrireParticipantPublic(
                                            MATCH_PUBLIC_ID,
                                            new InscriptionPubliqueRequest(
                                                    "S1002"
                                            )
                                    );
                            return null;
                        },
                        () -> {
                            participationService
                                    .inscrireParticipantPublic(
                                            MATCH_PUBLIC_ID,
                                            new InscriptionPubliqueRequest(
                                                    "L1002"
                                            )
                                    );
                            return null;
                        }
                )
        );

        verifierUnSuccesEtUnRefusMetier(resultats);

        long participantsActifs = participationRepository
                .countByMatchIdAndStatutParticipationNot(
                        MATCH_PUBLIC_ID,
                        StatutParticipation.LIBEREE
                );

        assertThat(participantsActifs).isEqualTo(4L);
    }

    @Test
    void deuxCreationsSimultaneesNeDoiventPasReserverLeMemeCreneau()
            throws Exception {
        LocalDateTime debut = LocalDate.now()
                .plusDays(20)
                .atTime(8, 0);

        List<ResultatConcurrent> resultats = executerEnParallele(
                List.of(
                        () -> {
                            matchCreationService.creerMatch(
                                    new CreerMatchRequest(
                                            TERRAIN_LIBRE_ID,
                                            "G1001",
                                            debut,
                                            ModeCreation.PUBLIC
                                    )
                            );
                            return null;
                        },
                        () -> {
                            matchCreationService.creerMatch(
                                    new CreerMatchRequest(
                                            TERRAIN_LIBRE_ID,
                                            "L1002",
                                            debut,
                                            ModeCreation.PUBLIC
                                    )
                            );
                            return null;
                        }
                )
        );

        verifierUnSuccesEtUnRefusMetier(resultats);

        long matchesCrees = padelMatchRepository
                .findByTerrainId(TERRAIN_LIBRE_ID)
                .stream()
                .filter(match ->
                        match.getDateHeureDebut().equals(debut)
                )
                .filter(match ->
                        match.getEtatCycle() != EtatCycleMatch.ANNULE
                )
                .count();

        assertThat(matchesCrees).isEqualTo(1L);
    }

    @Test
    void deuxPaiementsSimultanesNeDoiventPasDepasserLeSoldeDisponible()
            throws Exception {
        Membre membre = membreRepository.findByMatricule("L1002")
                .orElseThrow();

        membre.setSoldeCredit(new BigDecimal("20.00"));
        membreRepository.saveAndFlush(membre);

        Participation premiereParticipation =
                participationRepository.saveAndFlush(
                        creerParticipation(
                                padelMatchRepository
                                        .findById(MATCH_PUBLIC_ID)
                                        .orElseThrow(),
                                membre
                        )
                );

        Participation deuxiemeParticipation =
                participationRepository.saveAndFlush(
                        creerParticipation(
                                padelMatchRepository
                                        .findById(MATCH_PRIVE_ID)
                                        .orElseThrow(),
                                membre
                        )
                );

        List<ResultatConcurrent> resultats = executerEnParallele(
                List.of(
                        () -> {
                            paiementService
                                    .payerParticipationStandard(
                                            premiereParticipation.getId()
                                    );
                            return null;
                        },
                        () -> {
                            paiementService
                                    .payerParticipationStandard(
                                            deuxiemeParticipation.getId()
                                    );
                            return null;
                        }
                )
        );

        verifierUnSuccesEtUnRefusMetier(resultats);

        Membre membreRecharge =
                membreRepository.findById(membre.getId())
                        .orElseThrow();

        assertThat(membreRecharge.getSoldeCredit())
                .isEqualByComparingTo("5.00");

        boolean premierPaiementExiste =
                paiementRepository.existsByParticipationId(
                        premiereParticipation.getId()
                );

        boolean secondPaiementExiste =
                paiementRepository.existsByParticipationId(
                        deuxiemeParticipation.getId()
                );

        assertThat(premierPaiementExiste)
                .isNotEqualTo(secondPaiementExiste);
    }

    private Participation creerParticipation(
            PadelMatch match,
            Membre membre
    ) {
        return Participation.builder()
                .match(match)
                .membre(membre)
                .roleParticipation(RoleParticipation.JOUEUR)
                .modeEntree(
                        ModeEntreeParticipation.INSCRIPTION_PUBLIQUE
                )
                .statutParticipation(
                        StatutParticipation.EN_ATTENTE_PAIEMENT
                )
                .dateAffectation(LocalDateTime.now())
                .build();
    }

    private List<ResultatConcurrent> executerEnParallele(
            List<Callable<Void>> actions
    ) throws Exception {
        ExecutorService executor =
                Executors.newFixedThreadPool(actions.size());

        CountDownLatch prets =
                new CountDownLatch(actions.size());
        CountDownLatch depart =
                new CountDownLatch(1);

        try {
            List<Future<ResultatConcurrent>> futures =
                    actions.stream()
                            .map(action -> executor.submit(() -> {
                                prets.countDown();
                                depart.await(
                                        5,
                                        TimeUnit.SECONDS
                                );

                                try {
                                    action.call();
                                    return ResultatConcurrent.succes();
                                } catch (Throwable erreur) {
                                    return ResultatConcurrent.echec(
                                            erreur
                                    );
                                }
                            }))
                            .toList();

            assertThat(
                    prets.await(5, TimeUnit.SECONDS)
            ).isTrue();

            depart.countDown();

            return futures.stream()
                    .map(this::recupererResultat)
                    .toList();
        } finally {
            executor.shutdownNow();
        }
    }

    private ResultatConcurrent recupererResultat(
            Future<ResultatConcurrent> future
    ) {
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Le scénario concurrent ne s'est pas terminé.",
                    exception
            );
        }
    }

    private void verifierUnSuccesEtUnRefusMetier(
            List<ResultatConcurrent> resultats
    ) {
        List<ResultatConcurrent> succes = resultats.stream()
                .filter(ResultatConcurrent::reussi)
                .toList();

        List<Throwable> erreurs = resultats.stream()
                .filter(resultat -> !resultat.reussi())
                .map(ResultatConcurrent::erreur)
                .toList();

        assertThat(succes).hasSize(1);
        assertThat(erreurs).hasSize(1);
        assertThat(erreurs.getFirst())
                .isInstanceOf(
                        ConfigurationMetierException.class
                );
    }

    private record ResultatConcurrent(
            boolean reussi,
            Throwable erreur
    ) {
        private static ResultatConcurrent succes() {
            return new ResultatConcurrent(true, null);
        }

        private static ResultatConcurrent echec(
                Throwable erreur
        ) {
            return new ResultatConcurrent(false, erreur);
        }
    }
}
