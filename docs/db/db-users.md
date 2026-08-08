# Utilisateurs DB et droits associés — Padel Marius

## 1. Objectif

Ce document explique les utilisateurs de base de données prévus pour le projet Padel Marius.

Le projet respecte une règle importante :

```txt
Le frontend Angular ne se connecte jamais directement à la base de données.
Le frontend ne contient aucun SQL.
Le frontend ne possède aucun utilisateur DB.
Le backend Spring Boot est le seul composant applicatif qui accède à la base via JPA/JDBC.
```

Cette séparation permet de garder une architecture claire :

```txt
Angular
  |
  | HTTP REST / JSON
  v
Spring Boot Backend
  |
  | JPA / JDBC
  v
Base de données SQL
```

---

## 2. Base utilisée pour le MVP

Pour la démonstration principale, le projet utilise H2 en mémoire.

Configuration locale :

```txt
URL      : jdbc:h2:mem:padeldb
User     : sa
Password : vide
```

Ce choix est utilisé uniquement pour simplifier le MVP local.

Avantages pour la démo :

- aucune installation de base externe n’est nécessaire ;
- la base démarre automatiquement avec le backend ;
- le schéma est créé automatiquement au démarrage ;
- les données de démonstration sont chargées automatiquement ;
- aucun script SQL manuel ne doit être exécuté pour lancer la démo.

Point important :

```txt
Le user sa est acceptable uniquement pour H2 local de démonstration.
Ce n’est pas le modèle de sécurité cible pour une base réelle.
```

---

## 3. Users DB prévus en cible

Pour une base plus réaliste, par exemple PostgreSQL, le projet distingue plusieurs utilisateurs avec des responsabilités différentes.

Objectif :

```txt
Ne pas utiliser un seul utilisateur DB avec tous les droits pour toute l’application.
Séparer les droits selon le rôle technique de chaque utilisateur.
```

Users prévus :

```txt
padel_admin      : initialisation Docker locale
padel_migration  : création / évolution du schéma
padel_app        : accès applicatif backend
padel_readonly   : lecture seule
```

---

## 4. User `padel_admin`

### Rôle

`padel_admin` est le user technique créé par Docker PostgreSQL au démarrage du container.

Il sert à :

- initialiser la base locale ;
- exécuter les scripts présents dans `docker/postgres/init/` ;
- créer les autres utilisateurs DB si nécessaire.

### Droits

```txt
Droits élevés sur la base locale Docker.
```

### Utilisation

```txt
Utilisé uniquement par Docker pour l’initialisation locale.
Le backend applicatif ne doit pas utiliser ce user.
Le frontend ne connaît jamais ce user.
```

---

## 5. User `padel_migration`

### Rôle

`padel_migration` est prévu pour les opérations de création et d’évolution du schéma.

Il sert à :

- créer les tables ;
- créer les contraintes ;
- créer les index ;
- modifier le schéma lors d’évolutions futures ;
- exécuter des scripts de migration.

### Droits prévus

```txt
CONNECT sur la base
USAGE sur le schéma public
CREATE sur le schéma public
Droits de création / modification du schéma
```

### Utilisation dans le MVP

`padel_migration` est utilisé par Liquibase pour les opérations de création et
d’évolution du schéma PostgreSQL.

Le profil PostgreSQL fournit à Liquibase les identifiants de
`padel_migration`. Hibernate utilise ensuite `ddl-auto=validate` avec
`padel_app` et ne modifie pas le schéma.

Sur H2, Liquibase utilise la connexion locale `sa`.

Point à expliquer à l’oral :

```txt
Le user de migration sert à créer ou modifier la structure de la base.
Il est séparé du user applicatif utilisé par le backend.
```

---

## 6. User `padel_app`

### Rôle

`padel_app` est le user applicatif utilisé par le backend Spring Boot.

Il sert à :

- lire les données métier ;
- créer des données métier ;
- modifier des données métier ;
- supprimer des données si nécessaire dans le cadre du MVP.

Exemples d’actions réalisées par le backend avec ce user :

- consulter les disponibilités ;
- créer un match ;
- inscrire un joueur ;
- enregistrer un paiement ;
- créer ou régler une dette ;
- consulter les statistiques ;
- gérer les fermetures.

### Droits prévus

```txt
CONNECT sur padel_db
USAGE sur le schéma public
SELECT sur les tables applicatives
INSERT sur les tables applicatives
UPDATE sur les tables applicatives
DELETE sur les tables applicatives si nécessaire pour le MVP
USAGE / SELECT / UPDATE sur les séquences
```

