package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.fermeture.CreerFermetureRequest;
import com.padelMarius.backend.dto.fermeture.FermetureAdminResponse;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.Fermeture;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.PorteeFermeture;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.Terrain;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.FermetureRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.SiteRepository;
import com.padelMarius.backend.repository.TerrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.NaturePaiement;
import com.padelMarius.backend.entity.Paiement;
import com.padelMarius.backend.entity.StatutPaiement;
import com.padelMarius.backend.repository.PaiementRepository;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminFermetureService {

    private final FermetureRepository fermetureRepository;
    private final SiteRepository siteRepository;
    private final TerrainRepository terrainRepository;
    private final PadelMatchRepository padelMatchRepository;
    private final PaiementRepository paiementRepository;

    @Transactional
    public FermetureAdminResponse creerFermeture(CreerFermetureRequest request) {
        validerPorteeEtSite(request);

        Site site = null;

        if (request.portee() == PorteeFermeture.LOCALE) {
            site = siteRepository.findById(request.siteId())
                    .orElseThrow(() -> new RessourceIntrouvableException(
                            "Site introuvable avec l'id " + request.siteId()
                    ));
        }

        verifierDoublonFermeture(request, site);

        Fermeture fermeture = Fermeture.builder()
                .dateFermeture(request.dateFermeture())
                .portee(request.portee())
                .site(site)
                .motif(request.motif())
                .build();

        Fermeture fermetureSauvegardee = fermetureRepository.save(fermeture);

        List<Terrain> terrainsConcernes = trouverTerrainsConcernes(request, site);
        ResultatAnnulation resultatAnnulation = annulerMatchesAVenir(request, terrainsConcernes);

        return new FermetureAdminResponse(
                fermetureSauvegardee.getId(),
                fermetureSauvegardee.getDateFermeture(),
                fermetureSauvegardee.getPortee(),
                site != null ? site.getId() : null,
                site != null ? site.getNom() : null,
                fermetureSauvegardee.getMotif(),
                resultatAnnulation.nombreMatchesAnnules(),
                resultatAnnulation.nombreRemboursementsCredites(),
                resultatAnnulation.montantTotalRembourse()
        );
    }

    private void validerPorteeEtSite(CreerFermetureRequest request) {
        if (request.portee() == PorteeFermeture.GLOBALE && request.siteId() != null) {
            throw new ConfigurationMetierException(
                    "Une fermeture globale ne doit pas avoir de site."
            );
        }

        if (request.portee() == PorteeFermeture.LOCALE && request.siteId() == null) {
            throw new ConfigurationMetierException(
                    "Une fermeture locale doit avoir un site."
            );
        }
    }

    private void verifierDoublonFermeture(CreerFermetureRequest request, Site site) {
        boolean existeDeja;

        if (request.portee() == PorteeFermeture.GLOBALE) {
            existeDeja = fermetureRepository.existsByDateFermetureAndPorteeAndSiteIsNull(
                    request.dateFermeture(),
                    PorteeFermeture.GLOBALE
            );
        } else {
            existeDeja = fermetureRepository.existsBySiteIdAndDateFermetureAndPortee(
                    site.getId(),
                    request.dateFermeture(),
                    PorteeFermeture.LOCALE
            );
        }

        if (existeDeja) {
            throw new ConfigurationMetierException(
                    "Une fermeture existe déjà pour cette date et ce périmètre."
            );
        }
    }

    private List<Terrain> trouverTerrainsConcernes(CreerFermetureRequest request, Site site) {
        if (request.portee() == PorteeFermeture.GLOBALE) {
            return terrainRepository.findAll();
        }

        return terrainRepository.findBySiteAndActifTrue(site);
    }
    private ResultatRemboursement rembourserPaiementsDuMatch(PadelMatch match) {
        List<Paiement> paiements = paiementRepository
                .findByParticipation_Match_IdAndNaturePaiementAndStatutPaiement(
                        match.getId(),
                        NaturePaiement.PARTICIPATION,
                        StatutPaiement.PAYE
                );

        int nombreRemboursements = 0;
        BigDecimal montantTotal = BigDecimal.ZERO;

        for (Paiement paiement : paiements) {
            Membre membre = paiement.getMembre();

            if (membre != null && paiement.getMontant() != null) {
                BigDecimal soldeActuel = membre.getSoldeCredit() != null
                        ? membre.getSoldeCredit()
                        : BigDecimal.ZERO;

                membre.setSoldeCredit(soldeActuel.add(paiement.getMontant()));
                paiement.setStatutPaiement(StatutPaiement.ANNULE);

                nombreRemboursements++;
                montantTotal = montantTotal.add(paiement.getMontant());
            }
        }

        return new ResultatRemboursement(nombreRemboursements, montantTotal);
    }

    private record ResultatAnnulation(
            int nombreMatchesAnnules,
            int nombreRemboursementsCredites,
            BigDecimal montantTotalRembourse
    ) {
    }

    private record ResultatRemboursement(
            int nombreRemboursements,
            BigDecimal montantTotalRembourse
    ) {
    }
    private ResultatAnnulation annulerMatchesAVenir(
            CreerFermetureRequest request,
            List<Terrain> terrainsConcernes
    ) {
        if (terrainsConcernes.isEmpty()) {
            return new ResultatAnnulation(0, 0, BigDecimal.ZERO);
        }

        LocalDateTime debutJour = request.dateFermeture().atStartOfDay();
        LocalDateTime finJour = request.dateFermeture().atTime(LocalTime.MAX);

        List<PadelMatch> matches = padelMatchRepository
                .findByTerrainInAndDateHeureDebutGreaterThanEqualAndDateHeureDebutBeforeAndEtatCycle(
                        terrainsConcernes,
                        debutJour,
                        finJour,
                        EtatCycleMatch.A_VENIR
                );

        int nombreRemboursements = 0;
        BigDecimal montantTotalRembourse = BigDecimal.ZERO;

        for (PadelMatch match : matches) {
            ResultatRemboursement resultatRemboursement = rembourserPaiementsDuMatch(match);

            nombreRemboursements += resultatRemboursement.nombreRemboursements();
            montantTotalRembourse = montantTotalRembourse.add(resultatRemboursement.montantTotalRembourse());

            match.setEtatCycle(EtatCycleMatch.ANNULE);
        }

        return new ResultatAnnulation(
                matches.size(),
                nombreRemboursements,
                montantTotalRembourse
        );
    }
}