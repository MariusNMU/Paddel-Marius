package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.matchpublic.MatchPublicResponse;
import com.padelMarius.backend.dto.matchpublic.RejoindreMatchPublicRequest;
import com.padelMarius.backend.dto.matchpublic.RejoindreMatchPublicResponse;
import com.padelMarius.backend.dto.paiement.PaiementResponse;
import com.padelMarius.backend.dto.paiement.PayerParticipationRequest;
import com.padelMarius.backend.dto.participation.InscriptionPubliqueRequest;
import com.padelMarius.backend.dto.participation.ParticipationResponse;
import com.padelMarius.backend.entity.CategorieMembre;
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
import com.padelMarius.backend.entity.StatutPaiement;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.entity.Terrain;
import com.padelMarius.backend.entity.VisibiliteMatch;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.repository.MembreRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchPublicServiceTest {

    @Mock
    private PadelMatchRepository padelMatchRepository;

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private ParticipationService participationService;

    @Mock
    private PaiementService paiementService;

    @Mock
    private MembreRepository membreRepository;

    private MatchPublicService matchPublicService;

    @BeforeEach
    void setUp() {
        matchPublicService = new MatchPublicService(
                padelMatchRepository,
                participationRepository,
                participationService,
                paiementService,
                membreRepository
        );
    }

    @Test
    void listerMatchesPublicsDisponibles_shouldReturnOnlyPublicMatchesWithAvailablePlacesForSite() {
        LocalDate date = LocalDate.of(2026, 6, 20);

        Site siteBruxelles = creerSite(1001L, "Padel Bruxelles");
        Site siteNamur = creerSite(1002L, "Padel Namur");

        Terrain terrainBruxelles = creerTerrain(1101L, siteBruxelles, "T1");
        Terrain terrainNamur = creerTerrain(1201L, siteNamur, "T1");

        PadelMatch matchDisponible = creerMatch(
                3001L,
                terrainBruxelles,
                LocalDateTime.of(2026, 6, 20, 9, 0)
        );

        PadelMatch matchAutreSite = creerMatch(
                3002L,
                terrainNamur,
                LocalDateTime.of(2026, 6, 20, 11, 0)
        );

        PadelMatch matchComplet = creerMatch(
                3003L,
                terrainBruxelles,
                LocalDateTime.of(2026, 6, 20, 13, 0)
        );

        when(padelMatchRepository.findByVisibiliteCouranteAndEtatCycleAndDateHeureDebutGreaterThanEqualAndDateHeureDebutBefore(
                VisibiliteMatch.PUBLIC,
                EtatCycleMatch.A_VENIR,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()
        )).thenReturn(List.of(matchDisponible, matchAutreSite, matchComplet));

        when(participationRepository.findByMatchId(3001L))
                .thenReturn(List.of(
                        creerParticipation(1L, matchDisponible, StatutParticipation.CONFIRMEE),
                        creerParticipation(2L, matchDisponible, StatutParticipation.CONFIRMEE)
                ));

        when(participationRepository.findByMatchId(3003L))
                .thenReturn(List.of(
                        creerParticipation(3L, matchComplet, StatutParticipation.CONFIRMEE),
                        creerParticipation(4L, matchComplet, StatutParticipation.CONFIRMEE),
                        creerParticipation(5L, matchComplet, StatutParticipation.CONFIRMEE),
                        creerParticipation(6L, matchComplet, StatutParticipation.CONFIRMEE)
                ));

        List<MatchPublicResponse> responses = matchPublicService.listerMatchesPublicsDisponibles(
                1001L,
                date
        );

        assertEquals(1, responses.size());
        assertEquals(3001L, responses.getFirst().matchId());
        assertEquals(2, responses.getFirst().nombreParticipantsActifs());
        assertEquals(2, responses.getFirst().placesDisponibles());
        assertEquals(new BigDecimal("15.00"), responses.getFirst().montantParticipation());
    }

    @Test
    void rejoindreEtPayer_shouldCreateParticipationAndPayment() {
        ParticipationResponse participationResponse = new ParticipationResponse(
                3105L,
                3001L,
                2001L,
                "G1001",
                RoleParticipation.JOUEUR,
                ModeEntreeParticipation.INSCRIPTION_PUBLIQUE,
                StatutParticipation.EN_ATTENTE_PAIEMENT,
                LocalDateTime.of(2026, 5, 12, 10, 0)
        );

        PaiementResponse paiementResponse = new PaiementResponse(
                6008L,
                3105L,
                2001L,
                "G1001",
                NaturePaiement.PARTICIPATION,
                new BigDecimal("15.00"),
                StatutPaiement.PAYE,
                StatutParticipation.CONFIRMEE,
                LocalDateTime.of(2026, 5, 12, 10, 1),
                LocalDateTime.of(2026, 5, 12, 10, 1)
        );

        Membre membre = Membre.builder()
                .matricule("G1001")
                .nom("Dupont")
                .prenom("Marie")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .soldeCredit(new BigDecimal("85.00"))
                .build();

        ReflectionTestUtils.setField(membre, "id", 2001L);

        when(participationService.inscrireParticipantPublic(
                3001L,
                new InscriptionPubliqueRequest("G1001")
        )).thenReturn(participationResponse);

        when(paiementService.payerParticipation(
                3105L,
                new PayerParticipationRequest(new BigDecimal("15.00"))
        )).thenReturn(paiementResponse);

        when(membreRepository.findByMatricule("G1001"))
                .thenReturn(Optional.of(membre));

        RejoindreMatchPublicResponse response = matchPublicService.rejoindreEtPayer(
                3001L,
                new RejoindreMatchPublicRequest("G1001")
        );

        assertEquals(3001L, response.matchId());
        assertEquals(3105L, response.participationId());
        assertEquals(6008L, response.paiementId());
        assertEquals("G1001", response.matriculeJoueur());
        assertEquals(new BigDecimal("15.00"), response.montantPaye());
        assertEquals(StatutParticipation.CONFIRMEE, response.statutParticipation());
        assertEquals(new BigDecimal("85.00"), response.soldeRestant());
    }

    @Test
    void rejoindreEtPayer_shouldRejectBlankMatricule() {
        ConfigurationMetierException exception = assertThrows(
                ConfigurationMetierException.class,
                () -> matchPublicService.rejoindreEtPayer(
                        3001L,
                        new RejoindreMatchPublicRequest(" ")
                )
        );

        assertEquals("Le matricule du joueur est obligatoire.", exception.getMessage());
    }

    private Site creerSite(Long id, String nom) {
        Site site = Site.builder()
                .code("SITE-" + id)
                .nom(nom)
                .adresse("Adresse " + id)
                .actif(true)
                .build();

        ReflectionTestUtils.setField(site, "id", id);

        return site;
    }

    private Terrain creerTerrain(Long id, Site site, String numero) {
        Terrain terrain = Terrain.builder()
                .site(site)
                .numero(numero)
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

    private Participation creerParticipation(
            Long id,
            PadelMatch match,
            StatutParticipation statutParticipation
    ) {
        Membre membre = Membre.builder()
                .matricule("G" + id)
                .nom("Nom " + id)
                .prenom("Prenom " + id)
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .soldeCredit(new BigDecimal("100.00"))
                .build();

        ReflectionTestUtils.setField(membre, "id", 2000L + id);

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