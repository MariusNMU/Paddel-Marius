package com.padelMarius.backend.dto.parametre;

import java.math.BigDecimal;

public record ParametresMetierResponse(
        int dureeMatchMinutes,
        int pauseEntreMatchesMinutes,
        int nombreJoueursMaximum,
        BigDecimal prixTotalMatch,
        BigDecimal montantParticipationStandard,
        BigDecimal soldeInitialJoueur
) {
}