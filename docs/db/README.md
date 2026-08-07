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
- l’inscription des joueurs ;
- la connexion joueur par matricule et mot de passe ;
- la connexion administrateur par login et mot de passe ;
- les matches privés et publics ;
- les réservations de terrains ;
- les participations ;
- les paiements ;
- les dettes organisateur ;
- les pénalités ;
- le solde crédit joueur ;
- les statistiques administrateur ;
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
Spring Security
SecurityFilterChain stateless
Filtre JWT OncePerRequestFilter
Sécurité par rôles et @PreAuthorize
Bean Validation
H2 Database
PostgreSQL Docker optionnel
OpenAPI / Swagger
BCrypt pour les mots de passe
JWT avec signature HMAC SHA-256
```

### Frontend

```txt
Angular
TypeScript
Angular Router
Angular HttpClient
Guards Angular
Interceptor Angular
Vitest / Angular unit tests
Cypress E2E
```

### Base de données

```txt
H2 en mémoire pour le MVP principal
PostgreSQL Docker optionnel
SQL relationnel
JPA / Hibernate
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
Base de données SQL
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
      config/
      controller/
      dto/
      entity/
      exception/
      repository/
      security/
      service/

    src/main/resources/
      application.yml
      application.properties
      application-postgres.properties
      data.sql

    src/test/java/com/padelMarius/backend/
      config/
      controller/
      repository/
      security/
      service/

  frontend/
    src/app/
      guards/
      interceptors/
      models/
      pages/
      services/
      shared/
      app.config.ts
      app.routes.ts
      app.html
      app.css

    cypress/
      e2e/

  docs/
    db/
      README.md
      schema.sql
      data-demo.sql
      db-users.md
      db-users-h2.sql
      postgres-demo-seed.md

  docker/
    postgres/
      init/
        01-create-users-and-rights.sql

  docker-compose.yml
  README.md
  ARCHITECTURE.md
  EXPLOITATION.md
  DEMO.md
```

---

## 5. Fonctionnalités principales

### Joueur

- inscription joueur ;
- connexion joueur avec matricule et mot de passe ;
- authentification avec token JWT ;
- consultation du solde crédit ;
- consultation des disponibilités ;
- création d'un match privé ou public ;
- consultation des matches publics ;
- inscription à un match public avec paiement ;
- consultation de ses réservations ;
- consultation de ses invitations privées ;
- paiement ou refus d’une invitation privée ;
- consultation de ses dettes ;
- paiement d'une dette ;
- consultation de l'historique des transactions.

### Administrateur

- connexion administrateur ;
- authentification avec token JWT ;
- dashboard administrateur ;
- consultation des statistiques ;
- consultation des membres ;
- consultation de l'état opérationnel des sites actifs et inactifs ;
- création d'une fermeture globale ou locale ;
- annulation des matches concernés par une fermeture ;
- remboursement des joueurs concernés ;
- traitement de veille automatique avec déclenchement manuel disponible ;
- traitement d'échéance automatique avec déclenchement manuel disponible.

---

## 6. Règles métier principales

- un site possède ses propres terrains ;
- un site possède ses propres horaires annuels ;
- une fermeture peut être globale ou locale ;
- un match correspond à une réservation de terrain ;
- un match dure 1h30 ;
- il y a 15 minutes entre deux matches ;
- un match coûte 60 euros ;
- une participation coûte 15 euros ;
- un match contient maximum 4 joueurs ;
- un match privé doit atteindre 4 joueurs ;
- un match privé incomplet devient public lors du traitement automatique J-1 ;
- une participation non payée peut être libérée par ce même traitement ;
- les matches publics d'un site ou terrain inactif ne peuvent pas être vus ou
  rejoints ;
- l'organisateur peut recevoir une dette si le match n'est pas entièrement payé ;
- une dette ouverte bloque une nouvelle réservation ;
- les dettes ouvertes des statistiques sont filtrées selon la période du match ;
- une pénalité active bloque une nouvelle réservation ;
- une pénalité simple dure 7 jours ;
- un membre `GLOBAL` peut réserver sur tous les sites ;
- un membre `SITE` peut réserver uniquement sur son site ;
- un membre `LIBRE` peut réserver sur tous les sites mais avec une fenêtre plus courte ;
- un administrateur `GLOBAL` gère tous les sites ;
- un administrateur `SITE` gère uniquement son site.

---

## 7. Authentification et sécurité

### Joueurs

Les joueurs se connectent avec :

```txt
matricule
mot de passe
```

Le matricule reste l'identifiant métier principal du joueur.

Le mot de passe n'est pas stocké en clair.
Le backend stocke uniquement un hash BCrypt.

### Administrateurs

Les administrateurs se connectent avec :

```txt
login
mot de passe
```

Deux rôles administrateur existent :

```txt
GLOBAL
SITE
```

### JWT et Spring Security

Après connexion, le backend génère un JWT signé et limité dans le temps.

Le frontend conserve le token dans son contexte d'authentification et
l'ajoute aux requêtes protégées avec un interceptor Angular.

Header utilisé :

```txt
Authorization: Bearer <token>
```

Le backend utilise une SecurityFilterChain stateless. Le
JwtAuthenticationFilter, basé sur OncePerRequestFilter, valide le JWT et
place l'utilisateur authentifié dans le SecurityContext.

Les routes publiques sont explicitement autorisées. Les autres routes sont
protégées par défaut.

### Autorisations et session Angular

Le backend applique les autorisations définitives :

- ROLE_JOUEUR pour les parcours joueur ;
- ROLE_ADMIN pour les endpoints administrateur ;
- ROLE_ADMIN_GLOBAL et ROLE_ADMIN_SITE pour les portées administratives ;
- @PreAuthorize pour les contrôles métier plus précis.

Les guards Angular améliorent la navigation, mais ne remplacent pas les
contrôles du backend.

AuthFacadeService orchestre les connexions et les déconnexions.
AuthContextService conserve la session et synchronise les changements de
localStorage entre les onglets.

### Limites connues

La sécurité est complète pour le périmètre du MVP, mais elle ne fournit pas :

- de refresh token ;
- de révocation serveur des JWT déjà émis ;
- de rotation automatique du secret JWT.

La valeur JWT locale est réservée à la démonstration. Un déploiement réel
doit fournir PADEL_JWT_SECRET depuis l'environnement.

---

## 8. Comptes de démonstration

### Joueurs

```txt
G1001 / password
Joueur GLOBAL actif

