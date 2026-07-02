package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.fermeture.CreerFermetureRequest;
import com.padelMarius.backend.dto.fermeture.FermetureAdminResponse;
import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.Fermeture;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.ModeCreation;
import com.padelMarius.backend.entity.NaturePaiement;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.Paiement;
import com.padelMarius.backend.entity.Participation;
import com.padelMarius.backend.entity.PorteeFermeture;
import com.padelMarius.backend.entity.RoleParticipation;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.StatutPaiement;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.entity.Terrain;
import com.padelMarius.backend.entity.VisibiliteMatch;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.FermetureRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.PaiementRepository;
import com.padelMarius.backend.repository.SiteRepository;
import com.padelMarius.backend.repository.TerrainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminFermetureServiceTest {

    @Mock
    private FermetureRepository fermetureRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private TerrainRepository terrainRepository;

    @Mock
    private PadelMatchRepository padelMatchRepository;

    @Mock
    private PaiementRepository paiementRepository;

    private AdminFermetureService adminFermetureService;

    @BeforeEach
    void setUp() {
        adminFermetureService = new AdminFermetureService(
                fermetureRepository,
                siteRepository,
                terrainRepository,
                padelMatchRepository,
                paiementRepository
        );
    }

    @Test
    void creerFermeture_shouldCreateGlobalClosureAndCancelFutureMatches() {
        LocalDate dateFermeture = LocalDate.of(2026, 7, 21);

        CreerFermetureRequest request = new CreerFermetureRequest(
                dateFermeture,
                PorteeFermeture.GLOBALE,
                null,
                "Fermeture exceptionnelle"
        );

        Terrain terrain = creerTerrain(10L, creerSite(1L));
        PadelMatch match = creerMatch(100L, terrain, dateFermeture.atTime(9, 0));

        when(fermetureRepository.existsByDateFermetureAndPorteeAndSiteIsNull(
                dateFermeture,
                PorteeFermeture.GLOBALE
        )).thenReturn(false);

        when(fermetureRepository.save(any(Fermeture.class)))
                .thenAnswer(invocation -> {
                    Fermeture fermeture = invocation.getArgument(0);
                    ReflectionTestUtils.setField(fermeture, "id", 50L);
                    return fermeture;
                });

        when(terrainRepository.findAll())
                .thenReturn(List.of(terrain));

        when(padelMatchRepository.findByTerrainInAndDateHeureDebutGreaterThanEqualAndDateHeureDebutBeforeAndEtatCycle(
                List.of(terrain),
                dateFermeture.atStartOfDay(),
                dateFermeture.atTime(LocalTime.MAX),
                EtatCycleMatch.A_VENIR
        )).thenReturn(List.of(match));

        when(paiementRepository.findByParticipation_Match_IdAndNaturePaiementAndStatutPaiement(
                match.getId(),
                NaturePaiement.PARTICIPATION,
                StatutPaiement.PAYE
        )).thenReturn(List.of());

        FermetureAdminResponse response = adminFermetureService.creerFermeture(request);

        assertEquals(50L, response.fermetureId());
        assertEquals(dateFermeture, response.dateFermeture());
        assertEquals(PorteeFermeture.GLOBALE, response.portee());
        assertEquals(null, response.siteId());
        assertEquals("Fermeture exceptionnelle", response.motif());
        assertEquals(1, response.nombreMatchesAnnules());
        assertEquals(EtatCycleMatch.ANNULE, match.getEtatCycle());
    }

    @Test
    void creerFermeture_shouldCreateLocalClosureForSite() {
        LocalDate dateFermeture = LocalDate.of(2026, 8, 15);

        Site site = creerSite(1001L);
        Terrain terrain = creerTerrain(1101L, site);
        PadelMatch match = creerMatch(3001L, terrain, dateFermeture.atTime(11, 0));

        CreerFermetureRequest request = new CreerFermetureRequest(
                dateFermeture,
                PorteeFermeture.LOCALE,
                1001L,
                "Maintenance Bruxelles"
        );

        when(siteRepository.findById(1001L))
                .thenReturn(Optional.of(site));

        when(fermetureRepository.existsBySiteIdAndDateFermetureAndPortee(
                1001L,
                dateFermeture,
                PorteeFermeture.LOCALE
        )).thenReturn(false);

        when(fermetureRepository.save(any(Fermeture.class)))
                .thenAnswer(invocation -> {
                    Fermeture fermeture = invocation.getArgument(0);
                    ReflectionTestUtils.setField(fermeture, "id", 51L);
                    return fermeture;
                });

        when(terrainRepository.findBySiteAndActifTrue(site))
                .thenReturn(List.of(terrain));

        when(padelMatchRepository.findByTerrainInAndDateHeureDebutGreaterThanEqualAndDateHeureDebutBeforeAndEtatCycle(
                List.of(terrain),
                dateFermeture.atStartOfDay(),
                dateFermeture.atTime(LocalTime.MAX),
                EtatCycleMatch.A_VENIR
        )).thenReturn(List.of(match));

        when(paiementRepository.findByParticipation_Match_IdAndNaturePaiementAndStatutPaiement(
                match.getId(),
                NaturePaiement.PARTICIPATION,
                StatutPaiement.PAYE
        )).thenReturn(List.of());

        FermetureAdminResponse response = adminFermetureService.creerFermeture(request);

        assertEquals(51L, response.fermetureId());
        assertEquals(dateFermeture, response.dateFermeture());
        assertEquals(PorteeFermeture.LOCALE, response.portee());
        assertEquals(1001L, response.siteId());
        assertEquals("Padel 1001", response.nomSite());
        assertEquals("Maintenance Bruxelles", response.motif());
        assertEquals(1, response.nombreMatchesAnnules());
        assertEquals(EtatCycleMatch.ANNULE, match.getEtatCycle());
    }

    @Test
    void creerFermeture_shouldRefundPaidParticipationAndCancelPayment() {
        LocalDate dateFermeture = LocalDate.of(2026, 8, 15);

        Site site = creerSite(1001L);
        Terrain terrain = creerTerrain(1101L, site);
        PadelMatch match = creerMatch(3001L, terrain, dateFermeture.atTime(11, 0));

        Membre joueur = creerMembre(20L, "G0001", new BigDecimal("85.00"));
        Participation participation = creerParticipation(400L, match, joueur);
        Paiement paiement = creerPaiementParticipation(
                500L,
                joueur,
                participation,
                new BigDecimal("15.00")
        );

        CreerFermetureRequest request = new CreerFermetureRequest(
                dateFermeture,
                PorteeFermeture.LOCALE,
                1001L,
                "Maintenance Bruxelles"
        );

        when(siteRepository.findById(1001L))
                .thenReturn(Optional.of(site));

        when(fermetureRepository.existsBySiteIdAndDateFermetureAndPortee(
                1001L,
                dateFermeture,
                PorteeFermeture.LOCALE
        )).thenReturn(false);

        when(fermetureRepository.save(any(Fermeture.class)))
                .thenAnswer(invocation -> {
                    Fermeture fermeture = invocation.getArgument(0);
                    ReflectionTestUtils.setField(fermeture, "id", 51L);
                    return fermeture;
                });

        when(terrainRepository.findBySiteAndActifTrue(site))
                .thenReturn(List.of(terrain));

        when(padelMatchRepository.findByTerrainInAndDateHeureDebutGreaterThanEqualAndDateHeureDebutBeforeAndEtatCycle(
                List.of(terrain),
                dateFermeture.atStartOfDay(),
                dateFermeture.atTime(LocalTime.MAX),
                EtatCycleMatch.A_VENIR
        )).thenReturn(List.of(match));

        when(paiementRepository.findByParticipation_Match_IdAndNaturePaiementAndStatutPaiement(
                match.getId(),
                NaturePaiement.PARTICIPATION,
                StatutPaiement.PAYE
        )).thenReturn(List.of(paiement));

        FermetureAdminResponse response = adminFermetureService.creerFermeture(request);

        assertEquals(1, response.nombreMatchesAnnules());
        assertEquals(1, response.nombreRemboursementsCredites());
        assertEquals(0, new BigDecimal("15.00").compareTo(response.montantTotalRembourse()));
        assertEquals(0, new BigDecimal("100.00").compareTo(joueur.getSoldeCredit()));
        assertEquals(StatutPaiement.ANNULE, paiement.getStatutPaiement());
        assertEquals(EtatCycleMatch.ANNULE, match.getEtatCycle());
    }

    @Test
    void creerFermeture_shouldRejectGlobalClosureWithSite() {
        CreerFermetureRequest request = new CreerFermetureRequest(
                LocalDate.of(2026, 7, 21),
                PorteeFermeture.GLOBALE,
                1001L,
                "Erreur"
        );

        ConfigurationMetierException exception = assertThrows(
                ConfigurationMetierException.class,
                () -> adminFermetureService.creerFermeture(request)
        );

        assertEquals(
                "Une fermeture globale ne doit pas avoir de site.",
                exception.getMessage()
        );
    }

    @Test
    void creerFermeture_shouldRejectLocalClosureWithoutSite() {
        CreerFermetureRequest request = new CreerFermetureRequest(
                LocalDate.of(2026, 8, 15),
                PorteeFermeture.LOCALE,
                null,
                "Erreur"
        );

        ConfigurationMetierException exception = assertThrows(
                ConfigurationMetierException.class,
                () -> adminFermetureService.creerFermeture(request)
        );

        assertEquals(
                "Une fermeture locale doit avoir un site.",
                exception.getMessage()
        );
    }

    @Test
    void creerFermeture_shouldRejectUnknownSite() {
        CreerFermetureRequest request = new CreerFermetureRequest(
                LocalDate.of(2026, 8, 15),
                PorteeFermeture.LOCALE,
                9999L,
                "Site inconnu"
        );

        when(siteRepository.findById(9999L))
                .thenReturn(Optional.empty());

        RessourceIntrouvableException exception = assertThrows(
                RessourceIntrouvableException.class,
                () -> adminFermetureService.creerFermeture(request)
        );

        assertEquals(
                "Site introuvable avec l'id 9999",
                exception.getMessage()
        );
    }

    @Test
    void creerFermeture_shouldRejectDuplicateClosure() {
        LocalDate dateFermeture = LocalDate.of(2026, 7, 21);

        CreerFermetureRequest request = new CreerFermetureRequest(
                dateFermeture,
                PorteeFermeture.GLOBALE,
                null,
                "Doublon"
        );

        when(fermetureRepository.existsByDateFermetureAndPorteeAndSiteIsNull(
                dateFermeture,
                PorteeFermeture.GLOBALE
        )).thenReturn(true);

        ConfigurationMetierException exception = assertThrows(
                ConfigurationMetierException.class,
                () -> adminFermetureService.creerFermeture(request)
        );

        assertEquals(
                "Une fermeture existe déjà pour cette date et ce périmètre.",
                exception.getMessage()
        );
    }

    private Site creerSite(Long id) {
        Site site = Site.builder()
                .code("SITE-" + id)
                .nom("Padel " + id)
                .adresse("Adresse " + id)
                .actif(true)
                .build();

        ReflectionTestUtils.setField(site, "id", id);

        return site;
    }

    private Terrain creerTerrain(Long id, Site site) {
        Terrain terrain = Terrain.builder()
                .site(site)
                .numero("T" + id)
                .actif(true)
                .build();

        ReflectionTestUtils.setField(terrain, "id", id);

        return terrain;
    }

    private PadelMatch creerMatch(Long id, Terrain terrain, LocalDateTime dateHeureDebut) {
        PadelMatch match = PadelMatch.builder()
                .terrain(terrain)
                .dateHeureDebut(dateHeureDebut)
                .dateHeureFin(dateHeureDebut.plusMinutes(90))
                .modeCreation(ModeCreation.PUBLIC)
                .visibiliteCourante(VisibiliteMatch.PUBLIC)
                .prixTotal(new BigDecimal("60.00"))
                .dateCreation(LocalDateTime.of(2026, 5, 1, 10, 0))
                .etatCycle(EtatCycleMatch.A_VENIR)
                .build();

        ReflectionTestUtils.setField(match, "id", id);

        return match;
    }

    private Membre creerMembre(Long id, String matricule, BigDecimal soldeCredit) {
        Membre membre = Membre.builder()
                .matricule(matricule)
                .nom("Nom " + id)
                .prenom("Prenom " + id)
                .motDePasseHash("$2y$10$w7Hmtss9GA8U9RAxfZeb3.JmBalmCw64iEo6pY5YEgNky9FM7OriK")
                .categorieMembre(CategorieMembre.GLOBAL)
                .soldeCredit(soldeCredit)
                .actif(true)
                .build();

        ReflectionTestUtils.setField(membre, "id", id);

        return membre;
    }

    private Participation creerParticipation(Long id, PadelMatch match, Membre membre) {
        Participation participation = Participation.builder()
                .match(match)
                .membre(membre)
                .roleParticipation(RoleParticipation.JOUEUR)
                .statutParticipation(StatutParticipation.CONFIRMEE)
                .dateAffectation(LocalDateTime.of(2026, 5, 1, 10, 0))
                .dateConfirmation(LocalDateTime.of(2026, 5, 1, 10, 5))
                .build();

        ReflectionTestUtils.setField(participation, "id", id);

        return participation;
    }

    private Paiement creerPaiementParticipation(
            Long id,
            Membre membre,
            Participation participation,
            BigDecimal montant
    ) {
        Paiement paiement = Paiement.builder()
                .membre(membre)
                .naturePaiement(NaturePaiement.PARTICIPATION)
                .montant(montant)
                .dateHeurePaiement(LocalDateTime.of(2026, 5, 10, 12, 0))
                .statutPaiement(StatutPaiement.PAYE)
                .participation(participation)
                .dette(null)
                .build();

        ReflectionTestUtils.setField(paiement, "id", id);

        return paiement;
    }
}
