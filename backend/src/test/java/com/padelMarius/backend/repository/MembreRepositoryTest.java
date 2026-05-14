package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.Site;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class MembreRepositoryTest {

    @Autowired
    private MembreRepository membreRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Test
    void findByMatriculeStartingWith_shouldReturnMembersWithExpectedPrefix() {
        membreRepository.save(Membre.builder()
                .matricule("G2001")
                .nom("Nom Global")
                .prenom("Prenom Global")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .soldeCredit(new BigDecimal("100.00"))
                .build());

        membreRepository.save(Membre.builder()
                .matricule("G2002")
                .nom("Nom Global 2")
                .prenom("Prenom Global 2")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .soldeCredit(new BigDecimal("100.00"))
                .build());

        membreRepository.save(Membre.builder()
                .matricule("L2001")
                .nom("Nom Libre")
                .prenom("Prenom Libre")
                .categorieMembre(CategorieMembre.LIBRE)
                .actif(true)
                .soldeCredit(new BigDecimal("100.00"))
                .build());

        List<Membre> membres = membreRepository.findByMatriculeStartingWith("G");

        assertEquals(2, membres.size());
    }

    @Test
    void findBySiteRattachementIdOrderByMatriculeAsc_shouldReturnMembersForSite() {
        Site bruxelles = siteRepository.save(Site.builder()
                .code("BRU-TEST")
                .nom("Padel Bruxelles Test")
                .adresse("Adresse Bruxelles")
                .actif(true)
                .build());

        Site namur = siteRepository.save(Site.builder()
                .code("NAM-TEST")
                .nom("Padel Namur Test")
                .adresse("Adresse Namur")
                .actif(true)
                .build());

        membreRepository.save(Membre.builder()
                .matricule("S3002")
                .nom("Nom Bruxelles 2")
                .prenom("Prenom Bruxelles 2")
                .categorieMembre(CategorieMembre.SITE)
                .siteRattachement(bruxelles)
                .actif(true)
                .soldeCredit(new BigDecimal("100.00"))
                .build());

        membreRepository.save(Membre.builder()
                .matricule("S3001")
                .nom("Nom Bruxelles 1")
                .prenom("Prenom Bruxelles 1")
                .categorieMembre(CategorieMembre.SITE)
                .siteRattachement(bruxelles)
                .actif(true)
                .soldeCredit(new BigDecimal("100.00"))
                .build());

        membreRepository.save(Membre.builder()
                .matricule("S4001")
                .nom("Nom Namur")
                .prenom("Prenom Namur")
                .categorieMembre(CategorieMembre.SITE)
                .siteRattachement(namur)
                .actif(true)
                .soldeCredit(new BigDecimal("100.00"))
                .build());

        List<Membre> membres = membreRepository
                .findBySiteRattachementIdOrderByMatriculeAsc(bruxelles.getId());

        assertEquals(2, membres.size());
        assertEquals("S3001", membres.getFirst().getMatricule());
    }
}