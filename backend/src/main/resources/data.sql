-- ============================================================
-- Seed de démonstration automatique - H2 MVP
-- Projet : Padel Marius
-- Exécuté automatiquement au démarrage backend.
--
-- Les dates métier sont relatives à CURRENT_DATE pour rester
-- utilisables pendant la session d’août et après.
--
-- H2 uniquement :
-- PostgreSQL n’exécute pas ce fichier car le profil postgres
-- désactive spring.sql.init.mode.
-- ============================================================

-- Sites
INSERT INTO site (id, code, nom, adresse, actif) VALUES
                                                     (1001, 'BRU', 'Padel Bruxelles', 'Rue du Padel 1, 1000 Bruxelles', TRUE),
                                                     (1002, 'NAM', 'Padel Namur', 'Avenue des Sports 10, 5000 Namur', TRUE);

-- Terrains
INSERT INTO terrain (id, site_id, numero, actif) VALUES
                                                     (1101, 1001, 'T1', TRUE),
                                                     (1102, 1001, 'T2', TRUE),
                                                     (1103, 1001, 'T3', TRUE),
                                                     (1201, 1002, 'T1', TRUE),
                                                     (1202, 1002, 'T2', TRUE);

-- Horaires annuels de l'année courante et de l'année suivante.
-- La seconde année sécurise les réservations à J+N proches du 31 décembre.
INSERT INTO horaire_annuel_site (
    id,
    site_id,
    annee_civile,
    heure_debut_reservation,
    heure_fin_reservation
) VALUES
      (1301, 1001, EXTRACT(YEAR FROM CURRENT_DATE), TIME '08:00:00', TIME '22:00:00'),
      (1302, 1002, EXTRACT(YEAR FROM CURRENT_DATE), TIME '09:00:00', TIME '21:00:00'),
      (1303, 1001, EXTRACT(YEAR FROM CURRENT_DATE) + 1, TIME '08:00:00', TIME '22:00:00'),
      (1304, 1002, EXTRACT(YEAR FROM CURRENT_DATE) + 1, TIME '09:00:00', TIME '21:00:00');

-- Fermetures futures
INSERT INTO fermeture (
    id,
    date_fermeture,
    portee,
    motif,
    site_id
) VALUES
      (1401, DATEADD('DAY', 10, CURRENT_DATE), 'GLOBALE', 'Fermeture globale de démonstration', NULL),
      (1402, DATEADD('DAY', 15, CURRENT_DATE), 'LOCALE', 'Maintenance annuelle Bruxelles', 1001);

-- Membres
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
) VALUES
      (2001, 'G1001', 'Dupont', 'Marie', 'GLOBAL', NULL, TRUE, '$2y$10$w7Hmtss9GA8U9RAxfZeb3.JmBalmCw64iEo6pY5YEgNky9FM7OriK', 100.00),
      (2002, 'G1002', 'Lambert', 'Paul', 'GLOBAL', NULL, TRUE, '$2y$10$Rh28chj1ghKjqw6glJwO7ujROBPlWMykS5VG3iOt0mhmzh8pplP46', 100.00),
      (2003, 'S1001', 'Martin', 'Sophie', 'SITE', 1001, TRUE, '$2y$10$puiefDGYIGQ4RVkEwNV9OuKYfqs9mdn5c6pP4EiEo2CEaDLMiB7RS', 100.00),
      (2004, 'S1002', 'Bernard', 'Luc', 'SITE', 1002, TRUE, '$2y$10$g3AYSKR2m3WQuFI7sZFIh.bHfaRutGSwSbqwrLVkENOqcDyeYAjce', 100.00),
      (2005, 'L1001', 'Durand', 'Nina', 'LIBRE', NULL, TRUE, '$2y$10$kOdTqIion2NAEdmjH5na2e3GuMO9c5tVKLmUOJoJpzHQqXCDrGyfy', 100.00),
      (2006, 'L1002', 'Petit', 'Hugo', 'LIBRE', NULL, TRUE, '$2y$10$O2n1/VVVle3C4HJtzZblwOmpFx1EKRBbEZQ/qT/DBe0QQGYk1/66m', 100.00),
      (2007, 'G9999', 'Inactif', 'Test', 'GLOBAL', NULL, FALSE, '$2y$10$CCb6yUS4grhSHq5ou4tL7.BfjjnK4ahv/qEuiF9Bj.rxiG1ofstya', 100.00);

