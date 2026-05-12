package com.padelMarius.backend.dto.matchpublic;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MatchPublicResponse(
        Long matchId,
        Long siteId,
        String nomSite,
        Long terrainId,
        String numeroTerrain,
        LocalDateTime dateHeureDebut,
        LocalDateTime dateHeureFin,
        int nombreParticipantsActifs,
        int placesDisponibles,
        BigDecimal prixTotal,
        BigDecimal montantParticipation
) {
}