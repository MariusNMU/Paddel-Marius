package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.disponibilite.CreneauDisponibiliteResponse;
import com.padelMarius.backend.dto.disponibilite.DisponibilitesResponse;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.Fermeture;
import com.padelMarius.backend.entity.HoraireAnnuelSite;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.Terrain;
import com.padelMarius.backend.entity.PorteeFermeture;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.FermetureRepository;
import com.padelMarius.backend.repository.HoraireAnnuelSiteRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.SiteRepository;
import com.padelMarius.backend.repository.TerrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.padelMarius.backend.config.ReglesMetier.DUREE_MATCH;
import static com.padelMarius.backend.config.ReglesMetier.PAUSE_ENTRE_MATCHES;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DisponibiliteService {

    private final SiteRepository siteRepository;
    private final TerrainRepository terrainRepository;
    private final HoraireAnnuelSiteRepository horaireAnnuelSiteRepository;
    private final FermetureRepository fermetureRepository;
    private final PadelMatchRepository padelMatchRepository;

    public DisponibilitesResponse consulterDisponibilites(Long siteId, LocalDate date) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new RessourceIntrouvableException("Site introuvable avec l'id " + siteId));

        Optional<Fermeture> fermeture = trouverFermeture(site, date);

        if (fermeture.isPresent()) {
            return new DisponibilitesResponse(
                    site.getId(),
                    site.getNom(),
                    date,
                    true,
                    fermeture.get().getMotif(),
                    List.of()
            );
        }

        HoraireAnnuelSite horaire = horaireAnnuelSiteRepository
                .findBySiteAndAnneeCivile(site, date.getYear())
                .orElseThrow(() -> new ConfigurationMetierException(
                        "Aucun horaire annuel configuré pour le site "
                                + site.getId()
                                + " et l'année "
                                + date.getYear()
                ));

        verifierHoraireValide(horaire);

        List<Terrain> terrainsActifs = terrainRepository.findBySiteAndActifTrue(site);

        if (terrainsActifs.isEmpty()) {
            return new DisponibilitesResponse(
                    site.getId(),
                    site.getNom(),
                    date,
                    false,
                    null,
                    List.of()
            );
        }

        LocalDateTime debutJour = date.atStartOfDay();
        LocalDateTime finJour = date.plusDays(1).atStartOfDay().minusNanos(1);

        List<PadelMatch> matchesDuJour = padelMatchRepository
                .findByTerrainInAndDateHeureDebutBetween(terrainsActifs, debutJour, finJour);

        Map<Long, List<PadelMatch>> matchesParTerrainId = matchesDuJour.stream()
                .filter(match -> match.getEtatCycle() != EtatCycleMatch.ANNULE)
                .filter(match -> match.getTerrain() != null)
                .filter(match -> match.getTerrain().getId() != null)
                .collect(Collectors.groupingBy(match -> match.getTerrain().getId()));

        List<CreneauDisponibiliteResponse> creneauxDisponibles = new ArrayList<>();

        for (Terrain terrain : terrainsActifs) {
            List<PadelMatch> matchesDuTerrain = matchesParTerrainId.getOrDefault(terrain.getId(), List.of());

            creneauxDisponibles.addAll(
                    calculerCreneauxDisponiblesPourTerrain(date, horaire, terrain, matchesDuTerrain)
            );
        }

        creneauxDisponibles.sort(
                Comparator.comparing(CreneauDisponibiliteResponse::dateHeureDebut)
                        .thenComparing(CreneauDisponibiliteResponse::numeroTerrain)
        );

        return new DisponibilitesResponse(
                site.getId(),
                site.getNom(),
                date,
                false,
                null,
                creneauxDisponibles
        );
    }

    private Optional<Fermeture> trouverFermeture(Site site, LocalDate date) {
        Optional<Fermeture> fermetureLocale = fermetureRepository.findFirstByDateFermetureAndPorteeAndSite(
                date,
                PorteeFermeture.LOCALE,
                site
        );

        if (fermetureLocale.isPresent()) {
            return fermetureLocale;
        }

        return fermetureRepository.findFirstByDateFermetureAndPortee(
                date,
                PorteeFermeture.GLOBALE
        );
    }

    private void verifierHoraireValide(HoraireAnnuelSite horaire) {
        LocalTime heureDebut = horaire.getHeureDebutReservation();
        LocalTime heureFin = horaire.getHeureFinReservation();

        if (heureDebut == null || heureFin == null) {
            throw new ConfigurationMetierException("Les horaires du site sont incomplets.");
        }

        if (!heureFin.isAfter(heureDebut)) {
            throw new ConfigurationMetierException("L'heure de fin doit être après l'heure de début.");
        }
    }

    private List<CreneauDisponibiliteResponse> calculerCreneauxDisponiblesPourTerrain(
            LocalDate date,
            HoraireAnnuelSite horaire,
            Terrain terrain,
            List<PadelMatch> matchesDuTerrain
    ) {
        List<CreneauDisponibiliteResponse> creneaux = new ArrayList<>();

        LocalDateTime debutCreneau = date.atTime(horaire.getHeureDebutReservation());
        LocalDateTime finReservation = date.atTime(horaire.getHeureFinReservation());

        while (!debutCreneau.plus(DUREE_MATCH).isAfter(finReservation)) {
            LocalDateTime finCreneau = debutCreneau.plus(DUREE_MATCH);

            if (creneauEstDisponible(debutCreneau, finCreneau, matchesDuTerrain)) {
                creneaux.add(new CreneauDisponibiliteResponse(
                        terrain.getId(),
                        terrain.getNumero(),
                        debutCreneau,
                        finCreneau
                ));
            }

            debutCreneau = debutCreneau.plus(DUREE_MATCH).plus(PAUSE_ENTRE_MATCHES);
        }

        return creneaux;
    }

    private boolean creneauEstDisponible(
            LocalDateTime debutCreneau,
            LocalDateTime finCreneau,
            List<PadelMatch> matchesDuTerrain
    ) {
        return matchesDuTerrain.stream()
                .noneMatch(match -> chevaucheAvecPauseObligatoire(debutCreneau, finCreneau, match));
    }

    private boolean chevaucheAvecPauseObligatoire(
            LocalDateTime debutCreneau,
            LocalDateTime finCreneau,
            PadelMatch matchExistant
    ) {
        LocalDateTime debutMatchExistant = matchExistant.getDateHeureDebut();

        if (debutMatchExistant == null) {
            return false;
        }

        LocalDateTime finMatchExistant = debutMatchExistant.plus(DUREE_MATCH);

        boolean creneauFinitAssezTot = !finCreneau.plus(PAUSE_ENTRE_MATCHES).isAfter(debutMatchExistant);
        boolean creneauCommenceAssezTard = !debutCreneau.isBefore(finMatchExistant.plus(PAUSE_ENTRE_MATCHES));

        return !(creneauFinitAssezTot || creneauCommenceAssezTard);
    }
}
