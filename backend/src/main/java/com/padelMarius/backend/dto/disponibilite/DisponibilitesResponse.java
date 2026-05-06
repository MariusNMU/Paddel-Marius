package com.padelMarius.backend.dto.disponibilite;

import java.time.LocalDate;
import java.util.List;

public record DisponibilitesResponse(
        Long siteId,
        LocalDate date,
        boolean ferme,
        String motifFermeture,
        List<CreneauDisponibiliteResponse> creneaux
) {
}