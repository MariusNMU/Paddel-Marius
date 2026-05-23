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