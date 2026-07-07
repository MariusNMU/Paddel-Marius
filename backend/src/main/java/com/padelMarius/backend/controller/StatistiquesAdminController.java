package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.statistique.StatistiquesAdminResponse;
import com.padelMarius.backend.service.StatistiquesAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class StatistiquesAdminController {

    private final StatistiquesAdminService statistiquesAdminService;

    @GetMapping("/api/admin/statistiques")
    @PreAuthorize("@adminAuthorizationService.peutAccederAuSite(authentication, #siteId)")
    public ResponseEntity<StatistiquesAdminResponse> consulterStatistiques(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateDebut,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateFin,

            @RequestParam(required = false)
            Long siteId
    ) {
        StatistiquesAdminResponse response =
                statistiquesAdminService.calculerStatistiques(
                        dateDebut,
                        dateFin,
                        siteId
                );

        return ResponseEntity.ok(response);
    }
}
