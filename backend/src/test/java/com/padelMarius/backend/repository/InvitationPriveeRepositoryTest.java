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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class InvitationPriveeRepositoryTest {

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
    void findByMembreIdAndModeEntreeAndStatutParticipation_shouldReturnPendingPrivateInvitations() {
        DonneesTest donnees = creerDonneesTest();

        Participation invitationPriveeEnAttente = creerParticipation(
                donnees.match(),
                donnees.joueur1(),
                RoleParticipation.JOUEUR,
                ModeEntreeParticipation.INVITATION_PRIVEE,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );
        Participation invitationPriveeConfirmee = creerParticipation(
                donnees.match(),
                donnees.joueur2(),
                RoleParticipation.JOUEUR,
                ModeEntreeParticipation.INVITATION_PRIVEE,
                StatutParticipation.CONFIRMEE
        );
        Participation inscriptionPubliqueEnAttente = creerParticipation(
                donnees.match(),
                donnees.joueur3(),
                RoleParticipation.JOUEUR,
                ModeEntreeParticipation.INSCRIPTION_PUBLIQUE,
                StatutParticipation.EN_ATTENTE_PAIEMENT
        );

        participationRepository.saveAll(List.of(
                invitationPriveeEnAttente,
                invitationPriveeConfirmee,
                inscriptionPubliqueEnAttente
        ));
        participationRepository.flush();

        List<Participation> resultat =
                participationRepository.findByMembreIdAndModeEntreeAndStatutParticipation(
                        donnees.joueur1().getId(),
                        ModeEntreeParticipation.INVITATION_PRIVEE,
                        StatutParticipation.EN_ATTENTE_PAIEMENT
                );

        assertThat(resultat)
                .hasSize(1)
                .first()
                .extracting(Participation::getId)
                .isEqualTo(invitationPriveeEnAttente.getId());
    }

    @Test
    void countByMatchIdAndStatutParticipationNot_shouldCountActiveParticipations() {
        DonneesTest donnees = creerDonneesTest();

        Participation organisateur = creerParticipation(
                donnees.match(),
                donnees.organisateur(),
                RoleParticipation.ORGANISATEUR,
                ModeEntreeParticipation.CREATION,
                StatutParticipation.CONFIRMEE
        );
        Participation joueurConfirme = creerParticipation(
                donnees.match(),
                donnees.joueur1(),
                RoleParticipation.JOUEUR,
                ModeEntreeParticipation.INVITATION_PRIVEE,
                StatutParticipation.CONFIRMEE
        );
        Participation joueurLibere = creerParticipation(
                donnees.match(),
                donnees.joueur2(),
                RoleParticipation.JOUEUR,
                ModeEntreeParticipation.INVITATION_PRIVEE,
                StatutParticipation.LIBEREE
        );

        participationRepository.saveAll(List.of(organisateur, joueurConfirme, joueurLibere));
        participationRepository.flush();

        long nombreParticipationsActives = participationRepository.countByMatchIdAndStatutParticipationNot(
                donnees.match().getId(),
                StatutParticipation.LIBEREE
        );

        assertThat(nombreParticipationsActives).isEqualTo(2);
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

        Membre organisateur = creerMembre("G0001");
        Membre joueur1 = creerMembre("G0002");
        Membre joueur2 = creerMembre("G0003");
        Membre joueur3 = creerMembre("G0004");

        membreRepository.saveAll(List.of(organisateur, joueur1, joueur2, joueur3));
        membreRepository.flush();

        PadelMatch match = new PadelMatch();
        match.setTerrain(terrain);
        match.setDateHeureDebut(LocalDateTime.of(2026, 5, 20, 9, 0));
        match.setDateHeureFin(LocalDateTime.of(2026, 5, 20, 10, 30));
        match.setModeCreation(ModeCreation.PRIVE);
        match.setVisibiliteCourante(VisibiliteMatch.PRIVE);
        match.setPrixTotal(new BigDecimal("60.00"));
        match.setDateCreation(LocalDateTime.of(2026, 5, 1, 10, 0));
        match.setEtatCycle(EtatCycleMatch.A_VENIR);
        match = padelMatchRepository.saveAndFlush(match);

        return new DonneesTest(match, organisateur, joueur1, joueur2, joueur3);
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
        participation.setDateAffectation(LocalDateTime.of(2026, 5, 1, 10, 0));
        return participation;
    }

    private record DonneesTest(
            PadelMatch match,
            Membre organisateur,
            Membre joueur1,
            Membre joueur2,
            Membre joueur3
    ) {
    }
}
