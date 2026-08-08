package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.Administrateur;
import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.RoleAdministrateur;
import com.padelMarius.backend.entity.Site;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AuthRepositoryTest {

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private MembreRepository membreRepository;

    @Autowired
    private AdministrateurRepository administrateurRepository;

    @Test
    void membreRepository_shouldFindMemberByMatriculeForPlayerAuthentication() {
        Membre membre = membreRepository.save(Membre.builder()
                .matricule("G0001")
                .nom("Dupont")
                .prenom("Marie")
                .motDePasseHash("$2y$10$w7Hmtss9GA8U9RAxfZeb3.JmBalmCw64iEo6pY5YEgNky9FM7OriK")
                .categorieMembre(CategorieMembre.GLOBAL)
                .siteRattachement(null)
                .actif(true)
                .build());

        Optional<Membre> resultat = membreRepository
                .findByMatriculeIgnoreCase("g0001");

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getId()).isEqualTo(membre.getId());
        assertThat(resultat.get().getMatricule()).isEqualTo("G0001");
        assertThat(resultat.get().isActif()).isTrue();
    }

    @Test
    void administrateurRepository_shouldFindAdminByLoginForAdminAuthentication() {
        Site site = siteRepository.save(Site.builder()
                .code("BRU")
                .nom("Padel Bruxelles")
                .adresse("Rue du Test 1, 1000 Bruxelles")
                .actif(true)
                .build());

        Administrateur administrateur = administrateurRepository.save(
                Administrateur.builder()
                        .nom("Admin")
                        .prenom("Bruxelles")
                        .emailOuLogin("admin-bruxelles")
                        .motDePasseHash("$2y$10$NfvQuu66degMeOzxLi5q1.mpoEhsdnUpoiAWGRUE2Ma9zydeH5GEu")
                        .roleAdministrateur(RoleAdministrateur.SITE)
                        .site(site)
                        .actif(true)
                        .build()
        );

        Optional<Administrateur> resultat =
                administrateurRepository
                        .findByEmailOuLoginIgnoreCase("ADMIN-BRUXELLES");

        assertThat(resultat).isPresent();
        assertThat(resultat.get().getId()).isEqualTo(administrateur.getId());
        assertThat(resultat.get().getEmailOuLogin()).isEqualTo("admin-bruxelles");
        assertThat(resultat.get().getMotDePasseHash()).startsWith("$2");
        assertThat(resultat.get().getRoleAdministrateur()).isEqualTo(RoleAdministrateur.SITE);
        assertThat(resultat.get().getSite().getId()).isEqualTo(site.getId());
        assertThat(resultat.get().isActif()).isTrue();
    }
}
