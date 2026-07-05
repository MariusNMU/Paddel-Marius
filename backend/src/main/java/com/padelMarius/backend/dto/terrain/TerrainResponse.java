package com.padelMarius.backend.dto.terrain;

public record TerrainResponse(
        Long terrainId,
        String numeroTerrain,
        Long siteId,
        String nomSite
) {
}
