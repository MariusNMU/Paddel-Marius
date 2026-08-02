package com.padelMarius.backend.service;

import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.Terrain;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

import static com.padelMarius.backend.config.ReglesMetier.FENETRE_RESERVATION_GLOBAL_JOURS;
import static com.padelMarius.backend.config.ReglesMetier.FENETRE_RESERVATION_LIBRE_JOURS;
import static com.padelMarius.backend.config.ReglesMetier.FENETRE_RESERVATION_SITE_JOURS;

@Service
public class ReglesReservationMembreService {

    private final Clock clock;

    public ReglesReservationMembreService(Clock clock) {
        this.clock = clock;
    }

    public void verifierReglesCreationMatch(
            Membre membre,
            Terrain terrain,
            LocalDateTime dateHeureDebut
    ) {
        verifierReglesReservation(
                membre,
                terrain,
                dateHeureDebut
        );
    }

    public void verifierReglesReservation(
            Membre membre,
            Terrain terrain,
            LocalDateTime dateHeureDebut
    ) {
        if (membre == null) {
            throw new IllegalArgumentException(
                    "Le membre est obligatoire pour vérifier les règles de réservation."
            );
        }

        if (terrain == null) {
            throw new IllegalArgumentException(
                    "Le terrain est obligatoire pour vérifier les règles de réservation."
            );
        }

        verifierTerrainEtSiteActifs(terrain);

        if (dateHeureDebut == null) {
            throw new ConfigurationMetierException(
                    "La date de début du match est obligatoire."
            );
        }

        CategorieMembre categorie =
                membre.getCategorieMembre();

        if (categorie == null) {
            throw new ConfigurationMetierException(
                    "La catégorie du membre est obligatoire."
            );
        }

        String matricule =
                normaliserMatricule(membre.getMatricule());

        switch (categorie) {
            case GLOBAL ->
                    verifierMembreGlobal(
                            matricule,
                            dateHeureDebut
                    );

            case SITE ->
                    verifierMembreSite(
                            membre,
                            terrain,
                            matricule,
                            dateHeureDebut
                    );

            case LIBRE ->
                    verifierMembreLibre(
                            matricule,
                            dateHeureDebut
                    );

            default ->
                    throw new ConfigurationMetierException(
                            "Catégorie de membre non supportée : "
                                    + categorie
                    );
        }
    }

    private void verifierTerrainEtSiteActifs(Terrain terrain) {
        if (!terrain.isActif()) {
            throw new ConfigurationMetierException(
                    "Le terrain demandé est inactif."
            );
        }

        if (terrain.getSite() == null) {
            throw new ConfigurationMetierException(
                    "Le terrain demandé n'est rattaché à aucun site."
            );
        }

        if (!terrain.getSite().isActif()) {
            throw new ConfigurationMetierException(
                    "Le site du terrain demandé est inactif."
            );
        }
    }

    private void verifierMembreGlobal(String matricule, LocalDateTime dateHeureDebut) {
        verifierPrefixe(matricule, "G", "GLOBAL");
        verifierFenetreReservation(
                dateHeureDebut,
                FENETRE_RESERVATION_GLOBAL_JOURS,
                "GLOBAL"
        );
    }

    private void verifierMembreSite(Membre membre, Terrain terrain, String matricule, LocalDateTime dateHeureDebut) {
        verifierPrefixe(matricule, "S", "SITE");
        verifierFenetreReservation(
                dateHeureDebut,
                FENETRE_RESERVATION_SITE_JOURS,
                "SITE"
        );
        verifierReservationSurSiteDeRattachement(membre, terrain);
    }

    private void verifierMembreLibre(String matricule, LocalDateTime dateHeureDebut) {
        verifierPrefixe(matricule, "L", "LIBRE");
        verifierFenetreReservation(
                dateHeureDebut,
                FENETRE_RESERVATION_LIBRE_JOURS,
                "LIBRE"
        );
    }

    private void verifierPrefixe(String matricule, String prefixeAttendu, String categorie) {
        if (matricule.isBlank()) {
            throw new ConfigurationMetierException("Le matricule du membre est obligatoire.");
        }

        if (!matricule.startsWith(prefixeAttendu)) {
            throw new ConfigurationMetierException(
                    "Un membre " + categorie + " doit avoir un matricule qui commence par " + prefixeAttendu + "."
            );
        }
    }

    private void verifierFenetreReservation(LocalDateTime dateHeureDebut, int nombreJoursAutorises, String categorie) {
        LocalDate dateDuJour = LocalDate.now(clock);
        LocalDate dateMatch = dateHeureDebut.toLocalDate();
        LocalDate dateMaxAutorisee = dateDuJour.plusDays(nombreJoursAutorises);

        if (dateMatch.isAfter(dateMaxAutorisee)) {
            throw new ConfigurationMetierException(
                    "Un membre " + categorie + " ne peut réserver que jusqu'à "
                            + nombreJoursAutorises + " jours avant la date du match."
            );
        }
    }

    private void verifierReservationSurSiteDeRattachement(Membre membre, Terrain terrain) {
        Site siteRattachement = membre.getSiteRattachement();
        Site siteTerrain = terrain.getSite();

        if (siteRattachement == null) {
            throw new ConfigurationMetierException("Un membre SITE doit avoir un site de rattachement.");
        }

        if (siteTerrain == null) {
            throw new ConfigurationMetierException("Le terrain doit être rattaché à un site.");
        }

        if (!memeSite(siteRattachement, siteTerrain)) {
            throw new ConfigurationMetierException("Un membre SITE ne peut réserver que sur son site de rattachement.");
        }
    }

    private boolean memeSite(Site siteA, Site siteB) {
        if (siteA.getId() != null && siteB.getId() != null) {
            return Objects.equals(siteA.getId(), siteB.getId());
        }

        return siteA == siteB;
    }

    private String normaliserMatricule(String matricule) {
        if (matricule == null) {
            return "";
        }

        return matricule.trim().toUpperCase(Locale.ROOT);
    }
}
