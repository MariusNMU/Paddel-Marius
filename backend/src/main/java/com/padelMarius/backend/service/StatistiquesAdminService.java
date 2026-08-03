package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.statistique.StatistiquesAdminResponse;
import com.padelMarius.backend.entity.Dette;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.Paiement;
import com.padelMarius.backend.entity.Participation;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.StatutDette;
import com.padelMarius.backend.entity.StatutPaiement;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.DetteRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.PaiementRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import com.padelMarius.backend.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.padelMarius.backend.config.ReglesMetier.NOMBRE_JOUEURS_MAXIMUM;

@Service
@RequiredArgsConstructor
public class StatistiquesAdminService {

    private final SiteRepository siteRepository;
    private final PadelMatchRepository padelMatchRepository;
    private final PaiementRepository paiementRepository;
    private final DetteRepository detteRepository;
    private final ParticipationRepository participationRepository;

    @Transactional(readOnly = true)
    public StatistiquesAdminResponse calculerStatistiques(
            LocalDate dateDebut,
            LocalDate dateFin,
            Long siteId
    ) {
        verifierPeriode(dateDebut, dateFin);

        Site siteFiltre = recupererSiteSiDemande(siteId);

        LocalDateTime debutInclus = dateDebut.atStartOfDay();
        LocalDateTime finExclusive = dateFin.plusDays(1).atStartOfDay();

        List<PadelMatch> matches = padelMatchRepository
                .findByDateHeureDebutGreaterThanEqualAndDateHeureDebutBefore(
                        debutInclus,
                        finExclusive
                )
                .stream()
                .filter(match -> match.getEtatCycle() != EtatCycleMatch.ANNULE)
                .filter(match -> matchConcerneSite(match, siteId))
                .toList();

        List<Paiement> paiementsPayes = paiementRepository
                .findByDateHeurePaiementGreaterThanEqualAndDateHeurePaiementBeforeAndStatutPaiement(
                        debutInclus,
                        finExclusive,
                        StatutPaiement.PAYE
                )
                .stream()
                .filter(this::paiementNonLieAMatchAnnule)
                .filter(paiement -> paiementConcerneSite(paiement, siteId))
                .toList();

        List<Dette> dettesOuvertes = detteRepository
                .findByStatutDetteAndMatch_DateHeureDebutGreaterThanEqualAndMatch_DateHeureDebutBefore(
                        StatutDette.OUVERTE,
                        debutInclus,
                        finExclusive
                )
                .stream()
                .filter(dette -> detteConcerneSite(dette, siteId))
                .toList();

        long nombreMatches = matches.size();

        long nombreMatchesAVenir = matches.stream()
                .filter(match -> match.getEtatCycle() == EtatCycleMatch.A_VENIR)
                .count();

        long nombreMatchesTermines = matches.stream()
                .filter(match -> match.getEtatCycle() == EtatCycleMatch.TERMINE)
                .count();

        long nombrePaiements = paiementsPayes.size();

        BigDecimal chiffreAffaires = paiementsPayes.stream()
                .map(Paiement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long nombreDettesOuvertes = dettesOuvertes.size();

        BigDecimal montantDettesOuvertes = dettesOuvertes.stream()
                .map(Dette::getMontantRestant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long nombreParticipationsActives = compterParticipationsActives(matches);

        long capaciteTheoriqueJoueurs =
                nombreMatches * NOMBRE_JOUEURS_MAXIMUM;

        BigDecimal tauxRemplissage = calculerTauxRemplissage(
                nombreParticipationsActives,
                capaciteTheoriqueJoueurs
        );

        return new StatistiquesAdminResponse(
                dateDebut,
                dateFin,
                siteId,
                siteFiltre == null ? null : siteFiltre.getNom(),
                nombreMatches,
                nombreMatchesAVenir,
                nombreMatchesTermines,
                nombrePaiements,
                chiffreAffaires,
                nombreDettesOuvertes,
                montantDettesOuvertes,
                nombreParticipationsActives,
                capaciteTheoriqueJoueurs,
                tauxRemplissage
        );
    }

    private void verifierPeriode(LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut == null || dateFin == null) {
            throw new ConfigurationMetierException(
                    "La date de début et la date de fin sont obligatoires."
            );
        }

        if (dateFin.isBefore(dateDebut)) {
            throw new ConfigurationMetierException(
                    "La date de fin doit être supérieure ou égale à la date de début."
            );
        }
    }

    private Site recupererSiteSiDemande(Long siteId) {
        if (siteId == null) {
            return null;
        }

        return siteRepository.findById(siteId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Site introuvable avec l'id " + siteId
                ));
    }

    private long compterParticipationsActives(List<PadelMatch> matches) {
        long compteur = 0;

        for (PadelMatch match : matches) {
            if (match.getEtatCycle() == EtatCycleMatch.ANNULE) {
                continue;
            }

            List<Participation> participations = participationRepository.findByMatchId(match.getId());

            compteur += participations.stream()
                    .filter(participation ->
                            participation.getStatutParticipation() != StatutParticipation.LIBEREE
                    )
                    .count();
        }

        return compteur;
    }

    private BigDecimal calculerTauxRemplissage(
            long nombreParticipationsActives,
            long capaciteTheoriqueJoueurs
    ) {
        if (capaciteTheoriqueJoueurs == 0) {
            return new BigDecimal("0.00");
        }

        return BigDecimal.valueOf(nombreParticipationsActives)
                .multiply(new BigDecimal("100"))
                .divide(
                        BigDecimal.valueOf(capaciteTheoriqueJoueurs),
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private boolean paiementNonLieAMatchAnnule(Paiement paiement) {
        if (paiement.getParticipation() != null
                && paiement.getParticipation().getMatch() != null) {
            return paiement.getParticipation().getMatch().getEtatCycle() != EtatCycleMatch.ANNULE;
        }

        if (paiement.getDette() != null
                && paiement.getDette().getMatch() != null) {
            return paiement.getDette().getMatch().getEtatCycle() != EtatCycleMatch.ANNULE;
        }

        return true;
    }

    private boolean paiementConcerneSite(Paiement paiement, Long siteId) {
        if (siteId == null) {
            return true;
        }

        if (paiement.getParticipation() != null) {
            return matchConcerneSite(paiement.getParticipation().getMatch(), siteId);
        }

        if (paiement.getDette() != null) {
            return detteConcerneSite(paiement.getDette(), siteId);
        }

        return false;
    }

    private boolean detteConcerneSite(Dette dette, Long siteId) {
        if (siteId == null) {
            return true;
        }

        return matchConcerneSite(dette.getMatch(), siteId);
    }

    private boolean matchConcerneSite(PadelMatch match, Long siteId) {
        if (siteId == null) {
            return true;
        }

        return match.getTerrain() != null
                && match.getTerrain().getSite() != null
                && siteId.equals(match.getTerrain().getSite().getId());
    }
}
