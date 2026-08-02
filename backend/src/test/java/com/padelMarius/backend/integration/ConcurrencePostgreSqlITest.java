package com.padelMarius.backend.integration;

import com.padelMarius.backend.dto.dette.PayerDetteRequest;
import com.padelMarius.backend.dto.fermeture.CreerFermetureRequest;
import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.Dette;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.ModeCreation;
import com.padelMarius.backend.entity.ModeEntreeParticipation;
import com.padelMarius.backend.entity.NaturePaiement;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.Paiement;
import com.padelMarius.backend.entity.Participation;
import com.padelMarius.backend.entity.PorteeFermeture;
import com.padelMarius.backend.entity.RoleParticipation;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.StatutDette;
import com.padelMarius.backend.entity.StatutPaiement;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.entity.Terrain;
import com.padelMarius.backend.entity.VisibiliteMatch;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.repository.DetteRepository;
import com.padelMarius.backend.repository.MembreRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.PaiementRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import com.padelMarius.backend.repository.SiteRepository;
import com.padelMarius.backend.repository.TerrainRepository;
import com.padelMarius.backend.service.DetteService;
import com.padelMarius.backend.service.AdminFermetureService;
import com.padelMarius.backend.service.PaiementService;
import com.padelMarius.backend.service.TraitementEcheanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.padelMarius.backend.config.ReglesMetier.DUREE_MATCH;
import static com.padelMarius.backend.config.ReglesMetier.MONTANT_PARTICIPATION_STANDARD;
import static com.padelMarius.backend.config.ReglesMetier.PRIX_TOTAL_MATCH;
import static com.padelMarius.backend.config.ReglesMetier.SOLDE_INITIAL_JOUEUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.database-platform="
                + "org.hibernate.dialect.PostgreSQLDialect"
})
@Testcontainers
@DirtiesContext(
        classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD
)
class ConcurrencePostgreSqlITest {

    private static final BigDecimal MONTANT_DETTE =
            PRIX_TOTAL_MATCH.subtract(
                    MONTANT_PARTICIPATION_STANDARD
            );

    @Container
    private static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void configurerPostgreSql(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                POSTGRES::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                POSTGRES::getUsername
        );

        registry.add(
                "spring.datasource.password",
                POSTGRES::getPassword
        );

