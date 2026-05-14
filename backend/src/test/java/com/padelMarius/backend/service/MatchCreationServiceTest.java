package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.disponibilite.CreneauDisponibiliteResponse;
import com.padelMarius.backend.dto.disponibilite.DisponibilitesResponse;
import com.padelMarius.backend.dto.match.CreerMatchRequest;
import com.padelMarius.backend.dto.match.MatchResponse;
import com.padelMarius.backend.entity.*;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchCreationServiceTest {

    @Mock
    private TerrainRepository terrainRepository;

    @Mock
    private MembreRepository membreRepository;

    @Mock
    private DetteRepository detteRepository;

    @Mock
    private PenaliteRepository penaliteRepository;

    @Mock
    private PadelMatchRepository padelMatchRepository;

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private DisponibiliteService disponibiliteService;

    @Mock
    private ReglesReservationMembreService reglesReservationMembreService;

    @Mock
    private DetteService detteService;

    @Mock
    private Clock clock;

    @InjectMocks
    private MatchCreationService matchCreationService;

    @BeforeEach
    void configurerHorloge() {
        lenient().when(clock.instant())
                .thenReturn(Instant.parse("2026-05-14T10:00:00Z"));
        lenient().when(clock.getZone())
                .thenReturn(ZoneId.of("Europe/Brussels"));
    }

    @Test
    void shouldCreatePrivateMatch() {
        Scenario scenario = configurerCasValide();

        CreerMatchRequest request = new CreerMatchRequest(
                scenario.terrain().getId(),
                scenario.organisateur().getMatricule(),
                scenario.dateHeureDebut(),
                ModeCreation.PRIVE
        );

        MatchResponse response = matchCreationService.creerMatch(request);

        verify(detteService).creerDetteInitialeOrganisateur(
                any(PadelMatch.class),
                eq(scenario.organisateur())
        );

        assertEquals(100L, response.matchId());
        assertEquals(10L, response.terrainId());
        assertEquals(1L, response.siteId());
        assertEquals("G0001", response.matriculeOrganisateur());
        assertEquals(scenario.dateHeureDebut(), response.dateHeureDebut());
        assertEquals(scenario.dateHeureFin(), response.dateHeureFin());
        assertEquals(ModeCreation.PRIVE, response.modeCreation());
        assertEquals(VisibiliteMatch.PRIVE, response.visibiliteCourante());
        assertEquals(new BigDecimal("60.00"), response.prixTotal());
        assertEquals(EtatCycleMatch.A_VENIR, response.etatCycle());
        assertEquals(200L, response.participationOrganisateurId());

        verify(padelMatchRepository).save(any(PadelMatch.class));
        verify(participationRepository).save(any(Participation.class));
        verify(reglesReservationMembreService).verifierReglesCreationMatch(
                scenario.organisateur(),
                scenario.terrain(),
                scenario.dateHeureDebut()
        );
    }

    @Test
    void shouldCreateInitialOrganizerDebtWhenMatchIsCreated() {
        Scenario scenario = configurerCasValide();

        CreerMatchRequest request = new CreerMatchRequest(
                scenario.terrain().getId(),
                scenario.organisateur().getMatricule(),
                scenario.dateHeureDebut(),
                ModeCreation.PUBLIC
        );

        matchCreationService.creerMatch(request);

        verify(detteService).creerDetteInitialeOrganisateur(
                any(PadelMatch.class),
                eq(scenario.organisateur())
        );
    }

    @Test
    void creerMatchDevraitVerifierLesReglesDeReservationDuMembre() {
        Scenario scenario = configurerCasValide();

        CreerMatchRequest request = new CreerMatchRequest(
                scenario.terrain().getId(),
                scenario.organisateur().getMatricule(),
                scenario.dateHeureDebut(),
                ModeCreation.PRIVE
        );

        matchCreationService.creerMatch(request);

        verify(reglesReservationMembreService).verifierReglesCreationMatch(
                scenario.organisateur(),
                scenario.terrain(),
                request.dateHeureDebut()
        );
    }

    @Test
    void creerMatchDevraitRefuserUneDateDansLePasseOuALHeureCourante() {
        Scenario scenario = configurerCasValideSansDetteNiPenalite();

        List<LocalDateTime> datesRefusees = List.of(
                LocalDateTime.of(2026, 5, 13, 9, 0),
                LocalDateTime.of(2026, 5, 14, 11, 59),
                LocalDateTime.of(2026, 5, 14, 12, 0)
        );

        for (LocalDateTime dateRefusee : datesRefusees) {
            CreerMatchRequest request = new CreerMatchRequest(
                    scenario.terrain().getId(),
                    scenario.organisateur().getMatricule(),
                    dateRefusee,
                    ModeCreation.PRIVE
            );

            assertThrows(
                    ConfigurationMetierException.class,
                    () -> matchCreationService.creerMatch(request)
            );
        }

        verifyNoInteractions(reglesReservationMembreService);
        verify(padelMatchRepository, never()).save(any());
        verify(participationRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenMatchStartsBeforeCurrentTime() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        Membre organisateur = creerMembreGlobal(20L, "G0001");

        CreerMatchRequest request = new CreerMatchRequest(
                terrain.getId(),
                organisateur.getMatricule(),
                LocalDateTime.of(2026, 5, 14, 11, 59),
                ModeCreation.PRIVE
        );

        when(terrainRepository.findById(10L)).thenReturn(Optional.of(terrain));
        when(membreRepository.findByMatricule("G0001")).thenReturn(Optional.of(organisateur));

        ConfigurationMetierException exception = assertThrows(
                ConfigurationMetierException.class,
                () -> matchCreationService.creerMatch(request)
        );

        assertTrue(exception.getMessage().contains("passé"));
        verify(padelMatchRepository, never()).save(any(PadelMatch.class));
        verify(participationRepository, never()).save(any(Participation.class));
    }

    @Test
    void shouldCreatePublicMatch() {
        Scenario scenario = configurerCasValide();

        CreerMatchRequest request = new CreerMatchRequest(
                scenario.terrain().getId(),
                scenario.organisateur().getMatricule(),
                scenario.dateHeureDebut(),
                ModeCreation.PUBLIC
        );

        MatchResponse response = matchCreationService.creerMatch(request);

        assertEquals(ModeCreation.PUBLIC, response.modeCreation());
        assertEquals(VisibiliteMatch.PUBLIC, response.visibiliteCourante());
    }

    @Test
    void shouldKeepDatePassagePublicNullWhenMatchIsCreatedDirectlyPublic() {
        Scenario scenario = configurerCasValide();

        CreerMatchRequest request = new CreerMatchRequest(
                scenario.terrain().getId(),
                scenario.organisateur().getMatricule(),
                scenario.dateHeureDebut(),
                ModeCreation.PUBLIC
        );

        matchCreationService.creerMatch(request);

        ArgumentCaptor<PadelMatch> matchCaptor = ArgumentCaptor.forClass(PadelMatch.class);
        verify(padelMatchRepository).save(matchCaptor.capture());

        PadelMatch matchSauvegarde = matchCaptor.getValue();

        assertEquals(ModeCreation.PUBLIC, matchSauvegarde.getModeCreation());
        assertEquals(VisibiliteMatch.PUBLIC, matchSauvegarde.getVisibiliteCourante());
        assertNull(matchSauvegarde.getDatePassagePublic());
    }

    @Test
    void shouldThrowWhenTerrainDoesNotExist() {
        CreerMatchRequest request = new CreerMatchRequest(
                999L,
                "G0001",
                LocalDateTime.of(2026, 5, 20, 9, 0),
                ModeCreation.PRIVE
        );

        when(terrainRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                RessourceIntrouvableException.class,
                () -> matchCreationService.creerMatch(request)
        );

        verify(padelMatchRepository, never()).save(any());
        verify(participationRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenOrganisateurDoesNotExist() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);

        CreerMatchRequest request = new CreerMatchRequest(
                terrain.getId(),
                "UNKNOWN",
                LocalDateTime.of(2026, 5, 20, 9, 0),
                ModeCreation.PRIVE
        );

        when(terrainRepository.findById(10L)).thenReturn(Optional.of(terrain));
        when(membreRepository.findByMatricule("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(
                RessourceIntrouvableException.class,
                () -> matchCreationService.creerMatch(request)
        );

        verify(padelMatchRepository, never()).save(any());
        verify(participationRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenOrganisateurHasOpenDebt() {
        Scenario scenario = configurerCasValideSansDetteNiPenalite();

        CreerMatchRequest request = new CreerMatchRequest(
                scenario.terrain().getId(),
                scenario.organisateur().getMatricule(),
                scenario.dateHeureDebut(),
                ModeCreation.PRIVE
        );

        when(detteRepository.existsByMembreResponsableIdAndStatutDette(20L, StatutDette.OUVERTE))
                .thenReturn(true);

        assertThrows(
                ConfigurationMetierException.class,
                () -> matchCreationService.creerMatch(request)
        );

        verify(padelMatchRepository, never()).save(any());
        verify(participationRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenOrganisateurHasActivePenalty() {
        Scenario scenario = configurerCasValideSansDetteNiPenalite();

        CreerMatchRequest request = new CreerMatchRequest(
                scenario.terrain().getId(),
                scenario.organisateur().getMatricule(),
                scenario.dateHeureDebut(),
                ModeCreation.PRIVE
        );

        when(detteRepository.existsByMembreResponsableIdAndStatutDette(20L, StatutDette.OUVERTE))
                .thenReturn(false);
        when(penaliteRepository.existsByMembreIdAndStatutPenalite(20L, StatutPenalite.ACTIVE))
                .thenReturn(true);

        assertThrows(
                ConfigurationMetierException.class,
                () -> matchCreationService.creerMatch(request)
        );

        verify(padelMatchRepository, never()).save(any());
        verify(participationRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenSlotIsNotAvailable() {
        Scenario scenario = configurerCasValideSansDetteNiPenalite();

        CreerMatchRequest request = new CreerMatchRequest(
                scenario.terrain().getId(),
                scenario.organisateur().getMatricule(),
                scenario.dateHeureDebut(),
                ModeCreation.PRIVE
        );

        when(detteRepository.existsByMembreResponsableIdAndStatutDette(20L, StatutDette.OUVERTE))
                .thenReturn(false);
        when(penaliteRepository.existsByMembreIdAndStatutPenalite(20L, StatutPenalite.ACTIVE))
                .thenReturn(false);
        when(disponibiliteService.consulterDisponibilites(1L, scenario.dateHeureDebut().toLocalDate()))
                .thenReturn(new DisponibilitesResponse(
                        1L,
                        scenario.dateHeureDebut().toLocalDate(),
                        false,
                        null,
                        List.of()
                ));

        assertThrows(
                ConfigurationMetierException.class,
                () -> matchCreationService.creerMatch(request)
        );

        verify(padelMatchRepository, never()).save(any());
        verify(participationRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenTerrainIsInactive() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        terrain.setActif(false);

        CreerMatchRequest request = new CreerMatchRequest(
                terrain.getId(),
                "G0001",
                LocalDateTime.of(2026, 5, 20, 9, 0),
                ModeCreation.PRIVE
        );

        when(terrainRepository.findById(10L)).thenReturn(Optional.of(terrain));

        assertThrows(
                ConfigurationMetierException.class,
                () -> matchCreationService.creerMatch(request)
        );

        verify(padelMatchRepository, never()).save(any());
        verify(participationRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenSiteIsInactive() {
        Site site = creerSite(1L);
        site.setActif(false);

        Terrain terrain = creerTerrain(10L, site);

        CreerMatchRequest request = new CreerMatchRequest(
                terrain.getId(),
                "G0001",
                LocalDateTime.of(2026, 5, 20, 9, 0),
                ModeCreation.PRIVE
        );

        when(terrainRepository.findById(10L)).thenReturn(Optional.of(terrain));

        assertThrows(
                ConfigurationMetierException.class,
                () -> matchCreationService.creerMatch(request)
        );

        verify(padelMatchRepository, never()).save(any());
        verify(participationRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenOrganisateurIsInactive() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);

        Membre organisateur = creerMembreGlobal(20L, "G0001");
        organisateur.setActif(false);

        CreerMatchRequest request = new CreerMatchRequest(
                terrain.getId(),
                organisateur.getMatricule(),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                ModeCreation.PRIVE
        );

        when(terrainRepository.findById(10L)).thenReturn(Optional.of(terrain));
        when(membreRepository.findByMatricule("G0001")).thenReturn(Optional.of(organisateur));

        assertThrows(
                ConfigurationMetierException.class,
                () -> matchCreationService.creerMatch(request)
        );

        verify(padelMatchRepository, never()).save(any());
        verify(participationRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenSiteMemberBooksOutsideAttachedSite() {
        Site siteTerrain = creerSite(1L);
        Terrain terrain = creerTerrain(10L, siteTerrain);

        Site autreSite = creerSite(2L);
        Membre organisateur = creerMembreSite(20L, "S0001", autreSite);

        CreerMatchRequest request = new CreerMatchRequest(
                terrain.getId(),
                organisateur.getMatricule(),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                ModeCreation.PRIVE
        );

        when(terrainRepository.findById(10L)).thenReturn(Optional.of(terrain));
        when(membreRepository.findByMatricule("S0001")).thenReturn(Optional.of(organisateur));
        doThrow(new ConfigurationMetierException(
                "Un membre SITE ne peut réserver que sur son site de rattachement."
        )).when(reglesReservationMembreService).verifierReglesCreationMatch(
                organisateur,
                terrain,
                request.dateHeureDebut()
        );

        assertThrows(
                ConfigurationMetierException.class,
                () -> matchCreationService.creerMatch(request)
        );

        verify(padelMatchRepository, never()).save(any());
        verify(participationRepository, never()).save(any());
    }

    @Test
    void creerMatchDevraitRefuserSiReglesReservationMembreNonRespectees() {
        Scenario scenario = configurerCasValideSansDetteNiPenalite();

        CreerMatchRequest request = new CreerMatchRequest(
                scenario.terrain().getId(),
                scenario.organisateur().getMatricule(),
                LocalDateTime.of(2026, 6, 30, 9, 0),
                ModeCreation.PRIVE
        );

        doThrow(new ConfigurationMetierException("Un membre ne peut pas reserver aussi tot."))
                .when(reglesReservationMembreService)
                .verifierReglesCreationMatch(
                        scenario.organisateur(),
                        scenario.terrain(),
                        request.dateHeureDebut()
                );

        ConfigurationMetierException exception = assertThrows(
                ConfigurationMetierException.class,
                () -> matchCreationService.creerMatch(request)
        );

        assertTrue(exception.getMessage().contains("reserver"));
        verify(padelMatchRepository, never()).save(any(PadelMatch.class));
        verify(participationRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenOrganisateurAlreadyParticipatesInOverlappingMatch() {
        Scenario scenario = configurerCasValideSansDetteNiPenalite();

        CreerMatchRequest request = new CreerMatchRequest(
                scenario.terrain().getId(),
                scenario.organisateur().getMatricule(),
                scenario.dateHeureDebut(),
                ModeCreation.PRIVE
        );

        when(detteRepository.existsByMembreResponsableIdAndStatutDette(20L, StatutDette.OUVERTE))
                .thenReturn(false);
        when(penaliteRepository.existsByMembreIdAndStatutPenalite(20L, StatutPenalite.ACTIVE))
                .thenReturn(false);
        when(disponibiliteService.consulterDisponibilites(1L, scenario.dateHeureDebut().toLocalDate()))
                .thenReturn(new DisponibilitesResponse(
                        1L,
                        scenario.dateHeureDebut().toLocalDate(),
                        false,
                        null,
                        List.of(
                                new CreneauDisponibiliteResponse(
                                        scenario.terrain().getId(),
                                        scenario.terrain().getNumero(),
                                        scenario.dateHeureDebut(),
                                        scenario.dateHeureFin()
                                )
                        )
                ));

        PadelMatch matchExistant = PadelMatch.builder()
                .terrain(scenario.terrain())
                .dateHeureDebut(LocalDateTime.of(2026, 5, 20, 10, 30))
                .dateHeureFin(LocalDateTime.of(2026, 5, 20, 12, 0))
                .build();

        Participation participationExistante = Participation.builder()
                .match(matchExistant)
                .membre(scenario.organisateur())
                .roleParticipation(RoleParticipation.JOUEUR)
                .modeEntree(ModeEntreeParticipation.INSCRIPTION_PUBLIQUE)
                .statutParticipation(StatutParticipation.CONFIRMEE)
                .dateAffectation(LocalDateTime.of(2026, 5, 1, 10, 0))
                .build();

        when(participationRepository.findByMembreId(20L))
                .thenReturn(List.of(participationExistante));

        assertThrows(
                ConfigurationMetierException.class,
                () -> matchCreationService.creerMatch(request)
        );

        verify(padelMatchRepository, never()).save(any());
        verify(participationRepository, never()).save(any());
    }

    private Scenario configurerCasValide() {
        Scenario scenario = configurerCasValideSansDetteNiPenalite();

        when(detteRepository.existsByMembreResponsableIdAndStatutDette(20L, StatutDette.OUVERTE))
                .thenReturn(false);
        when(penaliteRepository.existsByMembreIdAndStatutPenalite(20L, StatutPenalite.ACTIVE))
                .thenReturn(false);
        when(disponibiliteService.consulterDisponibilites(1L, scenario.dateHeureDebut().toLocalDate()))
                .thenReturn(new DisponibilitesResponse(
                        1L,
                        scenario.dateHeureDebut().toLocalDate(),
                        false,
                        null,
                        List.of(
                                new CreneauDisponibiliteResponse(
                                        scenario.terrain().getId(),
                                        scenario.terrain().getNumero(),
                                        scenario.dateHeureDebut(),
                                        scenario.dateHeureFin()
                                )
                        )
                ));
        when(participationRepository.findByMembreId(20L)).thenReturn(List.of());

        when(padelMatchRepository.save(any(PadelMatch.class))).thenAnswer(invocation -> {
            PadelMatch match = invocation.getArgument(0);
            ReflectionTestUtils.setField(match, "id", 100L);
            return match;
        });

        when(participationRepository.save(any(Participation.class))).thenAnswer(invocation -> {
            Participation participation = invocation.getArgument(0);
            ReflectionTestUtils.setField(participation, "id", 200L);
            return participation;
        });

        return scenario;
    }

    private Scenario configurerCasValideSansDetteNiPenalite() {
        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site);
        Membre organisateur = creerMembreGlobal(20L, "G0001");

        LocalDateTime dateHeureDebut = LocalDateTime.of(2026, 5, 20, 9, 0);
        LocalDateTime dateHeureFin = LocalDateTime.of(2026, 5, 20, 10, 30);

        when(terrainRepository.findById(10L)).thenReturn(Optional.of(terrain));
        when(membreRepository.findByMatricule("G0001")).thenReturn(Optional.of(organisateur));

        return new Scenario(site, terrain, organisateur, dateHeureDebut, dateHeureFin);
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

    private Membre creerMembreGlobal(Long id, String matricule) {
        Membre membre = Membre.builder()
                .matricule(matricule)
                .nom("Dupont")
                .prenom("Jean")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .build();

        ReflectionTestUtils.setField(membre, "id", id);
        return membre;
    }

    private Membre creerMembreSite(Long id, String matricule, Site siteRattachement) {
        Membre membre = Membre.builder()
                .matricule(matricule)
                .nom("Martin")
                .prenom("Sophie")
                .categorieMembre(CategorieMembre.SITE)
                .siteRattachement(siteRattachement)
                .actif(true)
                .build();

        ReflectionTestUtils.setField(membre, "id", id);
        return membre;
    }

    private record Scenario(
            Site site,
            Terrain terrain,
            Membre organisateur,
            LocalDateTime dateHeureDebut,
            LocalDateTime dateHeureFin
    ) {
    }
}
