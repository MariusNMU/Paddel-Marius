package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.Administrateur;
import com.padelMarius.backend.entity.Dette;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.StatutDette;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql("/data.sql")
class DemoSeedDataTest {

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private MembreRepository membreRepository;

    @Autowired
    private AdministrateurRepository administrateurRepository;

    @Autowired
    private DetteRepository detteRepository;

    @Test
    void dataSql_shouldCreateDemoSitesMembersAdminsAndDebts() {
        Optional<Site> siteBruxelles = siteRepository.findByCode("BRU");
        Optional<Site> siteNamur = siteRepository.findByCode("NAM");

        Optional<Membre> membreGlobal = membreRepository.findByMatricule("G1001");
        Optional<Membre> membreAvecDette = membreRepository.findByMatricule("G1002");
        Optional<Membre> membreInactif = membreRepository.findByMatricule("G9999");

        Optional<Administrateur> adminGlobal =
                administrateurRepository.findByEmailOuLogin("admin-global");

        Optional<Administrateur> adminBruxelles =
                administrateurRepository.findByEmailOuLogin("admin-bruxelles");

        List<Dette> dettesOuvertes = detteRepository.findByStatutDette(StatutDette.OUVERTE);

        assertThat(siteBruxelles).isPresent();
        assertThat(siteBruxelles.get().getNom()).isEqualTo("Padel Bruxelles");

        assertThat(siteNamur).isPresent();
        assertThat(siteNamur.get().getNom()).isEqualTo("Padel Namur");

        assertThat(membreGlobal).isPresent();
        assertThat(membreGlobal.get().isActif()).isTrue();

        assertThat(membreAvecDette).isPresent();
        assertThat(membreAvecDette.get().isActif()).isTrue();

        assertThat(membreInactif).isPresent();
        assertThat(membreInactif.get().isActif()).isFalse();

        assertThat(adminGlobal).isPresent();
        assertThat(adminGlobal.get().getMotDePasse()).isEqualTo("secret");

        assertThat(adminBruxelles).isPresent();
        assertThat(adminBruxelles.get().getSite().getCode()).isEqualTo("BRU");

        assertThat(dettesOuvertes).hasSize(1);
        assertThat(dettesOuvertes.get(0).getMembreResponsable().getMatricule()).isEqualTo("G1002");
    }
}