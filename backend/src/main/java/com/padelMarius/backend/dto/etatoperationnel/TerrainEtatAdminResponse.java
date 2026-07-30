package com.padelMarius.backend.dto.etatoperationnel;

import java.util.List;

public record TerrainEtatAdminResponse(
        Long terrainId,
        String numeroTerrain,
        boolean actif,
        EtatTerrainOperationnel etatTerrain,
        List<MatchEtatAdminResponse> matches
) {
}