G1002 / password
Joueur GLOBAL avec dette ouverte et pénalité active

S1001 / password
Joueur SITE Bruxelles

S1002 / password
Joueur SITE Namur

L1001 / password
Joueur LIBRE actif

L1002 / password
Joueur LIBRE actif

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

## 9. Démarrage rapide

### 9.1. Backend

Depuis la racine du projet :

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Backend disponible sur :

```txt
http://localhost:8080
```

Health check :

```txt
http://localhost:8080/actuator/health
```

Swagger :

```txt
http://localhost:8080/swagger-ui.html
```

Spécification OpenAPI JSON :

```txt
http://localhost:8080/v3/api-docs
```

---

### 9.2. Frontend

Dans un deuxième terminal :

```powershell
cd frontend
npm.cmd ci
npm.cmd start
```

Frontend disponible sur :

```txt
http://localhost:4200
```

Le frontend utilise un proxy Angular pour rediriger les appels `/api/**` vers le backend Spring Boot.

Fichier concerné :

```txt
frontend/proxy.conf.json
```

---

## 10. Base de données

### 10.1. Base par défaut

Le MVP utilise H2 en mémoire.

```txt
URL      : jdbc:h2:mem:padeldb
Username : sa
Password : vide
```

La base démarre automatiquement avec le backend.

Le schéma est créé automatiquement par Liquibase. Hibernate utilise
`ddl-auto=validate` pour contrôler sa cohérence avec les entités JPA.

Les données de démonstration H2 sont chargées par le changeset Liquibase.

Changelog et migration initiale :

```txt
backend/src/main/resources/db/changelog/db.changelog-master.yaml
backend/src/main/resources/db/changelog/changes/001-create-initial-schema.sql
```

---

### 10.2. Artefacts DB de remise

Les artefacts DB sont disponibles dans :

```txt
docs/db/
```

Fichiers importants :

```txt
docs/db/README.md
docs/db/schema.sql
docs/db/data-demo.sql
docs/db/db-users.md
docs/db/db-users-h2.sql
docs/db/postgres-demo-seed.md
```

Rôle des fichiers :

```txt
schema.sql       : schéma relationnel du MVP
data-demo.sql    : données de démonstration lisibles pour la remise
db-users.md      : explication des users DB et droits associés
db-users-h2.sql  : exemple pédagogique de users DB H2
```

---

### 10.3. PostgreSQL Docker optionnel

H2 reste la configuration par défaut pour la démo rapide.

Une base PostgreSQL locale peut aussi être lancée avec Docker :

```powershell
docker compose up -d postgres
```

Puis le backend peut être démarré avec le profil PostgreSQL :

```powershell
cd backend
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=postgres
```

Le seed PostgreSQL de démonstration est exécuté automatiquement avec ce profil.

Documentation complémentaire :

```txt
docs/db/postgres-demo-seed.md
```

---

### 10.4. Users DB

Pour le MVP local H2 :

```txt
sa
mot de passe vide
```

Ce choix est uniquement destiné au développement local et à la démonstration rapide.

