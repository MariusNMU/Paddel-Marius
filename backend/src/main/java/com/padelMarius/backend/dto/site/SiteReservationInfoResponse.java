package com.padelMarius.backend.dto.site;

import java.time.LocalTime;
import java.util.List;

public record SiteReservationInfoResponse(
        Long siteId,
        String codeSite,
        String nomSite,
        LocalTime heureDebutReservation,
        LocalTime heureFinReservation,
        List<TerrainReservationInfoResponse> terrains
) {
}