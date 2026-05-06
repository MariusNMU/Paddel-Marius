package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.ModeCreation;
import com.padelMarius.backend.entity.ModeEntreeParticipation;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.Participation;
import com.padelMarius.backend.entity.RoleParticipation;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.entity.Terrain;
import com.padelMarius.backend.entity.VisibiliteMatch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CoreRepositoryTest {

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private TerrainRepository terrainRepository;

    @Autowired
    private MembreRepository membreRepository;

    @Autowired
    private PadelMatchRepository padelMatchRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    @Test
    void shouldSaveSiteAndTerrain() {
        Site site = Site.builder()
                .code("BRU")
                .nom("Padel Bruxelles")
                .adresse("Rue du Test 1, 1000 Bruxelles")
                .actif(true)
                .build();

        Site savedSite = siteRepository.save(site);

        Terrain terrain = Terrain.builder()
                .numero("T1")
                .actif(true)
                .site(savedSite)
                .build();

        terrainRepository.save(terrain);

        assertThat(siteRepository.findByCode("BRU")).isPresent();
        assertThat(siteRepository.existsByCode("BRU")).isTrue();
        assertThat(terrainRepository.findBySiteId(savedSite.getId())).hasSize(1);
        assertThat(terrainRepository.findBySiteIdAndActifTrue(savedSite.getId())).hasSize(1);
    }

    @Test
    void shouldSaveMembreAndFindByMatricule() {
        Membre membre = Membre.builder()
                .matricule("G0001")
                .nom("Dupont")
                .prenom("Jean")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .build();

        membreRepository.save(membre);

        assertThat(membreRepository.findByMatricule("G0001")).isPresent();
        assertThat(membreRepository.existsByMatricule("G0001")).isTrue();
    }

    @Test
    void shouldSaveMatchAndParticipation() {
        Site site = siteRepository.save(Site.builder()
                .code("LIE")
                .nom("Padel Liège")
                .adresse("Rue du Test 2, 4000 Liège")
                .actif(true)
                .build());

        Terrain terrain = terrainRepository.save(Terrain.builder()
                .numero("T1")
                .actif(true)
                .site(site)
                .build());

        Membre membre = membreRepository.save(Membre.builder()
                .matricule("G0002")
                .nom("Martin")
                .prenom("Sophie")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .build());

        LocalDateTime debut = LocalDateTime.of(2026, 5, 20, 10, 0);
        LocalDateTime fin = debut.plusMinutes(90);

        PadelMatch match = padelMatchRepository.save(PadelMatch.builder()
                .terrain(terrain)
                .dateHeureDebut(debut)
                .dateHeureFin(fin)
                .modeCreation(ModeCreation.PRIVE)
                .visibiliteCourante(VisibiliteMatch.PRIVE)
                .prixTotal(new BigDecimal("60.00"))
                .dateCreation(LocalDateTime.now())
                .etatCycle(EtatCycleMatch.A_VENIR)
                .build());

        participationRepository.save(Participation.builder()
                .match(match)
                .membre(membre)
                .roleParticipation(RoleParticipation.ORGANISATEUR)
                .modeEntree(ModeEntreeParticipation.CREATION)
                .statutParticipation(StatutParticipation.EN_ATTENTE_PAIEMENT)
                .dateAffectation(LocalDateTime.now())
                .build());

        assertThat(padelMatchRepository.findByTerrainId(terrain.getId())).hasSize(1);
        assertThat(participationRepository.findByMatchId(match.getId())).hasSize(1);
        assertThat(participationRepository.findByMembreId(membre.getId())).hasSize(1);
        assertThat(participationRepository.countByMatchId(match.getId())).isEqualTo(1);
        assertThat(participationRepository.existsByMatchIdAndMembreId(match.getId(), membre.getId())).isTrue();
    }
}