package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.etatoperationnel.EtatOperationnelAdminResponse;
import com.padelMarius.backend.service.EtatOperationnelAdminService;
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
public class EtatOperationnelAdminController {

    private final EtatOperationnelAdminService etatOperationnelAdminService;

    @GetMapping("/api/admin/etat-operationnel")
    @PreAuthorize("@adminAuthorizationService.peutAccederAuSite(authentication, #siteId)")
    public ResponseEntity<EtatOperationnelAdminResponse> consulterEtatOperationnel(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @RequestParam
            Long siteId
    ) {
        EtatOperationnelAdminResponse response =
                etatOperationnelAdminService.consulterEtatOperationnel(
                        date,
                        siteId
                );

        return ResponseEntity.ok(response);
    }
}
