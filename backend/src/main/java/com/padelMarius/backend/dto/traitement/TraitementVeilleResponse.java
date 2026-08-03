package com.padelMarius.backend.dto.traitement;

import java.time.LocalDate;

public record TraitementVeilleResponse(
        LocalDate dateTraitement,
        LocalDate dateMatchTraitee,
        int matchesAnalyses,
        int matchesPassesPublics,
        int participationsLiberees
) {
}
