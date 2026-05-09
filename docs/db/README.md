# Base de données — Padel Marius

## Objectif

Ce dossier contient les artefacts DB du projet.

Le professeur attend un script DB ou un artefact de schéma. Ce dossier fournit :

- `schema.sql` : schéma relationnel du MVP
- `data-demo.sql` : données de démonstration
- `db-users.md` : explication des users DB et droits
- `db-users-h2.sql` : script H2 de démonstration des users DB
- explication du seed automatique utilisé par le backend

---

## Base utilisée pour le MVP

Le backend utilise H2 en mémoire.

Configuration principale :

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:padeldb
    username: sa
    password: