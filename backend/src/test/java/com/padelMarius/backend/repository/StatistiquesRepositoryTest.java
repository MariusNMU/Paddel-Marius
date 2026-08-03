package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.Dette;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.ModeCreation;
import com.padelMarius.backend.entity.ModeEntreeParticipation;
import com.padelMarius.backend.entity.NaturePaiement;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.Paiement;
import com.padelMarius.backend.entity.Participation;
import com.padelMarius.backend.entity.RoleParticipation;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.StatutDette;
import com.padelMarius.backend.entity.StatutPaiement;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.entity.Terrain;
import com.padelMarius.backend.entity.VisibiliteMatch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class StatistiquesRepositoryTest {

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
    private PaiementRepository paiementRepository;

    @Autowired
    private DetteRepository detteRepository;

    @Test
    void paiementRepository_shouldFindPaidPaymentsInDateRange() {
        Site site = creerSite("BRU");
        Terrain terrain = creerTerrain(site, "T1");

        Membre membre1 = creerMembre("G0001");
        Membre membre2 = creerMembre("G0002");
        Membre membre3 = creerMembre("G0003");

        PadelMatch match1 = creerMatch(terrain, LocalDateTime.of(2026, 5, 20, 9, 0));
        PadelMatch match2 = creerMatch(terrain, LocalDateTime.of(2026, 5, 21, 9, 0));
        PadelMatch match3 = creerMatch(terrain, LocalDateTime.of(2026, 5, 22, 9, 0));

        Participation participation1 = creerParticipation(match1, membre1);
        Participation participation2 = creerParticipation(match2, membre2);
        Participation participation3 = creerParticipation(match3, membre3);

        Paiement paiementPayeDansPeriode = creerPaiementParticipation(
                membre1,
                participation1,
                new BigDecimal("15.00"),
                LocalDateTime.of(2026, 5, 10, 12, 0),
                StatutPaiement.PAYE
        );

        Paiement paiementPayeHorsPeriode = creerPaiementParticipation(
                membre2,
                participation2,
                new BigDecimal("15.00"),
                LocalDateTime.of(2026, 6, 1, 12, 0),
                StatutPaiement.PAYE
        );

        Paiement paiementNonPayeDansPeriode = creerPaiementParticipation(
                membre3,
                participation3,
                new BigDecimal("15.00"),
                LocalDateTime.of(2026, 5, 11, 12, 0),
                StatutPaiement.EN_ATTENTE
        );

        List<Paiement> paiementsTrouves =
                paiementRepository.findByDateHeurePaiementGreaterThanEqualAndDateHeurePaiementBeforeAndStatutPaiement(
                        LocalDateTime.of(2026, 5, 1, 0, 0),
                        LocalDateTime.of(2026, 6, 1, 0, 0),
                        StatutPaiement.PAYE
                );

        assertThat(paiementsTrouves)
                .extracting(Paiement::getId)
                .containsExactly(paiementPayeDansPeriode.getId());

        assertThat(paiementsTrouves)
                .extracting(Paiement::getId)
                .doesNotContain(
                        paiementPayeHorsPeriode.getId(),
                        paiementNonPayeDansPeriode.getId()
                );
    }

    @Test
    void detteRepository_shouldFindOpenDebtsForMatchPeriod() {
        Site site = creerSite("NAM");
        Terrain terrain = creerTerrain(site, "T1");

        Membre responsable = creerMembre("G0100");

        PadelMatch matchAvecDetteOuverte = creerMatch(
                terrain,
                LocalDateTime.of(2026, 5, 20, 9, 0)
        );

        PadelMatch matchAvecDetteReglee = creerMatch(
                terrain,
                LocalDateTime.of(2026, 5, 21, 9, 0)
        );

        PadelMatch matchHorsPeriode = creerMatch(
                terrain,
                LocalDateTime.of(2026, 6, 1, 0, 0)
        );

        Dette detteOuverte = detteRepository.save(Dette.builder()
                .match(matchAvecDetteOuverte)
                .membreResponsable(responsable)
                .montantInitial(new BigDecimal("30.00"))
                .montantRestant(new BigDecimal("30.00"))
                .dateCreation(LocalDateTime.of(2026, 5, 12, 12, 0))
                .statutDette(StatutDette.OUVERTE)
                .build());

        Dette detteReglee = detteRepository.save(Dette.builder()
                .match(matchAvecDetteReglee)
                .membreResponsable(responsable)
                .montantInitial(new BigDecimal("15.00"))
                .montantRestant(new BigDecimal("0.00"))
                .dateCreation(LocalDateTime.of(2026, 5, 12, 12, 0))
                .dateReglement(LocalDateTime.of(2026, 5, 13, 12, 0))
                .statutDette(StatutDette.REGLEE)
                .build());

        Dette detteOuverteHorsPeriode = detteRepository.save(Dette.builder()
                .match(matchHorsPeriode)
                .membreResponsable(responsable)
                .montantInitial(new BigDecimal("45.00"))
                .montantRestant(new BigDecimal("45.00"))
                .dateCreation(LocalDateTime.of(2026, 5, 12, 12, 0))
                .statutDette(StatutDette.OUVERTE)
                .build());

        List<Dette> dettesOuvertes = detteRepository
                .findByStatutDetteAndMatch_DateHeureDebutGreaterThanEqualAndMatch_DateHeureDebutBefore(
                        StatutDette.OUVERTE,
                        LocalDateTime.of(2026, 5, 1, 0, 0),
                        LocalDateTime.of(2026, 6, 1, 0, 0)
                );

        assertThat(dettesOuvertes)
                .extracting(Dette::getId)
                .containsExactly(detteOuverte.getId());

        assertThat(dettesOuvertes)
                .extracting(Dette::getId)
                .doesNotContain(
                        detteReglee.getId(),
                        detteOuverteHorsPeriode.getId()
                );
    }

    private Site creerSite(String code) {
        return siteRepository.save(Site.builder()
                .code(code)
                .nom("Padel " + code)
                .adresse("Rue du Test 1, 1000 Bruxelles")
                .actif(true)
                .build());
    }

    private Terrain creerTerrain(Site site, String numero) {
        return terrainRepository.save(Terrain.builder()
                .site(site)
                .numero(numero)
                .actif(true)
                .build());
    }

    private Membre creerMembre(String matricule) {
        return membreRepository.save(Membre.builder()
                .matricule(matricule)
                .nom("Nom " + matricule)
                .prenom("Prenom " + matricule)
                .motDePasseHash("$2y$10$w7Hmtss9GA8U9RAxfZeb3.JmBalmCw64iEo6pY5YEgNky9FM7OriK")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .build());
    }

    private PadelMatch creerMatch(Terrain terrain, LocalDateTime debut) {
        return padelMatchRepository.save(PadelMatch.builder()
                .terrain(terrain)
                .dateHeureDebut(debut)
                .dateHeureFin(debut.plusMinutes(90))
                .modeCreation(ModeCreation.PUBLIC)
                .visibiliteCourante(VisibiliteMatch.PUBLIC)
                .prixTotal(new BigDecimal("60.00"))
                .dateCreation(LocalDateTime.of(2026, 5, 1, 10, 0))
                .etatCycle(EtatCycleMatch.A_VENIR)
                .build());
    }

    private Participation creerParticipation(PadelMatch match, Membre membre) {
        return participationRepository.save(Participation.builder()
                .match(match)
                .membre(membre)
                .roleParticipation(RoleParticipation.JOUEUR)
                .modeEntree(ModeEntreeParticipation.INSCRIPTION_PUBLIQUE)
                .statutParticipation(StatutParticipation.CONFIRMEE)
                .dateAffectation(LocalDateTime.of(2026, 5, 1, 10, 0))
                .dateConfirmation(LocalDateTime.of(2026, 5, 1, 10, 5))
                .build());
    }

    private Paiement creerPaiementParticipation(
            Membre membre,
            Participation participation,
            BigDecimal montant,
            LocalDateTime dateHeurePaiement,
            StatutPaiement statutPaiement
    ) {
        return paiementRepository.save(Paiement.builder()
                .membre(membre)
                .naturePaiement(NaturePaiement.PARTICIPATION)
                .montant(montant)
                .dateHeurePaiement(dateHeurePaiement)
                .statutPaiement(statutPaiement)
                .participation(participation)
                .dette(null)
                .build());
    }
}
