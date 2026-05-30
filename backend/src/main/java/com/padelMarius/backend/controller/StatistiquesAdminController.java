package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.statistique.StatistiquesAdminResponse;
import com.padelMarius.backend.service.AdminAuthorizationService;
import com.padelMarius.backend.service.StatistiquesAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class StatistiquesAdminController {

    private final StatistiquesAdminService statistiquesAdminService;
    private final AdminAuthorizationService adminAuthorizationService;

    @GetMapping("/api/admin/statistiques")
    public ResponseEntity<StatistiquesAdminResponse> consulterStatistiques(
            @RequestHeader(name = "Authorization", required = false)
            String authorization,

            @RequestHeader(name = "X-Admin-Login", required = false)
            String adminLogin,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateDebut,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateFin,

            @RequestParam(required = false)
            Long siteId
    ) {
        String adminIdentite = choisirIdentiteAdmin(authorization, adminLogin);

        adminAuthorizationService.verifierAccesAdminSite(adminIdentite, siteId);

        StatistiquesAdminResponse response = statistiquesAdminService.calculerStatistiques(
                dateDebut,
                dateFin,
                siteId
        );

        return ResponseEntity.ok(response);
    }

    private String choisirIdentiteAdmin(String authorization, String adminLogin) {
        if (authorization != null && !authorization.isBlank()) {
            return authorization;
        }

        return adminLogin;
    }
}
