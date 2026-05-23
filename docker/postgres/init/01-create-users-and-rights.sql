-- ==========================================================
-- Initialisation PostgreSQL locale pour le projet Padel
-- Ce script est exécuté automatiquement par Docker
-- uniquement au premier démarrage du volume PostgreSQL.
-- ==========================================================

-- On limite les droits publics sur le schéma public.
REVOKE CREATE ON SCHEMA public FROM PUBLIC;

-- User prévu pour les futures migrations ou scripts de schéma.
-- Il n'est pas encore utilisé par Spring Boot dans cette PR.
DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT FROM pg_roles WHERE rolname = 'padel_migration'
    ) THEN
CREATE ROLE padel_migration LOGIN PASSWORD 'padel_migration_password';
END IF;
END
$$;

-- User applicatif utilisé par le backend Spring Boot.
-- Ce n'est pas un superuser PostgreSQL.
DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT FROM pg_roles WHERE rolname = 'padel_app'
    ) THEN
CREATE ROLE padel_app LOGIN PASSWORD 'padel_app_password';
END IF;
END
$$;

-- User lecture seule, utile pour expliquer la sécurité DB à l'oral.
-- Il n'est pas utilisé par le frontend.
DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT FROM pg_roles WHERE rolname = 'padel_readonly'
    ) THEN
CREATE ROLE padel_readonly LOGIN PASSWORD 'padel_readonly_password';
END IF;
END
$$;

-- Droits de connexion à la base.
GRANT CONNECT ON DATABASE padel_db TO padel_migration;
GRANT CONNECT ON DATABASE padel_db TO padel_app;
GRANT CONNECT ON DATABASE padel_db TO padel_readonly;

-- Droits sur le schéma public.
GRANT USAGE, CREATE ON SCHEMA public TO padel_migration;
GRANT USAGE, CREATE ON SCHEMA public TO padel_app;
GRANT USAGE ON SCHEMA public TO padel_readonly;

-- Droits sur les tables déjà existantes.
-- Au premier démarrage, il n'y en aura probablement pas encore.
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO padel_app;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO padel_readonly;

-- Droits sur les séquences déjà existantes.
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO padel_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO padel_readonly;

-- Droits par défaut pour les futures tables créées par padel_app.
ALTER DEFAULT PRIVILEGES FOR ROLE padel_app IN SCHEMA public
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO padel_app;

ALTER DEFAULT PRIVILEGES FOR ROLE padel_app IN SCHEMA public
GRANT SELECT ON TABLES TO padel_readonly;

ALTER DEFAULT PRIVILEGES FOR ROLE padel_app IN SCHEMA public
GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO padel_app;

ALTER DEFAULT PRIVILEGES FOR ROLE padel_app IN SCHEMA public
GRANT USAGE, SELECT ON SEQUENCES TO padel_readonly;