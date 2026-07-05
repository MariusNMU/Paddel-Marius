package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.site.SiteResponse;
import com.padelMarius.backend.service.SiteConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteConsultationService siteConsultationService;

    @GetMapping
    public List<SiteResponse> listerSitesActifs() {
        return siteConsultationService.listerSitesActifs();
    }
}