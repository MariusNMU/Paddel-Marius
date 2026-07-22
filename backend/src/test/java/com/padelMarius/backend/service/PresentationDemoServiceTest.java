package com.padelMarius.backend.service;

import com.padelMarius.backend.config.DonneesDemonstrationProperties;
import com.padelMarius.backend.dto.demo.PresentationDemoResponse;
import com.padelMarius.backend.dto.site.SiteResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.padelMarius.backend.config.ReglesMetier.FENETRE_RESERVATION_GLOBAL_JOURS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PresentationDemoServiceTest {

    @Mock
    private SiteConsultationService siteConsultationService;

    private PresentationDemoService presentationDemoService;

    @BeforeEach
    void setUp() {
        DonneesDemonstrationProperties properties =
                new DonneesDemonstrationProperties();
        properties.setMotDePasseJoueur("password");

        DonneesDemonstrationProperties.Joueur joueur =
                new DonneesDemonstrationProperties.Joueur();
        joueur.setMatricule("G1001");
        joueur.setDescription("joueur GLOBAL actif");
        properties.setJoueurs(List.of(joueur));

        DonneesDemonstrationProperties.Administrateur administrateur =
                new DonneesDemonstrationProperties.Administrateur();
        administrateur.setLogin("admin-global");
        administrateur.setMotDePasse("secret");
        administrateur.setDescription("administrateur GLOBAL");
        properties.setAdministrateurs(List.of(administrateur));

        presentationDemoService = new PresentationDemoService(
                properties,
                siteConsultationService
        );
    }

    @Test
    void consulterPresentation_shouldCombineRulesSitesAndDemoAccounts() {
        SiteResponse site = new SiteResponse(
                1001L,
                "BRU",
                "Padel Bruxelles",
                "Rue du Padel 1"
        );

        when(siteConsultationService.listerSitesActifs())
                .thenReturn(List.of(site));

        PresentationDemoResponse response =
                presentationDemoService.consulterPresentation();

        assertThat(response.categoriesMembres()).hasSize(3);
        assertThat(response.categoriesMembres().getFirst().regle())
                .contains(String.valueOf(
                        FENETRE_RESERVATION_GLOBAL_JOURS
                ));

        assertThat(response.sites()).containsExactly(site);

        assertThat(response.joueurs()).singleElement()
                .satisfies(joueur -> {
                    assertThat(joueur.matricule())
                            .isEqualTo("G1001");
                    assertThat(joueur.motDePasse())
                            .isEqualTo("password");
                });

        assertThat(response.administrateurs()).singleElement()
                .satisfies(administrateur -> {
                    assertThat(administrateur.login())
                            .isEqualTo("admin-global");
                    assertThat(administrateur.motDePasse())
                            .isEqualTo("secret");
                });
    }
}
