package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.disponibilite.DisponibilitesResponse;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.Fermeture;
import com.padelMarius.backend.entity.HoraireAnnuelSite;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.PorteeFermeture;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.Terrain;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.FermetureRepository;
import com.padelMarius.backend.repository.HoraireAnnuelSiteRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.SiteRepository;
import com.padelMarius.backend.repository.TerrainRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisponibiliteServiceTest {

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private TerrainRepository terrainRepository;

    @Mock
    private HoraireAnnuelSiteRepository horaireAnnuelSiteRepository;

    @Mock
    private FermetureRepository fermetureRepository;

    @Mock
    private PadelMatchRepository padelMatchRepository;

    @InjectMocks
    private DisponibiliteService disponibiliteService;

    @Test
    void shouldReturnAvailableSlotsWhenSiteIsOpenAndNoMatchExists() {
        LocalDate date = LocalDate.of(2026, 5, 8);

        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site, "1");
        HoraireAnnuelSite horaire = creerHoraire(site, 2026, LocalTime.of(9, 0), LocalTime.of(13, 0));

        when(siteRepository.findById(1L)).thenReturn(Optional.of(site));
        when(fermetureRepository.findFirstByDateFermetureAndPorteeAndSite(date, PorteeFermeture.LOCALE, site))
                .thenReturn(Optional.empty());
        when(fermetureRepository.findFirstByDateFermetureAndPortee(date, PorteeFermeture.GLOBALE))
                .thenReturn(Optional.empty());
        when(horaireAnnuelSiteRepository.findBySiteAndAnneeCivile(site, 2026))
                .thenReturn(Optional.of(horaire));
        when(terrainRepository.findBySiteAndActifTrue(site))
                .thenReturn(List.of(terrain));
        when(padelMatchRepository.findByTerrainInAndDateHeureDebutBetween(anyList(), any(), any()))
                .thenReturn(List.of());

        DisponibilitesResponse response = disponibiliteService.consulterDisponibilites(1L, date);

        assertEquals(1L, response.siteId());
        assertEquals(date, response.date());
        assertFalse(response.ferme());
        assertEquals(2, response.creneaux().size());

        assertEquals(LocalDateTime.of(2026, 5, 8, 9, 0), response.creneaux().get(0).dateHeureDebut());
        assertEquals(LocalDateTime.of(2026, 5, 8, 10, 30), response.creneaux().get(0).dateHeureFin());

        assertEquals(LocalDateTime.of(2026, 5, 8, 10, 45), response.creneaux().get(1).dateHeureDebut());
        assertEquals(LocalDateTime.of(2026, 5, 8, 12, 15), response.creneaux().get(1).dateHeureFin());
    }

    @Test
    void shouldExcludeSlotAlreadyUsedByExistingMatch() {
        LocalDate date = LocalDate.of(2026, 5, 8);

        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site, "1");
        HoraireAnnuelSite horaire = creerHoraire(site, 2026, LocalTime.of(9, 0), LocalTime.of(13, 0));

        PadelMatch matchExistant = creerMatch(
                terrain,
                LocalDateTime.of(2026, 5, 8, 9, 0)
        );

        when(siteRepository.findById(1L)).thenReturn(Optional.of(site));
        when(fermetureRepository.findFirstByDateFermetureAndPorteeAndSite(date, PorteeFermeture.LOCALE, site))
                .thenReturn(Optional.empty());
        when(fermetureRepository.findFirstByDateFermetureAndPortee(date, PorteeFermeture.GLOBALE))
                .thenReturn(Optional.empty());
        when(horaireAnnuelSiteRepository.findBySiteAndAnneeCivile(site, 2026))
                .thenReturn(Optional.of(horaire));
        when(terrainRepository.findBySiteAndActifTrue(site))
                .thenReturn(List.of(terrain));
        when(padelMatchRepository.findByTerrainInAndDateHeureDebutBetween(anyList(), any(), any()))
                .thenReturn(List.of(matchExistant));

        DisponibilitesResponse response = disponibiliteService.consulterDisponibilites(1L, date);

        assertFalse(response.ferme());
        assertEquals(1, response.creneaux().size());
        assertEquals(LocalDateTime.of(2026, 5, 8, 10, 45), response.creneaux().get(0).dateHeureDebut());
        assertEquals(LocalDateTime.of(2026, 5, 8, 12, 15), response.creneaux().get(0).dateHeureFin());
    }

    @Test
    void shouldIgnoreCancelledMatchWhenCalculatingAvailableSlots() {
        LocalDate date = LocalDate.of(2026, 5, 8);

        Site site = creerSite(1L);
        Terrain terrain = creerTerrain(10L, site, "1");
        HoraireAnnuelSite horaire = creerHoraire(site, 2026, LocalTime.of(9, 0), LocalTime.of(13, 0));

        PadelMatch matchAnnule = creerMatch(
                terrain,
                LocalDateTime.of(2026, 5, 8, 9, 0)
        );
        matchAnnule.setEtatCycle(EtatCycleMatch.ANNULE);

        when(siteRepository.findById(1L)).thenReturn(Optional.of(site));
        when(fermetureRepository.findFirstByDateFermetureAndPorteeAndSite(date, PorteeFermeture.LOCALE, site))
                .thenReturn(Optional.empty());
        when(fermetureRepository.findFirstByDateFermetureAndPortee(date, PorteeFermeture.GLOBALE))
                .thenReturn(Optional.empty());
        when(horaireAnnuelSiteRepository.findBySiteAndAnneeCivile(site, 2026))
                .thenReturn(Optional.of(horaire));
        when(terrainRepository.findBySiteAndActifTrue(site))
                .thenReturn(List.of(terrain));
        when(padelMatchRepository.findByTerrainInAndDateHeureDebutBetween(anyList(), any(), any()))
                .thenReturn(List.of(matchAnnule));

        DisponibilitesResponse response = disponibiliteService.consulterDisponibilites(1L, date);

        assertFalse(response.ferme());
        assertEquals(2, response.creneaux().size());
        assertEquals(LocalDateTime.of(2026, 5, 8, 9, 0), response.creneaux().get(0).dateHeureDebut());
        assertEquals(LocalDateTime.of(2026, 5, 8, 10, 45), response.creneaux().get(1).dateHeureDebut());
    }

    @Test
    void shouldReturnClosedResponseWhenLocalClosureExists() {
        LocalDate date = LocalDate.of(2026, 5, 8);

        Site site = creerSite(1L);
        Fermeture fermeture = creerFermeture(date, PorteeFermeture.LOCALE, site, "Travaux");

        when(siteRepository.findById(1L)).thenReturn(Optional.of(site));
        when(fermetureRepository.findFirstByDateFermetureAndPorteeAndSite(date, PorteeFermeture.LOCALE, site))
                .thenReturn(Optional.of(fermeture));

        DisponibilitesResponse response = disponibiliteService.consulterDisponibilites(1L, date);

        assertTrue(response.ferme());
        assertEquals("Travaux", response.motifFermeture());
        assertTrue(response.creneaux().isEmpty());

        verify(horaireAnnuelSiteRepository, never()).findBySiteAndAnneeCivile(any(), anyInt());
        verify(terrainRepository, never()).findBySiteAndActifTrue(any());
        verify(padelMatchRepository, never()).findByTerrainInAndDateHeureDebutBetween(anyList(), any(), any());
    }

    @Test
    void shouldReturnClosedResponseWhenGlobalClosureExists() {
        LocalDate date = LocalDate.of(2026, 5, 8);

        Site site = creerSite(1L);
        Fermeture fermeture = creerFermeture(date, PorteeFermeture.GLOBALE, null, "Fermeture annuelle");

        when(siteRepository.findById(1L)).thenReturn(Optional.of(site));
        when(fermetureRepository.findFirstByDateFermetureAndPorteeAndSite(date, PorteeFermeture.LOCALE, site))
                .thenReturn(Optional.empty());
        when(fermetureRepository.findFirstByDateFermetureAndPortee(date, PorteeFermeture.GLOBALE))
                .thenReturn(Optional.of(fermeture));

        DisponibilitesResponse response = disponibiliteService.consulterDisponibilites(1L, date);

        assertTrue(response.ferme());
        assertEquals("Fermeture annuelle", response.motifFermeture());
        assertTrue(response.creneaux().isEmpty());

        verify(horaireAnnuelSiteRepository, never()).findBySiteAndAnneeCivile(any(), anyInt());
        verify(terrainRepository, never()).findBySiteAndActifTrue(any());
        verify(padelMatchRepository, never()).findByTerrainInAndDateHeureDebutBetween(anyList(), any(), any());
    }

    @Test
    void shouldThrowWhenSiteDoesNotExist() {
        LocalDate date = LocalDate.of(2026, 5, 8);

        when(siteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                RessourceIntrouvableException.class,
                () -> disponibiliteService.consulterDisponibilites(999L, date)
        );
    }

    @Test
    void shouldThrowWhenAnnualScheduleDoesNotExist() {
        LocalDate date = LocalDate.of(2026, 5, 8);

        Site site = creerSite(1L);

        when(siteRepository.findById(1L)).thenReturn(Optional.of(site));
        when(fermetureRepository.findFirstByDateFermetureAndPorteeAndSite(date, PorteeFermeture.LOCALE, site))
                .thenReturn(Optional.empty());
        when(fermetureRepository.findFirstByDateFermetureAndPortee(date, PorteeFermeture.GLOBALE))
                .thenReturn(Optional.empty());
        when(horaireAnnuelSiteRepository.findBySiteAndAnneeCivile(site, 2026))
                .thenReturn(Optional.empty());

        assertThrows(
                ConfigurationMetierException.class,
                () -> disponibiliteService.consulterDisponibilites(1L, date)
        );
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

    private Terrain creerTerrain(Long id, Site site, String numero) {
        Terrain terrain = Terrain.builder()
                .site(site)
                .numero(numero)
                .actif(true)
                .build();

        ReflectionTestUtils.setField(terrain, "id", id);
        return terrain;
    }

    private HoraireAnnuelSite creerHoraire(Site site, int annee, LocalTime heureDebut, LocalTime heureFin) {
        return HoraireAnnuelSite.builder()
                .site(site)
                .anneeCivile(annee)
                .heureDebutReservation(heureDebut)
                .heureFinReservation(heureFin)
                .build();
    }

    private PadelMatch creerMatch(Terrain terrain, LocalDateTime debut) {
        return PadelMatch.builder()
                .terrain(terrain)
                .dateHeureDebut(debut)
                .dateHeureFin(debut.plusMinutes(90))
                .etatCycle(EtatCycleMatch.A_VENIR)
                .build();
    }

    private Fermeture creerFermeture(LocalDate date, PorteeFermeture portee, Site site, String motif) {
        return Fermeture.builder()
                .dateFermeture(date)
                .portee(portee)
                .site(site)
                .motif(motif)
                .build();
    }
}
