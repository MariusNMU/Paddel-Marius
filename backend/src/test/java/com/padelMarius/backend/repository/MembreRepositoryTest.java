package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.Membre;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class MembreRepositoryTest {

    @Autowired
    private MembreRepository membreRepository;

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
}