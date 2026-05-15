package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.admin.MembreAdminResponse;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.MembreRepository;
import com.padelMarius.backend.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMembreService {

    private final MembreRepository membreRepository;
    private final SiteRepository siteRepository;

    public List<MembreAdminResponse> listerTousLesMembres() {
        return membreRepository.findAllByOrderByMatriculeAsc()
                .stream()
                .map(this::convertirEnResponse)
                .toList();
    }

    public List<MembreAdminResponse> listerMembresParSite(Long siteId) {
        siteRepository.findById(siteId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Site introuvable avec l'id " + siteId
                ));

        return membreRepository.findBySiteRattachementIdOrderByMatriculeAsc(siteId)
                .stream()
                .map(this::convertirEnResponse)
                .toList();
    }

    private MembreAdminResponse convertirEnResponse(Membre membre) {
        Site site = membre.getSiteRattachement();

        return new MembreAdminResponse(
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