package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.admin.MembreAdminResponse;
import com.padelMarius.backend.service.AdminMembreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminMembreController {

    private final AdminMembreService adminMembreService;

    @GetMapping("/membres")
    public List<MembreAdminResponse> listerTousLesMembres() {
        return adminMembreService.listerTousLesMembres();
    }

    @GetMapping("/sites/{siteId}/membres")
    public List<MembreAdminResponse> listerMembresParSite(
            @PathVariable Long siteId
    ) {
        return adminMembreService.listerMembresParSite(siteId);
    }
}