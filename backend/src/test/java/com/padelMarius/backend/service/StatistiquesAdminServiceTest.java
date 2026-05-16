package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.statistique.StatistiquesAdminResponse;
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
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.PaiementRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import com.padelMarius.backend.repository.SiteRepository;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatistiquesAdminServiceTest {

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private PadelMatchRepository padelMatchRepository;

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private DetteRepository detteRepository;

    @Mock
    private ParticipationRepository participationRepository;

    private StatistiquesAdminService statistiquesAdminService;

    @BeforeEach
    void setUp() {
        statistiquesAdminService = new StatistiquesAdminService(
                siteRepository,
                padelMatchRepository,
                paiementRepository,
                detteRepository,
                participationRepository
        );
    }

    @Test
    void calculerStatistiques_shouldReturnGlobalStats() {
        Site site = creerSite(1L, "Padel Bruxelles");
        Terrain terrain = creerTerrain(10L, site);

        PadelMatch matchAVenir = creerMatch(100L, terrain, EtatCycleMatch.A_VENIR);
        PadelMatch matchTermine = creerMatch(101L, terrain, EtatCycleMatch.TERMINE);

        Membre organisateur = creerMembre(20L, "G0001");
        Membre joueur1 = creerMembre(21L, "G0002");
        Membre joueur2 = creerMembre(22L, "G0003");
        Membre joueur3 = creerMembre(23L, "G0004");
        Membre joueur4 = creerMembre(24L, "G0005");
        Membre joueur5 = creerMembre(25L, "G0006");
        Membre joueurLibere = creerMembre(26L, "G0007");

        Participation participationOrganisateur = creerParticipation(
                300L,
                matchAVenir,
                organisateur,
                RoleParticipation.ORGANISATEUR,
                StatutParticipation.CONFIRMEE
        );

        Participation participationJoueur1 = creerParticipation(
                301L,
                matchAVenir,
                joueur1,
                RoleParticipation.JOUEUR,
                StatutParticipation.CONFIRMEE
        );

        Participation participationLiberee = creerParticipation(
                302L,
                matchAVenir,
                joueurLibere,
                RoleParticipation.JOUEUR,
                StatutParticipation.LIBEREE
        );

        Participation participationTerminee1 = creerParticipation(
                303L,
                matchTermine,
                joueur2,
                RoleParticipation.ORGANISATEUR,
                StatutParticipation.CONFIRMEE
        );

        Participation participationTerminee2 = creerParticipation(
                304L,
                matchTermine,
                joueur3,
                RoleParticipation.JOUEUR,
                StatutParticipation.CONFIRMEE
        );

        Participation participationTerminee3 = creerParticipation(
                305L,
                matchTermine,
                joueur4,
                RoleParticipation.JOUEUR,
                StatutParticipation.CONFIRMEE
        );

        Participation participationTerminee4 = creerParticipation(
                306L,
                matchTermine,
                joueur5,
                RoleParticipation.JOUEUR,
                StatutParticipation.CONFIRMEE
        );

        Dette detteOuverte = creerDette(
                500L,
                matchAVenir,
                organisateur,
                new BigDecimal("30.00"),
                StatutDette.OUVERTE
        );

        Dette detteReglee = creerDette(
                501L,
                matchTermine,
                organisateur,
                new BigDecimal("0.00"),
                StatutDette.REGLEE
        );

        Paiement paiementParticipation = creerPaiementParticipation(
                700L,
                joueur1,
                participationJoueur1,
                new BigDecimal("15.00")
        );

        Paiement paiementDette = creerPaiementDette(
                701L,
                organisateur,
                detteReglee,
                new BigDecimal("30.00")
        );

        LocalDate dateDebut = LocalDate.of(2026, 5, 1);
        LocalDate dateFin = LocalDate.of(2026, 5, 31);

        when(padelMatchRepository.findByDateHeureDebutGreaterThanEqualAndDateHeureDebutBefore(
                LocalDateTime.of(2026, 5, 1, 0, 0),
                LocalDateTime.of(2026, 6, 1, 0, 0)
        )).thenReturn(List.of(matchAVenir, matchTermine));

        when(paiementRepository.findByDateHeurePaiementGreaterThanEqualAndDateHeurePaiementBeforeAndStatutPaiement(
                LocalDateTime.of(2026, 5, 1, 0, 0),
                LocalDateTime.of(2026, 6, 1, 0, 0),
                StatutPaiement.PAYE
        )).thenReturn(List.of(paiementParticipation, paiementDette));

        when(detteRepository.findByStatutDette(StatutDette.OUVERTE))
                .thenReturn(List.of(detteOuverte));

        when(participationRepository.findByMatchId(100L))
                .thenReturn(List.of(
                        participationOrganisateur,
                        participationJoueur1,
                        participationLiberee
                ));

        when(participationRepository.findByMatchId(101L))
                .thenReturn(List.of(
                        participationTerminee1,
                        participationTerminee2,
                        participationTerminee3,
                        participationTerminee4
                ));

        StatistiquesAdminResponse response = statistiquesAdminService.calculerStatistiques(
                dateDebut,
                dateFin,
                null
        );

        assertEquals(dateDebut, response.dateDebut());
        assertEquals(dateFin, response.dateFin());
        assertEquals(null, response.siteId());
        assertEquals(null, response.nomSite());
        assertEquals(2, response.nombreMatches());
        assertEquals(1, response.nombreMatchesAVenir());
        assertEquals(1, response.nombreMatchesTermines());
        assertEquals(2, response.nombrePaiements());
        assertEquals(0, new BigDecimal("45.00").compareTo(response.chiffreAffaires()));
        assertEquals(1, response.nombreDettesOuvertes());
        assertEquals(0, new BigDecimal("30.00").compareTo(response.montantDettesOuvertes()));
        assertEquals(6, response.nombreParticipationsActives());
        assertEquals(8, response.capaciteTheoriqueJoueurs());
        assertEquals(0, new BigDecimal("75.00").compareTo(response.tauxRemplissage()));
    }

    @Test
    void calculerStatistiques_shouldReturnStatsFilteredBySite() {
        Site siteBruxelles = creerSite(1L, "Padel Bruxelles");
        Site siteNamur = creerSite(2L, "Padel Namur");

        Terrain terrainBruxelles = creerTerrain(10L, siteBruxelles);
        Terrain terrainNamur = creerTerrain(11L, siteNamur);

        PadelMatch matchBruxelles = creerMatch(100L, terrainBruxelles, EtatCycleMatch.A_VENIR);
        PadelMatch matchNamur = creerMatch(101L, terrainNamur, EtatCycleMatch.A_VENIR);

        Membre organisateur = creerMembre(20L, "G0001");
        Membre joueurBruxelles = creerMembre(21L, "G0002");
        Membre joueurNamur = creerMembre(22L, "G0003");

        Participation participationOrganisateur = creerParticipation(
                300L,
                matchBruxelles,
                organisateur,
                RoleParticipation.ORGANISATEUR,
                StatutParticipation.CONFIRMEE
        );

        Participation participationJoueurBruxelles = creerParticipation(
                301L,
                matchBruxelles,
                joueurBruxelles,
                RoleParticipation.JOUEUR,
                StatutParticipation.CONFIRMEE
        );

        Participation participationJoueurNamur = creerParticipation(
                302L,
                matchNamur,
                joueurNamur,
                RoleParticipation.JOUEUR,
                StatutParticipation.CONFIRMEE
        );

        Paiement paiementBruxelles = creerPaiementParticipation(
                700L,
                joueurBruxelles,
                participationJoueurBruxelles,
                new BigDecimal("15.00")
        );

        Paiement paiementNamur = creerPaiementParticipation(
                701L,
                joueurNamur,
                participationJoueurNamur,
                new BigDecimal("15.00")
        );

        Dette detteBruxelles = creerDette(
                500L,
                matchBruxelles,
                organisateur,
                new BigDecimal("20.00"),
                StatutDette.OUVERTE
        );

        Dette detteNamur = creerDette(
                501L,
                matchNamur,
                organisateur,
                new BigDecimal("40.00"),
                StatutDette.OUVERTE
        );

        LocalDate dateDebut = LocalDate.of(2026, 5, 1);
        LocalDate dateFin = LocalDate.of(2026, 5, 31);

        when(siteRepository.findById(1L))
                .thenReturn(Optional.of(siteBruxelles));

        when(padelMatchRepository.findByDateHeureDebutGreaterThanEqualAndDateHeureDebutBefore(
                LocalDateTime.of(2026, 5, 1, 0, 0),
                LocalDateTime.of(2026, 6, 1, 0, 0)
        )).thenReturn(List.of(matchBruxelles, matchNamur));

        when(paiementRepository.findByDateHeurePaiementGreaterThanEqualAndDateHeurePaiementBeforeAndStatutPaiement(
                LocalDateTime.of(2026, 5, 1, 0, 0),
                LocalDateTime.of(2026, 6, 1, 0, 0),
                StatutPaiement.PAYE
        )).thenReturn(List.of(paiementBruxelles, paiementNamur));

        when(detteRepository.findByStatutDette(StatutDette.OUVERTE))
                .thenReturn(List.of(detteBruxelles, detteNamur));

        when(participationRepository.findByMatchId(100L))
                .thenReturn(List.of(
                        participationOrganisateur,
                        participationJoueurBruxelles
                ));

        StatistiquesAdminResponse response = statistiquesAdminService.calculerStatistiques(
                dateDebut,
                dateFin,
                1L
        );

        assertEquals(1L, response.siteId());
        assertEquals("Padel Bruxelles", response.nomSite());
        assertEquals(1, response.nombreMatches());
        assertEquals(1, response.nombreMatchesAVenir());
        assertEquals(0, response.nombreMatchesTermines());
        assertEquals(1, response.nombrePaiements());
        assertEquals(0, new BigDecimal("15.00").compareTo(response.chiffreAffaires()));
        assertEquals(1, response.nombreDettesOuvertes());
        assertEquals(0, new BigDecimal("20.00").compareTo(response.montantDettesOuvertes()));
        assertEquals(2, response.nombreParticipationsActives());
        assertEquals(4, response.capaciteTheoriqueJoueurs());
        assertEquals(0, new BigDecimal("50.00").compareTo(response.tauxRemplissage()));
    }

    @Test
    void calculerStatistiques_shouldRejectInvalidDateRange() {
        ConfigurationMetierException exception = assertThrows(
                ConfigurationMetierException.class,
                () -> statistiquesAdminService.calculerStatistiques(
                        LocalDate.of(2026, 5, 31),
                        LocalDate.of(2026, 5, 1),
                        null
                )
        );

        assertEquals(
                "La date de fin doit être supérieure ou égale à la date de début.",
                exception.getMessage()
        );

        verifyNoInteractions(siteRepository);
        verifyNoInteractions(padelMatchRepository);
        verifyNoInteractions(paiementRepository);
        verifyNoInteractions(detteRepository);
        verifyNoInteractions(participationRepository);
    }

    @Test
    void calculerStatistiques_shouldRejectUnknownSite() {
        when(siteRepository.findById(999L))
                .thenReturn(Optional.empty());

        RessourceIntrouvableException exception = assertThrows(
                RessourceIntrouvableException.class,
                () -> statistiquesAdminService.calculerStatistiques(
                        LocalDate.of(2026, 5, 1),
                        LocalDate.of(2026, 5, 31),
                        999L
                )
        );

        assertEquals("Site introuvable avec l'id 999", exception.getMessage());

        verifyNoInteractions(padelMatchRepository);
        verifyNoInteractions(paiementRepository);
        verifyNoInteractions(detteRepository);
        verifyNoInteractions(participationRepository);
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

    private Terrain creerTerrain(Long id, Site site) {
        Terrain terrain = Terrain.builder()
                .site(site)
                .numero("T" + id)
                .actif(true)
                .build();

        ReflectionTestUtils.setField(terrain, "id", id);

        return terrain;
    }

    private Membre creerMembre(Long id, String matricule) {
        Membre membre = Membre.builder()
                .matricule(matricule)
                .nom("Nom " + id)
                .prenom("Prenom " + id)
                .motDePasseHash("$2y$10$w7Hmtss9GA8U9RAxfZeb3.JmBalmCw64iEo6pY5YEgNky9FM7OriK")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .build();

        ReflectionTestUtils.setField(membre, "id", id);

        return membre;
    }

    private PadelMatch creerMatch(
            Long id,
            Terrain terrain,
            EtatCycleMatch etatCycleMatch
    ) {
        PadelMatch match = PadelMatch.builder()
                .terrain(terrain)
                .dateHeureDebut(LocalDateTime.of(2026, 5, 20, 9, 0))
                .dateHeureFin(LocalDateTime.of(2026, 5, 20, 10, 30))
                .modeCreation(ModeCreation.PUBLIC)
                .visibiliteCourante(VisibiliteMatch.PUBLIC)
                .prixTotal(new BigDecimal("60.00"))
                .dateCreation(LocalDateTime.of(2026, 5, 1, 10, 0))
                .etatCycle(etatCycleMatch)
                .build();

        ReflectionTestUtils.setField(match, "id", id);

        return match;
    }

    private Participation creerParticipation(
            Long id,
            PadelMatch match,
            Membre membre,
            RoleParticipation roleParticipation,
            StatutParticipation statutParticipation
    ) {
        ModeEntreeParticipation modeEntree = roleParticipation == RoleParticipation.ORGANISATEUR
                ? ModeEntreeParticipation.CREATION
                : ModeEntreeParticipation.INSCRIPTION_PUBLIQUE;

        Participation participation = Participation.builder()
                .match(match)
                .membre(membre)
                .roleParticipation(roleParticipation)
                .modeEntree(modeEntree)
                .statutParticipation(statutParticipation)
                .dateAffectation(LocalDateTime.of(2026, 5, 1, 10, 0))
                .dateConfirmation(
                        statutParticipation == StatutParticipation.CONFIRMEE
                                ? LocalDateTime.of(2026, 5, 1, 10, 5)
                                : null
                )
                .dateLiberation(
                        statutParticipation == StatutParticipation.LIBEREE
                                ? LocalDateTime.of(2026, 5, 19, 8, 0)
                                : null
                )
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

    private Paiement creerPaiementDette(
            Long id,
            Membre membre,
            Dette dette,
            BigDecimal montant
    ) {
        Paiement paiement = Paiement.builder()
                .membre(membre)
                .naturePaiement(NaturePaiement.REGLEMENT_DETTE)
                .montant(montant)
                .dateHeurePaiement(LocalDateTime.of(2026, 5, 11, 12, 0))
                .statutPaiement(StatutPaiement.PAYE)
                .participation(null)
                .dette(dette)
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
                .dateCreation(LocalDateTime.of(2026, 5, 12, 12, 0))
                .statutDette(statutDette)
                .build();

        ReflectionTestUtils.setField(dette, "id", id);

        return dette;
    }
}