Pour une cible plus réaliste, la documentation DB prévoit :

```txt
padel_migration : création / évolution du schéma
padel_app       : user applicatif backend avec droits CRUD
padel_readonly  : user lecture seule
```

Point important :

```txt
Le frontend n'a aucun user DB.
Le frontend ne se connecte jamais à la base.
Le backend est le seul composant qui accède à la DB.
```

---

## 11. Tests et validation

### 11.1. Tests backend

Depuis la racine du projet :

```powershell
cd backend
.\mvnw.cmd clean test
cd ..
```

Les tests backend couvrent notamment :

```txt
controllers
services
repositories
config
security
```

---

### 11.2. Build frontend

Depuis la racine du projet :

```powershell
cd frontend
npm.cmd run build
cd ..
```

---

### 11.3. Tests unitaires frontend

Depuis la racine du projet :

```powershell
cd frontend
npm.cmd run test -- --watch=false
cd ..
```

---

### 11.4. Tests Cypress E2E

Depuis la racine du projet :

```powershell
cd frontend
npm.cmd run cypress:run
cd ..
```

Scénarios E2E principaux :

- connexion joueur ;
- consultation du solde ;
- consultation des disponibilités ;
- rejoindre un match public ;
- connexion administrateur ;
- consultation des statistiques administrateur.

---

## 12. API HTTP

Le backend expose une API REST.

Exemples d'endpoints :

```http
GET /actuator/health
POST /api/auth/joueur
POST /api/auth/admin
GET /api/disponibilites?siteId=1001&date=<date-demo>
POST /api/matches
GET /api/matches/publics?siteId=1001&date=<date-demo>
POST /api/matches/{matchId}/participants/public/payer
POST /api/participations/{participationId}/paiements
GET /api/membres/{matricule}/solde
GET /api/membres/{matricule}/reservations
GET /api/membres/{matricule}/dettes/ouvertes
GET /api/membres/{matricule}/paiements
POST /api/dettes/{detteId}/paiements
POST /api/admin/fermetures
GET /api/admin/sites
GET /api/admin/etat-operationnel?date=<date>&siteId=<site-id>
GET /api/admin/statistiques?dateDebut=<date-debut>&dateFin=<date-fin>
GET /api/admin/membres
POST /api/admin/matches/traitement-veille?date=<date-traitement>
POST /api/admin/matches/traitement-echeance
```

Swagger permet de visualiser l'API quand le backend est démarré :

```txt
http://localhost:8080/swagger-ui.html
```

---

## 13. Documentation de remise

Les documents principaux sont à la racine :

```txt
README.md
ARCHITECTURE.md
EXPLOITATION.md
DEMO.md
```

### `ARCHITECTURE.md`

Explique :

- l’architecture backend ;
- l’architecture frontend ;
- la séparation controller / service / repository ;
- les outils et frameworks structurants ;
- Swagger / OpenAPI ;
- CORS ;
- sécurité Spring Security et JWT ;
- autorisations par rôles et `@PreAuthorize` ;
- limites connues pour un déploiement réel ;
- tests.

### `EXPLOITATION.md`

Explique :

- comment installer ;
- comment lancer le backend ;
- comment lancer le frontend ;
- comment lancer les tests ;
- les ports utilisés ;
- la base de données utilisée ;
- les credentials de démonstration ;
- les problèmes fréquents.

### `DEMO.md`

Explique :

- le scénario de démonstration ;
- les comptes à utiliser ;
- les cas métier à montrer ;
- les cas de refus ;
- les points techniques à présenter.

### `docs/db/`

Contient :

- le schéma SQL ;
- les données de démonstration ;
- la documentation des users DB ;
- les scripts DB utiles à la remise.

---

## 14. GitHub et méthode de travail

Le projet est suivi avec GitHub.

Méthode utilisée :

```txt
issue GitHub par fonctionnalité
branche par issue
commits courts et cohérents
pull request avant merge
validation avant merge
```

Le dépôt contient le frontend et le backend dans un seul repository.

Le dépôt reste public pour la remise et l’examen.

---

## 15. Commandes utiles résumées

### Backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

### Tests backend

```powershell
cd backend
.\mvnw.cmd clean test
cd ..
```

### Frontend

```powershell
cd frontend
npm.cmd ci
npm.cmd start
```

### Build frontend

```powershell
cd frontend
npm.cmd run build
cd ..
```

### Tests frontend

```powershell
cd frontend
npm.cmd run test -- --watch=false
cd ..
```

### Cypress

```powershell
cd frontend
npm.cmd run cypress:run
cd ..
```

### PostgreSQL Docker optionnel

```powershell
docker compose up -d postgres
```

### Backend avec PostgreSQL optionnel

```powershell
cd backend
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=postgres
```
