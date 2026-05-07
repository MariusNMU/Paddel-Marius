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
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ParticipationRepositoryTest {

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
    void findByMatchIdDevraitRetournerLesParticipationsDuMatch() {
        DonneesTest donnees = creerDonneesTest();

        Participation organisateur = creerParticipation(
                donnees.match(),
                donnees.organisateur(),
                RoleParticipation.ORGANISATEUR,
                ModeEntreeParticipation.CREATION,
                StatutParticipation.CONFIRMEE
        );

        Participation joueur = creerParticipation(
                donnees.match(),
                donnees.joueur1(),
                RoleParticipation.JOUEUR,
                ModeEntreeParticipation.INVITATION_PRIVEE,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        participationRepository.saveAll(List.of(organisateur, joueur));
        participationRepository.flush();

        List<Participation> resultat = participationRepository.findByMatchId(donnees.match().getId());

        assertThat(resultat)
                .hasSize(2)
                .extracting(Participation::getId)
                .containsExactlyInAnyOrder(organisateur.getId(), joueur.getId());
    }

    @Test
    void findByMembreIdDevraitRetournerLesParticipationsDuMembre() {
        DonneesTest donnees = creerDonneesTest();

        Participation participation = creerParticipation(
                donnees.match(),
                donnees.joueur1(),
                RoleParticipation.JOUEUR,
                ModeEntreeParticipation.INSCRIPTION_PUBLIQUE,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        participationRepository.saveAndFlush(participation);

        List<Participation> resultat = participationRepository.findByMembreId(donnees.joueur1().getId());

        assertThat(resultat)
                .hasSize(1)
                .first()
                .extracting(Participation::getId)
                .isEqualTo(participation.getId());
    }

    @Test
    void existsByMatchIdAndMembreIdDevraitDetecterUnParticipantExistant() {
        DonneesTest donnees = creerDonneesTest();

        Participation participation = creerParticipation(
                donnees.match(),
                donnees.joueur1(),
                RoleParticipation.JOUEUR,
                ModeEntreeParticipation.INVITATION_PRIVEE,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        participationRepository.saveAndFlush(participation);

        boolean existe = participationRepository.existsByMatchIdAndMembreId(
                donnees.match().getId(),
                donnees.joueur1().getId()
        );

        boolean inexistant = participationRepository.existsByMatchIdAndMembreId(
                donnees.match().getId(),
                donnees.joueur2().getId()
        );

        assertThat(existe).isTrue();
        assertThat(inexistant).isFalse();
    }

    @Test
    void contrainteUniqueMatchMembreDevraitRefuserDeuxParticipationsPourLeMemeMembreDansLeMemeMatch() {
        DonneesTest donnees = creerDonneesTest();

        Participation premiereParticipation = creerParticipation(
                donnees.match(),
                donnees.joueur1(),
                RoleParticipation.JOUEUR,
                ModeEntreeParticipation.INVITATION_PRIVEE,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        participationRepository.saveAndFlush(premiereParticipation);

        Participation doublon = creerParticipation(
                donnees.match(),
                donnees.joueur1(),
                RoleParticipation.JOUEUR,
                ModeEntreeParticipation.INSCRIPTION_PUBLIQUE,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        assertThatThrownBy(() -> participationRepository.saveAndFlush(doublon))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findByMatchIdDevraitAussiRetournerUneParticipationLibereePourQueLeServicePuisseLaFiltrer() {
        DonneesTest donnees = creerDonneesTest();

        Participation participationConfirmee = creerParticipation(
                donnees.match(),
                donnees.joueur1(),
                RoleParticipation.JOUEUR,
                ModeEntreeParticipation.INVITATION_PRIVEE,
                StatutParticipation.CONFIRMEE
        );

        Participation participationLiberee = creerParticipation(
                donnees.match(),
                donnees.joueur2(),
                RoleParticipation.JOUEUR,
                ModeEntreeParticipation.INVITATION_PRIVEE,
                StatutParticipation.LIBEREE
        );
        participationLiberee.setDateLiberation(LocalDateTime.now().minusHours(1));

        participationRepository.saveAll(List.of(participationConfirmee, participationLiberee));
        participationRepository.flush();

        List<Participation> resultat = participationRepository.findByMatchId(donnees.match().getId());

        assertThat(resultat)
                .hasSize(2)
                .extracting(Participation::getStatutParticipation)
                .containsExactlyInAnyOrder(
                        StatutParticipation.CONFIRMEE,
                        StatutParticipation.LIBEREE
                );
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

        Membre organisateur = creerMembre("G0001", CategorieMembre.GLOBAL, null);
        Membre joueur1 = creerMembre("G0002", CategorieMembre.GLOBAL, null);
        Membre joueur2 = creerMembre("L0003", CategorieMembre.LIBRE, null);

        membreRepository.saveAll(List.of(organisateur, joueur1, joueur2));
        membreRepository.flush();

        PadelMatch match = new PadelMatch();
        match.setTerrain(terrain);
        match.setDateHeureDebut(LocalDateTime.of(2026, 5, 20, 9, 0));
        match.setDateHeureFin(LocalDateTime.of(2026, 5, 20, 10, 30));
        match.setModeCreation(ModeCreation.PRIVE);
        match.setVisibiliteCourante(VisibiliteMatch.PRIVE);
        match.setPrixTotal(new BigDecimal("60.00"));
        match.setDateCreation(LocalDateTime.of(2026, 5, 1, 12, 0));
        match.setEtatCycle(EtatCycleMatch.A_VENIR);
        match = padelMatchRepository.saveAndFlush(match);

        return new DonneesTest(site, terrain, match, organisateur, joueur1, joueur2);
    }

    private Membre creerMembre(String matricule, CategorieMembre categorieMembre, Site siteRattachement) {
        Membre membre = new Membre();
        membre.setMatricule(matricule);
        membre.setNom("Nom " + matricule);
        membre.setPrenom("Prenom " + matricule);
        membre.setCategorieMembre(categorieMembre);
        membre.setSiteRattachement(siteRattachement);
        membre.setActif(true);
        return membre;
    }

    private Participation creerParticipation(
            PadelMatch match,
            Membre membre,
            RoleParticipation roleParticipation,
            ModeEntreeParticipation modeEntree,
            StatutParticipation statutParticipation
    ) {
        Participation participation = new Participation();
        participation.setMatch(match);
        participation.setMembre(membre);
        participation.setRoleParticipation(roleParticipation);
        participation.setModeEntree(modeEntree);
        participation.setStatutParticipation(statutParticipation);
        participation.setDateAffectation(LocalDateTime.now());
        return participation;
    }

    private record DonneesTest(
            Site site,
            Terrain terrain,
            PadelMatch match,
            Membre organisateur,
            Membre joueur1,
            Membre joueur2
    ) {
    }
}