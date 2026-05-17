# Padel Marius

Application web de réservation de terrains de padel.

Le projet est réalisé dans le cadre des cours PDW / SGBD.  
L'objectif est de fournir un MVP fonctionnel, démontrable, testé et documenté.

---

## 1. Résumé du projet

Padel Marius permet de gérer plusieurs sites de padel.

Le MVP couvre :

- les sites ;
- les terrains par site ;
- les horaires annuels par site ;
- les jours de fermeture globaux et locaux ;
- les membres `GLOBAL`, `SITE`, `LIBRE` ;
- les matches privés et publics ;
- les réservations de terrains ;
- les participations ;
- les paiements ;
- les dettes organisateur ;
- les pénalités ;
- le solde crédit joueur ;
- les statistiques admin ;
- une interface joueur ;
- une interface administrateur.

---

## 2. Stack technique

### Backend

```txt
Java 21
Spring Boot
Maven Wrapper
Spring Web MVC
Spring Data JPA
Bean Validation
H2 Database
OpenAPI / Swagger
BCrypt pour les mots de passe
```

### Frontend

```txt
Angular
TypeScript
Angular Router
Angular HttpClient
Vitest / Angular unit tests
Cypress E2E
```

### Base de données

```txt
H2 en mémoire pour le MVP
SQL relationnel
Seed automatique au démarrage backend
Artefacts SQL fournis dans docs/db
```

---

## 3. Architecture générale

```txt
Frontend Angular
        |
        | HTTP REST / JSON
        v
Backend Spring Boot REST API
        |
        | JPA / Repositories
        v
Base de données H2 SQL
```

Règles importantes :

```txt
Le frontend ne contient aucun SQL.
Le frontend ne se connecte jamais directement à la base de données.
Le backend est le seul composant qui accède à la base de données.
Le backend expose une API REST.
```

---

## 4. Organisation du dépôt

```txt
Paddel-Marius/
  backend/
    src/main/java/com/padelMarius/backend/
      controller/
      service/
      repository/
      entity/
      dto/
      exception/
      config/

    src/test/java/com/padelMarius/backend/
      controller/
      service/
      repository/
      config/

  frontend/
    src/app/
      pages/
      services/
      models/
      guards/
      interceptors/
      shared/

  docs/
    db/
      schema.sql
      data-demo.sql
      db-users.md
      db-users-h2.sql

  README.md
  ARCHITECTURE.md
  EXPLOITATION.md
  DEMO.md
```

---

## 5. Fonctionnalités principales

### Joueur

- inscription joueur ;
- connexion joueur ;
- consultation du solde ;
- consultation des disponibilités ;
- création d'un match ;
- consultation des matches publics ;
- inscription à un match public avec paiement ;
- consultation des réservations ;
- consultation des dettes ;
- paiement d'une dette ;
- consultation de l'historique des transactions.

### Admin

- connexion admin ;
- dashboard admin ;
- consultation des statistiques ;
- consultation des membres ;
- création d'une fermeture globale ou locale ;
- traitement de veille ;
- traitement d'échéance.

---

## 6. Règles métier principales

- un match dure 1h30 ;
- il y a 15 minutes entre deux matches ;
- un match coûte 60 euros ;
- une participation coûte 15 euros ;
- un match contient maximum 4 joueurs ;
- un match privé incomplet peut devenir public à J-1 ;
- une participation non payée peut être libérée à J-1 ;
- l'organisateur peut recevoir une dette si le match n'est pas entièrement payé ;
- une dette ouverte bloque une nouvelle réservation ;
- une pénalité active bloque une nouvelle réservation ;
- un membre `GLOBAL` peut réserver sur tous les sites ;
- un membre `SITE` peut réserver uniquement sur son site ;
- un membre `LIBRE` peut réserver sur tous les sites mais avec une fenêtre plus courte ;
- un admin `GLOBAL` gère tous les sites ;
- un admin `SITE` gère uniquement son site.

---

## 7. Comptes de démonstration

### Joueurs

```txt
G1001 / password
Joueur GLOBAL actif

G1002 / password
Joueur GLOBAL actif avec dette ouverte

S1001 / password
Joueur SITE Bruxelles

S1002 / password
Joueur SITE Namur

L1001 / password
Joueur LIBRE actif

L1002 / password
Joueur LIBRE avec pénalité active

G9999 / password
Joueur inactif pour tester le refus
```

### Administrateurs

```txt
admin-global / secret
Admin GLOBAL

admin-bruxelles / secret-site
Admin SITE Bruxelles

admin-namur / secret-site
Admin SITE Namur

admin-inactif / secret
Admin inactif pour tester le refus
```

Les mots de passe de démonstration sont saisis en clair par l'utilisateur, mais ils sont stockés sous forme hashée côté backend.

---

## 8. Démarrage rapide

### Backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Backend disponible sur :

```txt
http://localhost:8080
```

Swagger :

```txt
http://localhost:8080/swagger-ui.html
```

Health check :

```txt
http://localhost:8080/api/health
```

### Frontend

Dans un deuxième terminal :

```powershell
cd frontend
npm install
npm start
```

Frontend disponible sur :

```txt
http://localhost:4200
```

---

## 9. Tests

### Backend

```powershell
cd backend
.\mvnw.cmd clean test
cd ..
```

### Frontend unit tests

```powershell
cd frontend
npm run test
cd ..
```

### Frontend build

```powershell
cd frontend
npm run build
cd ..
```

### Cypress E2E

```powershell
cd frontend
npm run cypress:run
cd ..
```

---

## 10. Documentation de remise

Les documents principaux sont à la racine :

```txt
ARCHITECTURE.md
EXPLOITATION.md
DEMO.md
```

Les documents DB sont dans :

```txt
docs/db/
```

Fichiers DB importants :

```txt
docs/db/schema.sql
docs/db/data-demo.sql
docs/db/db-users.md
docs/db/db-users-h2.sql
```

---

## 11. GitHub

Le projet est suivi avec :

- issues GitHub ;
- branche par issue ;
- commits courts et cohérents ;
- pull requests ;
- validation avant merge.

