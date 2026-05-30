package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.membre.MembreResponse;
import com.padelMarius.backend.service.AdminAuthorizationService;
import com.padelMarius.backend.service.MembreAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/membres")
@RequiredArgsConstructor
public class AdminMembreController {

    private final MembreAdminService membreAdminService;
    private final AdminAuthorizationService adminAuthorizationService;

    @GetMapping
    public ResponseEntity<List<MembreResponse>> listerMembres(
            @RequestHeader(name = "Authorization", required = false)
            String authorization,

            @RequestHeader(name = "X-Admin-Login", required = false)
            String adminLogin,

            @RequestParam(required = false)
            Long siteId
    ) {
        String adminIdentite = choisirIdentiteAdmin(authorization, adminLogin);

        adminAuthorizationService.verifierAccesAdminSite(adminIdentite, siteId);

        if (siteId == null) {
            return ResponseEntity.ok(membreAdminService.listerTousLesMembres());
        }

        return ResponseEntity.ok(membreAdminService.listerMembresParSite(siteId));
    }

    private String choisirIdentiteAdmin(String authorization, String adminLogin) {
        if (authorization != null && !authorization.isBlank()) {
            return authorization;
        }

        return adminLogin;
    }
}
