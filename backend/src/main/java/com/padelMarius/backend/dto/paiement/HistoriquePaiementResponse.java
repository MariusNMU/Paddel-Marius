package com.padelMarius.backend.dto.paiement;

import com.padelMarius.backend.entity.NaturePaiement;
import com.padelMarius.backend.entity.StatutPaiement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistoriquePaiementResponse(
        Long paiementId,
        Long membreId,
        String matriculeMembre,
        NaturePaiement naturePaiement,
        BigDecimal montant,
        StatutPaiement statutPaiement,
        LocalDateTime dateHeurePaiement,
        Long participationId,
        Long detteId,
        Long matchId
) {
}
