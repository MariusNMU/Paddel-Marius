-- ============================================================
-- Seed de démonstration automatique - H2 MVP
-- Projet : Padel Marius
-- Exécuté automatiquement au démarrage backend.
-- ============================================================

-- Sites
INSERT INTO site (id, code, nom, adresse, actif) VALUES
                                                     (1001, 'BRU', 'Padel Bruxelles', 'Rue du Padel 1, 1000 Bruxelles', TRUE),
                                                     (1002, 'NAM', 'Padel Namur', 'Avenue des Sports 10, 5000 Namur', TRUE);

-- Terrains
INSERT INTO terrain (id, site_id, numero, actif) VALUES
                                                     (1101, 1001, 'T1', TRUE),
                                                     (1102, 1001, 'T2', TRUE),
                                                     (1201, 1002, 'T1', TRUE),
                                                     (1202, 1002, 'T2', TRUE);

-- Horaires annuels
INSERT INTO horaire_annuel_site (
    id,
    site_id,
    annee_civile,
    heure_debut_reservation,
    heure_fin_reservation
) VALUES
      (1301, 1001, 2026, TIME '08:00:00', TIME '22:00:00'),
      (1302, 1002, 2026, TIME '09:00:00', TIME '21:00:00');

-- Fermetures
INSERT INTO fermeture (
    id,
    date_fermeture,
    portee,
    motif,
    site_id
) VALUES
      (1401, DATE '2026-07-21', 'GLOBALE', 'Fête nationale', NULL),
      (1402, DATE '2026-08-15', 'LOCALE', 'Maintenance annuelle Bruxelles', 1001);

-- Membres
INSERT INTO membre (
    id,
    matricule,
    nom,
    prenom,
    categorie_membre,
    site_rattachement_id,
    actif
) VALUES
      (2001, 'G1001', 'Dupont', 'Marie', 'GLOBAL', NULL, TRUE),
      (2002, 'G1002', 'Lambert', 'Paul', 'GLOBAL', NULL, TRUE),
      (2003, 'S1001', 'Martin', 'Sophie', 'SITE', 1001, TRUE),
      (2004, 'S1002', 'Bernard', 'Luc', 'SITE', 1002, TRUE),
      (2005, 'L1001', 'Durand', 'Nina', 'LIBRE', NULL, TRUE),
      (2006, 'L1002', 'Petit', 'Hugo', 'LIBRE', NULL, TRUE),
      (2007, 'G9999', 'Inactif', 'Test', 'GLOBAL', NULL, FALSE);

-- Administrateurs
INSERT INTO administrateur (
    id,
    nom,
    prenom,
    email_ou_login,
    mot_de_passe,
    role_administrateur,
    site_id,
    actif
) VALUES
      (2101, 'Admin', 'Global', 'admin-global', 'secret', 'GLOBAL', NULL, TRUE),
      (2102, 'Admin', 'Bruxelles', 'admin-bruxelles', 'secret-site', 'SITE', 1001, TRUE),
      (2103, 'Admin', 'Inactif', 'admin-inactif', 'secret', 'GLOBAL', NULL, FALSE);

-- Matches
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
) VALUES
      (3001, 1101, TIMESTAMP '2026-06-20 09:00:00', TIMESTAMP '2026-06-20 10:30:00', 'PUBLIC', 'PUBLIC', 60.00, TIMESTAMP '2026-05-08 10:00:00', NULL, 'A_VENIR'),
      (3002, 1102, TIMESTAMP '2026-06-20 11:00:00', TIMESTAMP '2026-06-20 12:30:00', 'PRIVE', 'PRIVE', 60.00, TIMESTAMP '2026-05-08 10:15:00', NULL, 'A_VENIR'),
      (3003, 1201, TIMESTAMP '2026-05-10 09:00:00', TIMESTAMP '2026-05-10 10:30:00', 'PUBLIC', 'PUBLIC', 60.00, TIMESTAMP '2026-05-01 09:00:00', NULL, 'TERMINE');

