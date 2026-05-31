# Architecture — Padel Marius

## 1. Objectif du document

Ce document décrit l'architecture technique du projet Padel Marius.

Il répond aux attentes PDW :

- architecture frontend ;
- architecture backend ;
- couches principales ;
- outils, librairies et frameworks ;
- URL Swagger ;
- séparation frontend / backend ;
- sécurité minimale ;
- communication HTTP.

---

## 2. Vue d'ensemble

```txt
+---------------------------+
| Frontend Angular          |
| - pages                   |
| - services HTTP           |
| - guards                  |
| - interceptor admin       |
+-------------+-------------+
              |
              | HTTP REST / JSON
              v
+-------------+-------------+
| Backend Spring Boot       |
| - controllers             |
| - services                |
| - repositories            |
| - entities                |
| - DTO                     |
+-------------+-------------+
              |
              | JPA / SQL
              v
+-------------+-------------+
| Base de données H2        |
| SQL relationnelle         |
+---------------------------+
```

Le frontend et le backend sont séparés.

Le frontend Angular ne se connecte jamais directement à la base de données.  
Le frontend ne contient aucun SQL.  
Le backend est le seul composant qui accède à la base de données.

---

## 3. Architecture backend

### 3.1. Package racine

```txt
com.padelMarius.backend
```

### 3.2. Structure principale

```txt
backend/src/main/java/com/padelMarius/backend/
  config/
  controller/
  dto/
  entity/
  exception/
  repository/
  service/
```

### 3.3. Rôle des couches

#### Controller

Emplacement :

```txt
backend/src/main/java/com/padelMarius/backend/controller
```

Responsabilités :

- recevoir les requêtes HTTP ;
- lire les path params, query params, headers et body ;
- valider les DTO request ;
- appeler les services ;
- retourner les DTO response ;
- retourner les bons statuts HTTP.

Les controllers ne contiennent pas la logique métier lourde.

Exemples :

```txt
MatchController
ParticipationController
PaiementController
DetteController
AdminFermetureController
StatistiquesAdminController
AuthController
```

---

#### Service

Emplacement :

```txt
backend/src/main/java/com/padelMarius/backend/service
```

Responsabilités :

- appliquer les règles métier ;
- orchestrer plusieurs repositories ;
- gérer les transactions quand nécessaire ;
- calculer les disponibilités ;
- créer les matches ;
- gérer les paiements ;
- gérer les dettes ;
- gérer les pénalités ;
- calculer les statistiques ;
- vérifier les rôles admin.

Exemples :

```txt
MatchCreationService
ParticipationService
PaiementService
DetteService
TraitementVeilleService
StatistiquesAdminService
AdminAuthorizationService
```

---

#### Repository

Emplacement :

```txt
backend/src/main/java/com/padelMarius/backend/repository
```

Responsabilités :

- accéder à la base de données ;
- utiliser Spring Data JPA ;
- fournir les requêtes nécessaires aux services.

Les repositories ne contiennent pas de logique métier.

Exemples :

```txt
MembreRepository
PadelMatchRepository
ParticipationRepository
PaiementRepository
DetteRepository
AdministrateurRepository
```

---

#### Entity

Emplacement :

```txt
backend/src/main/java/com/padelMarius/backend/entity
```

Responsabilités :

- représenter les tables de la base ;
- définir les relations JPA ;
- définir les enums métier.

Entités principales :

```txt
Site
Terrain
HoraireAnnuelSite
Fermeture
Membre
Administrateur
PadelMatch
Participation
Paiement
Dette
Penalite
```

---

#### DTO

Emplacement :

```txt
backend/src/main/java/com/padelMarius/backend/dto
```

Responsabilités :

- représenter les données d'entrée API ;
- représenter les données de sortie API ;
- éviter d'exposer directement les entities JPA au frontend.

---

#### Exception

Emplacement :

```txt
backend/src/main/java/com/padelMarius/backend/exception
```

Responsabilités :

- exceptions métier ;
- exceptions d'authentification ;
- exceptions d'autorisation ;
- exceptions de ressource introuvable.

Le backend utilise aussi un handler global :

```txt
ApiExceptionHandler
```

Il permet de retourner des erreurs lisibles au frontend.

---

## 4. Architecture frontend

### 4.1. Emplacement

```txt
frontend/src/app
```

### 4.2. Structure principale

```txt
frontend/src/app/
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
```

### 4.3. Pages Angular

Emplacement :

```txt
frontend/src/app/pages
```

Responsabilités :

- afficher les écrans ;
- gérer les formulaires simples ;
- appeler les services Angular ;
- afficher les résultats ;
- afficher les messages d'erreur.

Pages principales :

```txt
accueil
joueur-auth
inscription-joueur
disponibilites
creer-match
matches-publics
mes-reservations
mes-dettes
mon-solde
historique-transactions
admin-login
admin-dashboard
admin-statistiques
admin-membres
admin-traitement-veille
admin-fermetures
```

---

### 4.4. Services Angular

Emplacement :

```txt
frontend/src/app/services
```

Responsabilités :

- centraliser les appels HTTP ;
- retourner des `Observable<T>` ;
- éviter de mettre les appels API directement dans les templates ;
- gérer le contexte d'authentification simple.

Exemples :

```txt
AuthApiService
AuthContextService
DisponibiliteApiService
MatchApiService
MatchPublicApiService
ReservationApiService
DetteApiService
AdminStatsApiService
AdminFermetureApiService
AdminMembreApiService
```

