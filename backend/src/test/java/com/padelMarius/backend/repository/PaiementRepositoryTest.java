package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.CategorieMembre;
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
class PaiementRepositoryTest {

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private PadelMatchRepository padelMatchRepository;

    @Autowired
    private MembreRepository membreRepository;

    @Autowired
    private TerrainRepository terrainRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Test
    void findByParticipationMatchIdAndNaturePaiementAndStatutPaiementDevraitRetournerSeulementLesPaiementsPayesDuMatch() {
        DonneesTest donnees = creerDonneesTest();

        Participation participationPayeeMatch1 = creerParticipation(
                donnees.match1(),
                donnees.joueur1(),
                RoleParticipation.JOUEUR
        );

        Participation participationAnnuleeMatch1 = creerParticipation(
                donnees.match1(),
                donnees.joueur2(),
                RoleParticipation.JOUEUR
        );

        Participation participationPayeeMatch2 = creerParticipation(
                donnees.match2(),
                donnees.joueur3(),
                RoleParticipation.JOUEUR
        );

        participationRepository.saveAll(List.of(
                participationPayeeMatch1,
                participationAnnuleeMatch1,
                participationPayeeMatch2
        ));
        participationRepository.flush();

        Paiement paiementPayeMatch1 = creerPaiementParticipation(
                donnees.joueur1(),
                participationPayeeMatch1,
                new BigDecimal("15.00"),
                StatutPaiement.PAYE
        );

        Paiement paiementAnnuleMatch1 = creerPaiementParticipation(
                donnees.joueur2(),
                participationAnnuleeMatch1,
                new BigDecimal("15.00"),
                StatutPaiement.ANNULE
        );

        Paiement paiementPayeMatch2 = creerPaiementParticipation(
                donnees.joueur3(),
                participationPayeeMatch2,
                new BigDecimal("15.00"),
                StatutPaiement.PAYE
        );

        paiementRepository.saveAll(List.of(
                paiementPayeMatch1,
                paiementAnnuleMatch1,
                paiementPayeMatch2
        ));
        paiementRepository.flush();

        List<Paiement> resultat = paiementRepository.findByParticipation_Match_IdAndNaturePaiementAndStatutPaiement(
                donnees.match1().getId(),
                NaturePaiement.PARTICIPATION,
                StatutPaiement.PAYE
        );

        assertThat(resultat)
                .hasSize(1)
                .first()
                .extracting(Paiement::getId)
                .isEqualTo(paiementPayeMatch1.getId());
    }

    private DonneesTest creerDonneesTest() {
        Site site = new Site();
        site.setCode("SITE01");
        site.setNom("Padel Central");
        site.setAdresse("Rue du Test 1");
        site.setActif(true);
        site = siteRepository.saveAndFlush(site);

        Terrain terrain = new Terrain();
        terrain.setSite(site);
        terrain.setNumero("T1");
        terrain.setActif(true);
        terrain = terrainRepository.saveAndFlush(terrain);

        Membre joueur1 = creerMembre("G0001");
        Membre joueur2 = creerMembre("G0002");
        Membre joueur3 = creerMembre("G0003");

        membreRepository.saveAll(List.of(joueur1, joueur2, joueur3));
        membreRepository.flush();

        PadelMatch match1 = creerMatch(terrain, LocalDateTime.of(2026, 5, 20, 9, 0));
        PadelMatch match2 = creerMatch(terrain, LocalDateTime.of(2026, 5, 21, 9, 0));

        padelMatchRepository.saveAll(List.of(match1, match2));
        padelMatchRepository.flush();

        return new DonneesTest(site, terrain, match1, match2, joueur1, joueur2, joueur3);
    }

    private Membre creerMembre(String matricule) {
        Membre membre = new Membre();
        membre.setMatricule(matricule);
        membre.setNom("Nom " + matricule);
        membre.setPrenom("Prenom " + matricule);
        membre.setCategorieMembre(CategorieMembre.GLOBAL);
        membre.setActif(true);
        return membre;
    }

    private PadelMatch creerMatch(Terrain terrain, LocalDateTime dateHeureDebut) {
        PadelMatch match = new PadelMatch();
        match.setTerrain(terrain);
        match.setDateHeureDebut(dateHeureDebut);
        match.setDateHeureFin(dateHeureDebut.plusMinutes(90));
        match.setModeCreation(ModeCreation.PUBLIC);
        match.setVisibiliteCourante(VisibiliteMatch.PUBLIC);
        match.setPrixTotal(new BigDecimal("60.00"));
        match.setDateCreation(LocalDateTime.of(2026, 5, 1, 12, 0));
        match.setEtatCycle(EtatCycleMatch.A_VENIR);
        return match;
    }

    private Participation creerParticipation(
            PadelMatch match,
            Membre membre,
            RoleParticipation roleParticipation
    ) {
        Participation participation = new Participation();
        participation.setMatch(match);
        participation.setMembre(membre);
        participation.setRoleParticipation(roleParticipation);
        participation.setModeEntree(ModeEntreeParticipation.INSCRIPTION_PUBLIQUE);
        participation.setStatutParticipation(StatutParticipation.CONFIRMEE);
        participation.setDateAffectation(LocalDateTime.of(2026, 5, 1, 12, 0));
        participation.setDateConfirmation(LocalDateTime.of(2026, 5, 1, 12, 5));
        return participation;
    }

    private Paiement creerPaiementParticipation(
            Membre membre,
            Participation participation,
            BigDecimal montant,
            StatutPaiement statutPaiement
    ) {
        Paiement paiement = new Paiement();
        paiement.setMembre(membre);
        paiement.setNaturePaiement(NaturePaiement.PARTICIPATION);
        paiement.setMontant(montant);
        paiement.setDateHeurePaiement(LocalDateTime.of(2026, 5, 1, 12, 10));
        paiement.setStatutPaiement(statutPaiement);
        paiement.setParticipation(participation);
        paiement.setDette(null);
        return paiement;
    }

    private record DonneesTest(
            Site site,
            Terrain terrain,
            PadelMatch match1,
            PadelMatch match2,
            Membre joueur1,
            Membre joueur2,
            Membre joueur3
    ) {
    }
}