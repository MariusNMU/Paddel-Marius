package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.Administrateur;
import com.padelMarius.backend.entity.Dette;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.StatutDette;
import com.padelMarius.backend.entity.Terrain;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql("/data.sql")
class DemoSeedDataTest {

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private TerrainRepository terrainRepository;

    @Autowired
    private HoraireAnnuelSiteRepository horaireAnnuelSiteRepository;

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
        int anneeCourante = LocalDate.now().getYear();
        List<Terrain> terrainsBruxelles = terrainRepository.findBySiteIdAndActifTrue(1001L);
        Optional<Membre> membreGlobal = membreRepository.findByMatricule("G1001");
        Optional<Membre> membreAvecDette = membreRepository.findByMatricule("G1002");
        Optional<Membre> membreInactif = membreRepository.findByMatricule("G9999");

        Optional<Administrateur> adminGlobal =
                administrateurRepository.findByEmailOuLogin("admin-global");

        Optional<Administrateur> adminBruxelles =
                administrateurRepository.findByEmailOuLogin("admin-bruxelles");

        Optional<Administrateur> adminNamur =
                administrateurRepository.findByEmailOuLogin("admin-namur");

        List<Dette> dettesOuvertes = detteRepository.findByStatutDette(StatutDette.OUVERTE);

        assertThat(siteBruxelles).isPresent();
        assertThat(siteBruxelles.get().getNom()).isEqualTo("Padel Bruxelles");
        assertThat(terrainsBruxelles).hasSize(3);
        assertThat(terrainsBruxelles)
                .extracting(Terrain::getNumero)
                .contains("T1", "T2", "T3");
        assertThat(siteNamur).isPresent();
        assertThat(siteNamur.get().getNom()).isEqualTo("Padel Namur");

        assertThat(
                horaireAnnuelSiteRepository.existsBySiteIdAndAnneeCivile(
                        1001L,
                        anneeCourante
                )
        ).isTrue();

        assertThat(
                horaireAnnuelSiteRepository.existsBySiteIdAndAnneeCivile(
                        1001L,
                        anneeCourante + 1
                )
        ).isTrue();

        assertThat(
                horaireAnnuelSiteRepository.existsBySiteIdAndAnneeCivile(
                        1002L,
                        anneeCourante
                )
        ).isTrue();

        assertThat(
                horaireAnnuelSiteRepository.existsBySiteIdAndAnneeCivile(
                        1002L,
                        anneeCourante + 1
                )
        ).isTrue();

        assertThat(membreGlobal).isPresent();
        assertThat(membreGlobal.get().isActif()).isTrue();
        assertThat(membreGlobal.get().getMotDePasseHash()).startsWith("$2");

        assertThat(membreAvecDette).isPresent();
        assertThat(membreAvecDette.get().isActif()).isTrue();

        assertThat(membreInactif).isPresent();
        assertThat(membreInactif.get().isActif()).isFalse();

        assertThat(adminGlobal).isPresent();
        assertThat(adminGlobal.get().getMotDePasseHash()).startsWith("$2");

        assertThat(adminBruxelles).isPresent();
        assertThat(adminBruxelles.get().getMotDePasseHash()).startsWith("$2");
        assertThat(adminBruxelles.get().getSite().getCode()).isEqualTo("BRU");

        assertThat(adminNamur).isPresent();
        assertThat(adminNamur.get().getMotDePasseHash()).startsWith("$2");
        assertThat(adminNamur.get().getSite().getCode()).isEqualTo("NAM");

        assertThat(dettesOuvertes).hasSize(1);
        assertThat(dettesOuvertes.get(0).getMembreResponsable().getMatricule()).isEqualTo("G1002");
    }
}
