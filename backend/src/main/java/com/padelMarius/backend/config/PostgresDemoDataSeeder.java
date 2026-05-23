package com.padelMarius.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@Profile("postgres")
@RequiredArgsConstructor
public class PostgresDemoDataSeeder implements CommandLineRunner {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        String motDePasseJoueur = passwordEncoder.encode("password");
        String motDePasseAdminGlobal = passwordEncoder.encode("secret");
        String motDePasseAdminSite = passwordEncoder.encode("secret-site");

        insererSites();
        insererTerrains();
        insererHorairesAnnuels();
        insererFermetures();
        insererMembres(motDePasseJoueur);
        insererAdministrateurs(motDePasseAdminGlobal, motDePasseAdminSite);
        insererMatches();
        insererParticipations();
        insererDettes();
        insererPenalites();
        insererPaiements();

        synchroniserSequences();
    }

    private void insererSites() {
        insererSite(
                1001L,
                "BRU",
                "Padel Bruxelles",
                "Rue du Padel 1, 1000 Bruxelles",
                true
        );

        insererSite(
                1002L,
                "NAM",
                "Padel Namur",
                "Avenue des Sports 10, 5000 Namur",
                true
        );
    }

    private void insererSite(
            Long id,
            String code,
            String nom,
            String adresse,
            boolean actif
    ) {
        jdbcTemplate.update("""
                INSERT INTO site (id, code, nom, adresse, actif)
                VALUES (:id, :code, :nom, :adresse, :actif)
                ON CONFLICT (id) DO UPDATE SET
                    code = EXCLUDED.code,
                    nom = EXCLUDED.nom,
                    adresse = EXCLUDED.adresse,
                    actif = EXCLUDED.actif
                """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("code", code)
                        .addValue("nom", nom)
                        .addValue("adresse", adresse)
                        .addValue("actif", actif)
        );
    }

    private void insererTerrains() {
        insererTerrain(1101L, 1001L, "T1", true);
        insererTerrain(1102L, 1001L, "T2", true);
        insererTerrain(1103L, 1001L, "T3", true);
        insererTerrain(1201L, 1002L, "T1", true);
        insererTerrain(1202L, 1002L, "T2", true);
    }

    private void insererTerrain(
            Long id,
            Long siteId,
            String numero,
            boolean actif
    ) {
        jdbcTemplate.update("""
                INSERT INTO terrain (id, site_id, numero, actif)
                VALUES (:id, :siteId, :numero, :actif)
                ON CONFLICT (id) DO UPDATE SET
                    site_id = EXCLUDED.site_id,
                    numero = EXCLUDED.numero,
                    actif = EXCLUDED.actif
                """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("siteId", siteId)
                        .addValue("numero", numero)
                        .addValue("actif", actif)
        );
    }

    private void insererHorairesAnnuels() {
        insererHoraireAnnuel(
                1301L,
                1001L,
                2026,
                LocalTime.of(8, 0),
                LocalTime.of(22, 0)
        );

        insererHoraireAnnuel(
                1302L,
                1002L,
                2026,
                LocalTime.of(9, 0),
                LocalTime.of(21, 0)
        );
    }

    private void insererHoraireAnnuel(
            Long id,
            Long siteId,
            Integer anneeCivile,
            LocalTime heureDebutReservation,
            LocalTime heureFinReservation
    ) {
        jdbcTemplate.update("""
                INSERT INTO horaire_annuel_site (
                    id,
                    site_id,
                    annee_civile,
                    heure_debut_reservation,
                    heure_fin_reservation
                )
                VALUES (
                    :id,
                    :siteId,
                    :anneeCivile,
                    :heureDebutReservation,
                    :heureFinReservation
                )
                ON CONFLICT (id) DO UPDATE SET
                    site_id = EXCLUDED.site_id,
                    annee_civile = EXCLUDED.annee_civile,
                    heure_debut_reservation = EXCLUDED.heure_debut_reservation,
                    heure_fin_reservation = EXCLUDED.heure_fin_reservation
                """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("siteId", siteId)
                        .addValue("anneeCivile", anneeCivile)
                        .addValue("heureDebutReservation", heureDebutReservation)
                        .addValue("heureFinReservation", heureFinReservation)
        );
    }

    private void insererFermetures() {
        insererFermeture(
                1401L,
                LocalDate.of(2026, 7, 21),
                "GLOBALE",
                "Fête nationale",
                null
        );

        insererFermeture(
                1402L,
                LocalDate.of(2026, 8, 15),
                "LOCALE",
                "Maintenance annuelle Bruxelles",
                1001L
        );
    }

    private void insererFermeture(
            Long id,
            LocalDate dateFermeture,
            String portee,
            String motif,
            Long siteId
    ) {
        jdbcTemplate.update("""
                INSERT INTO fermeture (
                    id,
                    date_fermeture,
                    portee,
                    motif,
                    site_id
                )
                VALUES (
                    :id,
                    :dateFermeture,
                    :portee,
                    :motif,
                    :siteId
                )
                ON CONFLICT (id) DO UPDATE SET
                    date_fermeture = EXCLUDED.date_fermeture,
                    portee = EXCLUDED.portee,
                    motif = EXCLUDED.motif,
                    site_id = EXCLUDED.site_id
                """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("dateFermeture", dateFermeture)
                        .addValue("portee", portee)
                        .addValue("motif", motif)
                        .addValue("siteId", siteId)
        );
    }

    private void insererMembres(String motDePasseJoueur) {
        insererMembre(
                2001L,
                "G1001",
                "Dupont",
                "Marie",
                "GLOBAL",
                null,
                true,
                motDePasseJoueur,
                new BigDecimal("100.00")
        );

        insererMembre(
                2002L,
                "G1002",
                "Lambert",
                "Paul",
                "GLOBAL",
                null,
                true,
                motDePasseJoueur,
                new BigDecimal("100.00")
        );

        insererMembre(
                2003L,
                "S1001",
                "Martin",
                "Sophie",
                "SITE",
                1001L,
                true,
                motDePasseJoueur,
                new BigDecimal("100.00")
        );

        insererMembre(
                2004L,
                "S1002",
                "Bernard",
                "Luc",
                "SITE",
                1002L,
                true,
                motDePasseJoueur,
                new BigDecimal("100.00")
        );

        insererMembre(
                2005L,
                "L1001",
                "Durand",
                "Nina",
                "LIBRE",
                null,
                true,
                motDePasseJoueur,
                new BigDecimal("100.00")
        );

        insererMembre(
                2006L,
                "L1002",
                "Petit",
                "Hugo",
                "LIBRE",
                null,
                true,
                motDePasseJoueur,
                new BigDecimal("100.00")
        );

        insererMembre(
                2007L,
                "G9999",
                "Inactif",
                "Test",
                "GLOBAL",
                null,
                false,
                motDePasseJoueur,
                new BigDecimal("100.00")
        );
    }

    private void insererMembre(
            Long id,
            String matricule,
            String nom,
            String prenom,
            String categorieMembre,
            Long siteRattachementId,
            boolean actif,
            String motDePasseHash,
            BigDecimal soldeCredit
    ) {
        jdbcTemplate.update("""
                INSERT INTO membre (
                    id,
                    matricule,
                    nom,
                    prenom,
                    categorie_membre,
                    site_rattachement_id,
                    actif,
                    mot_de_passe_hash,
                    solde_credit
                )
                VALUES (
                    :id,
                    :matricule,
                    :nom,
                    :prenom,
                    :categorieMembre,
                    :siteRattachementId,
                    :actif,
                    :motDePasseHash,
                    :soldeCredit
                )
                ON CONFLICT (id) DO UPDATE SET
                    matricule = EXCLUDED.matricule,
                    nom = EXCLUDED.nom,
                    prenom = EXCLUDED.prenom,
                    categorie_membre = EXCLUDED.categorie_membre,
                    site_rattachement_id = EXCLUDED.site_rattachement_id,
                    actif = EXCLUDED.actif,
                    mot_de_passe_hash = EXCLUDED.mot_de_passe_hash,
                    solde_credit = EXCLUDED.solde_credit
                """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("matricule", matricule)
                        .addValue("nom", nom)
                        .addValue("prenom", prenom)
                        .addValue("categorieMembre", categorieMembre)
                        .addValue("siteRattachementId", siteRattachementId)
                        .addValue("actif", actif)
                        .addValue("motDePasseHash", motDePasseHash)
                        .addValue("soldeCredit", soldeCredit)
        );
    }

    private void insererAdministrateurs(
            String motDePasseAdminGlobal,
            String motDePasseAdminSite
    ) {
        insererAdministrateur(
                2101L,
                "Admin",
                "Global",
                "admin-global",
                motDePasseAdminGlobal,
                "GLOBAL",
                null,
                true
        );

        insererAdministrateur(
                2102L,
                "Admin",
                "Bruxelles",
                "admin-bruxelles",
                motDePasseAdminSite,
                "SITE",
                1001L,
                true
        );

        insererAdministrateur(
                2103L,
                "Admin",
                "Inactif",
                "admin-inactif",
                motDePasseAdminGlobal,
                "GLOBAL",
                null,
                false
        );

        insererAdministrateur(
                2104L,
                "Admin",
                "Namur",
                "admin-namur",
                motDePasseAdminSite,
                "SITE",
                1002L,
                true
        );
    }

    private void insererAdministrateur(
            Long id,
            String nom,
            String prenom,
            String emailOuLogin,
            String motDePasseHash,
            String roleAdministrateur,
            Long siteId,
            boolean actif
    ) {
        jdbcTemplate.update("""
                INSERT INTO administrateur (
                    id,
                    nom,
                    prenom,
                    email_ou_login,
                    mot_de_passe_hash,
                    role_administrateur,
                    site_id,
                    actif
                )
                VALUES (
                    :id,
                    :nom,
                    :prenom,
                    :emailOuLogin,
                    :motDePasseHash,
                    :roleAdministrateur,
                    :siteId,
                    :actif
                )
                ON CONFLICT (id) DO UPDATE SET
                    nom = EXCLUDED.nom,
                    prenom = EXCLUDED.prenom,
                    email_ou_login = EXCLUDED.email_ou_login,
                    mot_de_passe_hash = EXCLUDED.mot_de_passe_hash,
                    role_administrateur = EXCLUDED.role_administrateur,
                    site_id = EXCLUDED.site_id,
                    actif = EXCLUDED.actif
                """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("nom", nom)
                        .addValue("prenom", prenom)
                        .addValue("emailOuLogin", emailOuLogin)
                        .addValue("motDePasseHash", motDePasseHash)
                        .addValue("roleAdministrateur", roleAdministrateur)
                        .addValue("siteId", siteId)
                        .addValue("actif", actif)
        );
    }

    private void insererMatches() {
        insererMatch(
                3001L,
                1101L,
                LocalDateTime.of(2026, 6, 20, 9, 0),
                LocalDateTime.of(2026, 6, 20, 10, 30),
                "PUBLIC",
                "PUBLIC",
                new BigDecimal("60.00"),
                LocalDateTime.of(2026, 5, 8, 10, 0),
                null,
                "A_VENIR"
        );

        insererMatch(
                3002L,
                1102L,
                LocalDateTime.of(2026, 6, 20, 11, 0),
                LocalDateTime.of(2026, 6, 20, 12, 30),
                "PRIVE",
                "PRIVE",
                new BigDecimal("60.00"),
                LocalDateTime.of(2026, 5, 8, 10, 15),
                null,
                "A_VENIR"
        );

        insererMatch(
                3003L,
                1201L,
                LocalDateTime.of(2026, 5, 10, 9, 0),
                LocalDateTime.of(2026, 5, 10, 10, 30),
                "PUBLIC",
                "PUBLIC",
                new BigDecimal("60.00"),
                LocalDateTime.of(2026, 5, 1, 9, 0),
                null,
                "TERMINE"
        );
    }

    private void insererMatch(
            Long id,
            Long terrainId,
            LocalDateTime dateHeureDebut,
            LocalDateTime dateHeureFin,
            String modeCreation,
            String visibiliteCourante,
            BigDecimal prixTotal,
            LocalDateTime dateCreation,
            LocalDateTime datePassagePublic,
            String etatCycle
    ) {
        jdbcTemplate.update("""
                INSERT INTO padel_match (
                    id,
                    terrain_id,
                    date_heure_debut,
                    date_heure_fin,
                    mode_creation,
                    visibilite_courante,
                    prix_total,
                    date_creation,
                    date_passage_public,
                    etat_cycle
                )
                VALUES (
                    :id,
                    :terrainId,
                    :dateHeureDebut,
                    :dateHeureFin,
                    :modeCreation,
                    :visibiliteCourante,
                    :prixTotal,
                    :dateCreation,
                    :datePassagePublic,
                    :etatCycle
                )
                ON CONFLICT (id) DO UPDATE SET
                    terrain_id = EXCLUDED.terrain_id,
                    date_heure_debut = EXCLUDED.date_heure_debut,
                    date_heure_fin = EXCLUDED.date_heure_fin,
                    mode_creation = EXCLUDED.mode_creation,
                    visibilite_courante = EXCLUDED.visibilite_courante,
                    prix_total = EXCLUDED.prix_total,
                    date_creation = EXCLUDED.date_creation,
                    date_passage_public = EXCLUDED.date_passage_public,
                    etat_cycle = EXCLUDED.etat_cycle
                """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("terrainId", terrainId)
                        .addValue("dateHeureDebut", dateHeureDebut)
                        .addValue("dateHeureFin", dateHeureFin)
                        .addValue("modeCreation", modeCreation)
                        .addValue("visibiliteCourante", visibiliteCourante)
                        .addValue("prixTotal", prixTotal)
                        .addValue("dateCreation", dateCreation)
                        .addValue("datePassagePublic", datePassagePublic)
                        .addValue("etatCycle", etatCycle)
        );
    }

    private void insererParticipations() {
        insererParticipation(
                3101L,
                3001L,
                2001L,
                "ORGANISATEUR",
                "CREATION",
                "CONFIRMEE",
                LocalDateTime.of(2026, 5, 8, 10, 0),
                LocalDateTime.of(2026, 5, 8, 10, 2),
                null
        );

        insererParticipation(
                3102L,
                3001L,
                2003L,
                "JOUEUR",
                "INSCRIPTION_PUBLIQUE",
                "CONFIRMEE",
                LocalDateTime.of(2026, 5, 8, 10, 10),
                LocalDateTime.of(2026, 5, 8, 10, 12),
                null
        );

        insererParticipation(
                3201L,
                3002L,
                2002L,
                "ORGANISATEUR",
                "CREATION",
                "CONFIRMEE",
                LocalDateTime.of(2026, 5, 8, 10, 15),
                LocalDateTime.of(2026, 5, 8, 10, 16),
                null
        );

        insererParticipation(
                3202L,
                3002L,
                2005L,
                "JOUEUR",
                "INVITATION_PRIVEE",
                "EN_ATTENTE_PAIEMENT",
                LocalDateTime.of(2026, 5, 8, 10, 20),
                null,
                null
        );

        insererParticipation(
                3301L,
                3003L,
                2001L,
                "ORGANISATEUR",
                "CREATION",
                "CONFIRMEE",
                LocalDateTime.of(2026, 5, 1, 9, 0),
                LocalDateTime.of(2026, 5, 1, 9, 1),
                null
        );

        insererParticipation(
                3302L,
                3003L,
                2003L,
                "JOUEUR",
                "INSCRIPTION_PUBLIQUE",
                "CONFIRMEE",
                LocalDateTime.of(2026, 5, 1, 9, 5),
                LocalDateTime.of(2026, 5, 1, 9, 6),
                null
        );

        insererParticipation(
                3303L,
                3003L,
                2004L,
                "JOUEUR",
                "INSCRIPTION_PUBLIQUE",
                "CONFIRMEE",
                LocalDateTime.of(2026, 5, 1, 9, 10),
                LocalDateTime.of(2026, 5, 1, 9, 11),
                null
        );

        insererParticipation(
                3304L,
                3003L,
                2006L,
                "JOUEUR",
                "INSCRIPTION_PUBLIQUE",
                "CONFIRMEE",
                LocalDateTime.of(2026, 5, 1, 9, 15),
                LocalDateTime.of(2026, 5, 1, 9, 16),
                null
        );
    }

    private void insererParticipation(
            Long id,
            Long matchId,
            Long membreId,
            String roleParticipation,
            String modeEntree,
            String statutParticipation,
            LocalDateTime dateAffectation,
            LocalDateTime dateConfirmation,
            LocalDateTime dateLiberation
    ) {
        jdbcTemplate.update("""
                INSERT INTO participation (
                    id,
                    match_id,
                    membre_id,
                    role_participation,
                    mode_entree,
                    statut_participation,
                    date_affectation,
                    date_confirmation,
                    date_liberation
                )
                VALUES (
                    :id,
                    :matchId,
                    :membreId,
                    :roleParticipation,
                    :modeEntree,
                    :statutParticipation,
                    :dateAffectation,
                    :dateConfirmation,
                    :dateLiberation
                )
                ON CONFLICT (id) DO UPDATE SET
                    match_id = EXCLUDED.match_id,
                    membre_id = EXCLUDED.membre_id,
                    role_participation = EXCLUDED.role_participation,
                    mode_entree = EXCLUDED.mode_entree,
                    statut_participation = EXCLUDED.statut_participation,
                    date_affectation = EXCLUDED.date_affectation,
                    date_confirmation = EXCLUDED.date_confirmation,
                    date_liberation = EXCLUDED.date_liberation
                """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("matchId", matchId)
                        .addValue("membreId", membreId)
                        .addValue("roleParticipation", roleParticipation)
                        .addValue("modeEntree", modeEntree)
                        .addValue("statutParticipation", statutParticipation)
                        .addValue("dateAffectation", dateAffectation)
                        .addValue("dateConfirmation", dateConfirmation)
                        .addValue("dateLiberation", dateLiberation)
        );
    }

    private void insererDettes() {
        insererDette(
                4001L,
                3002L,
                2002L,
                new BigDecimal("30.00"),
                new BigDecimal("30.00"),
                LocalDateTime.of(2026, 5, 8, 11, 0),
                null,
                "OUVERTE"
        );
    }

    private void insererDette(
            Long id,
            Long matchId,
            Long membreResponsableId,
            BigDecimal montantInitial,
            BigDecimal montantRestant,
            LocalDateTime dateCreation,
            LocalDateTime dateReglement,
            String statutDette
    ) {
        jdbcTemplate.update("""
                INSERT INTO dette (
                    id,
                    match_id,
                    membre_responsable_id,
                    montant_initial,
                    montant_restant,
                    date_creation,
                    date_reglement,
                    statut_dette
                )
                VALUES (
                    :id,
                    :matchId,
                    :membreResponsableId,
                    :montantInitial,
                    :montantRestant,
                    :dateCreation,
                    :dateReglement,
                    :statutDette
                )
                ON CONFLICT (id) DO NOTHING
                """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("matchId", matchId)
                        .addValue("membreResponsableId", membreResponsableId)
                        .addValue("montantInitial", montantInitial)
                        .addValue("montantRestant", montantRestant)
                        .addValue("dateCreation", dateCreation)
                        .addValue("dateReglement", dateReglement)
                        .addValue("statutDette", statutDette)
        );
    }

    private void insererPenalites() {
        insererPenalite(
                5001L,
                2006L,
                3002L,
                "RESERVATION_PRIVEE_INCOMPLETE",
                "Pénalité de démonstration",
                LocalDateTime.of(2026, 5, 8, 11, 30),
                LocalDateTime.of(2026, 5, 15, 11, 30),
                "ACTIVE"
        );
    }

    private void insererPenalite(
            Long id,
            Long membreId,
            Long matchSourceId,
            String typePenalite,
            String motif,
            LocalDateTime dateDebut,
            LocalDateTime dateFin,
            String statutPenalite
    ) {
        jdbcTemplate.update("""
                INSERT INTO penalite (
                    id,
                    membre_id,
                    match_source_id,
                    type_penalite,
                    motif,
                    date_debut,
                    date_fin,
                    statut_penalite
                )
                VALUES (
                    :id,
                    :membreId,
                    :matchSourceId,
                    :typePenalite,
                    :motif,
                    :dateDebut,
                    :dateFin,
                    :statutPenalite
                )
                ON CONFLICT (id) DO UPDATE SET
                    membre_id = EXCLUDED.membre_id,
                    match_source_id = EXCLUDED.match_source_id,
                    type_penalite = EXCLUDED.type_penalite,
                    motif = EXCLUDED.motif,
                    date_debut = EXCLUDED.date_debut,
                    date_fin = EXCLUDED.date_fin,
                    statut_penalite = EXCLUDED.statut_penalite
                """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("membreId", membreId)
                        .addValue("matchSourceId", matchSourceId)
                        .addValue("typePenalite", typePenalite)
                        .addValue("motif", motif)
                        .addValue("dateDebut", dateDebut)
                        .addValue("dateFin", dateFin)
                        .addValue("statutPenalite", statutPenalite)
        );
    }

    private void insererPaiements() {
        insererPaiement(
                6001L,
                2001L,
                "PARTICIPATION",
                new BigDecimal("15.00"),
                LocalDateTime.of(2026, 5, 8, 10, 2),
                "PAYE",
                3101L,
                null
        );

        insererPaiement(
                6002L,
                2003L,
                "PARTICIPATION",
                new BigDecimal("15.00"),
                LocalDateTime.of(2026, 5, 8, 10, 12),
                "PAYE",
                3102L,
                null
        );

        insererPaiement(
                6003L,
                2002L,
                "PARTICIPATION",
                new BigDecimal("15.00"),
                LocalDateTime.of(2026, 5, 8, 10, 16),
                "PAYE",
                3201L,
                null
        );

        insererPaiement(
                6004L,
                2001L,
                "PARTICIPATION",
                new BigDecimal("15.00"),
                LocalDateTime.of(2026, 5, 1, 9, 1),
                "PAYE",
                3301L,
                null
        );

        insererPaiement(
                6005L,
                2003L,
                "PARTICIPATION",
                new BigDecimal("15.00"),
                LocalDateTime.of(2026, 5, 1, 9, 6),
                "PAYE",
                3302L,
                null
        );

        insererPaiement(
                6006L,
                2004L,
                "PARTICIPATION",
                new BigDecimal("15.00"),
                LocalDateTime.of(2026, 5, 1, 9, 11),
                "PAYE",
                3303L,
                null
        );

        insererPaiement(
                6007L,
                2006L,
                "PARTICIPATION",
                new BigDecimal("15.00"),
                LocalDateTime.of(2026, 5, 1, 9, 16),
                "PAYE",
                3304L,
                null
        );
    }

    private void insererPaiement(
            Long id,
            Long membreId,
            String naturePaiement,
            BigDecimal montant,
            LocalDateTime dateHeurePaiement,
            String statutPaiement,
            Long participationId,
            Long detteId
    ) {
        jdbcTemplate.update("""
                INSERT INTO paiement (
                    id,
                    membre_id,
                    nature_paiement,
                    montant,
                    date_heure_paiement,
                    statut_paiement,
                    participation_id,
                    dette_id
                )
                VALUES (
                    :id,
                    :membreId,
                    :naturePaiement,
                    :montant,
                    :dateHeurePaiement,
                    :statutPaiement,
                    :participationId,
                    :detteId
                )
                ON CONFLICT (id) DO UPDATE SET
                    membre_id = EXCLUDED.membre_id,
                    nature_paiement = EXCLUDED.nature_paiement,
                    montant = EXCLUDED.montant,
                    date_heure_paiement = EXCLUDED.date_heure_paiement,
                    statut_paiement = EXCLUDED.statut_paiement,
                    participation_id = EXCLUDED.participation_id,
                    dette_id = EXCLUDED.dette_id
                """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("membreId", membreId)
                        .addValue("naturePaiement", naturePaiement)
                        .addValue("montant", montant)
                        .addValue("dateHeurePaiement", dateHeurePaiement)
                        .addValue("statutPaiement", statutPaiement)
                        .addValue("participationId", participationId)
                        .addValue("detteId", detteId)
        );
    }

    private void synchroniserSequences() {
        synchroniserSequence("site");
        synchroniserSequence("terrain");
        synchroniserSequence("horaire_annuel_site");
        synchroniserSequence("fermeture");
        synchroniserSequence("membre");
        synchroniserSequence("administrateur");
        synchroniserSequence("padel_match");
        synchroniserSequence("participation");
        synchroniserSequence("dette");
        synchroniserSequence("penalite");
        synchroniserSequence("paiement");
    }

    private void synchroniserSequence(String table) {
        jdbcTemplate.getJdbcOperations().execute("""
                SELECT setval(
                    pg_get_serial_sequence('%s', 'id'),
                    COALESCE((SELECT MAX(id) FROM %s), 1),
                    true
                )
                """.formatted(table, table)
        );
    }
}