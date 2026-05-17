package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.traitement.TraitementEcheanceResponse;
import com.padelMarius.backend.service.AdminAuthorizationService;
import com.padelMarius.backend.service.TraitementEcheanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TraitementEcheanceController {

    private final TraitementEcheanceService traitementEcheanceService;
    private final AdminAuthorizationService adminAuthorizationService;

    @PostMapping("/api/admin/matches/traitement-echeance")
    public ResponseEntity<TraitementEcheanceResponse> traiterMatchesArrivesAEcheance(
            @RequestHeader(name = "X-Admin-Login", required = false)
            String adminLogin
    ) {
        adminAuthorizationService.verifierAdminGlobal(adminLogin);

        return ResponseEntity.ok(
                traitementEcheanceService.traiterMatchesArrivesAEcheance()
        );
    }
}