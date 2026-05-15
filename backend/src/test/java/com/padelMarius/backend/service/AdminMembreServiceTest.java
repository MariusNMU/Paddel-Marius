package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.admin.MembreAdminResponse;
import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.repository.MembreRepository;
import com.padelMarius.backend.repository.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AdminMembreServiceTest {

    private MembreRepository membreRepository;
    private SiteRepository siteRepository;
    private AdminMembreService service;

    @BeforeEach
    void setUp() {
        membreRepository = mock(MembreRepository.class);
        siteRepository = mock(SiteRepository.class);
        service = new AdminMembreService(membreRepository, siteRepository);
    }

    @Test
    void listerTousLesMembres_shouldReturnAllMembers() {
        Site bruxelles = creerSite(1001L, "BRU", "Padel Bruxelles");

        Membre membre = creerMembre(
                2003L,
                "S1001",
                "Martin",
                "Sophie",
                CategorieMembre.SITE,
                bruxelles
        );

        when(membreRepository.findAllByOrderByMatriculeAsc())
                .thenReturn(List.of(membre));

        List<MembreAdminResponse> resultats = service.listerTousLesMembres();

        assertEquals(1, resultats.size());
        assertEquals("S1001", resultats.getFirst().matricule());
        assertEquals(1001L, resultats.getFirst().siteRattachementId());
        assertEquals("Padel Bruxelles", resultats.getFirst().nomSiteRattachement());
    }

    @Test
    void listerMembresParSite_shouldReturnMembersAttachedToSite() {
        Site namur = creerSite(1002L, "NAM", "Padel Namur");

        Membre membre = creerMembre(
                2004L,
                "S1002",
                "Bernard",
                "Luc",
                CategorieMembre.SITE,
                namur
        );

        when(siteRepository.findById(1002L)).thenReturn(Optional.of(namur));
        when(membreRepository.findBySiteRattachementIdOrderByMatriculeAsc(1002L))
                .thenReturn(List.of(membre));

        List<MembreAdminResponse> resultats = service.listerMembresParSite(1002L);

        assertEquals(1, resultats.size());
        assertEquals("S1002", resultats.getFirst().matricule());
        assertEquals("Padel Namur", resultats.getFirst().nomSiteRattachement());
    }

    private Site creerSite(Long id, String code, String nom) {
        Site site = Site.builder()
                .code(code)
                .nom(nom)
                .adresse("Adresse")
                .actif(true)
                .build();

        ReflectionTestUtils.setField(site, "id", id);
        return site;
    }

    private Membre creerMembre(
            Long id,
            String matricule,
            String nom,
            String prenom,
            CategorieMembre categorieMembre,
            Site site
    ) {
        Membre membre = Membre.builder()
                .matricule(matricule)
                .nom(nom)
                .prenom(prenom)
                .categorieMembre(categorieMembre)
                .siteRattachement(site)
                .actif(true)
                .soldeCredit(new BigDecimal("100.00"))
                .build();

        ReflectionTestUtils.setField(membre, "id", id);
        return membre;
    }
}