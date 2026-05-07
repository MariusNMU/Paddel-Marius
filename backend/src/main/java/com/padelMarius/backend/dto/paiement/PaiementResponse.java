package com.padelMarius.backend.dto.paiement;

import com.padelMarius.backend.entity.NaturePaiement;
import com.padelMarius.backend.entity.StatutPaiement;
import com.padelMarius.backend.entity.StatutParticipation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaiementResponse(
        Long paiementId,
        Long participationId,
        Long membreId,
        String matriculeMembre,
        NaturePaiement naturePaiement,
        BigDecimal montant,
        StatutPaiement statutPaiement,
        StatutParticipation statutParticipation,
        LocalDateTime dateHeurePaiement,
        LocalDateTime dateConfirmationParticipation
) {
}