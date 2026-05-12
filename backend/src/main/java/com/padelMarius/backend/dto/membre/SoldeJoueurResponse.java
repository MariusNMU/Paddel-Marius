package com.padelMarius.backend.dto.membre;

import java.math.BigDecimal;

public record SoldeJoueurResponse(
        Long membreId,
        String matricule,
        BigDecimal soldeCredit
) {
}