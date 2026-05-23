# PostgreSQL Docker — Projet Padel

## Objectif

Cette configuration permet de lancer une vraie base PostgreSQL locale avec Docker.

Important :

- H2 reste la base par défaut du projet.
- PostgreSQL est utilisé uniquement si le backend est lancé avec le profil Spring `postgres`.
- Le frontend ne se connecte jamais à PostgreSQL.
- Le backend est le seul composant qui accède à la base de données.

## Démarrer PostgreSQL

Depuis la racine du projet :

```powershell
docker compose up -d postgres

## Seed de démonstration

Lorsque le backend est lancé avec le profil `postgres`, un seed Java crée automatiquement les données de démonstration PostgreSQL.

Le seed reprend les mêmes IDs que le seed H2 pour que le frontend fonctionne de manière identique avec H2 et PostgreSQL.

Documentation détaillée :

```txt
docs/db/postgres-demo-seed.md

Joueur GLOBAL :
matricule    : G1001
mot de passe : password

Admin GLOBAL :
login        : admin-global
mot de passe : secret

Admin site Bruxelles :
login        : admin-bruxelles
mot de passe : secret-site

Site Bruxelles : 1001
Site Namur     : 1002

Terrain Bruxelles T1 : 1101
Terrain Bruxelles T2 : 1102
Terrain Bruxelles T3 : 1103
Terrain Namur T1     : 1201
Terrain Namur T2     : 1202