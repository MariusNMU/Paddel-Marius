package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.membre.MembreResponse;
import com.padelMarius.backend.service.MembreAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/membres")
@RequiredArgsConstructor
public class AdminMembreController {

    private final MembreAdminService membreAdminService;

    @GetMapping
    @PreAuthorize("@adminAuthorizationService.peutAccederAuSite(authentication, #siteId)")
    public ResponseEntity<List<MembreResponse>> listerMembres(
            @RequestParam(required = false)
            Long siteId
    ) {
        if (siteId == null) {
            return ResponseEntity.ok(
                    membreAdminService.listerTousLesMembres()
            );
        }

        return ResponseEntity.ok(
                membreAdminService.listerMembresParSite(siteId)
        );
    }
}
