package com.padelMarius.backend.dto.traitement;

import java.time.LocalDateTime;

public record TraitementEcheanceResponse(
        LocalDateTime dateHeureTraitement,
        int matchesAnalyses,
        int matchesDemarres,
        int dettesCreees
) {
}