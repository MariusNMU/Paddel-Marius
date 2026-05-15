package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.Site;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

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
                .build());

        membreRepository.save(Membre.builder()
                .matricule("G2002")
                .nom("Nom Global 2")
                .prenom("Prenom Global 2")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .build());

        membreRepository.save(Membre.builder()
                .matricule("L2001")
                .nom("Nom Libre")
                .prenom("Prenom Libre")
                .categorieMembre(CategorieMembre.LIBRE)
                .actif(true)
                .build());

        List<Membre> membres = membreRepository.findByMatriculeStartingWith("G");

        assertEquals(2, membres.size());
    }

    @Test
    void findBySiteRattachementId_shouldReturnMembersLinkedToSite() {
        Site bruxelles = siteRepository.save(Site.builder()
                .code("BRU-TEST")
                .nom("Padel Bruxelles Test")
                .adresse("Rue Test 1")
                .actif(true)
                .build());

        Site namur = siteRepository.save(Site.builder()
                .code("NAM-TEST")
                .nom("Padel Namur Test")
                .adresse("Rue Test 2")
                .actif(true)
                .build());

        membreRepository.save(Membre.builder()
                .matricule("S3001")
                .nom("Nom Bruxelles")
                .prenom("Prenom Bruxelles")
                .categorieMembre(CategorieMembre.SITE)
                .siteRattachement(bruxelles)
                .actif(true)
                .build());

        membreRepository.save(Membre.builder()
                .matricule("S3002")
                .nom("Nom Namur")
                .prenom("Prenom Namur")
                .categorieMembre(CategorieMembre.SITE)
                .siteRattachement(namur)
                .actif(true)
                .build());

        membreRepository.save(Membre.builder()
                .matricule("G3001")
                .nom("Nom Global")
                .prenom("Prenom Global")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .build());

        List<Membre> membresBruxelles = membreRepository.findBySiteRattachementId(bruxelles.getId());

        assertEquals(1, membresBruxelles.size());
        assertEquals("S3001", membresBruxelles.get(0).getMatricule());
    }
}
