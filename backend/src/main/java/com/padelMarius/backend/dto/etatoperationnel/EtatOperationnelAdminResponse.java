package com.padelMarius.backend.dto.etatoperationnel;

import java.time.LocalDate;
import java.util.List;

public record EtatOperationnelAdminResponse(
        LocalDate date,
        Long siteId,
        String nomSite,
        boolean siteActif,
        boolean ferme,
        String motifFermeture,
        List<TerrainEtatAdminResponse> terrains
) {
}
