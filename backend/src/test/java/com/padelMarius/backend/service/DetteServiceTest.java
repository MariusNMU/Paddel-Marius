package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.dette.DetteResponse;
import com.padelMarius.backend.dto.dette.PaiementDetteResponse;
import com.padelMarius.backend.dto.dette.PayerDetteRequest;
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
import com.padelMarius.backend.entity.RoleParticipation;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.StatutDette;
import com.padelMarius.backend.entity.StatutPaiement;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.entity.Terrain;
import com.padelMarius.backend.entity.VisibiliteMatch;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.DetteRepository;
import com.padelMarius.backend.repository.MembreRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.PaiementRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DetteServiceTest {

    @Mock
    private PadelMatchRepository padelMatchRepository;

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private DetteRepository detteRepository;

    @Mock
    private MembreRepository membreRepository;

    private DetteService detteService;

    @BeforeEach
    void setUp() {
        Clock clockFixe = Clock.fixed(
                LocalDate.of(2026, 5, 7)
                        .atTime(12, 0)
                        .atZone(ZoneId.systemDefault())
                        .toInstant(),
                ZoneId.systemDefault()
        );

        detteService = new DetteService(
                padelMatchRepository,
                participationRepository,
                paiementRepository,
                detteRepository,
                membreRepository,
                clockFixe
        );
    }

    @Test
    void genererDettePourMatch_shouldCreateDebt_whenMatchIsNotFullyPaid() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain);
        Membre organisateur = creerMembre(20L, "G0001");

        Participation participationOrganisateur = creerParticipation(
                300L,
                match,
                organisateur,
                RoleParticipation.ORGANISATEUR
        );

        Paiement paiement1 = creerPaiementParticipation(400L, organisateur, new BigDecimal("15.00"));
        Paiement paiement2 = creerPaiementParticipation(401L, organisateur, new BigDecimal("15.00"));

        when(padelMatchRepository.findById(100L)).thenReturn(Optional.of(match));
        when(detteRepository.findByMatchId(100L)).thenReturn(Optional.empty());
        when(participationRepository.findByMatchId(100L)).thenReturn(List.of(participationOrganisateur));
        when(paiementRepository.findByParticipation_Match_IdAndNaturePaiementAndStatutPaiement(
                100L,
                NaturePaiement.PARTICIPATION,
                StatutPaiement.PAYE
        )).thenReturn(List.of(paiement1, paiement2));

        when(detteRepository.save(any(Dette.class))).thenAnswer(invocation -> {
            Dette dette = invocation.getArgument(0);
            ReflectionTestUtils.setField(dette, "id", 500L);
            return dette;
        });

        DetteResponse response = detteService.genererDettePourMatch(100L);

        assertEquals(500L, response.detteId());
        assertEquals(100L, response.matchId());
        assertEquals(20L, response.membreResponsableId());
        assertEquals("G0001", response.matriculeResponsable());
        assertEquals(0, new BigDecimal("30.00").compareTo(response.montantInitial()));
        assertEquals(0, new BigDecimal("30.00").compareTo(response.montantRestant()));
        assertEquals(StatutDette.OUVERTE, response.statutDette());
        assertEquals(LocalDateTime.of(2026, 5, 7, 12, 0), response.dateCreation());

        verify(detteRepository).save(any(Dette.class));
    }

    @Test
    void genererDettePourMatch_shouldReject_whenMatchDoesNotExist() {
        when(padelMatchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                RessourceIntrouvableException.class,
                () -> detteService.genererDettePourMatch(999L)
        );

        verify(detteRepository, never()).save(any());
    }

    @Test
    void genererDettePourMatch_shouldUpdateExistingDebt_whenDebtAlreadyExistsForMatch() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain);
        Membre organisateur = creerMembre(20L, "G0001");
        Dette detteExistante = creerDette(500L, match, organisateur, new BigDecimal("30.00"), StatutDette.OUVERTE);
        Participation participationOrganisateur = creerParticipation(
                300L,
                match,
                organisateur,
                RoleParticipation.ORGANISATEUR
        );

        when(padelMatchRepository.findById(100L)).thenReturn(Optional.of(match));
        when(detteRepository.findByMatchId(100L)).thenReturn(Optional.of(detteExistante));
        when(participationRepository.findByMatchId(100L)).thenReturn(List.of(participationOrganisateur));
        when(paiementRepository.findByParticipation_Match_IdAndNaturePaiementAndStatutPaiement(
                100L,
                NaturePaiement.PARTICIPATION,
                StatutPaiement.PAYE
        )).thenReturn(List.of());
        when(detteRepository.save(any(Dette.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DetteResponse response = detteService.genererDettePourMatch(100L);

        assertEquals(500L, response.detteId());
        assertEquals(0, new BigDecimal("60.00").compareTo(response.montantRestant()));
        assertEquals(StatutDette.OUVERTE, response.statutDette());
        verify(detteRepository).save(detteExistante);
    }

    @Test
    void genererDettePourMatch_shouldReject_whenMatchIsFullyPaid() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain);
        Membre organisateur = creerMembre(20L, "G0001");

        Participation participationOrganisateur = creerParticipation(
                300L,
                match,
                organisateur,
                RoleParticipation.ORGANISATEUR
        );

        Paiement paiement1 = creerPaiementParticipation(400L, organisateur, new BigDecimal("15.00"));
        Paiement paiement2 = creerPaiementParticipation(401L, organisateur, new BigDecimal("15.00"));
        Paiement paiement3 = creerPaiementParticipation(402L, organisateur, new BigDecimal("15.00"));
        Paiement paiement4 = creerPaiementParticipation(403L, organisateur, new BigDecimal("15.00"));

        when(padelMatchRepository.findById(100L)).thenReturn(Optional.of(match));
        when(detteRepository.findByMatchId(100L)).thenReturn(Optional.empty());
        when(participationRepository.findByMatchId(100L)).thenReturn(List.of(participationOrganisateur));
        when(paiementRepository.findByParticipation_Match_IdAndNaturePaiementAndStatutPaiement(
                100L,
                NaturePaiement.PARTICIPATION,
                StatutPaiement.PAYE
        )).thenReturn(List.of(paiement1, paiement2, paiement3, paiement4));

        assertThrows(
                ConfigurationMetierException.class,
                () -> detteService.genererDettePourMatch(100L)
        );

        verify(detteRepository, never()).save(any());
    }

    @Test
    void actualiser_dette_pour_match_doit_ignorer_un_match_futur() {
        // Arrange
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatchFutur(100L, terrain);

        // Act
        Optional<DetteResponse> response =
                detteService.actualiserDettePourMatch(match);

        // Assert
        assertTrue(response.isEmpty());
        verifyNoInteractions(
                detteRepository,
                participationRepository,
                paiementRepository
        );
    }

    @Test
    void generer_dette_doit_refuser_un_match_avant_echeance() {
        // Arrange
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatchFutur(100L, terrain);

        when(padelMatchRepository.findById(100L))
                .thenReturn(Optional.of(match));

        // Act
        ConfigurationMetierException exception = assertThrows(
                ConfigurationMetierException.class,
                () -> detteService.genererDettePourMatch(100L)
        );

        // Assert
        assertTrue(exception.getMessage().contains("heure de début"));

        verifyNoInteractions(
                detteRepository,
                participationRepository,
                paiementRepository
        );
    }

    @Test
    void actualiser_dettes_organisateur_doit_ignorer_les_matches_futurs() {
        // Arrange
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatchFutur(100L, terrain);
        Membre organisateur = creerMembre(20L, "G0001");

        Participation participationOrganisateur = creerParticipation(
                300L,
                match,
                organisateur,
                RoleParticipation.ORGANISATEUR
        );

        when(participationRepository.findByMembreId(20L))
                .thenReturn(List.of(participationOrganisateur));

        // Act
        detteService.actualiserDettesOrganisateur(organisateur);

        // Assert
        verifyNoInteractions(detteRepository, paiementRepository);
    }

    @Test
    void consulterDettesOuvertes_shouldReturnOpenDebtsForMember() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain);
        Membre organisateur = creerMembre(20L, "G0001");
        Dette dette = creerDette(500L, match, organisateur, new BigDecimal("30.00"), StatutDette.OUVERTE);

        when(membreRepository.findByMatricule("G0001")).thenReturn(Optional.of(organisateur));
        when(detteRepository.findByMembreResponsableIdAndStatutDette(20L, StatutDette.OUVERTE))
                .thenReturn(List.of(dette));

        List<DetteResponse> response = detteService.consulterDettesOuvertes("G0001");

        assertEquals(1, response.size());
        assertEquals(500L, response.get(0).detteId());
        assertEquals(StatutDette.OUVERTE, response.get(0).statutDette());
    }

    @Test
    void payerDette_shouldCreatePaymentAndCloseDebt_whenRequestIsValid() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain);
        Membre organisateur = creerMembre(20L, "G0001");
        Dette dette = creerDette(500L, match, organisateur, new BigDecimal("30.00"), StatutDette.OUVERTE);

        when(detteRepository.findById(500L)).thenReturn(Optional.of(dette));
        when(paiementRepository.existsByDetteId(500L)).thenReturn(false);
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> {
            Paiement paiement = invocation.getArgument(0);
            ReflectionTestUtils.setField(paiement, "id", 700L);
            return paiement;
        });
        when(detteRepository.save(any(Dette.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaiementDetteResponse response = detteService.payerDette(
                500L,
                new PayerDetteRequest(new BigDecimal("30.00"))
        );

        assertEquals(700L, response.paiementId());
        assertEquals(500L, response.detteId());
        assertEquals(20L, response.membreId());
        assertEquals("G0001", response.matriculeMembre());
        assertEquals(NaturePaiement.REGLEMENT_DETTE, response.naturePaiement());
        assertEquals(0, new BigDecimal("30.00").compareTo(response.montant()));
        assertEquals(StatutPaiement.PAYE, response.statutPaiement());
        assertEquals(StatutDette.REGLEE, response.statutDette());
        assertEquals(LocalDateTime.of(2026, 5, 7, 12, 0), response.dateHeurePaiement());
        assertEquals(LocalDateTime.of(2026, 5, 7, 12, 0), response.dateReglementDette());

        assertEquals(StatutDette.REGLEE, dette.getStatutDette());
        assertEquals(0, new BigDecimal("0.00").compareTo(dette.getMontantRestant()));
        assertEquals(0, new BigDecimal("70.00").compareTo(organisateur.getSoldeCredit()));
    }

    @Test
    void payerDette_shouldReject_whenDebtIsAlreadyClosed() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain);
        Membre organisateur = creerMembre(20L, "G0001");
        Dette dette = creerDette(500L, match, organisateur, new BigDecimal("0.00"), StatutDette.REGLEE);

        when(detteRepository.findById(500L)).thenReturn(Optional.of(dette));

        assertThrows(
                ConfigurationMetierException.class,
                () -> detteService.payerDette(
                        500L,
                        new PayerDetteRequest(new BigDecimal("30.00"))
                )
        );

        verify(paiementRepository, never()).save(any());
    }

    @Test
    void payerDette_shouldReject_whenAmountDoesNotMatchRemainingDebt() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain);
        Membre organisateur = creerMembre(20L, "G0001");
        Dette dette = creerDette(500L, match, organisateur, new BigDecimal("30.00"), StatutDette.OUVERTE);

        when(detteRepository.findById(500L)).thenReturn(Optional.of(dette));
        when(paiementRepository.existsByDetteId(500L)).thenReturn(false);

        assertThrows(
                ConfigurationMetierException.class,
                () -> detteService.payerDette(
                        500L,
                        new PayerDetteRequest(new BigDecimal("15.00"))
                )
        );

        verify(paiementRepository, never()).save(any());
    }

    @Test
    void actualiserDettePourMatch_shouldNotReopenDebt_whenDebtAlreadyHasPayment() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain);
        Membre organisateur = creerMembre(20L, "G0001");

        Dette detteExistante = creerDette(
                500L,
                match,
                organisateur,
                new BigDecimal("0.00"),
                StatutDette.REGLEE
        );

        when(detteRepository.findByMatchId(100L)).thenReturn(Optional.of(detteExistante));
        when(paiementRepository.existsByDetteId(500L)).thenReturn(true);
        when(detteRepository.save(any(Dette.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<DetteResponse> response = detteService.actualiserDettePourMatch(match);

        assertTrue(response.isEmpty());
        assertEquals(StatutDette.REGLEE, detteExistante.getStatutDette());
        assertEquals(0, new BigDecimal("0.00").compareTo(detteExistante.getMontantRestant()));
        assertEquals(LocalDateTime.of(2026, 5, 7, 12, 0), detteExistante.getDateReglement());

        verify(detteRepository).save(detteExistante);
    }

    private Site creerSite(Long id) {
        Site site = Site.builder()
                .code("SITE-" + id)
                .nom("Site " + id)
                .adresse("Adresse " + id)
                .actif(true)
                .build();

        ReflectionTestUtils.setField(site, "id", id);
        return site;
    }

    private Terrain creerTerrain(Long id, Site site) {
        Terrain terrain = Terrain.builder()
                .site(site)
                .numero("1")
                .actif(true)
                .build();

        ReflectionTestUtils.setField(terrain, "id", id);
        return terrain;
    }

    private Membre creerMembre(Long id, String matricule) {
        Membre membre = Membre.builder()
                .matricule(matricule)
                .nom("Nom")
                .prenom("Prenom")
                .motDePasseHash("$2y$10$w7Hmtss9GA8U9RAxfZeb3.JmBalmCw64iEo6pY5YEgNky9FM7OriK")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .soldeCredit(new BigDecimal("100.00"))
                .build();

        ReflectionTestUtils.setField(membre, "id", id);
        return membre;
    }

    private PadelMatch creerMatch(Long id, Terrain terrain) {
        return creerMatch(
                id,
                terrain,
                LocalDateTime.of(2026, 5, 7, 11, 0)
        );
    }

    private PadelMatch creerMatchFutur(Long id, Terrain terrain) {
        return creerMatch(
                id,
                terrain,
                LocalDateTime.of(2026, 5, 8, 9, 0)
        );
    }

    private PadelMatch creerMatch(
            Long id,
            Terrain terrain,
            LocalDateTime dateHeureDebut
    ) {
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

    private Participation creerParticipation(
            Long id,
            PadelMatch match,
            Membre membre,
            RoleParticipation roleParticipation
    ) {
        Participation participation = Participation.builder()
                .match(match)
                .membre(membre)
                .roleParticipation(roleParticipation)
                .modeEntree(ModeEntreeParticipation.CREATION)
                .statutParticipation(StatutParticipation.CONFIRMEE)
                .dateAffectation(LocalDateTime.of(2026, 5, 1, 10, 0))
                .dateConfirmation(LocalDateTime.of(2026, 5, 1, 10, 5))
                .build();

        ReflectionTestUtils.setField(participation, "id", id);
        return participation;
    }

    private Paiement creerPaiementParticipation(Long id, Membre membre, BigDecimal montant) {
        Paiement paiement = Paiement.builder()
                .membre(membre)
                .naturePaiement(NaturePaiement.PARTICIPATION)
                .montant(montant)
                .dateHeurePaiement(LocalDateTime.of(2026, 5, 1, 10, 5))
                .statutPaiement(StatutPaiement.PAYE)
                .participation(null)
                .dette(null)
                .build();

        ReflectionTestUtils.setField(paiement, "id", id);
        return paiement;
    }

    private Dette creerDette(
            Long id,
            PadelMatch match,
            Membre responsable,
            BigDecimal montantRestant,
            StatutDette statutDette
    ) {
        Dette dette = Dette.builder()
                .match(match)
                .membreResponsable(responsable)
                .montantInitial(montantRestant)
                .montantRestant(montantRestant)
                .dateCreation(LocalDateTime.of(2026, 5, 7, 12, 0))
                .statutDette(statutDette)
                .build();

        ReflectionTestUtils.setField(dette, "id", id);
        return dette;
    }
}
