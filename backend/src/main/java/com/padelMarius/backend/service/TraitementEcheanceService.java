package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.traitement.TraitementEcheanceResponse;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.ModeCreation;
import com.padelMarius.backend.entity.NaturePaiement;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.Paiement;
import com.padelMarius.backend.entity.Participation;
import com.padelMarius.backend.entity.Penalite;
import com.padelMarius.backend.entity.RoleParticipation;
import com.padelMarius.backend.entity.StatutDette;
import com.padelMarius.backend.entity.StatutPaiement;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.entity.StatutPenalite;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.repository.DetteRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.PaiementRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import com.padelMarius.backend.repository.PenaliteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TraitementEcheanceService {

    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal PRIX_TOTAL_PAR_DEFAUT = new BigDecimal("60.00");
    private static final int NOMBRE_JOUEURS_REQUIS = 4;
    private static final long DUREE_PENALITE_JOURS = 7L;
    private static final String TYPE_PENALITE_MATCH_PRIVE_INCOMPLET = "RESERVATION_PRIVEE_INCOMPLETE";
    private static final String MOTIF_PENALITE_MATCH_PRIVE_INCOMPLET =
            "Match privé incomplet au moment du match.";

    private final PadelMatchRepository padelMatchRepository;
    private final PaiementRepository paiementRepository;
    private final DetteRepository detteRepository;
    private final ParticipationRepository participationRepository;
    private final PenaliteRepository penaliteRepository;
    private final DetteService detteService;
    private final Clock clock;

    @Transactional
    public TraitementEcheanceResponse traiterMatchesArrivesAEcheance() {
        LocalDateTime maintenant = LocalDateTime.now(clock);

        List<PadelMatch> matches = padelMatchRepository
                .findByEtatCycleAndDateHeureDebutLessThanEqual(
                        EtatCycleMatch.A_VENIR,
                        maintenant
                );

        int matchesDemarres = 0;
        int dettesCreees = 0;

        for (PadelMatch match : matches) {
            if (detteDoitEtreCreee(match)) {
                detteService.genererDettePourMatch(match.getId());
                dettesCreees++;
            }

            creerPenaliteSiMatchPriveIncomplet(match, maintenant);

            match.setEtatCycle(EtatCycleMatch.DEMARRE);
            padelMatchRepository.save(match);
            matchesDemarres++;
        }

        return new TraitementEcheanceResponse(
                maintenant,
                matches.size(),
                matchesDemarres,
                dettesCreees
        );
    }

    private boolean detteDoitEtreCreee(PadelMatch match) {
        if (match.getId() == null) {
            return false;
        }

        boolean detteExisteDeja = detteRepository.findByMatchId(match.getId())
                .filter(dette -> dette.getStatutDette() == StatutDette.OUVERTE)
                .isPresent();

        if (detteExisteDeja) {
            return false;
        }

        BigDecimal prixTotal = montantOuPrixDefaut(match.getPrixTotal());
        BigDecimal totalPaye = calculerTotalPayePourMatch(match.getId());

        return prixTotal.subtract(totalPaye).compareTo(ZERO) > 0;
    }

    private BigDecimal calculerTotalPayePourMatch(Long matchId) {
        return paiementRepository.findByParticipation_Match_IdAndNaturePaiementAndStatutPaiement(
                        matchId,
                        NaturePaiement.PARTICIPATION,
                        StatutPaiement.PAYE
                )
                .stream()
                .map(Paiement::getMontant)
                .filter(Objects::nonNull)
                .map(this::normaliserMontant)
                .reduce(ZERO, BigDecimal::add);
    }

    private void creerPenaliteSiMatchPriveIncomplet(PadelMatch match, LocalDateTime maintenant) {
        if (match.getModeCreation() != ModeCreation.PRIVE) {
            return;
        }

        List<Participation> participations = participationRepository.findByMatchId(match.getId());

        long participationsConfirmees = participations.stream()
                .filter(participation -> participation.getStatutParticipation() == StatutParticipation.CONFIRMEE)
                .count();

        if (participationsConfirmees >= NOMBRE_JOUEURS_REQUIS) {
            return;
        }

        boolean penaliteExisteDeja = !penaliteRepository.findByMatchSourceId(match.getId()).isEmpty();

        if (penaliteExisteDeja) {
            return;
        }

        Participation participationOrganisateur = participations.stream()
                .filter(participation -> participation.getRoleParticipation() == RoleParticipation.ORGANISATEUR)
                .filter(participation -> participation.getStatutParticipation() != StatutParticipation.LIBEREE)
                .findFirst()
                .orElseThrow(() -> new ConfigurationMetierException(
                        "Le match doit avoir une participation organisateur active."
                ));

        Membre organisateur = participationOrganisateur.getMembre();

        Penalite penalite = Penalite.builder()
                .membre(organisateur)
                .matchSource(match)
                .typePenalite(TYPE_PENALITE_MATCH_PRIVE_INCOMPLET)
                .motif(MOTIF_PENALITE_MATCH_PRIVE_INCOMPLET)
                .dateDebut(maintenant)
                .dateFin(maintenant.plusDays(DUREE_PENALITE_JOURS))
                .statutPenalite(StatutPenalite.ACTIVE)
                .build();

        penaliteRepository.save(penalite);
    }

    private BigDecimal montantOuPrixDefaut(BigDecimal montant) {
        if (montant == null) {
            return PRIX_TOTAL_PAR_DEFAUT;
        }

        return normaliserMontant(montant);
    }

    private BigDecimal normaliserMontant(BigDecimal montant) {
        if (montant == null) {
            return ZERO;
        }

        return montant.setScale(2, RoundingMode.HALF_UP);
    }
}