-- Participations
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
) VALUES
      (3101, 3001, 2001, 'ORGANISATEUR', 'CREATION', 'CONFIRMEE', TIMESTAMP '2026-05-08 10:00:00', TIMESTAMP '2026-05-08 10:02:00', NULL),
      (3102, 3001, 2003, 'JOUEUR', 'INSCRIPTION_PUBLIQUE', 'CONFIRMEE', TIMESTAMP '2026-05-08 10:10:00', TIMESTAMP '2026-05-08 10:12:00', NULL),

      (3201, 3002, 2002, 'ORGANISATEUR', 'CREATION', 'CONFIRMEE', TIMESTAMP '2026-05-08 10:15:00', TIMESTAMP '2026-05-08 10:16:00', NULL),
      (3202, 3002, 2005, 'JOUEUR', 'INVITATION_PRIVEE', 'EN_ATTENTE_PAIEMENT', TIMESTAMP '2026-05-08 10:20:00', NULL, NULL),

      (3301, 3003, 2001, 'ORGANISATEUR', 'CREATION', 'CONFIRMEE', TIMESTAMP '2026-05-01 09:00:00', TIMESTAMP '2026-05-01 09:01:00', NULL),
      (3302, 3003, 2003, 'JOUEUR', 'INSCRIPTION_PUBLIQUE', 'CONFIRMEE', TIMESTAMP '2026-05-01 09:05:00', TIMESTAMP '2026-05-01 09:06:00', NULL),
      (3303, 3003, 2004, 'JOUEUR', 'INSCRIPTION_PUBLIQUE', 'CONFIRMEE', TIMESTAMP '2026-05-01 09:10:00', TIMESTAMP '2026-05-01 09:11:00', NULL),
      (3304, 3003, 2006, 'JOUEUR', 'INSCRIPTION_PUBLIQUE', 'CONFIRMEE', TIMESTAMP '2026-05-01 09:15:00', TIMESTAMP '2026-05-01 09:16:00', NULL);

-- Dette ouverte de démonstration
INSERT INTO dette (
    id,
    match_id,
    membre_responsable_id,
    montant_initial,
    montant_restant,
    date_creation,
    date_reglement,
    statut_dette
) VALUES
    (4001, 3002, 2002, 30.00, 30.00, TIMESTAMP '2026-05-08 11:00:00', NULL, 'OUVERTE');

-- Pénalité active de démonstration
INSERT INTO penalite (
    id,
    membre_id,
    match_source_id,
    type_penalite,
    motif,
    date_debut,
    date_fin,
    statut_penalite
) VALUES
    (5001, 2006, 3002, 'RESERVATION_PRIVEE_INCOMPLETE', 'Pénalité de démonstration', TIMESTAMP '2026-05-08 11:30:00', TIMESTAMP '2026-05-15 11:30:00', 'ACTIVE');

-- Paiements
INSERT INTO paiement (
    id,
    membre_id,
    nature_paiement,
    montant,
    date_heure_paiement,
    statut_paiement,
    participation_id,
    dette_id
) VALUES
      (6001, 2001, 'PARTICIPATION', 15.00, TIMESTAMP '2026-05-08 10:02:00', 'PAYE', 3101, NULL),
      (6002, 2003, 'PARTICIPATION', 15.00, TIMESTAMP '2026-05-08 10:12:00', 'PAYE', 3102, NULL),
      (6003, 2002, 'PARTICIPATION', 15.00, TIMESTAMP '2026-05-08 10:16:00', 'PAYE', 3201, NULL),
      (6004, 2001, 'PARTICIPATION', 15.00, TIMESTAMP '2026-05-01 09:01:00', 'PAYE', 3301, NULL),
      (6005, 2003, 'PARTICIPATION', 15.00, TIMESTAMP '2026-05-01 09:06:00', 'PAYE', 3302, NULL),
      (6006, 2004, 'PARTICIPATION', 15.00, TIMESTAMP '2026-05-01 09:11:00', 'PAYE', 3303, NULL),
      (6007, 2006, 'PARTICIPATION', 15.00, TIMESTAMP '2026-05-01 09:16:00', 'PAYE', 3304, NULL);