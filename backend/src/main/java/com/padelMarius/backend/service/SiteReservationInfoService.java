package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.site.SiteReservationInfoResponse;
import com.padelMarius.backend.dto.site.TerrainReservationInfoResponse;
import com.padelMarius.backend.entity.HoraireAnnuelSite;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.Terrain;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.repository.HoraireAnnuelSiteRepository;
import com.padelMarius.backend.repository.SiteRepository;
import com.padelMarius.backend.repository.TerrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Year;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiteReservationInfoService {

    private final SiteRepository siteRepository;
    private final TerrainRepository terrainRepository;
    private final HoraireAnnuelSiteRepository horaireAnnuelSiteRepository;
    private final Clock clock;

    public List<SiteReservationInfoResponse> listerSitesAvecInfosReservation(Integer annee) {
        int anneeCible = annee != null
                ? annee
                : Year.now(clock).getValue();

        return siteRepository.findAll(Sort.by(Sort.Direction.ASC, "nom"))
                .stream()
                .filter(Site::isActif)
                .map(site -> convertirEnResponse(site, anneeCible))
                .toList();
    }

    private SiteReservationInfoResponse convertirEnResponse(Site site, int annee) {
        HoraireAnnuelSite horaire = horaireAnnuelSiteRepository
                .findBySiteAndAnneeCivile(site, annee)
                .orElseThrow(() -> new ConfigurationMetierException(
                        "Aucun horaire annuel configuré pour le site "
                                + site.getId()
                                + " et l'année "
                                + annee
                ));

        List<TerrainReservationInfoResponse> terrains = terrainRepository.findBySiteAndActifTrue(site)
                .stream()
                .sorted(Comparator.comparing(Terrain::getNumero))
                .map(terrain -> new TerrainReservationInfoResponse(
                        terrain.getId(),
                        terrain.getNumero()
                ))
                .toList();

        return new SiteReservationInfoResponse(
                site.getId(),
                site.getCode(),
                site.getNom(),
                horaire.getHeureDebutReservation(),
                horaire.getHeureFinReservation(),
                terrains
        );
    }
}