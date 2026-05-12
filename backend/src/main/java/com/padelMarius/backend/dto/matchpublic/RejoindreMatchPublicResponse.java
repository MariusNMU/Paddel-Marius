package com.padelMarius.backend.dto.matchpublic;

import com.padelMarius.backend.entity.StatutParticipation;

import java.math.BigDecimal;

public record RejoindreMatchPublicResponse(
        Long matchId,
        Long participationId,
        Long paiementId,
        String matriculeJoueur,
        BigDecimal montantPaye,
        StatutParticipation statutParticipation,
        BigDecimal soldeRestant
) {
}