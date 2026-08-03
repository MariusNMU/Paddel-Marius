# Padel Marius

Application web de réservation de terrains de padel.

Le projet est réalisé dans le cadre du cours de développement web et du cahier des charges SGBD.

---

## 1. Objectif du projet

Padel Marius permet de gérer la réservation de terrains de padel sur plusieurs sites.

Le projet couvre les besoins suivants :

- gestion de plusieurs sites ;
- gestion des terrains par site ;
- gestion des horaires annuels propres à chaque site ;
- gestion des jours de fermeture globaux et locaux ;
- gestion des membres `GLOBAL`, `SITE` et `LIBRE` ;
- réservation de matches privés ou publics ;
- inscription et paiement des participants ;
- gestion des dettes de l'organisateur ;
- gestion des pénalités ;
- consultation des réservations joueur ;
- consultation du solde crédit joueur ;
- consultation de l'historique des transactions ;
- interface administrateur ;
- statistiques administrateur.

---

## 2. Stack technique

### Backend

```txt
Java 21
Spring Boot
Spring Web MVC
Spring Data JPA
Spring Security
SecurityFilterChain stateless
Filtre JWT OncePerRequestFilter
Sécurité par rôles et @PreAuthorize
Bean Validation
Maven Wrapper
H2 Database
PostgreSQL Docker optionnel
OpenAPI / Swagger
BCrypt
JWT
```

### Frontend

```txt
Angular 21
Angular Material
TypeScript
Angular Router
Angular HttpClient
Angular Guards
Angular Interceptor JWT
Façades Angular avec Signals et RxJS
Vitest / Angular unit tests
Cypress E2E mocké
Cypress full stack autonome avec H2
```

### Base de données

```txt
SQL relationnel
H2 en mémoire par défaut
Seed H2 automatique au démarrage du backend
Données de démonstration relatives à la date du jour
PostgreSQL Docker optionnel
Seed PostgreSQL Java avec le profil postgres
Artefacts SQL disponibles dans docs/db
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

Règles d'architecture :

```txt
Le frontend ne contient aucun SQL.
Le frontend ne se connecte jamais directement à la base de données.
Le frontend appelle uniquement l'API HTTP du backend.
Le backend est le seul composant applicatif qui accède à la base de données.
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
      db/changelog/
        db.changelog-master.yaml
        changes/
          001-create-initial-schema.sql
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
      full-stack/
      support/

    cypress.config.ts
    cypress.fullstack.config.ts

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

### 5.1. Espace joueur

L'espace joueur permet de :

- se connecter avec un matricule et un mot de passe ;
- consulter son solde crédit ;
- consulter les disponibilités ;
- organiser un match privé ou public ;
- consulter les matches publics ;
- rejoindre un match public avec paiement ;
- consulter ses réservations ;
- consulter ses invitations privées ;
- confirmer et payer une invitation privée ;
- refuser une invitation privée ;
- consulter ses dettes ;
- payer une dette ;
- consulter l'historique de ses transactions.

### 5.2. Espace administrateur

L'espace administrateur permet de :

- se connecter avec un login et un mot de passe ;
- consulter un tableau de bord ;
- consulter les statistiques ;
- consulter la liste des membres ;
- consulter l'état opérationnel des sites, y compris les sites inactifs ;
- créer une fermeture globale ;
- créer une fermeture locale ;
- annuler les matches concernés par une fermeture ;
- déclencher le traitement de veille ;
- déclencher le traitement d'échéance.

Les deux traitements administratifs sont accessibles dans Angular uniquement
à un administrateur `GLOBAL`. Le backend applique également cette autorisation.

Le backend exécute automatiquement les deux traitements. La veille J-1 est
lancée après 30 secondes, puis toutes les heures. Les échéances sont traitées
après 60 secondes, puis toutes les 60 secondes. Les endpoints manuels restent
disponibles comme commandes de secours et pour la démonstration.

La veille se configure avec `PADEL_VEILLE_PLANIFICATION_ACTIVE`,
`PADEL_VEILLE_DELAI_INITIAL_MS` et `PADEL_VEILLE_INTERVALLE_MS`. Les échéances
se configurent avec `PADEL_ECHEANCES_PLANIFICATION_ACTIVE`,
`PADEL_ECHEANCES_DELAI_INITIAL_MS` et `PADEL_ECHEANCES_INTERVALLE_MS`.

---

## 6. Règles métier principales

### 6.1. Sites, terrains et horaires

- Un site possède plusieurs terrains.
- Chaque terrain appartient à un seul site.
- Les horaires de réservation sont définis par site et par année civile.
- Les heures de début et de fin de réservation peuvent être différentes selon le site.
- Un membre `SITE` ne peut pas être inscrit sur un site inactif.
- Les disponibilités d'un site inactif ne peuvent pas être consultées.
- Les matches publics d'un site ou d'un terrain inactif ne sont ni proposés
  ni accessibles à la participation.
