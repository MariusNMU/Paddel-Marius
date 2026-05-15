package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.membre.MembreResponse;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.MembreRepository;
import com.padelMarius.backend.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembreAdminService {

    private final MembreRepository membreRepository;
    private final SiteRepository siteRepository;

    @Transactional(readOnly = true)
    public List<MembreResponse> listerTousLesMembres() {
        return membreRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Membre::getMatricule))
                .map(this::convertirEnResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MembreResponse> listerMembresParSite(Long siteId) {
        if (siteId == null) {
            throw new ConfigurationMetierException("Le site est obligatoire.");
        }

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Site introuvable avec l'id " + siteId
                ));

        return membreRepository.findBySiteRattachementId(site.getId())
                .stream()
                .sorted(Comparator.comparing(Membre::getMatricule))
                .map(this::convertirEnResponse)
                .toList();
    }

    private MembreResponse convertirEnResponse(Membre membre) {
        Site site = membre.getSiteRattachement();

        return new MembreResponse(
                membre.getId(),
                membre.getMatricule(),
                membre.getNom(),
                membre.getPrenom(),
                membre.getCategorieMembre(),
                site != null ? site.getId() : null,
                site != null ? site.getNom() : null,
                membre.isActif(),
                membre.getSoldeCredit()
        );
    }
}