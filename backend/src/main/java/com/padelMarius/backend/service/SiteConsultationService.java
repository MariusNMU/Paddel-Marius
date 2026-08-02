package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.site.SiteResponse;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiteConsultationService {

    private final SiteRepository siteRepository;

    public List<SiteResponse> listerSitesActifs() {
        return siteRepository.findByActifTrueOrderByNomAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<SiteResponse> listerTousSites() {
        return siteRepository.findAllByOrderByNomAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private SiteResponse toResponse(Site site) {
        return new SiteResponse(
                site.getId(),
                site.getCode(),
                site.getNom(),
                site.getAdresse()
        );
    }
}
