--liquibase formatted sql

--changeset MariusNMU:003-create-refresh-token-table splitStatements:true endDelimiter:;

CREATE TABLE jeton_rafraichissement (
    identifiant VARCHAR(36) PRIMARY KEY,
    date_expiration TIMESTAMP NOT NULL,
    sujet VARCHAR(150) NOT NULL,
    type_utilisateur VARCHAR(20) NOT NULL,
    revoque BOOLEAN NOT NULL,
    date_revocation TIMESTAMP
);

CREATE INDEX ix_jeton_rafraichissement_expiration
    ON jeton_rafraichissement (date_expiration);

--rollback DROP TABLE jeton_rafraichissement;