### Tables concernées

```txt
site
terrain
horaire_annuel_site
fermeture
membre
administrateur
padel_match
participation
paiement
dette
penalite
jeton_rafraichissement
```

### Point de sécurité

```txt
padel_app ne doit pas être un superuser.
padel_app ne doit pas être utilisé pour administrer toute la base.
padel_app est uniquement le user applicatif du backend.
```

Point à expliquer à l’oral :

```txt
Le backend utilise un user applicatif avec des droits CRUD sur les tables métier.
Il n’utilise pas un user administrateur avec tous les droits.
```

---

## 7. User `padel_readonly`

### Rôle

`padel_readonly` est un user lecture seule.

Il peut servir à :

- consulter les données ;
- faire un audit simple ;
- vérifier les données ;
- produire du reporting sans modifier la base.

### Droits prévus

```txt
CONNECT sur padel_db
USAGE sur le schéma public
SELECT sur les tables applicatives
USAGE / SELECT sur les séquences
```

### Limitations

`padel_readonly` ne peut pas :

- créer de données ;
- modifier de données ;
- supprimer de données ;
- modifier le schéma ;
- créer des tables.

Point à expliquer à l’oral :

```txt
Ce user permet de consulter la base sans risque de modification.
Il est utile pour montrer le principe des droits séparés.
```

---

## 8. Aucun user DB pour le frontend

Le frontend Angular n’a aucun accès direct à la base.

Le frontend :

- ne contient pas de SQL ;
- ne possède pas de login DB ;
- ne possède pas de mot de passe DB ;
- ne se connecte jamais directement aux tables ;
- appelle uniquement l’API REST du backend.

Schéma de communication :

```txt
Frontend Angular
  |
  | Appels HTTP REST
  v
Backend Spring Boot
  |
  | JPA / Repositories
  v
Base SQL
```

Conséquence :

```txt
Même si un utilisateur ouvre le code frontend dans le navigateur,
il ne trouve aucun credential DB.
```

---

## 9. Scripts fournis dans le projet

Le projet contient plusieurs artefacts DB.

### Script PostgreSQL Docker

```txt
docker/postgres/init/01-create-users-and-rights.sql
```

Rôle :

- créer les users PostgreSQL locaux ;
- attribuer les droits principaux ;
- préparer une base plus réaliste que H2.

### Script H2 pédagogique

```txt
docs/db/db-users-h2.sql
```

Rôle :

- fournir un exemple pédagogique de users DB et de droits ;
- montrer le principe de séparation des droits ;
- servir de support d’explication à l’oral.

Ce script n’est pas exécuté automatiquement par le backend MVP.

### Schéma SQL de remise

```txt
docs/db/schema.sql
```

Rôle :

- fournir l’artefact de schéma attendu pour la remise ;
- montrer les tables, clés primaires, clés étrangères et contraintes.

### Données de démonstration

```txt
docs/db/data-demo.sql
backend/src/main/resources/data.sql
```

Rôle :

- `backend/src/main/resources/data.sql` est utilisé automatiquement par le backend H2 ;
- `docs/db/data-demo.sql` sert d’artefact de remise lisible.

---

## 10. Résumé des droits

| User DB | Rôle | Droits principaux | Utilisé par |
|---|---|---|---|
| `sa` | User H2 local MVP | Droits H2 locaux | Backend en démo H2 |
| `padel_admin` | Initialisation Docker | Droits élevés locaux | Docker PostgreSQL |
| `padel_migration` | Création / évolution du schéma | DDL / migrations | Liquibase |
| `padel_app` | User applicatif backend | CRUD sur tables métier | Backend Spring Boot |
| `padel_readonly` | Lecture seule | SELECT | Audit / reporting |

---

## 11. Choix retenu pour le MVP

Pour le MVP remis :

```txt
H2 est utilisé par défaut.
Le backend démarre seul.
La base est créée automatiquement.
Le seed est automatique.
Les users DB cibles sont documentés.
```

```txt
PostgreSQL Docker peut être utilisé.
Les users DB sont séparés.
Le backend utilise padel_app.
Le frontend n’a aucun accès DB.
```

---


```txt
Dans le MVP, H2 utilise le user local sa pour simplifier la démonstration.
Mais le projet documente une cible plus réaliste avec plusieurs users DB :
un user de migration pour le schéma, un user applicatif pour le backend,
et un user lecture seule pour l’audit.
Le frontend Angular n’a aucun user DB et ne se connecte jamais directement à la base.
Il consomme uniquement l’API REST exposée par le backend.
```

---

#
