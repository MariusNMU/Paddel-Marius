package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.site.SiteReservationInfoResponse;
import com.padelMarius.backend.service.SiteReservationInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SiteReservationInfoController {

    private final SiteReservationInfoService siteReservationInfoService;

    @GetMapping("/api/sites/reservation-infos")
    public List<SiteReservationInfoResponse> listerSitesAvecInfosReservation(
            @RequestParam(required = false) Integer annee
    ) {
        return siteReservationInfoService.listerSitesAvecInfosReservation(annee);
    }
}