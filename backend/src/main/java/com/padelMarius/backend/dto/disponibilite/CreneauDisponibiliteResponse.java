package com.padelMarius.backend.dto.disponibilite;

import java.time.LocalDateTime;

public record CreneauDisponibiliteResponse(
        Long terrainId,
        String numeroTerrain,
        LocalDateTime dateHeureDebut,
        LocalDateTime dateHeureFin
) {
}