        registry.add(
                "spring.datasource.driver-class-name",
                () -> "org.postgresql.Driver"
        );
    }

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private TerrainRepository terrainRepository;

    @Autowired
    private MembreRepository membreRepository;

    @Autowired
    private PadelMatchRepository padelMatchRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private DetteRepository detteRepository;

    @Autowired
    private PaiementService paiementService;

    @Autowired
    private AdminFermetureService adminFermetureService;

    @Autowired
    private DetteService detteService;

    @Autowired
    private TraitementEcheanceService traitementEcheanceService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private Clock clock;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void nettoyerDonneesMetier() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                    paiement,
                    penalite,
                    dette,
                    participation,
                    padel_match,
                    administrateur,
                    membre,
                    fermeture,
                    horaire_annuel_site,
                    terrain,
                    site
                RESTART IDENTITY CASCADE
                """);
    }

    @Test
    void liquibase_doit_creer_le_schema_postgresql_une_seule_fois() {
        Integer nombreMigrations = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM databasechangelog
                WHERE id = '001-create-initial-schema'
                """,
                Integer.class
        );

        assertThat(nombreMigrations).isEqualTo(1);
    }

    @Test
    void verrou_match_doit_bloquer_une_transaction_concurrente()
            throws Exception {
        Terrain terrain = creerTerrain();

        PadelMatch match = creerMatch(
                terrain,
                maintenant().plusDays(1),
                EtatCycleMatch.A_VENIR
        );

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch verrouPris =
                new CountDownLatch(1);

        CountDownLatch libererVerrou =
                new CountDownLatch(1);

        CountDownLatch secondeTransactionDemarree =
                new CountDownLatch(1);

        try {
            Future<?> premierVerrou = executor.submit(() ->
                    transactionTemplate.executeWithoutResult(status -> {
                        padelMatchRepository
                                .findByIdForUpdate(match.getId())
                                .orElseThrow();

                        verrouPris.countDown();

                        attendre(libererVerrou);
                    })
            );

            assertThat(
                    verrouPris.await(
                            5,
                            TimeUnit.SECONDS
                    )
            ).isTrue();

            Future<?> secondVerrou = executor.submit(() ->
                    transactionTemplate.executeWithoutResult(status -> {
                        secondeTransactionDemarree.countDown();

                        padelMatchRepository
                                .findByIdForUpdate(match.getId())
                                .orElseThrow();
                    })
            );

            assertThat(
                    secondeTransactionDemarree.await(
                            5,
                            TimeUnit.SECONDS
                    )
            ).isTrue();

            try {
                assertThatThrownBy(() ->
                        secondVerrou.get(
                                750,
                                TimeUnit.MILLISECONDS
                        )
                ).isInstanceOf(TimeoutException.class);
            } finally {
                libererVerrou.countDown();
            }

            premierVerrou.get(
                    5,
                    TimeUnit.SECONDS
            );

            secondVerrou.get(
                    5,
                    TimeUnit.SECONDS
            );
        } finally {
            libererVerrou.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void deux_paiements_de_la_meme_participation_ne_doivent_creer_qu_un_paiement()
            throws Exception {
        Terrain terrain = creerTerrain();

        Membre joueur = creerMembre(
                SOLDE_INITIAL_JOUEUR
        );

        PadelMatch match = creerMatch(
                terrain,
                maintenant().plusDays(1),
                EtatCycleMatch.A_VENIR
        );

        Participation participation = creerParticipation(
                match,
                joueur,
                RoleParticipation.JOUEUR,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        List<ResultatConcurrent> resultats =
                executerEnParallele(
                        List.of(
                                () -> {
                                    paiementService
                                            .payerParticipationStandard(
                                                    participation.getId()
                                            );
                                    return null;
                                },
                                () -> {
                                    paiementService
                                            .payerParticipationStandard(
                                                    participation.getId()
                                            );
                                    return null;
                                }
                        )
                );

        verifierUnSuccesEtUnRefusMetier(resultats);

        assertThat(
                paiementRepository.findByParticipationId(
                        participation.getId()
                )
        ).isPresent();

        assertThat(paiementRepository.count())
                .isEqualTo(1L);

        Participation participationRechargee =
                participationRepository
                        .findById(participation.getId())
                        .orElseThrow();

        assertThat(
                participationRechargee.getStatutParticipation()
        ).isEqualTo(StatutParticipation.CONFIRMEE);

        Membre joueurRecharge =
                membreRepository.findById(joueur.getId())
                        .orElseThrow();

        assertThat(joueurRecharge.getSoldeCredit())
                .isEqualByComparingTo(
                        SOLDE_INITIAL_JOUEUR.subtract(
                                MONTANT_PARTICIPATION_STANDARD
                        )
                );
    }

    @Test
    void fermeture_et_paiement_concurrents_doivent_laisser_un_etat_financier_coherent()
            throws Exception {
        Terrain terrain = creerTerrain();
        Membre joueur = creerMembre(SOLDE_INITIAL_JOUEUR);

        PadelMatch match = creerMatch(
                terrain,
                maintenant().plusDays(1),
                EtatCycleMatch.A_VENIR
        );
        Participation participation = creerParticipation(
                match,
                joueur,
                RoleParticipation.JOUEUR,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        CreerFermetureRequest fermeture = new CreerFermetureRequest(
                match.getDateHeureDebut().toLocalDate(),
                PorteeFermeture.GLOBALE,
                null,
                "Fermeture concurrente de test"
        );

        List<ResultatConcurrent> resultats = executerEnParallele(
                List.of(
                        () -> {
                            paiementService.payerParticipationStandard(
                                    participation.getId()
                            );
                            return null;
                        },
                        () -> {
                            adminFermetureService.creerFermeture(fermeture);
                            return null;
                        }
                )
        );

        List<Throwable> erreurs = resultats.stream()
                .filter(resultat -> !resultat.reussi())
                .map(ResultatConcurrent::erreur)
                .toList();

        assertThat(erreurs)
                .allMatch(ConfigurationMetierException.class::isInstance);

        PadelMatch matchRecharge = padelMatchRepository
                .findById(match.getId())
                .orElseThrow();
        Membre joueurRecharge = membreRepository
                .findById(joueur.getId())
                .orElseThrow();
        List<Paiement> paiements = paiementRepository.findAll();

        assertThat(matchRecharge.getEtatCycle())
                .isEqualTo(EtatCycleMatch.ANNULE);
        assertThat(joueurRecharge.getSoldeCredit())
                .isEqualByComparingTo(SOLDE_INITIAL_JOUEUR);
        assertThat(paiements.size()).isLessThanOrEqualTo(1);
        assertThat(paiements)
                .allMatch(paiement ->
                        paiement.getStatutPaiement() == StatutPaiement.ANNULE
                );
    }

    @Test
    void paiement_dette_et_recalcul_concurrents_ne_doivent_pas_rouvrir_la_dette()
            throws Exception {
        Terrain terrain = creerTerrain();

        BigDecimal soldeApresParticipation =
                SOLDE_INITIAL_JOUEUR.subtract(
                        MONTANT_PARTICIPATION_STANDARD
                );

        Membre organisateur = creerMembre(
                soldeApresParticipation
        );

        PadelMatch match = creerMatch(
                terrain,
                maintenant().minusMinutes(30),
                EtatCycleMatch.DEMARRE
        );

        Participation participationOrganisateur =
                creerParticipation(
                        match,
                        organisateur,
                        RoleParticipation.ORGANISATEUR,
                        StatutParticipation.CONFIRMEE
                );

        creerPaiementParticipation(
                participationOrganisateur
        );

        Dette dette = creerDetteOuverte(
                match,
                organisateur,
                MONTANT_DETTE
        );

        List<ResultatConcurrent> resultats =
                executerEnParallele(
                        List.of(
                                () -> {
                                    detteService.payerDette(
                                            dette.getId(),
                                            new PayerDetteRequest(
                                                    MONTANT_DETTE
                                            )
                                    );
                                    return null;
                                },
                                () -> {
                                    PadelMatch matchRecharge =
                                            padelMatchRepository
                                                    .findById(match.getId())
                                                    .orElseThrow();

                                    detteService
                                            .actualiserDettePourMatch(
                                                    matchRecharge
                                            );

                                    return null;
                                }
                        )
                );

        verifierTousLesAppelsOntReussi(resultats);

        Dette detteRechargee =
                detteRepository.findById(dette.getId())
                        .orElseThrow();

        assertThat(detteRechargee.getStatutDette())
                .isEqualTo(StatutDette.REGLEE);

        assertThat(detteRechargee.getMontantRestant())
                .isEqualByComparingTo("0.00");

        assertThat(detteRechargee.getDateReglement())
                .isNotNull();

        assertThat(
                paiementRepository.findByDetteId(
                        dette.getId()
                )
        ).isPresent();

        assertThat(paiementRepository.count())
                .isEqualTo(2L);

        Membre organisateurRecharge =
                membreRepository
                        .findById(organisateur.getId())
                        .orElseThrow();

        assertThat(organisateurRecharge.getSoldeCredit())
                .isEqualByComparingTo(
                        soldeApresParticipation.subtract(
                                MONTANT_DETTE
                        )
                );
    }

    @Test
    void deux_traitements_echeance_concurrents_doivent_rester_idempotents()
            throws Exception {
        Terrain terrain = creerTerrain();

        Membre organisateur = creerMembre(
                SOLDE_INITIAL_JOUEUR.subtract(
                        MONTANT_PARTICIPATION_STANDARD
                )
        );

        PadelMatch match = creerMatch(
                terrain,
                maintenant().minusMinutes(5),
                EtatCycleMatch.A_VENIR
        );

        Participation participationOrganisateur =
                creerParticipation(
                        match,
                        organisateur,
                        RoleParticipation.ORGANISATEUR,
                        StatutParticipation.CONFIRMEE
                );

        creerPaiementParticipation(
                participationOrganisateur
        );

        List<ResultatConcurrent> resultats =
                executerEnParallele(
                        List.of(
                                () -> {
                                    traitementEcheanceService
                                            .traiterMatchesArrivesAEcheance();
                                    return null;
                                },
                                () -> {
                                    traitementEcheanceService
                                            .traiterMatchesArrivesAEcheance();
                                    return null;
                                }
                        )
                );

        verifierTousLesAppelsOntReussi(resultats);

        assertThat(detteRepository.count())
                .isEqualTo(1L);

        Dette dette =
                detteRepository.findByMatchId(match.getId())
                        .orElseThrow();

        assertThat(dette.getStatutDette())
                .isEqualTo(StatutDette.OUVERTE);

        assertThat(dette.getMontantInitial())
                .isEqualByComparingTo(MONTANT_DETTE);

        assertThat(dette.getMontantRestant())
                .isEqualByComparingTo(MONTANT_DETTE);

        PadelMatch matchRecharge =
                padelMatchRepository.findById(match.getId())
                        .orElseThrow();

        assertThat(matchRecharge.getEtatCycle())
                .isEqualTo(EtatCycleMatch.DEMARRE);

        assertThat(paiementRepository.count())
                .isEqualTo(1L);
    }

    private Terrain creerTerrain() {
        Site site = siteRepository.saveAndFlush(
                Site.builder()
                        .code("TST")
                        .nom("Site PostgreSQL de test")
                        .adresse("1 rue des Tests, Bruxelles")
                        .actif(true)
                        .build()
        );

        return terrainRepository.saveAndFlush(
                Terrain.builder()
                        .numero("T1")
                        .actif(true)
                        .site(site)
                        .build()
        );
    }

    private Membre creerMembre(BigDecimal soldeCredit) {
        return membreRepository.saveAndFlush(
                Membre.builder()
                        .matricule("T1001")
                        .nom("Test")
                        .prenom("PostgreSQL")
                        .categorieMembre(CategorieMembre.GLOBAL)
                        .siteRattachement(null)
                        .actif(true)
                        .motDePasseHash("hash-test")
                        .soldeCredit(soldeCredit)
                        .build()
        );
    }

    private PadelMatch creerMatch(
            Terrain terrain,
            LocalDateTime dateHeureDebut,
            EtatCycleMatch etatCycle
    ) {
        return padelMatchRepository.saveAndFlush(
                PadelMatch.builder()
                        .terrain(terrain)
                        .dateHeureDebut(dateHeureDebut)
                        .dateHeureFin(
                                dateHeureDebut.plus(DUREE_MATCH)
                        )
                        .modeCreation(ModeCreation.PUBLIC)
                        .visibiliteCourante(
                                VisibiliteMatch.PUBLIC
                        )
                        .prixTotal(PRIX_TOTAL_MATCH)
                        .dateCreation(
                                dateHeureDebut.minusDays(1)
                        )
                        .datePassagePublic(
                                dateHeureDebut.minusHours(1)
                        )
                        .etatCycle(etatCycle)
                        .build()
        );
    }

    private Participation creerParticipation(
            PadelMatch match,
            Membre membre,
            RoleParticipation roleParticipation,
            StatutParticipation statutParticipation
    ) {
        LocalDateTime dateAffectation =
                maintenant().minusMinutes(20);

        LocalDateTime dateConfirmation =
                statutParticipation
                        == StatutParticipation.CONFIRMEE
                        ? dateAffectation.plusMinutes(1)
                        : null;

        ModeEntreeParticipation modeEntree =
                roleParticipation
                        == RoleParticipation.ORGANISATEUR
                        ? ModeEntreeParticipation.CREATION
                        : ModeEntreeParticipation
                          .INSCRIPTION_PUBLIQUE;

        return participationRepository.saveAndFlush(
                Participation.builder()
                        .match(match)
                        .membre(membre)
                        .roleParticipation(roleParticipation)
                        .modeEntree(modeEntree)
                        .statutParticipation(
                                statutParticipation
                        )
                        .dateAffectation(dateAffectation)
                        .dateConfirmation(dateConfirmation)
                        .dateLiberation(null)
                        .build()
        );
    }

    private Paiement creerPaiementParticipation(
            Participation participation
    ) {
        return paiementRepository.saveAndFlush(
                Paiement.builder()
                        .membre(participation.getMembre())
                        .naturePaiement(
                                NaturePaiement.PARTICIPATION
                        )
                        .montant(
                                MONTANT_PARTICIPATION_STANDARD
                        )
                        .dateHeurePaiement(
                                maintenant().minusMinutes(10)
                        )
                        .statutPaiement(
                                StatutPaiement.PAYE
                        )
                        .participation(participation)
                        .dette(null)
                        .build()
        );
    }

    private Dette creerDetteOuverte(
            PadelMatch match,
            Membre responsable,
            BigDecimal montant
    ) {
        return detteRepository.saveAndFlush(
                Dette.builder()
                        .match(match)
                        .membreResponsable(responsable)
                        .montantInitial(montant)
                        .montantRestant(montant)
                        .dateCreation(
                                maintenant().minusMinutes(5)
                        )
                        .dateReglement(null)
                        .statutDette(StatutDette.OUVERTE)
                        .build()
        );
    }

    private LocalDateTime maintenant() {
        return LocalDateTime.now(clock)
                .withNano(0);
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

                                    return ResultatConcurrent
                                            .succes();
                                } catch (Throwable erreur) {
                                    return ResultatConcurrent
                                            .echec(erreur);
                                }
                            }))
                            .toList();

            assertThat(
                    prets.await(
                            5,
                            TimeUnit.SECONDS
                    )
            ).isTrue();

            depart.countDown();

            return futures.stream()
                    .map(this::recupererResultat)
                    .toList();
        } finally {
            depart.countDown();
            executor.shutdownNow();
        }
    }

    private ResultatConcurrent recupererResultat(
            Future<ResultatConcurrent> future
    ) {
        try {
            return future.get(
                    15,
                    TimeUnit.SECONDS
            );
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
        List<ResultatConcurrent> succes =
                resultats.stream()
                        .filter(
                                ResultatConcurrent::reussi
                        )
                        .toList();

        List<Throwable> erreurs =
                resultats.stream()
                        .filter(resultat ->
                                !resultat.reussi()
                        )
                        .map(ResultatConcurrent::erreur)
                        .toList();

        assertThat(succes).hasSize(1);
        assertThat(erreurs).hasSize(1);

        assertThat(erreurs.getFirst())
                .isInstanceOf(
                        ConfigurationMetierException.class
                );
    }

    private void verifierTousLesAppelsOntReussi(
            List<ResultatConcurrent> resultats
    ) {
        List<Throwable> erreurs =
                resultats.stream()
                        .filter(resultat ->
                                !resultat.reussi()
                        )
                        .map(ResultatConcurrent::erreur)
                        .toList();

        assertThat(erreurs).isEmpty();
    }

    private void attendre(CountDownLatch latch) {
        try {
            boolean libere = latch.await(
                    10,
                    TimeUnit.SECONDS
            );

            if (!libere) {
                throw new IllegalStateException(
                        "Le verrou PostgreSQL n'a pas été libéré."
                );
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "L'attente du verrou PostgreSQL "
                            + "a été interrompue.",
                    exception
            );
        }
    }

    private record ResultatConcurrent(
            boolean reussi,
            Throwable erreur
    ) {
        private static ResultatConcurrent succes() {
            return new ResultatConcurrent(
                    true,
                    null
            );
        }

        private static ResultatConcurrent echec(
                Throwable erreur
        ) {
            return new ResultatConcurrent(
                    false,
                    erreur
            );
        }
    }
}
