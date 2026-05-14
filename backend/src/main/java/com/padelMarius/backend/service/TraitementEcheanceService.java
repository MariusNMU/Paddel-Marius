package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.traitement.TraitementEcheanceResponse;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.NaturePaiement;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.Paiement;
import com.padelMarius.backend.entity.StatutDette;
import com.padelMarius.backend.entity.StatutPaiement;
import com.padelMarius.backend.repository.DetteRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.PaiementRepository;
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

    private final PadelMatchRepository padelMatchRepository;
    private final PaiementRepository paiementRepository;
    private final DetteRepository detteRepository;
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