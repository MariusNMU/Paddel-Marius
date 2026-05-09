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