- L'état opérationnel administrateur permet néanmoins de consulter les sites
  inactifs afin d'expliquer leur indisponibilité.
- Une fermeture peut être globale ou locale.
- Une fermeture globale concerne tous les sites.
- Une fermeture locale concerne un seul site.

### 6.2. Réservations et matches

- Un match correspond à une réservation de terrain.
- Un match dure 1h30.
- Il y a 15 minutes entre deux matches sur un même terrain.
- Un match coûte 60 euros.
- Un match peut être privé ou public.
- Un match contient au maximum 4 joueurs.
- Une participation standard coûte 15 euros.
- Une inscription à un match public est confirmée après paiement.
- Une participation existante se paie par l'unique endpoint
  `POST /api/participations/{participationId}/paiements`, avec le montant
  standard de 15 euros dans le DTO de requête.

### 6.3. Matches privés

- Un match privé est organisé par un membre.
- L'organisateur invite les autres joueurs.
- Si une participation privée n'est pas payée à temps, la place peut être libérée.
- Un match privé incomplet devient public lors du traitement automatique J-1.
- Une pénalité peut être appliquée à l'organisateur selon les règles métier.

### 6.4. Matches publics

- Les places disponibles sont visibles par les joueurs.
- Un joueur rejoint lui-même un match public.
- La validation est immédiate après paiement.
- Le principe appliqué est : premier payé, premier servi.
- Si le match n'est pas entièrement payé, l'organisateur porte le solde dû.

### 6.5. Dettes et pénalités

- Une dette ouverte bloque l'organisation d'un nouveau match.
- Une dette peut être réglée par paiement.
- Les dettes ouvertes affichées dans les statistiques appartiennent à la
  période sélectionnée selon la date de début de leur match.
- Une pénalité active bloque l'organisation d'un nouveau match.
- Une pénalité simple dure 7 jours.

### 6.6. Catégories de membres

```txt
GLOBAL : matricule Gxxxx, réservation possible jusqu'à 21 jours avant le match, tous sites
SITE   : matricule Sxxxx, réservation possible jusqu'à 14 jours avant le match, site de rattachement
LIBRE  : matricule Lxxxx, réservation possible jusqu'à 5 jours avant le match, tous sites
```

---

## 7. Authentification et sécurité

### 7.1. Joueurs

Les joueurs se connectent avec :

```txt
matricule
mot de passe
```

Le matricule est l'identifiant métier du joueur.

Il n'y a pas de login séparé pour les joueurs.

Le mot de passe est utilisé pour sécuriser l'authentification de session.

Le backend ne stocke pas les mots de passe en clair.  
Il stocke uniquement des hash BCrypt.

### 7.2. Administrateurs

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

Un administrateur `GLOBAL` peut gérer tous les sites.

Un administrateur `SITE` est limité à son site de rattachement.

### 7.3. JWT et Spring Security

Après une connexion réussie, le backend génère un JWT signé et limité dans
le temps.

Le frontend conserve ce token dans son contexte d'authentification. Un
interceptor Angular l'ajoute aux requêtes protégées :

```http
Authorization: Bearer <token>
```

Le backend utilise une SecurityFilterChain stateless. Le
JwtAuthenticationFilter, basé sur OncePerRequestFilter, valide le JWT et
place l'utilisateur authentifié dans le SecurityContext Spring.

Les routes publiques sont explicitement autorisées. Les autres routes sont
protégées par défaut.

### 7.4. Autorisations frontend et backend

Le backend applique les autorisations définitives :

- ROLE_JOUEUR pour les parcours joueur ;
- ROLE_ADMIN pour les endpoints administrateur ;
- ROLE_ADMIN_GLOBAL et ROLE_ADMIN_SITE pour les portées administratives ;
- @PreAuthorize pour les règles nécessitant une vérification plus précise.

Les guards Angular améliorent la navigation, mais ne remplacent jamais les
contrôles du backend.

AuthFacadeService orchestre les connexions et les déconnexions.
AuthContextService conserve la session et synchronise les changements de
localStorage entre les onglets.

Lorsqu'un joueur est connecté, une connexion administrateur supprime la
session joueur, et inversement. Deux identités différentes ne peuvent donc
pas rester actives simultanément dans les onglets du navigateur.

Les mots de passe sont hachés avec BCrypt. Une nouvelle inscription impose
un mot de passe de 12 à 72 caractères et une confirmation identique.
Les refus de connexion utilisent un message générique.

---

## 8. API HTTP

Le backend expose une API REST.

Exemples d'endpoints principaux :

```http
GET /api/health
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

Les endpoints joueur et administrateur protégés utilisent le header :

```http
Authorization: Bearer <token>
```

Le backend renvoie les erreurs API importantes au format standard :

```json
{
  "code": "...",
  "message": "..."
}
```

Swagger est disponible quand le backend est démarré :

```txt
http://localhost:8080/swagger-ui.html
```

La spécification OpenAPI JSON est disponible ici :

```txt
http://localhost:8080/v3/api-docs
```

But : supprimer les dates obsolètes et documenter le contrat d'erreur API.

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
http://localhost:8080/api/health
```

Swagger :

```txt
http://localhost:8080/swagger-ui.html
```

### 9.2. Frontend

Dans un deuxième terminal :

Le frontend est validé avec Node.js `22.12.0` ou une version ultérieure de la
branche 22, et avec npm `11.6.0`. Le champ `packageManager`, les contraintes
`engines` et la CI utilisent cette même version npm afin de garantir un
`npm ci` reproductible.

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

### 10.1. H2 par défaut

Le MVP utilise H2 en mémoire par défaut.

```txt
URL      : jdbc:h2:mem:padeldb
Username : sa
Password : vide
```

La base démarre automatiquement avec le backend.

Le schéma est créé automatiquement par Liquibase. Hibernate utilise
`ddl-auto=validate` pour vérifier que le schéma correspond aux entités JPA,
sans modifier la structure de la base.

Les données de démonstration H2 sont chargées par le changeset Liquibase :

```txt
backend/src/main/resources/data.sql
```

Aucun script SQL manuel n'est nécessaire pour lancer la démonstration locale avec H2.

Le changelog principal et la migration initiale se trouvent dans :

```txt
backend/src/main/resources/db/changelog/db.changelog-master.yaml
backend/src/main/resources/db/changelog/changes/001-create-initial-schema.sql
```

### 10.2. Artefacts DB

Les artefacts DB sont disponibles dans :

```txt
docs/db/
```

Fichiers principaux :

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
schema.sql            : schéma relationnel du MVP
data-demo.sql         : données de démonstration lisibles
db-users.md           : explication des users DB et droits associés
db-users-h2.sql       : exemple pédagogique de users DB H2
postgres-demo-seed.md : explication du seed PostgreSQL optionnel
```

### 10.3. PostgreSQL Docker optionnel

H2 reste la configuration par défaut.

Liquibase crée le schéma avec `padel_migration`, puis le backend et le seeder
utilisent `padel_app`.

Une base PostgreSQL locale peut aussi être lancée avec Docker :

```powershell
docker compose up -d postgres
```

Puis le backend peut être démarré avec le profil PostgreSQL :

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

Les guillemets autour de `-Dspring-boot.run.profiles=postgres` sont recommandés sous PowerShell afin de transmettre correctement l'argument à Maven.

Le seed PostgreSQL de démonstration est exécuté automatiquement avec ce profil.

Documentation complémentaire :

```txt
docs/db/postgres-demo-seed.md
```

### 10.4. Users DB

Pour le MVP local H2 :

```txt
sa
mot de passe vide
```

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

#### Suite standard H2

Depuis la racine du projet :

```powershell
cd backend
.\mvnw.cmd clean test
cd ..
```

Cette commande exécute la suite backend standard avec H2. Docker Desktop
n'est pas nécessaire.

Les tests backend standard couvrent notamment :

```txt
controllers
services
repositories
config
security
intégration backend avec MockMvc + H2
```

#### Tests PostgreSQL complémentaires

Les tests de concurrence qui nécessitent le comportement réel de PostgreSQL
sont exécutés séparément avec Testcontainers.

Ils vérifient notamment le double paiement, le paiement face à une fermeture,
le paiement face au refus d'une invitation, le paiement face au traitement de
veille, le recalcul concurrent des dettes et l'idempotence du traitement
d'échéance.

Docker Desktop doit être démarré, puis :

```powershell
cd backend
docker info
.\mvnw.cmd -Ppostgresql-integration clean verify
cd ..
```

Cette commande exécute les classes suffixées par `ITest`.

Testcontainers crée un PostgreSQL temporaire et le supprime après les tests.
Il n'est pas nécessaire de lancer `docker compose up` pour cette validation.

### 11.2. Build frontend

Depuis la racine du projet :

```powershell
cd frontend
npm.cmd run build
cd ..
```

### 11.3. Tests unitaires frontend

Depuis la racine du projet :

```powershell
cd frontend
npm.cmd run test -- --watch=false
cd ..
```

### 11.4. Tests Cypress mockés

Les neuf scénarios Cypress mockés valident la présentation, la connexion, les
disponibilités, les matches publics, les invitations privées, les dettes, les
statistiques, les fermetures et les traitements automatiques avec des réponses
API simulées.

```powershell
cd frontend
npm.cmd run cypress:run
cd ..
```

### 11.5. Test Cypress full stack

Les deux scénarios Cypress full stack valident la présentation de démonstration
et un vrai parcours :

```txt
Angular réel
HTTP réel
Spring Boot réel
H2 réelle
```

Depuis la racine du projet :

```powershell
cd frontend
npm.cmd run cypress:run:fullstack
cd ..
```

Cette commande :

- démarre automatiquement le backend Spring Boot avec H2 ;
- attend le health check http://localhost:8080/api/health ;
- démarre Angular ;
- attend http://localhost:4200 ;
- exécute le parcours Cypress full stack ;
- arrête les processus démarrés à la fin du test.

### 11.6. GitHub Actions

Le dépôt contient un workflow GitHub Actions qui exécute automatiquement les validations principales sur les Pull Requests :

```txt
backend tests H2
backend PostgreSQL integration tests
frontend build
frontend tests
```

Workflow concerné :

```txt
.github/workflows/project-quality-gates.yml
```

---

## 12. Documentation du projet

Les documents principaux sont à la racine :

```txt
README.md
ARCHITECTURE.md
EXPLOITATION.md
DEMO.md
```

### `ARCHITECTURE.md`

Décrit :

- l'architecture frontend ;
- l'architecture backend ;
- la séparation controller / service / repository ;
- les outils et frameworks structurants ;
- Swagger / OpenAPI ;
- CORS ;
- sécurité Spring Security et JWT ;
- autorisations par rôles et `@PreAuthorize` ;
- limites connues pour un déploiement réel ;
- tests.

### `EXPLOITATION.md`

Décrit :

- les prérequis ;
- les commandes backend ;
- les commandes frontend ;
- les ports utilisés ;
- les commandes de test ;
- la base de données utilisée ;
- les comptes de démonstration ;
- les informations utiles pour démarrer le projet.

### `DEMO.md`

Décrit :

- le scénario de démonstration ;
- les comptes de démonstration ;
- les fonctionnalités principales à parcourir ;
- les cas de refus prévus ;
- les points techniques visibles dans le projet.

### `docs/db/`

Contient :

- le schéma SQL ;
- les données de démonstration ;
- la documentation des users DB ;
- les scripts DB utiles au projet.

---

## 13. GitHub et méthode de travail

Le projet est suivi avec GitHub.

Méthode utilisée :

```txt
issue GitHub par fonctionnalité ou correction
branche dédiée par issue
commits courts et cohérents
pull request avant merge
tests et validations avant merge
suppression des branches après fusion
```

Le dépôt contient le frontend et le backend dans un seul repository.

Les dernières PR ont notamment permis de stabiliser :

```txt
expiration des pénalités
cycle de vie des matches
annulations et disponibilités
remboursements et statistiques
données de démonstration relatives
test d'intégration backend
Cypress full stack
contrat d'erreur API
GitHub Actions
```

La branche main représente l'état stable du projet.

---

## 14. Commandes utiles

### Backend H2

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

### Tests backend

Depuis la racine :

```powershell
cd backend
.\mvnw.cmd clean test
cd ..
```

### Tests PostgreSQL complémentaires

Docker Desktop doit être démarré :

```powershell
cd backend
docker info
.\mvnw.cmd -Ppostgresql-integration clean verify
cd ..
```

Cette commande utilise un conteneur PostgreSQL temporaire géré par
Testcontainers. Elle ne démarre pas la base PostgreSQL Docker Compose de
démonstration.

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

### Cypress mocké

```powershell
cd frontend
npm.cmd run cypress:run
cd ..
```

### Cypress full stack

Avant le full stack, vérifie que les ports `8080` et `4200` ne sont pas déjà utilisés par une ancienne exécution.

```powershell
cd frontend
npm.cmd run cypress:run:fullstack
cd ..
```

### PostgreSQL Docker optionnel

```powershell
docker compose up -d postgres
```

### Backend avec PostgreSQL optionnel

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

### Arrêter PostgreSQL sans supprimer les données

```powershell
docker compose stop postgres
```

## 15. Points importants

- Le frontend Angular ne contient aucun SQL.
- Le frontend ne se connecte jamais directement à la base de données.
- Le backend Spring Boot expose une API REST.
- La logique métier principale est dans les services backend.
- Les repositories isolent l'accès aux données.
- H2 est utilisé par défaut pour une démonstration rapide.
- PostgreSQL Docker est disponible en option.
- Les données de démonstration sont relatives à la date du jour.
- Les tests backend, frontend, Cypress et la CI GitHub Actions montrent la stabilité du projet.
