package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ComplementaryRepositoryTest {

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

    @Autowired
    private HoraireAnnuelSiteRepository horaireAnnuelSiteRepository;

    @Autowired
    private FermetureRepository fermetureRepository;

    @Autowired
    private AdministrateurRepository administrateurRepository;

    @Autowired
    private DetteRepository detteRepository;

    @Autowired
    private PenaliteRepository penaliteRepository;

    @Autowired
    private PaiementRepository paiementRepository;

    @Test
    void shouldSaveHoraireAnnuelSiteAndFindBySiteAndYear() {
        Site site = createSite("HOR");

        HoraireAnnuelSite horaire = HoraireAnnuelSite.builder()
                .site(site)
                .anneeCivile(2026)
                .heureDebutReservation(LocalTime.of(8, 0))
                .heureFinReservation(LocalTime.of(22, 0))
                .build();

        horaireAnnuelSiteRepository.save(horaire);

        assertThat(horaireAnnuelSiteRepository.findBySiteId(site.getId())).hasSize(1);
        assertThat(horaireAnnuelSiteRepository.findBySiteIdAndAnneeCivile(site.getId(), 2026)).isPresent();
        assertThat(horaireAnnuelSiteRepository.existsBySiteIdAndAnneeCivile(site.getId(), 2026)).isTrue();
    }

    @Test
    void shouldSaveGlobalAndLocalFermeture() {
        Site site = createSite("FER");

        Fermeture fermetureGlobale = Fermeture.builder()
                .dateFermeture(LocalDate.of(2026, 12, 25))
                .portee(PorteeFermeture.GLOBALE)
                .motif("Noël")
                .site(null)
                .build();

        Fermeture fermetureLocale = Fermeture.builder()
                .dateFermeture(LocalDate.of(2026, 7, 15))
                .portee(PorteeFermeture.LOCALE)
                .motif("Travaux")
                .site(site)
                .build();

        fermetureRepository.save(fermetureGlobale);
        fermetureRepository.save(fermetureLocale);

        assertThat(fermetureRepository.findByDateFermeture(LocalDate.of(2026, 12, 25))).hasSize(1);
        assertThat(fermetureRepository.findBySiteId(site.getId())).hasSize(1);
        assertThat(fermetureRepository.existsByDateFermetureAndPorteeAndSiteIsNull(
                LocalDate.of(2026, 12, 25),
                PorteeFermeture.GLOBALE
        )).isTrue();
        assertThat(fermetureRepository.existsBySiteIdAndDateFermetureAndPortee(
                site.getId(),
                LocalDate.of(2026, 7, 15),
                PorteeFermeture.LOCALE
        )).isTrue();
        assertThat(fermetureRepository
                .findByDateFermetureBetweenOrderByDateFermetureAsc(
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 12, 31)
                ))
                .extracting(Fermeture::getDateFermeture)
                .containsExactly(
                        LocalDate.of(2026, 7, 15),
                        LocalDate.of(2026, 12, 25)
                );
    }

    @Test
    void shouldSaveAdministrateurAndFindByEmailOrLogin() {
        Site site = createSite("ADM");

        Administrateur adminGlobal = Administrateur.builder()
                .nom("Admin")
                .prenom("Global")
                .emailOuLogin("admin.global@padel.local")
                .motDePasseHash("$2y$10$8LeMp7OiV51kw/ixDBrUd.cihLaw6UMWoNV1WKuXfxpI9dyZxdcUK")
                .roleAdministrateur(RoleAdministrateur.GLOBAL)
                .site(null)
                .actif(true)
                .build();

        Administrateur adminSite = Administrateur.builder()
                .nom("Admin")
                .prenom("Site")
                .emailOuLogin("admin.site@padel.local")
                .motDePasseHash("$2y$10$8LeMp7OiV51kw/ixDBrUd.cihLaw6UMWoNV1WKuXfxpI9dyZxdcUK")
                .roleAdministrateur(RoleAdministrateur.SITE)
                .site(site)
                .actif(true)
                .build();

        administrateurRepository.save(adminGlobal);
        administrateurRepository.save(adminSite);

        assertThat(administrateurRepository.findByEmailOuLogin("admin.global@padel.local")).isPresent();
        assertThat(administrateurRepository.existsByEmailOuLogin("admin.site@padel.local")).isTrue();
        assertThat(administrateurRepository.findByRoleAdministrateur(RoleAdministrateur.GLOBAL)).hasSize(1);
        assertThat(administrateurRepository.findBySiteId(site.getId())).hasSize(1);
        assertThat(administrateurRepository.findByActifTrue()).hasSize(2);
    }

    @Test
    void shouldSaveDetteAndPenalite() {
        Site site = createSite("DET");
        Terrain terrain = createTerrain(site, "T1");
        Membre membre = createMembre("G9001");
        PadelMatch match = createMatch(terrain, LocalDateTime.of(2026, 5, 21, 10, 0));

        Dette dette = Dette.builder()
                .match(match)
                .membreResponsable(membre)
                .montantInitial(new BigDecimal("15.00"))
                .montantRestant(new BigDecimal("15.00"))
                .dateCreation(LocalDateTime.now())
                .statutDette(StatutDette.OUVERTE)
                .build();

        detteRepository.save(dette);

        Penalite penalite = Penalite.builder()
                .membre(membre)
                .matchSource(match)
                .typePenalite("MATCH_INCOMPLET")
                .motif("Match privé incomplet")
                .dateDebut(LocalDateTime.of(2026, 5, 21, 12, 0))
                .dateFin(LocalDateTime.of(2026, 5, 28, 12, 0))
                .statutPenalite(StatutPenalite.ACTIVE)
                .build();

        penaliteRepository.save(penalite);

        assertThat(detteRepository.findByMatchId(match.getId())).isPresent();
        assertThat(detteRepository.findByMembreResponsableId(membre.getId())).hasSize(1);
        assertThat(detteRepository.findByMembreResponsableIdAndStatutDette(
                membre.getId(),
                StatutDette.OUVERTE
        )).hasSize(1);
        assertThat(detteRepository.existsByMembreResponsableIdAndStatutDette(
                membre.getId(),
                StatutDette.OUVERTE
        )).isTrue();

        assertThat(penaliteRepository.findByMembreId(membre.getId())).hasSize(1);
        assertThat(penaliteRepository.findByMembreIdAndStatutPenalite(
                membre.getId(),
                StatutPenalite.ACTIVE
        )).hasSize(1);
        assertThat(penaliteRepository.findByMatchSourceId(match.getId())).hasSize(1);
        assertThat(penaliteRepository.existsByMembreIdAndStatutPenalite(
                membre.getId(),
                StatutPenalite.ACTIVE
        )).isTrue();
    }

    @Test
    void shouldSavePaiementForParticipationAndDette() {
        Site site = createSite("PAY");
        Terrain terrain = createTerrain(site, "T1");
        Membre membre = createMembre("G9002");
        PadelMatch match = createMatch(terrain, LocalDateTime.of(2026, 5, 22, 10, 0));

        Participation participation = participationRepository.save(Participation.builder()
                .match(match)
                .membre(membre)
                .roleParticipation(RoleParticipation.ORGANISATEUR)
                .modeEntree(ModeEntreeParticipation.CREATION)
                .statutParticipation(StatutParticipation.CONFIRMEE)
                .dateAffectation(LocalDateTime.now())
                .dateConfirmation(LocalDateTime.now())
                .build());

        Paiement paiementParticipation = Paiement.builder()
                .membre(membre)
                .naturePaiement(NaturePaiement.PARTICIPATION)
                .montant(new BigDecimal("15.00"))
                .dateHeurePaiement(LocalDateTime.now())
                .statutPaiement(StatutPaiement.PAYE)
                .participation(participation)
                .dette(null)
                .build();

        paiementRepository.save(paiementParticipation);

        Dette dette = detteRepository.save(Dette.builder()
                .match(match)
                .membreResponsable(membre)
                .montantInitial(new BigDecimal("45.00"))
                .montantRestant(new BigDecimal("45.00"))
                .dateCreation(LocalDateTime.now())
                .statutDette(StatutDette.OUVERTE)
                .build());

        Paiement paiementDette = Paiement.builder()
                .membre(membre)
                .naturePaiement(NaturePaiement.REGLEMENT_DETTE)
                .montant(new BigDecimal("45.00"))
                .dateHeurePaiement(LocalDateTime.now())
                .statutPaiement(StatutPaiement.PAYE)
                .participation(null)
                .dette(dette)
                .build();

        paiementRepository.save(paiementDette);

        assertThat(paiementRepository.findByMembreId(membre.getId())).hasSize(2);
        assertThat(paiementRepository.findByMembreIdAndStatutPaiement(
                membre.getId(),
                StatutPaiement.PAYE
        )).hasSize(2);
        assertThat(paiementRepository.findByNaturePaiement(NaturePaiement.PARTICIPATION)).hasSize(1);
        assertThat(paiementRepository.findByNaturePaiement(NaturePaiement.REGLEMENT_DETTE)).hasSize(1);
        assertThat(paiementRepository.findByParticipationId(participation.getId())).isPresent();
        assertThat(paiementRepository.findByDetteId(dette.getId())).isPresent();
        assertThat(paiementRepository.existsByParticipationId(participation.getId())).isTrue();
        assertThat(paiementRepository.existsByDetteId(dette.getId())).isTrue();
    }

    private Site createSite(String code) {
        return siteRepository.save(Site.builder()
                .code(code)
                .nom("Padel " + code)
                .adresse("Rue du Test 1, 1000 Bruxelles")
                .actif(true)
                .build());
    }

    private Terrain createTerrain(Site site, String numero) {
        return terrainRepository.save(Terrain.builder()
                .site(site)
                .numero(numero)
                .actif(true)
                .build());
    }

    private Membre createMembre(String matricule) {
        return membreRepository.save(Membre.builder()
                .matricule(matricule)
                .nom("Dupont")
                .prenom("Jean")
                .motDePasseHash("$2y$10$w7Hmtss9GA8U9RAxfZeb3.JmBalmCw64iEo6pY5YEgNky9FM7OriK")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .build());
    }

    private PadelMatch createMatch(Terrain terrain, LocalDateTime debut) {
        return padelMatchRepository.save(PadelMatch.builder()
                .terrain(terrain)
                .dateHeureDebut(debut)
                .dateHeureFin(debut.plusMinutes(90))
                .modeCreation(ModeCreation.PRIVE)
                .visibiliteCourante(VisibiliteMatch.PRIVE)
                .prixTotal(new BigDecimal("60.00"))
                .dateCreation(LocalDateTime.now())
                .etatCycle(EtatCycleMatch.A_VENIR)
                .build());
    }
}
