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
            @RequestHeader(name = "Authorization", required = false)
            String authorization,

            @RequestHeader(name = "X-Admin-Login", required = false)
            String adminLogin
    ) {
        String adminIdentite = choisirIdentiteAdmin(authorization, adminLogin);

        adminAuthorizationService.verifierAdminGlobal(adminIdentite);

        return ResponseEntity.ok(
                traitementEcheanceService.traiterMatchesArrivesAEcheance()
        );
    }

    private String choisirIdentiteAdmin(String authorization, String adminLogin) {
        if (authorization != null && !authorization.isBlank()) {
            return authorization;
        }

        return adminLogin;
    }
}
