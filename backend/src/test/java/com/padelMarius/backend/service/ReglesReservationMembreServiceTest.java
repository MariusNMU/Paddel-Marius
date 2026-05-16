package com.padelMarius.backend.service;

import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.Terrain;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReglesReservationMembreServiceTest {

    private ReglesReservationMembreService service;

    @BeforeEach
    void setUp() {
        Clock clockFixe = Clock.fixed(
                LocalDate.of(2026, 5, 7)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant(),
                ZoneId.systemDefault()
        );

        service = new ReglesReservationMembreService(clockFixe);
    }

    @Test
    void globalDevraitPouvoirReserverJusqua21JoursAvantSurToutSite() {
        Site site = creerSite("SITE-A");
        Terrain terrain = creerTerrain(site);
        Membre membre = creerMembre("G0001", CategorieMembre.GLOBAL, null);

        LocalDateTime dateMatch = LocalDateTime.of(2026, 5, 28, 9, 0);

        assertThatCode(() -> service.verifierReglesCreationMatch(membre, terrain, dateMatch))
                .doesNotThrowAnyException();
    }

    @Test
    void globalDevraitEtreRefuseSiReservationPlusDe21JoursAvant() {
        Site site = creerSite("SITE-A");
        Terrain terrain = creerTerrain(site);
        Membre membre = creerMembre("G0001", CategorieMembre.GLOBAL, null);

        LocalDateTime dateMatch = LocalDateTime.of(2026, 5, 29, 9, 0);

        assertThatThrownBy(() -> service.verifierReglesCreationMatch(membre, terrain, dateMatch))
                .isInstanceOf(ConfigurationMetierException.class)
                .hasMessageContaining("21 jours");
    }

    @Test
    void siteDevraitPouvoirReserverJusqua14JoursAvantSurSonSite() {
        Site site = creerSite("SITE-A");
        Terrain terrain = creerTerrain(site);
        Membre membre = creerMembre("S0001", CategorieMembre.SITE, site);

        LocalDateTime dateMatch = LocalDateTime.of(2026, 5, 21, 9, 0);

        assertThatCode(() -> service.verifierReglesCreationMatch(membre, terrain, dateMatch))
                .doesNotThrowAnyException();
    }

    @Test
    void siteDevraitEtreRefuseSiReservationPlusDe14JoursAvant() {
        Site site = creerSite("SITE-A");
        Terrain terrain = creerTerrain(site);
        Membre membre = creerMembre("S0001", CategorieMembre.SITE, site);

        LocalDateTime dateMatch = LocalDateTime.of(2026, 5, 22, 9, 0);

        assertThatThrownBy(() -> service.verifierReglesCreationMatch(membre, terrain, dateMatch))
                .isInstanceOf(ConfigurationMetierException.class)
                .hasMessageContaining("14 jours");
    }

    @Test
    void siteDevraitEtreRefuseSurUnAutreSite() {
        Site siteA = creerSite("SITE-A");
        Site siteB = creerSite("SITE-B");

        Terrain terrainSiteB = creerTerrain(siteB);
        Membre membre = creerMembre("S0001", CategorieMembre.SITE, siteA);

        LocalDateTime dateMatch = LocalDateTime.of(2026, 5, 21, 9, 0);

        assertThatThrownBy(() -> service.verifierReglesCreationMatch(membre, terrainSiteB, dateMatch))
                .isInstanceOf(ConfigurationMetierException.class)
                .hasMessageContaining("site de rattachement");
    }

    @Test
    void libreDevraitPouvoirReserverJusqua5JoursAvantSurToutSite() {
        Site site = creerSite("SITE-A");
        Terrain terrain = creerTerrain(site);
        Membre membre = creerMembre("L0001", CategorieMembre.LIBRE, null);

        LocalDateTime dateMatch = LocalDateTime.of(2026, 5, 12, 9, 0);

        assertThatCode(() -> service.verifierReglesCreationMatch(membre, terrain, dateMatch))
                .doesNotThrowAnyException();
    }

    @Test
    void libreDevraitEtreRefuseSiReservationPlusDe5JoursAvant() {
        Site site = creerSite("SITE-A");
        Terrain terrain = creerTerrain(site);
        Membre membre = creerMembre("L0001", CategorieMembre.LIBRE, null);

        LocalDateTime dateMatch = LocalDateTime.of(2026, 5, 13, 9, 0);

        assertThatThrownBy(() -> service.verifierReglesCreationMatch(membre, terrain, dateMatch))
                .isInstanceOf(ConfigurationMetierException.class)
                .hasMessageContaining("5 jours");
    }

    @Test
    void devraitRefuserUnMatriculeIncoherentAvecLaCategorie() {
        Site site = creerSite("SITE-A");
        Terrain terrain = creerTerrain(site);
        Membre membre = creerMembre("S0001", CategorieMembre.GLOBAL, null);

        LocalDateTime dateMatch = LocalDateTime.of(2026, 5, 28, 9, 0);

        assertThatThrownBy(() -> service.verifierReglesCreationMatch(membre, terrain, dateMatch))
                .isInstanceOf(ConfigurationMetierException.class)
                .hasMessageContaining("matricule");
    }

    private Site creerSite(String code) {
        Site site = new Site();
        site.setCode(code);
        site.setNom("Site " + code);
        site.setAdresse("Adresse " + code);
        site.setActif(true);
        return site;
    }

    private Terrain creerTerrain(Site site) {
        Terrain terrain = new Terrain();
        terrain.setSite(site);
        terrain.setNumero("T1");
        terrain.setActif(true);
        return terrain;
    }

    private Membre creerMembre(String matricule, CategorieMembre categorieMembre, Site siteRattachement) {
        Membre membre = new Membre();
        membre.setMatricule(matricule);
        membre.setNom("Nom " + matricule);
        membre.setPrenom("Prenom " + matricule);
        membre.setMotDePasseHash("$2y$10$w7Hmtss9GA8U9RAxfZeb3.JmBalmCw64iEo6pY5YEgNky9FM7OriK");
        membre.setCategorieMembre(categorieMembre);
        membre.setSiteRattachement(siteRattachement);
        membre.setActif(true);
        return membre;
    }
}
