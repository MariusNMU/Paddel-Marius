package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.membre.MembreResponse;
import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.MembreRepository;
import com.padelMarius.backend.repository.SiteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembreAdminServiceTest {

    @Mock
    private MembreRepository membreRepository;

    @Mock
    private SiteRepository siteRepository;

    @InjectMocks
    private MembreAdminService membreAdminService;

    @Test
    void listerTousLesMembres_shouldReturnAllMembersSortedByMatricule() {
        Membre libre = Membre.builder()
                .id(3L)
                .matricule("L1001")
                .nom("Durand")
                .prenom("Nina")
                .motDePasseHash("$2y$10$w7Hmtss9GA8U9RAxfZeb3.JmBalmCw64iEo6pY5YEgNky9FM7OriK")
                .categorieMembre(CategorieMembre.LIBRE)
                .actif(true)
                .build();

        Membre global = Membre.builder()
                .id(1L)
                .matricule("G1001")
                .nom("Dupont")
                .prenom("Marie")
                .motDePasseHash("$2y$10$w7Hmtss9GA8U9RAxfZeb3.JmBalmCw64iEo6pY5YEgNky9FM7OriK")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .build();

        when(membreRepository.findAll()).thenReturn(List.of(libre, global));

        List<MembreResponse> resultats = membreAdminService.listerTousLesMembres();

        assertEquals(2, resultats.size());
        assertEquals("G1001", resultats.get(0).matricule());
        assertEquals("L1001", resultats.get(1).matricule());
    }

    @Test
    void listerMembresParSite_shouldReturnMembersLinkedToSite() {
        Site bruxelles = Site.builder()
                .id(1001L)
                .code("BRU")
                .nom("Padel Bruxelles")
                .adresse("Rue du Padel 1")
                .actif(true)
                .build();

        Membre membreSite = Membre.builder()
                .id(2L)
                .matricule("S1001")
                .nom("Martin")
                .prenom("Sophie")
                .motDePasseHash("$2y$10$w7Hmtss9GA8U9RAxfZeb3.JmBalmCw64iEo6pY5YEgNky9FM7OriK")
                .categorieMembre(CategorieMembre.SITE)
                .siteRattachement(bruxelles)
                .actif(true)
                .build();

        when(siteRepository.findById(1001L)).thenReturn(Optional.of(bruxelles));
        when(membreRepository.findBySiteRattachementId(1001L)).thenReturn(List.of(membreSite));

        List<MembreResponse> resultats = membreAdminService.listerMembresParSite(1001L);

        assertEquals(1, resultats.size());
        assertEquals("S1001", resultats.get(0).matricule());
        assertEquals(1001L, resultats.get(0).siteRattachementId());
        assertEquals("Padel Bruxelles", resultats.get(0).nomSiteRattachement());
    }

    @Test
    void listerMembresParSite_shouldThrow_whenSiteDoesNotExist() {
        when(siteRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThrows(
                RessourceIntrouvableException.class,
                () -> membreAdminService.listerMembresParSite(9999L)
        );
    }
}
