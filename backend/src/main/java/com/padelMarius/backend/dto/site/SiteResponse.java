package com.padelMarius.backend.dto.site;

public record SiteResponse(
        Long siteId,
        String code,
        String nom,
        String adresse
) {
}
