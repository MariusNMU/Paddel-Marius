package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.site.SiteResponse;
import com.padelMarius.backend.service.SiteConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sites")
@RequiredArgsConstructor
public class AdminSiteController {

    private final SiteConsultationService siteConsultationService;

    @GetMapping
    @PreAuthorize("@adminAuthorizationService.estAdminGlobal(authentication)")
    public List<SiteResponse> listerTousSites() {
        return siteConsultationService.listerTousSites();
    }
}
