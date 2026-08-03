package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.paiement.PaiementResponse;
import com.padelMarius.backend.dto.paiement.PayerParticipationRequest;
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

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaiementServiceTest {

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private DetteRepository detteRepository;

    @Mock
    private MembreRepository membreRepository;

    @Mock
    private PadelMatchRepository padelMatchRepository;

    @Mock
    private DetteService detteService;

    private PaiementService paiementService;

    @BeforeEach
    void setUp() {
        Clock clockFixe = Clock.fixed(
                LocalDate.of(2026, 5, 7)
                        .atTime(12, 0)
                        .atZone(ZoneId.systemDefault())
                        .toInstant(),
                ZoneId.systemDefault()
        );

        paiementService = new PaiementService(
                participationRepository,
                paiementRepository,
                detteRepository,
                membreRepository,
                padelMatchRepository,
                detteService,
                clockFixe
        );
    }

    @Test
    void payerParticipation_shouldCreatePaymentAndConfirmParticipation_whenRequestIsValid() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain);
        Membre membre = creerMembre(21L, "G0002");

        Participation participation = creerParticipation(
                300L,
                match,
                membre,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        configurerParticipationVerrouillee(participation);
        when(paiementRepository.existsByParticipationId(300L)).thenReturn(false);
        when(detteRepository.findByMembreResponsableIdAndStatutDette(
                membre.getId(),
                StatutDette.OUVERTE
        )).thenReturn(List.of());
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> {
            Paiement paiement = invocation.getArgument(0);
            ReflectionTestUtils.setField(paiement, "id", 400L);
            return paiement;
        });
        when(participationRepository.save(any(Participation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaiementResponse response = paiementService.payerParticipation(
                300L,
                new PayerParticipationRequest(new BigDecimal("15.00"))
        );

        assertEquals(400L, response.paiementId());
        assertEquals(300L, response.participationId());
        assertEquals(21L, response.membreId());
        assertEquals("G0002", response.matriculeMembre());
        assertEquals(NaturePaiement.PARTICIPATION, response.naturePaiement());
        assertEquals(0, new BigDecimal("15.00").compareTo(response.montant()));
        assertEquals(0, new BigDecimal("0.00").compareTo(response.montantDettesReglees()));
        assertEquals(0, new BigDecimal("15.00").compareTo(response.montantTotalDebite()));
        assertEquals(StatutPaiement.PAYE, response.statutPaiement());
        assertEquals(StatutParticipation.CONFIRMEE, response.statutParticipation());
        assertEquals(LocalDateTime.of(2026, 5, 7, 12, 0), response.dateHeurePaiement());
        assertEquals(LocalDateTime.of(2026, 5, 7, 12, 0), response.dateConfirmationParticipation());

        assertEquals(StatutParticipation.CONFIRMEE, participation.getStatutParticipation());
        assertNotNull(participation.getDateConfirmation());

        verify(paiementRepository).save(any(Paiement.class));
        verify(participationRepository).save(participation);
        verify(detteService).actualiserDettePourMatch(any(PadelMatch.class));
    }

    @Test
    void payerParticipation_shouldCreatePayment_justBeforeMatchStart() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain);
        match.setDateHeureDebut(LocalDateTime.of(2026, 5, 7, 12, 0, 1));
        match.setDateHeureFin(LocalDateTime.of(2026, 5, 7, 13, 30, 1));

        Membre membre = creerMembre(21L, "G0002");
        Participation participation = creerParticipation(
                300L,
                match,
                membre,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        configurerParticipationVerrouillee(participation);
        when(paiementRepository.existsByParticipationId(300L))
                .thenReturn(false);
        when(detteRepository.findByMembreResponsableIdAndStatutDette(
                membre.getId(),
                StatutDette.OUVERTE
        )).thenReturn(List.of());
        when(paiementRepository.save(any(Paiement.class)))
                .thenAnswer(invocation -> {
                    Paiement paiement = invocation.getArgument(0);
                    ReflectionTestUtils.setField(paiement, "id", 400L);
                    return paiement;
                });
        when(participationRepository.save(any(Participation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaiementResponse response = paiementService.payerParticipationStandard(300L);

        assertEquals(400L, response.paiementId());
        assertEquals(StatutParticipation.CONFIRMEE, response.statutParticipation());
    }

    @Test
    void payerParticipation_shouldRejectMatchAfterStartTime() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain);
        match.setDateHeureDebut(LocalDateTime.of(2026, 5, 7, 11, 59, 59));

        Membre membre = creerMembre(21L, "G0002");
        Participation participation = creerParticipation(
                300L,
                match,
                membre,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        configurerParticipationVerrouillee(participation);

        ConfigurationMetierException exception = assertThrows(
                ConfigurationMetierException.class,
                () -> paiementService.payerParticipationStandard(300L)
        );

        assertEquals(
                "Le match n'accepte plus de paiement.",
                exception.getMessage()
        );
        verify(paiementRepository, never()).save(any());
    }

    @Test
    void payerParticipation_shouldRejectMatchAtExactStartTime() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain);
        match.setDateHeureDebut(LocalDateTime.of(2026, 5, 7, 12, 0));

        Membre membre = creerMembre(21L, "G0002");
        Participation participation = creerParticipation(
                300L,
                match,
                membre,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        configurerParticipationVerrouillee(participation);

        assertThrows(
                ConfigurationMetierException.class,
                () -> paiementService.payerParticipationStandard(300L)
        );

        verify(paiementRepository, never()).save(any());
    }

    @Test
    void payerParticipation_shouldRejectCancelledMatch() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain);
        match.setEtatCycle(EtatCycleMatch.ANNULE);

        Membre membre = creerMembre(21L, "G0002");
        Participation participation = creerParticipation(
                300L,
                match,
                membre,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        configurerParticipationVerrouillee(participation);

        assertThrows(
                ConfigurationMetierException.class,
                () -> paiementService.payerParticipationStandard(300L)
        );

        verify(paiementRepository, never()).save(any());
    }

    @Test
    void payerParticipationStandard_shouldCreatePaymentWithoutClientAmount() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain);
        Membre membre = creerMembre(21L, "G0002");

        Participation participation = creerParticipation(
                300L,
                match,
                membre,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        configurerParticipationVerrouillee(participation);
        when(paiementRepository.existsByParticipationId(300L)).thenReturn(false);
        when(detteRepository.findByMembreResponsableIdAndStatutDette(
                membre.getId(),
                StatutDette.OUVERTE
        )).thenReturn(List.of());
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> {
            Paiement paiement = invocation.getArgument(0);
            ReflectionTestUtils.setField(paiement, "id", 400L);
            return paiement;
        });
        when(participationRepository.save(any(Participation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaiementResponse response = paiementService.payerParticipationStandard(300L);

        assertEquals(400L, response.paiementId());
        assertEquals(300L, response.participationId());
        assertEquals(0, new BigDecimal("15.00").compareTo(response.montant()));
        assertEquals(0, new BigDecimal("15.00").compareTo(response.montantTotalDebite()));
        assertEquals(StatutParticipation.CONFIRMEE, response.statutParticipation());

        verify(paiementRepository).save(any(Paiement.class));
        verify(participationRepository).save(participation);
    }

    @Test
    void payerParticipation_shouldDebitMemberBalance() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain);
        Membre membre = creerMembre(21L, "G0002");

        Participation participation = creerParticipation(
                300L,
                match,
                membre,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        configurerParticipationVerrouillee(participation);
        when(paiementRepository.existsByParticipationId(300L)).thenReturn(false);
        when(detteRepository.findByMembreResponsableIdAndStatutDette(
                membre.getId(),
                StatutDette.OUVERTE
        )).thenReturn(List.of());
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> {
            Paiement paiement = invocation.getArgument(0);
            ReflectionTestUtils.setField(paiement, "id", 400L);
            return paiement;
        });
        when(participationRepository.save(any(Participation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paiementService.payerParticipation(
                300L,
                new PayerParticipationRequest(new BigDecimal("15.00"))
        );

        assertEquals(0, new BigDecimal("85.00").compareTo(membre.getSoldeCredit()));
    }

    @Test
    void shouldAddOpenDebtsToParticipationPayment() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain);
        PadelMatch autreMatch = creerMatch(101L, terrain);
        Membre membre = creerMembre(21L, "G0002");

        Participation participation = creerParticipation(
                300L,
                match,
                membre,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        Dette dette = Dette.builder()
                .match(autreMatch)
                .membreResponsable(membre)
                .montantInitial(new BigDecimal("30.00"))
                .montantRestant(new BigDecimal("30.00"))
                .dateCreation(LocalDateTime.of(2026, 5, 1, 10, 0))
                .dateReglement(null)
                .statutDette(StatutDette.OUVERTE)
                .build();

        ReflectionTestUtils.setField(dette, "id", 500L);

        configurerParticipationVerrouillee(participation);
        when(paiementRepository.existsByParticipationId(300L)).thenReturn(false);
        when(detteRepository.findByMembreResponsableIdAndStatutDette(
                membre.getId(),
                StatutDette.OUVERTE
        )).thenReturn(List.of(dette));
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> {
            Paiement paiement = invocation.getArgument(0);
            ReflectionTestUtils.setField(paiement, "id", 400L);
            return paiement;
        });
        when(participationRepository.save(any(Participation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(detteRepository.save(any(Dette.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaiementResponse response = paiementService.payerParticipation(
                300L,
                new PayerParticipationRequest(new BigDecimal("15.00"))
        );

        assertEquals(StatutParticipation.CONFIRMEE, participation.getStatutParticipation());
        assertEquals(StatutDette.REGLEE, dette.getStatutDette());
        assertEquals(0, new BigDecimal("55.00").compareTo(membre.getSoldeCredit()));
        assertEquals(0, new BigDecimal("45.00").compareTo(response.montantTotalDebite()));
        assertEquals(0, new BigDecimal("30.00").compareTo(response.montantDettesReglees()));

        verify(paiementRepository, times(2)).save(any(Paiement.class));
        verify(detteRepository).save(dette);
        verify(participationRepository).save(participation);
    }

    @Test
    void payerParticipation_shouldThrowNotFound_whenParticipationDoesNotExist() {
        when(participationRepository.findByIdForUpdate(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RessourceIntrouvableException.class,
                () -> paiementService.payerParticipation(
                        999L,
                        new PayerParticipationRequest(new BigDecimal("15.00"))
                )
        );

        verify(paiementRepository, never()).save(any());
        verify(participationRepository, never()).save(any());
    }

    @Test
    void payerParticipation_shouldRejectDoublePayment() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain);
        Membre membre = creerMembre(21L, "G0002");

        Participation participation = creerParticipation(
                300L,
                match,
                membre,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        configurerParticipationVerrouillee(participation);
        when(paiementRepository.existsByParticipationId(300L)).thenReturn(true);

        assertThrows(
                ConfigurationMetierException.class,
                () -> paiementService.payerParticipation(
                        300L,
                        new PayerParticipationRequest(new BigDecimal("15.00"))
                )
        );

        verify(paiementRepository, never()).save(any());
        verify(participationRepository, never()).save(any());
    }

    @Test
    void payerParticipation_shouldRejectReleasedParticipation() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain);
        Membre membre = creerMembre(21L, "G0002");

        Participation participation = creerParticipation(
                300L,
                match,
                membre,
                StatutParticipation.LIBEREE
        );

        configurerParticipationVerrouillee(participation);

        assertThrows(
                ConfigurationMetierException.class,
                () -> paiementService.payerParticipation(
                        300L,
                        new PayerParticipationRequest(new BigDecimal("15.00"))
                )
        );

        verify(paiementRepository, never()).save(any());
        verify(participationRepository, never()).save(any());
    }

    @Test
    void payerParticipation_shouldRejectAlreadyConfirmedParticipation() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain);
        Membre membre = creerMembre(21L, "G0002");

        Participation participation = creerParticipation(
                300L,
                match,
                membre,
                StatutParticipation.CONFIRMEE
        );

        configurerParticipationVerrouillee(participation);

        assertThrows(
                ConfigurationMetierException.class,
                () -> paiementService.payerParticipation(
                        300L,
                        new PayerParticipationRequest(new BigDecimal("15.00"))
                )
        );

        verify(paiementRepository, never()).save(any());
        verify(participationRepository, never()).save(any());
    }

    @Test
    void payerParticipation_shouldRejectInvalidAmount() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain);
        Membre membre = creerMembre(21L, "G0002");

        Participation participation = creerParticipation(
                300L,
                match,
                membre,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        configurerParticipationVerrouillee(participation);

        assertThrows(
                ConfigurationMetierException.class,
                () -> paiementService.payerParticipation(
                        300L,
                        new PayerParticipationRequest(new BigDecimal("10.00"))
                )
        );

        verify(paiementRepository, never()).save(any());
        verify(participationRepository, never()).save(any());
    }

    @Test
    void payerParticipation_shouldRejectInactiveMember() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain);
        Membre membre = creerMembre(21L, "G0002");
        membre.setActif(false);

        Participation participation = creerParticipation(
                300L,
                match,
                membre,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        configurerParticipationVerrouillee(participation);

        assertThrows(
                ConfigurationMetierException.class,
                () -> paiementService.payerParticipation(
                        300L,
                        new PayerParticipationRequest(new BigDecimal("15.00"))
                )
        );

        verify(paiementRepository, never()).save(any());
        verify(participationRepository, never()).save(any());
    }

    private void configurerParticipationVerrouillee(
            Participation participation
    ) {
        Long participationId = participation.getId();
        Membre membre = participation.getMembre();

        when(participationRepository.findByIdForUpdate(participationId))
                .thenReturn(Optional.of(participation));
        when(padelMatchRepository.findByIdForUpdate(participation.getMatch().getId()))
                .thenReturn(Optional.of(participation.getMatch()));
        when(membreRepository.findByIdForUpdate(membre.getId()))
                .thenReturn(Optional.of(membre));
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
        PadelMatch match = PadelMatch.builder()
                .terrain(terrain)
                .dateHeureDebut(LocalDateTime.of(2026, 5, 20, 9, 0))
                .dateHeureFin(LocalDateTime.of(2026, 5, 20, 10, 30))
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
            StatutParticipation statutParticipation
    ) {
        Participation participation = Participation.builder()
                .match(match)
                .membre(membre)
                .roleParticipation(RoleParticipation.JOUEUR)
                .modeEntree(ModeEntreeParticipation.INSCRIPTION_PUBLIQUE)
                .statutParticipation(statutParticipation)
                .dateAffectation(LocalDateTime.of(2026, 5, 1, 10, 0))
                .build();

        ReflectionTestUtils.setField(participation, "id", id);
        return participation;
    }
}
