package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.traitement.TraitementVeilleResponse;
import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.ModeCreation;
import com.padelMarius.backend.entity.ModeEntreeParticipation;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.Participation;
import com.padelMarius.backend.entity.Penalite;
import com.padelMarius.backend.entity.RoleParticipation;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.entity.StatutPenalite;
import com.padelMarius.backend.entity.Terrain;
import com.padelMarius.backend.entity.VisibiliteMatch;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import com.padelMarius.backend.repository.PenaliteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraitementVeilleServiceTest {

    @Mock
    private PadelMatchRepository padelMatchRepository;

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private PenaliteRepository penaliteRepository;

    private TraitementVeilleService traitementVeilleService;

    private final LocalDate dateTraitement = LocalDate.of(2026, 5, 19);
    private final LocalDateTime maintenantFixe = LocalDateTime.of(2026, 5, 19, 8, 0);

    @BeforeEach
    void setUp() {
        Clock clockFixe = Clock.fixed(
                maintenantFixe
                        .atZone(ZoneId.systemDefault())
                        .toInstant(),
                ZoneId.systemDefault()
        );

        traitementVeilleService = new TraitementVeilleService(
                padelMatchRepository,
                participationRepository,
                penaliteRepository,
                clockFixe
        );
    }

    @Test
    void traiterVeille_shouldMakePrivateIncompleteMatchPublic() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain, VisibiliteMatch.PRIVE);

        Membre organisateur = creerMembre(20L, "G0001");
        Membre joueur = creerMembre(21L, "G0002");

        Participation participationOrganisateur = creerParticipation(
                300L,
                match,
                organisateur,
                RoleParticipation.ORGANISATEUR,
                StatutParticipation.CONFIRMEE
        );

        Participation participationJoueur = creerParticipation(
                301L,
                match,
                joueur,
                RoleParticipation.JOUEUR,
                StatutParticipation.CONFIRMEE
        );

        stubRechercheMatchesDuLendemain(match);

        when(participationRepository.findByMatchId(100L))
                .thenReturn(List.of(participationOrganisateur, participationJoueur));

        when(penaliteRepository.findByMatchSourceId(100L))
                .thenReturn(List.of());

        TraitementVeilleResponse response = traitementVeilleService.traiterVeille(dateTraitement);

        assertEquals(VisibiliteMatch.PUBLIC, match.getVisibiliteCourante());
        assertEquals(maintenantFixe, match.getDatePassagePublic());
        assertEquals(1, response.matchesAnalyses());
        assertEquals(1, response.matchesPassesPublics());
        assertEquals(0, response.participationsLiberees());
        assertEquals(1, response.penalitesCreees());

        verify(padelMatchRepository).save(match);
    }

    @Test
    void traiterVeille_shouldReleaseUnpaidPlayerParticipation() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain, VisibiliteMatch.PUBLIC);

        Membre organisateur = creerMembre(20L, "G0001");
        Membre joueurNonPaye = creerMembre(21L, "G0002");

        Participation participationOrganisateur = creerParticipation(
                300L,
                match,
                organisateur,
                RoleParticipation.ORGANISATEUR,
                StatutParticipation.CONFIRMEE
        );

        Participation participationJoueurNonPaye = creerParticipation(
                301L,
                match,
                joueurNonPaye,
                RoleParticipation.JOUEUR,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        stubRechercheMatchesDuLendemain(match);

        when(participationRepository.findByMatchId(100L))
                .thenReturn(List.of(participationOrganisateur, participationJoueurNonPaye));

        TraitementVeilleResponse response = traitementVeilleService.traiterVeille(dateTraitement);

        assertEquals(StatutParticipation.LIBEREE, participationJoueurNonPaye.getStatutParticipation());
        assertEquals(maintenantFixe, participationJoueurNonPaye.getDateLiberation());
        assertEquals(1, response.matchesAnalyses());
        assertEquals(0, response.matchesPassesPublics());
        assertEquals(1, response.participationsLiberees());
        assertEquals(0, response.penalitesCreees());

        verify(participationRepository).save(participationJoueurNonPaye);
        verify(penaliteRepository, never()).save(any(Penalite.class));
    }

    @Test
    void traiterVeille_shouldCreateActivePenaltyForSevenDays() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain, VisibiliteMatch.PRIVE);

        Membre organisateur = creerMembre(20L, "G0001");

        Participation participationOrganisateur = creerParticipation(
                300L,
                match,
                organisateur,
                RoleParticipation.ORGANISATEUR,
                StatutParticipation.CONFIRMEE
        );

        stubRechercheMatchesDuLendemain(match);

        when(participationRepository.findByMatchId(100L))
                .thenReturn(List.of(participationOrganisateur));

        when(penaliteRepository.findByMatchSourceId(100L))
                .thenReturn(List.of());

        TraitementVeilleResponse response = traitementVeilleService.traiterVeille(dateTraitement);

        ArgumentCaptor<Penalite> penaliteCaptor = ArgumentCaptor.forClass(Penalite.class);
        verify(penaliteRepository).save(penaliteCaptor.capture());

        Penalite penalite = penaliteCaptor.getValue();

        assertEquals(1, response.penalitesCreees());
        assertEquals(organisateur, penalite.getMembre());
        assertEquals(match, penalite.getMatchSource());
        assertEquals("RESERVATION_PRIVEE_INCOMPLETE", penalite.getTypePenalite());
        assertEquals("Match privé incomplet la veille du match.", penalite.getMotif());
        assertEquals(maintenantFixe, penalite.getDateDebut());
        assertEquals(maintenantFixe.plusDays(7), penalite.getDateFin());
        assertEquals(StatutPenalite.ACTIVE, penalite.getStatutPenalite());
    }

    @Test
    void traiterVeille_shouldNotCreatePenaltyForPublicMatch() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain, VisibiliteMatch.PUBLIC);

        Membre organisateur = creerMembre(20L, "G0001");

        Participation participationOrganisateur = creerParticipation(
                300L,
                match,
                organisateur,
                RoleParticipation.ORGANISATEUR,
                StatutParticipation.CONFIRMEE
        );

        stubRechercheMatchesDuLendemain(match);

        when(participationRepository.findByMatchId(100L))
                .thenReturn(List.of(participationOrganisateur));

        TraitementVeilleResponse response = traitementVeilleService.traiterVeille(dateTraitement);

        assertEquals(VisibiliteMatch.PUBLIC, match.getVisibiliteCourante());
        assertEquals(1, response.matchesAnalyses());
        assertEquals(0, response.matchesPassesPublics());
        assertEquals(0, response.penalitesCreees());

        verify(penaliteRepository, never()).save(any(Penalite.class));
        verify(padelMatchRepository, never()).save(any(PadelMatch.class));
    }

    @Test
    void traiterVeille_shouldNotCreateDuplicatePenaltyWhenPenaltyAlreadyExists() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain, VisibiliteMatch.PRIVE);

        Membre organisateur = creerMembre(20L, "G0001");

        Participation participationOrganisateur = creerParticipation(
                300L,
                match,
                organisateur,
                RoleParticipation.ORGANISATEUR,
                StatutParticipation.CONFIRMEE
        );

        Penalite penaliteExistante = Penalite.builder()
                .membre(organisateur)
                .matchSource(match)
                .typePenalite("RESERVATION_PRIVEE_INCOMPLETE")
                .motif("Pénalité déjà créée.")
                .dateDebut(maintenantFixe.minusDays(1))
                .dateFin(maintenantFixe.plusDays(6))
                .statutPenalite(StatutPenalite.ACTIVE)
                .build();

        stubRechercheMatchesDuLendemain(match);

        when(participationRepository.findByMatchId(100L))
                .thenReturn(List.of(participationOrganisateur));

        when(penaliteRepository.findByMatchSourceId(100L))
                .thenReturn(List.of(penaliteExistante));

        TraitementVeilleResponse response = traitementVeilleService.traiterVeille(dateTraitement);

        assertEquals(VisibiliteMatch.PUBLIC, match.getVisibiliteCourante());
        assertEquals(1, response.matchesAnalyses());
        assertEquals(1, response.matchesPassesPublics());
        assertEquals(0, response.penalitesCreees());

        verify(padelMatchRepository).save(match);
        verify(penaliteRepository, never()).save(any(Penalite.class));
    }

    @Test
    void traiterVeille_shouldIgnoreMatchesThatAreNotAVenir() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);

        PadelMatch matchDemarre = creerMatch(100L, terrain, VisibiliteMatch.PRIVE);
        matchDemarre.setEtatCycle(EtatCycleMatch.DEMARRE);

        PadelMatch matchTermine = creerMatch(101L, terrain, VisibiliteMatch.PRIVE);
        matchTermine.setEtatCycle(EtatCycleMatch.TERMINE);

        stubRechercheMatchesDuLendemain(matchDemarre, matchTermine);

        TraitementVeilleResponse response = traitementVeilleService.traiterVeille(dateTraitement);

        assertEquals(2, response.matchesAnalyses());
        assertEquals(0, response.matchesPassesPublics());
        assertEquals(0, response.participationsLiberees());
        assertEquals(0, response.penalitesCreees());

        verifyNoInteractions(participationRepository);
        verifyNoInteractions(penaliteRepository);
        verify(padelMatchRepository, never()).save(any(PadelMatch.class));
    }

    @Test
    void traiterVeille_shouldKeepExistingDatePassagePublicWhenAlreadySet() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        PadelMatch match = creerMatch(100L, terrain, VisibiliteMatch.PRIVE);

        LocalDateTime ancienneDatePassagePublic = LocalDateTime.of(2026, 5, 18, 12, 0);
        match.setDatePassagePublic(ancienneDatePassagePublic);

        Membre organisateur = creerMembre(20L, "G0001");

        Participation participationOrganisateur = creerParticipation(
                300L,
                match,
                organisateur,
                RoleParticipation.ORGANISATEUR,
                StatutParticipation.CONFIRMEE
        );

        stubRechercheMatchesDuLendemain(match);

        when(participationRepository.findByMatchId(100L))
                .thenReturn(List.of(participationOrganisateur));

        when(penaliteRepository.findByMatchSourceId(100L))
                .thenReturn(List.of());

        TraitementVeilleResponse response = traitementVeilleService.traiterVeille(dateTraitement);

        assertEquals(VisibiliteMatch.PUBLIC, match.getVisibiliteCourante());
        assertEquals(ancienneDatePassagePublic, match.getDatePassagePublic());
        assertEquals(1, response.matchesAnalyses());
        assertEquals(1, response.matchesPassesPublics());
        assertEquals(1, response.penalitesCreees());

        verify(padelMatchRepository).save(match);
    }

    @Test
    void traiterVeille_shouldRejectNullDate() {
        assertThrows(
                ConfigurationMetierException.class,
                () -> traitementVeilleService.traiterVeille(null)
        );

        verifyNoInteractions(padelMatchRepository);
        verifyNoInteractions(participationRepository);
        verifyNoInteractions(penaliteRepository);
    }

    private void stubRechercheMatchesDuLendemain(PadelMatch... matches) {
        when(padelMatchRepository.findByDateHeureDebutGreaterThanEqualAndDateHeureDebutBefore(
                LocalDateTime.of(2026, 5, 20, 0, 0),
                LocalDateTime.of(2026, 5, 21, 0, 0)
        )).thenReturn(List.of(matches));
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
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .build();

        ReflectionTestUtils.setField(membre, "id", id);

        return membre;
    }

    private PadelMatch creerMatch(
            Long id,
            Terrain terrain,
            VisibiliteMatch visibiliteMatch
    ) {
        ModeCreation modeCreation = visibiliteMatch == VisibiliteMatch.PRIVE
                ? ModeCreation.PRIVE
                : ModeCreation.PUBLIC;

        PadelMatch match = PadelMatch.builder()
                .terrain(terrain)
                .dateHeureDebut(LocalDateTime.of(2026, 5, 20, 9, 0))
                .dateHeureFin(LocalDateTime.of(2026, 5, 20, 10, 30))
                .modeCreation(modeCreation)
                .visibiliteCourante(visibiliteMatch)
                .prixTotal(new BigDecimal("60.00"))
                .dateCreation(LocalDateTime.of(2026, 5, 1, 10, 0))
                .datePassagePublic(null)
                .etatCycle(EtatCycleMatch.A_VENIR)
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
                : ModeEntreeParticipation.INVITATION_PRIVEE;

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
                .dateLiberation(null)
                .build();

        ReflectionTestUtils.setField(participation, "id", id);

        return participation;
    }
}