# Utilisateurs DB et droits associés — Padel Marius

## 1. Objectif

Ce document explique les utilisateurs de base de données prévus pour le projet Padel Marius.

Le professeur demande de ne pas utiliser un utilisateur DB ayant l'entièreté des droits pour l'application.

L'objectif est donc de distinguer clairement :

- l'utilisateur de migration / création du schéma
- l'utilisateur applicatif utilisé par le backend
- l'utilisateur lecture seule
- l'utilisateur local H2 utilisé pour le MVP de démonstration

---

## 2. Règle fondamentale du projet

Le frontend Angular ne se connecte jamais à la base de données.

Le frontend :

- ne contient aucun SQL
- n'a aucun login DB
- n'a aucun mot de passe DB
- n'accède jamais directement aux tables

Le frontend appelle uniquement l'API HTTP REST du backend.

Schéma logique :

```txt
Angular
  |
  | HTTP REST
  v
Spring Boot Backend
  |
  | JDBC / JPA
  v
Base de données SQL

# Users DB et droits associés

## Principe général

Le frontend Angular ne se connecte jamais à la base de données.

Le frontend appelle uniquement l'API REST du backend.

Le backend est le seul composant applicatif qui possède une connexion à la base de données.

## Users PostgreSQL locaux

### 1. `padel_admin`

User créé par l'image Docker PostgreSQL.

Rôle :

- initialiser la base locale ;
- exécuter les scripts Docker au premier démarrage ;
- ne doit pas être utilisé par le backend applicatif.

Droits :

- droits élevés sur la base locale Docker.

Utilisation :

```txt
Docker uniquement

2. padel_app

User utilisé par le backend Spring Boot avec le profil postgres.

Rôle :

permettre au backend d'accéder à la base ;
lire les données ;
créer des données ;
modifier des données ;
supprimer des données si nécessaire pour le MVP.

Droits :

CONNECT sur padel_db
USAGE sur le schéma public
CREATE sur le schéma public pour le MVP
SELECT / INSERT / UPDATE / DELETE sur les tables applicatives
USAGE / SELECT / UPDATE sur les séquences

Remarque :

Dans cette version MVP, padel_app possède aussi le droit CREATE sur le schéma public pour permettre à Hibernate de créer les tables avec :

spring.jpa.hibernate.ddl-auto=update

Ce choix simplifie le développement local.

En version plus stricte, le schéma serait créé par Liquibase ou Flyway avec padel_migration, puis padel_app aurait uniquement des droits applicatifs.

3. padel_migration

User prévu pour une future évolution.

Rôle prévu :

créer le schéma ;
modifier le schéma ;
exécuter les migrations de base de données.

Utilisation actuelle :

Préparé mais pas encore utilisé par Spring Boot dans cette PR.
4. padel_readonly

User lecture seule.

Rôle :

consultation éventuelle ;
démonstration du principe de droits séparés.

Droits :

CONNECT sur padel_db
USAGE sur le schéma public
SELECT sur les tables
USAGE / SELECT sur les séquences