-- Administrateurs
INSERT INTO administrateur (
    id,
    nom,
    prenom,
    email_ou_login,
    mot_de_passe_hash,
    role_administrateur,
    site_id,
    actif
) VALUES
      (2101, 'Admin', 'Global', 'admin-global', '$2y$10$8LeMp7OiV51kw/ixDBrUd.cihLaw6UMWoNV1WKuXfxpI9dyZxdcUK', 'GLOBAL', NULL, TRUE),
      (2102, 'Admin', 'Bruxelles', 'admin-bruxelles', '$2y$10$NfvQuu66degMeOzxLi5q1.mpoEhsdnUpoiAWGRUE2Ma9zydeH5GEu', 'SITE', 1001, TRUE),
      (2103, 'Admin', 'Inactif', 'admin-inactif', '$2y$10$bWjqVrWLmIB9Nyx83hPWvuD7Jb0j34k25WKhM3d7Y4.ltruuVINFy', 'GLOBAL', NULL, FALSE),
      (2104, 'Admin', 'Namur', 'admin-namur', '$2y$10$Pynh4YlaL1ya8I9fq3nvJuX2v7BSjUwbROsKg3iZYn8XYc9DNeACm', 'SITE', 1002, TRUE);

-- Matches
-- 3001 : match public futur, utilisable pour la démo joueur.
-- 3002 : match privé futur avec dette de démonstration pour G1002.
-- 3003 : match terminé, utilisable pour les statistiques.
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
      (
          3001,
          1101,
          DATEADD('MINUTE', 540, CAST(DATEADD('DAY', 3, CURRENT_DATE) AS TIMESTAMP)),
          DATEADD('MINUTE', 630, CAST(DATEADD('DAY', 3, CURRENT_DATE) AS TIMESTAMP)),
          'PUBLIC',
          'PUBLIC',
          60.00,
          DATEADD('MINUTE', 600, CAST(DATEADD('DAY', -1, CURRENT_DATE) AS TIMESTAMP)),
          NULL,
          'A_VENIR'
      ),
      (
          3002,
          1102,
          DATEADD('MINUTE', 660, CAST(DATEADD('DAY', 4, CURRENT_DATE) AS TIMESTAMP)),
          DATEADD('MINUTE', 750, CAST(DATEADD('DAY', 4, CURRENT_DATE) AS TIMESTAMP)),
          'PRIVE',
          'PRIVE',
          60.00,
          DATEADD('MINUTE', 615, CAST(DATEADD('DAY', -1, CURRENT_DATE) AS TIMESTAMP)),
          NULL,
          'A_VENIR'
      ),
      (
          3003,
          1201,
          DATEADD('MINUTE', 540, CAST(DATEADD('DAY', -7, CURRENT_DATE) AS TIMESTAMP)),
          DATEADD('MINUTE', 630, CAST(DATEADD('DAY', -7, CURRENT_DATE) AS TIMESTAMP)),
          'PUBLIC',
          'PUBLIC',
          60.00,
          DATEADD('MINUTE', 540, CAST(DATEADD('DAY', -8, CURRENT_DATE) AS TIMESTAMP)),
          NULL,
          'TERMINE'
      );

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
      (
          3101,
          3001,
          2001,
          'ORGANISATEUR',
          'CREATION',
          'CONFIRMEE',
          DATEADD('MINUTE', 600, CAST(DATEADD('DAY', -1, CURRENT_DATE) AS TIMESTAMP)),
          DATEADD('MINUTE', 602, CAST(DATEADD('DAY', -1, CURRENT_DATE) AS TIMESTAMP)),
          NULL
      ),
      (
          3102,
          3001,
          2003,
          'JOUEUR',
          'INSCRIPTION_PUBLIQUE',
          'CONFIRMEE',
          DATEADD('MINUTE', 610, CAST(DATEADD('DAY', -1, CURRENT_DATE) AS TIMESTAMP)),
          DATEADD('MINUTE', 612, CAST(DATEADD('DAY', -1, CURRENT_DATE) AS TIMESTAMP)),
          NULL
      ),
      (
          3201,
          3002,
          2002,
          'ORGANISATEUR',
          'CREATION',
          'CONFIRMEE',
          DATEADD('MINUTE', 615, CAST(DATEADD('DAY', -1, CURRENT_DATE) AS TIMESTAMP)),
          DATEADD('MINUTE', 616, CAST(DATEADD('DAY', -1, CURRENT_DATE) AS TIMESTAMP)),
          NULL
      ),
      (
          3202,
          3002,
          2005,
          'JOUEUR',
          'INVITATION_PRIVEE',
          'EN_ATTENTE_PAIEMENT',
          DATEADD('MINUTE', 620, CAST(DATEADD('DAY', -1, CURRENT_DATE) AS TIMESTAMP)),
          NULL,
          NULL
      ),
      (
          3301,
          3003,
          2001,
          'ORGANISATEUR',
          'CREATION',
          'CONFIRMEE',
          DATEADD('MINUTE', 540, CAST(DATEADD('DAY', -8, CURRENT_DATE) AS TIMESTAMP)),
          DATEADD('MINUTE', 541, CAST(DATEADD('DAY', -8, CURRENT_DATE) AS TIMESTAMP)),
          NULL
      ),
      (
          3302,
          3003,
          2003,
          'JOUEUR',
          'INSCRIPTION_PUBLIQUE',
          'CONFIRMEE',
          DATEADD('MINUTE', 545, CAST(DATEADD('DAY', -8, CURRENT_DATE) AS TIMESTAMP)),
          DATEADD('MINUTE', 546, CAST(DATEADD('DAY', -8, CURRENT_DATE) AS TIMESTAMP)),
          NULL
      ),
      (
          3303,
          3003,
          2004,
          'JOUEUR',
          'INSCRIPTION_PUBLIQUE',
          'CONFIRMEE',
          DATEADD('MINUTE', 550, CAST(DATEADD('DAY', -8, CURRENT_DATE) AS TIMESTAMP)),
          DATEADD('MINUTE', 551, CAST(DATEADD('DAY', -8, CURRENT_DATE) AS TIMESTAMP)),
          NULL
      ),
      (
          3304,
          3003,
          2006,
          'JOUEUR',
          'INSCRIPTION_PUBLIQUE',
          'CONFIRMEE',
          DATEADD('MINUTE', 555, CAST(DATEADD('DAY', -8, CURRENT_DATE) AS TIMESTAMP)),
          DATEADD('MINUTE', 556, CAST(DATEADD('DAY', -8, CURRENT_DATE) AS TIMESTAMP)),
          NULL
      );

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
    (
        4001,
        3002,
        2002,
        30.00,
        30.00,
        DATEADD('MINUTE', 660, CAST(DATEADD('DAY', -1, CURRENT_DATE) AS TIMESTAMP)),
        NULL,
        'OUVERTE'
    );

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
    (
        5001,
        2006,
        3002,
        'RESERVATION_PRIVEE_INCOMPLETE',
        'Pénalité de démonstration active',
        DATEADD('MINUTE', 690, CAST(DATEADD('DAY', -1, CURRENT_DATE) AS TIMESTAMP)),
        DATEADD('MINUTE', 690, CAST(DATEADD('DAY', 6, CURRENT_DATE) AS TIMESTAMP)),
        'ACTIVE'
    );

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
      (
          6001,
          2001,
          'PARTICIPATION',
          15.00,
          DATEADD('MINUTE', 602, CAST(DATEADD('DAY', -1, CURRENT_DATE) AS TIMESTAMP)),
          'PAYE',
          3101,
          NULL
      ),
      (
          6002,
          2003,
          'PARTICIPATION',
          15.00,
          DATEADD('MINUTE', 612, CAST(DATEADD('DAY', -1, CURRENT_DATE) AS TIMESTAMP)),
          'PAYE',
          3102,
          NULL
      ),
      (
          6003,
          2002,
          'PARTICIPATION',
          15.00,
          DATEADD('MINUTE', 616, CAST(DATEADD('DAY', -1, CURRENT_DATE) AS TIMESTAMP)),
          'PAYE',
          3201,
          NULL
      ),
      (
          6004,
          2001,
          'PARTICIPATION',
          15.00,
          DATEADD('MINUTE', 541, CAST(DATEADD('DAY', -8, CURRENT_DATE) AS TIMESTAMP)),
          'PAYE',
          3301,
          NULL
      ),
      (
          6005,
          2003,
          'PARTICIPATION',
          15.00,
          DATEADD('MINUTE', 546, CAST(DATEADD('DAY', -8, CURRENT_DATE) AS TIMESTAMP)),
          'PAYE',
          3302,
          NULL
      ),
      (
          6006,
          2004,
          'PARTICIPATION',
          15.00,
          DATEADD('MINUTE', 551, CAST(DATEADD('DAY', -8, CURRENT_DATE) AS TIMESTAMP)),
          'PAYE',
          3303,
          NULL
      ),
      (
          6007,
          2006,
          'PARTICIPATION',
          15.00,
          DATEADD('MINUTE', 556, CAST(DATEADD('DAY', -8, CURRENT_DATE) AS TIMESTAMP)),
          'PAYE',
          3304,
          NULL
      );