---

### 4.5. Models TypeScript

Emplacement :

```txt
frontend/src/app/models
```

Responsabilités :

- typer les réponses API ;
- typer les requêtes envoyées au backend ;
- garder un contrat clair entre Angular et Spring Boot.

---

### 4.6. Guards Angular

Emplacement :

```txt
frontend/src/app/guards
```

Guards :

```txt
joueur.guard.ts
admin.guard.ts
```

Rôle :

- empêcher l'accès aux routes joueur si aucun joueur n'est connecté ;
- empêcher l'accès aux routes admin si aucun admin n'est connecté.

Exemple de routes protégées :

```txt
/joueur/disponibilites
/joueur/matches-publics
/joueur/mes-reservations
/admin/dashboard
/admin/statistiques
/admin/fermetures
```

---

### 4.7. Interceptor Angular

Emplacement :

```txt
frontend/src/app/interceptors
```

Interceptor :

```txt
admin-auth.interceptor.ts
```

Rôle :

- ajouter le header `Authorization: Bearer <token>` aux requêtes API quand un joueur ou un administrateur est connecté ;
- garder temporairement `X-Admin-Login` pour compatibilité MVP sur les endpoints admin ;
- permettre au backend de vérifier l'identité et le rôle admin.

---

## 5. Communication HTTP

Le frontend appelle uniquement l'API REST du backend.

Exemples d'endpoints :

```http
GET /api/health
POST /api/auth/joueur
POST /api/auth/admin
GET /api/disponibilites?siteId=1001&date=2026-06-20
POST /api/matches
GET /api/matches/publics?siteId=1001&date=2026-06-20
POST /api/matches/{matchId}/participants/public/payer
GET /api/membres/{matricule}/solde
GET /api/membres/{matricule}/reservations
GET /api/membres/{matricule}/paiements
POST /api/admin/fermetures
GET /api/admin/statistiques?dateDebut=2026-05-01&dateFin=2026-06-30
```

---

## 6. OpenAPI / Swagger

Quand le backend est démarré, Swagger est disponible ici :

```txt
http://localhost:8080/swagger-ui.html
```

La spécification OpenAPI JSON est disponible ici :

```txt
http://localhost:8080/v3/api-docs
```

---

## 7. Gestion CORS et proxy Angular

Le frontend démarre sur :

```txt
http://localhost:4200
```

Le backend démarre sur :

```txt
http://localhost:8080
```

En développement, Angular utilise un proxy :

```txt
frontend/proxy.conf.json
```

Ce proxy redirige les appels relatifs `/api/**` vers :

```txt
http://localhost:8080
```

Le backend contient aussi une configuration CORS locale qui autorise :

```txt
http://localhost:4200
```

à appeler :

```txt
/api/**
```

---

## 8. Sécurité MVP

Le MVP contient une sécurité simple adaptée au projet.

### Joueurs

Les joueurs se connectent avec :

```txt
matricule
mot de passe
```

Le matricule reste l'identifiant métier principal du joueur.

### Administrateurs

Les administrateurs se connectent avec :

```txt
login
mot de passe
```

Deux rôles existent :

```txt
GLOBAL
SITE
```

Règles :

- un admin `GLOBAL` peut gérer tous les sites ;
- un admin `SITE` ne peut gérer que son site ;
- les routes admin Angular sont protégées ;
- les endpoints admin backend vérifient le rôle via `AdminAuthorizationService`.

### Mots de passe

Les mots de passe ne sont pas stockés en clair.  
Le backend utilise BCrypt via `spring-security-crypto`.

### JWT MVP compatible

Le projet utilise un JWT MVP compatible avec l'architecture existante.
Après une connexion réussie, le backend génère un token signé.
Le token est renvoyé au frontend dans la réponse d'authentification.
Le frontend le stocke dans le service d'authentification existant.
Un interceptor Angular ajoute ensuite le token aux requêtes HTTP avec :

```txt
Authorization: Bearer <token>
---

## 9. Base de données

Le MVP utilise H2 en mémoire.

Configuration locale :

```txt
URL      : jdbc:h2:mem:padeldb
User     : sa
Password : vide
```

Le seed est automatique au démarrage backend via :

```txt
backend/src/main/resources/data.sql
```

Artefacts de remise :

```txt
docs/db/schema.sql
docs/db/data-demo.sql
docs/db/db-users.md
docs/db/db-users-h2.sql
```

Le frontend n'a aucun user DB.

---

## 10. Tests

### Backend

Tests présents sur les couches attendues :

```txt
controller
service
repository
config
```

Commande :

```powershell
cd backend
.\mvnw.cmd clean test
cd ..
```

### Frontend

Tests unitaires Angular :

```powershell
cd frontend
npm run test
cd ..
```

Build Angular :

```powershell
cd frontend
npm run build
cd ..
```

Tests E2E Cypress :

```powershell
cd frontend
npm run cypress:run
cd ..
```

---

## 11. Librairies et frameworks structurants

### Backend

```txt
Java 21
Spring Boot
Spring Web MVC
Spring Data JPA
Bean Validation
H2
OpenAPI / Springdoc
Lombok
BCrypt / spring-security-crypto
Maven Wrapper
```

### Frontend

```txt
Angular
TypeScript
Angular Router
Angular HttpClient
RxJS
Vitest
Cypress
start-server-and-test
```

---
