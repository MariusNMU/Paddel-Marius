-- ============================================================
-- Utilisateurs DB et droits - exemple H2
-- Projet : Padel Marius
-- ============================================================

-- ------------------------------------------------------------
-- 1. User de migration / schéma
-- ------------------------------------------------------------
-- Utilisé uniquement pour créer ou modifier le schéma.
-- Dans H2, le statut ADMIN sert ici à représenter les droits
-- de migration. Ce user ne doit pas être utilisé par le backend
-- en fonctionnement normal.

CREATE USER IF NOT EXISTS padel_migration PASSWORD 'migration_pwd';
ALTER USER padel_migration ADMIN TRUE;

-- ------------------------------------------------------------
-- 2. User applicatif backend
-- ------------------------------------------------------------
-- Utilisé par le backend Spring Boot en fonctionnement normal.
-- Il peut lire et modifier les données métier.
-- Il ne doit pas avoir de droits DDL comme CREATE, ALTER, DROP.

CREATE USER IF NOT EXISTS padel_app PASSWORD 'app_pwd';

GRANT SELECT, INSERT, UPDATE, DELETE ON site TO padel_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON terrain TO padel_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON horaire_annuel_site TO padel_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON fermeture TO padel_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON membre TO padel_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON administrateur TO padel_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON jeton_rafraichissement TO padel_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON padel_match TO padel_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON participation TO padel_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON paiement TO padel_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON dette TO padel_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON penalite TO padel_app;

-- ------------------------------------------------------------
-- 3. User lecture seule
-- ------------------------------------------------------------
-- Utilisé pour audit, vérification ou reporting simple.
-- Il ne peut pas modifier les données.

CREATE USER IF NOT EXISTS padel_readonly PASSWORD 'readonly_pwd';

GRANT SELECT ON site TO padel_readonly;
GRANT SELECT ON terrain TO padel_readonly;
GRANT SELECT ON horaire_annuel_site TO padel_readonly;
GRANT SELECT ON fermeture TO padel_readonly;
GRANT SELECT ON membre TO padel_readonly;
GRANT SELECT ON administrateur TO padel_readonly;
GRANT SELECT ON jeton_rafraichissement TO padel_readonly;
GRANT SELECT ON padel_match TO padel_readonly;
GRANT SELECT ON participation TO padel_readonly;
GRANT SELECT ON paiement TO padel_readonly;
GRANT SELECT ON dette TO padel_readonly;
GRANT SELECT ON penalite TO padel_readonly;

-- ------------------------------------------------------------
-- Résumé
-- ------------------------------------------------------------
-- padel_migration : droits élevés, uniquement pour schéma/migration
-- padel_app       : droits CRUD, utilisé par le backend
-- padel_readonly  : droits SELECT, utilisé pour audit/reporting
