package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.etatoperationnel.EtatOperationnelAdminResponse;
import com.padelMarius.backend.dto.etatoperationnel.EtatTerrainOperationnel;
import com.padelMarius.backend.dto.etatoperationnel.MatchEtatAdminResponse;
import com.padelMarius.backend.dto.etatoperationnel.OccupationHebdomadaireAdminResponse;
import com.padelMarius.backend.dto.etatoperationnel.TerrainEtatAdminResponse;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.Fermeture;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.PorteeFermeture;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.entity.Terrain;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.FermetureRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import com.padelMarius.backend.repository.SiteRepository;
import com.padelMarius.backend.repository.TerrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class EtatOperationnelAdminService {

    private final SiteRepository siteRepository;
    private final TerrainRepository terrainRepository;
    private final PadelMatchRepository padelMatchRepository;
    private final FermetureRepository fermetureRepository;
    private final ParticipationRepository participationRepository;

    @Transactional(readOnly = true)
    public EtatOperationnelAdminResponse consulterEtatOperationnel(
            LocalDate date,
            Long siteId
    ) {
        verifierParametres(date, siteId);

        Site site = chargerSite(siteId);
        List<Terrain> terrains = chargerTerrains(siteId);
        List<PadelMatch> matches = chargerMatches(
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay(),
                terrains
        );
        Fermeture fermeture = trouverFermeture(
                date,
                siteId,
                fermetureRepository.findByDateFermeture(date)
        );

        return creerEtatOperationnel(
                date,
                site,
                terrains,
                matches,
                fermeture
        );
    }

    @Transactional(readOnly = true)
    public OccupationHebdomadaireAdminResponse consulterOccupationHebdomadaire(
            LocalDate date,
            Long siteId
    ) {
        verifierParametres(date, siteId);

        LocalDate dateDebut = date.with(
                TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        );
        LocalDate dateFin = dateDebut.plusDays(6);

        Site site = chargerSite(siteId);
        List<Terrain> terrains = chargerTerrains(siteId);
        List<PadelMatch> matches = chargerMatches(
                dateDebut.atStartOfDay(),
                dateFin.plusDays(1).atStartOfDay(),
                terrains
        );
        List<Fermeture> fermetures = fermetureRepository
                .findByDateFermetureBetweenOrderByDateFermetureAsc(
                        dateDebut,
                        dateFin
                );

        Map<LocalDate, List<PadelMatch>> matchesParJour = matches.stream()
                .collect(Collectors.groupingBy(
                        match -> match.getDateHeureDebut().toLocalDate()
                ));

        List<EtatOperationnelAdminResponse> jours = IntStream.range(0, 7)
                .mapToObj(indexJour -> {
                    LocalDate jour = dateDebut.plusDays(indexJour);

                    return creerEtatOperationnel(
                            jour,
                            site,
                            terrains,
                            matchesParJour.getOrDefault(jour, List.of()),
                            trouverFermeture(
                                    jour,
                                    siteId,
                                    fermetures
                            )
                    );
                })
                .toList();

        return new OccupationHebdomadaireAdminResponse(
                dateDebut,
                dateFin,
                site.getId(),
                site.getNom(),
                site.isActif(),
                jours
        );
    }

    private Site chargerSite(Long siteId) {
        return siteRepository.findById(siteId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Site introuvable avec l'id " + siteId
                ));
    }

    private List<Terrain> chargerTerrains(Long siteId) {
        return terrainRepository.findBySiteId(siteId)
                .stream()
                .sorted(Comparator.comparing(Terrain::getNumero))
                .toList();
    }

    private EtatOperationnelAdminResponse creerEtatOperationnel(
            LocalDate date,
            Site site,
            List<Terrain> terrains,
            List<PadelMatch> matches,
            Fermeture fermeture
    ) {

        Map<Long, List<PadelMatch>> matchesParTerrain = matches.stream()
                .collect(Collectors.groupingBy(
                        match -> match.getTerrain().getId()
                ));

        List<TerrainEtatAdminResponse> etatsTerrains = terrains.stream()
                .map(terrain -> creerEtatTerrain(
                        terrain,
                        matchesParTerrain.getOrDefault(
                                terrain.getId(),
                                List.of()
                        ),
                        fermeture != null
                ))
                .toList();

        return new EtatOperationnelAdminResponse(
                date,
                site.getId(),
                site.getNom(),
                site.isActif(),
                fermeture != null,
                fermeture == null ? null : fermeture.getMotif(),
                etatsTerrains
        );
    }

    private void verifierParametres(
            LocalDate date,
            Long siteId
    ) {
        if (date == null) {
            throw new ConfigurationMetierException(
                    "La date est obligatoire."
            );
        }

        if (siteId == null) {
            throw new ConfigurationMetierException(
                    "Le site est obligatoire."
            );
        }
    }

    private List<PadelMatch> chargerMatches(
            LocalDateTime debutInclus,
            LocalDateTime finExclusive,
            List<Terrain> terrains
    ) {
        if (terrains.isEmpty()) {
            return List.of();
        }

        return padelMatchRepository
                .findByTerrainInAndDateHeureDebutGreaterThanEqualAndDateHeureDebutBeforeOrderByDateHeureDebutAsc(
                        terrains,
                        debutInclus,
                        finExclusive
                );
    }

    private Fermeture trouverFermeture(
            LocalDate date,
            Long siteId,
            List<Fermeture> fermetures
    ) {
        return fermetures.stream()
                .filter(fermeture ->
                        date.equals(fermeture.getDateFermeture())
                )
                .filter(fermeture ->
                        fermeture.getPortee() == PorteeFermeture.GLOBALE
                )
                .findFirst()
                .orElseGet(() -> fermetures.stream()
                        .filter(fermeture ->
                                date.equals(fermeture.getDateFermeture())
                        )
                        .filter(fermeture ->
                                fermeture.getPortee() == PorteeFermeture.LOCALE
                        )
                        .filter(fermeture ->
                                fermeture.getSite() != null
                                        && siteId.equals(
                                        fermeture.getSite().getId()
                                )
                        )
                        .findFirst()
                        .orElse(null));
    }

    private TerrainEtatAdminResponse creerEtatTerrain(
            Terrain terrain,
            List<PadelMatch> matches,
            boolean ferme
    ) {
        List<MatchEtatAdminResponse> matchesResponse = matches.stream()
                .map(this::creerMatchResponse)
                .toList();

        return new TerrainEtatAdminResponse(
                terrain.getId(),
                terrain.getNumero(),
                terrain.isActif(),
                determinerEtatTerrain(
                        terrain,
                        matches,
                        ferme
                ),
                matchesResponse
        );
    }

    private EtatTerrainOperationnel determinerEtatTerrain(
            Terrain terrain,
            List<PadelMatch> matches,
            boolean ferme
    ) {
        if (!terrain.isActif()) {
            return EtatTerrainOperationnel.INACTIF;
        }

        if (ferme) {
            return EtatTerrainOperationnel.FERME;
        }

        boolean reservationActive = matches.stream()
                .anyMatch(match ->
                        match.getEtatCycle() != EtatCycleMatch.ANNULE
                );

        return reservationActive
                ? EtatTerrainOperationnel.RESERVE
                : EtatTerrainOperationnel.DISPONIBLE;
    }

    private MatchEtatAdminResponse creerMatchResponse(
            PadelMatch match
    ) {
        long nombreParticipants = participationRepository
                .countByMatchIdAndStatutParticipationNot(
                        match.getId(),
                        StatutParticipation.LIBEREE
                );

        return new MatchEtatAdminResponse(
                match.getId(),
                match.getDateHeureDebut(),
                match.getDateHeureFin(),
                match.getVisibiliteCourante(),
                match.getEtatCycle(),
                nombreParticipants
        );
    }
}
