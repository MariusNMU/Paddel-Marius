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
- sécurité Spring Security et JWT ;
- communication HTTP.

---

## 2. Vue d'ensemble

```txt
+--------------------------------+
| Frontend Angular               |
| - pages                        |
| - services HTTP                |
| - guards                       |
| - interceptor JWT              |
| - models TypeScript            |
+---------------+----------------+
                |
                | HTTP REST / JSON via /api/**
                v
+---------------+----------------+
| Backend Spring Boot REST API   |
| - controllers                  |
| - services métier              |
| - repositories JPA             |
| - entities JPA                 |
| - DTO                          |
| - Spring Security + JWT        |
+---------------+----------------+
                |
                | JPA / SQL
                v
+---------------+----------------+
| Base de données relationnelle  |
| - H2 en mémoire par défaut     |
| - PostgreSQL Docker optionnel  |
+--------------------------------+
```

Le frontend et le backend sont séparés.

Le frontend Angular ne se connecte jamais directement à la base de données.
Le frontend ne contient aucun SQL.
Le frontend appelle uniquement l'API REST du backend.
Le backend est le seul composant applicatif qui accède à la base de données.

H2 est utilisé par défaut pour une démonstration rapide. PostgreSQL Docker est disponible comme configuration optionnelle plus réaliste.

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
  scheduler/
  security/
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
InvitationPriveeController
MatchPublicController
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
- sérialiser les transitions concurrentes sensibles avec des verrous
  pessimistes acquis par les repositories ;
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
TraitementEcheanceService
EtatOperationnelAdminService
StatistiquesAdminService
AdminAuthorizationService
```

---

#### Scheduler

Emplacement :

```txt
backend/src/main/java/com/padelMarius/backend/scheduler
```

`TraitementVeilleScheduler` exécute automatiquement les règles J-1 : libération
des participations non payées et passage public des matches privés incomplets.
`TraitementEcheanceScheduler` démarre et termine automatiquement les matches,
puis déclenche les dettes et pénalités prévues. Les deux schedulers appellent
la couche service ; les endpoints administrateur utilisent les mêmes services
pour un déclenchement manuel.

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

Les méthodes `findByIdForUpdate` et `findByMatchIdForUpdate` portent les verrous
JPA nécessaires pour empêcher qu'un paiement confirmé soit ensuite réécrit
comme participation libérée par un refus ou par le traitement de veille.

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
- valider certaines données reçues avec Bean Validation ;
- éviter d'exposer directement les entities JPA au frontend ;
- garder un contrat JSON clair entre Angular et Spring Boot.

Exemples :

```txt
CreerMatchRequest
MatchResponse
DisponibilitesResponse
PaiementResponse
ReservationJoueurResponse
StatistiquesAdminResponse
ApiErrorResponse
```

Le DTO ApiErrorResponse est le format standard utilisé pour les erreurs API importantes :

```json
{
  "code": "...",
  "message": "..."
}
```

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

Il transforme les exceptions en réponses HTTP lisibles et homogènes pour le frontend.

Le format standard est :

```json
{
  "code": "...",
  "message": "..."
}
```

Exemples d'erreurs gérées :

```txt
RESSOURCE_INTROUVABLE
CONFIGURATION_METIER_INVALIDE
AUTHENTIFICATION_INVALIDE
TROP_DE_TENTATIVES
ACCES_REFUSE
VALIDATION_INVALIDE
REQUETE_INVALIDE
JSON_INVALIDE
```

Le handler couvre aussi des erreurs Spring classiques comme :

```txt
validation @Valid invalide
paramètre de requête manquant
paramètre de mauvais type
JSON mal formé
contrainte de paramètre non respectée
```

Cela évite au frontend de recevoir plusieurs formats d'erreur différents.

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

- afficher les écrans et les données exposées par les façades ;
- lier les champs de formulaire à l'état de présentation ;
- déléguer les actions utilisateur aux façades ;
- afficher les chargements, erreurs et résultats exposés ;
- ne contenir ni appel HTTP direct, ni règle métier backend.

Pages principales :

```txt
accueil
joueur-auth
inscription-joueur
disponibilites
creer-match
matches-publics
invitations-recues
mes-reservations
mes-dettes
mon-solde
historique-transactions
admin-login
admin-dashboard
admin-statistiques
admin-membres
admin-etat-operationnel
admin-traitement-veille
admin-traitement-echeance
admin-fermetures
```

---

### 4.4. Services Angular

Emplacement :

```txt
frontend/src/app/services
```

Responsabilités :

- les clients API centralisent les appels HTTP et retournent des
  `Observable<T>` ;
- les façades possèdent l'état des parcours avec des signaux Angular ;
- les façades orchestrent les appels API, les chargements, les erreurs et les
  succès ;
- `AuthContextService` conserve et synchronise la session ;
- `InvitationNotificationService` synchronise le nombre d'invitations à
  traiter et actualise immédiatement le badge après paiement ou refus ;
- les composants restent limités à la présentation et aux événements
  utilisateur.

Exemples de clients API :

```txt
AuthApiService
DisponibiliteApiService
MatchApiService
MatchPublicApiService
PaiementApiService
InvitationApiService
ReservationApiService
DetteApiService
```

Exemples de façades :

```txt
AuthFacadeService
AppShellFacadeService
CreerMatchFacadeService
MatchesPublicsFacadeService
DisponibilitesFacadeService
MesReservationsFacadeService
MesDettesFacadeService
AdminStatistiquesFacadeService
AdminEtatOperationnelFacadeService
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
admin-global.guard.ts
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
auth.interceptor.ts
```

Rôle :

- ajouter le header `Authorization: Bearer <token>` aux requêtes API quand un joueur ou un administrateur est connecté ;
- limiter strictement cet ajout aux URLs relatives internes qui commencent par `/api/` ;
- ne pas ajouter l'ancien JWT aux endpoints `/api/auth/**` ;
- après un `401`, déclencher un refresh partagé et rejouer une seule fois la requête initiale ;
- ne transmettre aucun login administrateur en dehors du JWT ;
- permettre au backend de vérifier l'identité et le rôle admin.

---

## 5. Communication HTTP

Le frontend appelle uniquement l'API REST du backend.

Les appels Angular utilisent des URLs relatives du type :

```txt
/api/...
```

En développement, le proxy Angular redirige ces appels vers :

```txt
http://localhost:8080
```

Exemples d'endpoints :

```http
GET /actuator/health
POST /api/auth/joueur
POST /api/auth/admin
POST /api/auth/refresh
POST /api/auth/logout
GET /api/disponibilites?siteId=1001&date=<date-demo>
POST /api/matches
GET /api/matches/publics?siteId=1001&date=<date-demo>
POST /api/matches/{matchId}/participants/public/payer
POST /api/participations/{participationId}/paiements
GET /api/membres/{matricule}/solde
GET /api/membres/{matricule}/reservations
GET /api/membres/{matricule}/paiements
GET /api/membres/{matricule}/dettes/ouvertes
POST /api/dettes/{detteId}/paiements
POST /api/admin/fermetures
GET /api/admin/sites
GET /api/admin/etat-operationnel?date=<date>&siteId=<site-id>
GET /api/admin/statistiques?dateDebut=<date-debut>&dateFin=<date-fin>
GET /api/admin/membres
POST /api/admin/matches/traitement-veille?date=<date-traitement>
POST /api/admin/matches/traitement-echeance
```

Le point de contrôle `/actuator/health` est fourni par Spring Boot Actuator.
Seul l'endpoint `health` est exposé et les détails internes ne sont pas
retournés publiquement.

Les endpoints protégés utilisent :

```txt
Authorization: Bearer <token>
```

### 5.1. Opérations de participation exposées

L'API n'expose pas de création générique de participation publique sans paiement.

Pour un match privé, l'organisateur utilise l'invitation privée :

```http
POST /api/matches/{matchId}/invitations/privees
```

Le joueur invité confirme et paie sa participation existante par l'unique
endpoint de paiement :

```http
POST /api/participations/{participationId}/paiements
```

Le DTO contient le montant standard de 15 euros. L'ancien endpoint suffixé
`/standard` n'est plus exposé.

Pour un match public, le joueur utilise l'opération atomique :

```http
POST /api/matches/{matchId}/participants/public/payer
```

Cette opération crée la participation et effectue le paiement dans la même transaction métier. Une place publique ne peut donc pas être occupée sans paiement.

La génération d'une dette n'est pas exposée librement aux joueurs. Elle est déclenchée par le backend lors des traitements métier.

Le traitement J-1 est exécuté par `TraitementVeilleScheduler`. Le cycle
`A_VENIR` vers `DEMARRE`, puis `DEMARRE` vers `TERMINE`, est mis à jour par
`TraitementEcheanceScheduler`. Les deux tâches appellent leur couche service à
intervalle configurable. Les endpoints administrateur restent disponibles
pour un déclenchement manuel. Les opérations de participation et de paiement
vérifient également l'heure réelle du match : la planification ne remplace donc
pas les contrôles métier directs.

Les services refusent également la consultation ou la participation à un match
public lorsque son site ou son terrain est inactif. L'écran d'état opérationnel
utilise en revanche `GET /api/admin/sites` afin qu'un administrateur global
puisse diagnostiquer les sites actifs comme inactifs.

Dans les statistiques, les matches, les paiements et les dettes ouvertes sont
alignés sur la même période. Pour une dette, la période est déterminée par la
date de début du match auquel elle est rattachée.

### 5.2. Contrat d'erreur API

Les erreurs API importantes suivent le format :

```json
{
  "code": "...",
  "message": "..."
}
```

Ce contrat permet au frontend d'afficher des messages d'erreur lisibles sans dépendre du format technique par défaut de Spring.

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

## 8. Sécurité Spring Security et JWT

### 8.1. Authentification

Les joueurs se connectent avec leur matricule et leur mot de passe.

Les administrateurs se connectent avec leur login et leur mot de passe.
Deux portées administratives existent : `GLOBAL` et `SITE`.

Les mots de passe sont hachés avec BCrypt. Les nouvelles inscriptions
appliquent une longueur comprise entre 12 et 72 caractères.

Les recherches de matricule et de login utilisées par l'authentification sont
insensibles à la casse. Les réponses et les JWT reprennent ensuite la valeur
canonique de l'entité. Les DTO de connexion bornent le matricule à 10
caractères, le login à 150 et le mot de passe à 72.

`AuthService` transmet les identifiants à l'`AuthenticationManager` de Spring
Security. Son `DaoAuthenticationProvider` utilise le `PadelUserDetailsService`
pour charger le joueur ou l'administrateur depuis les repositories, puis le
`PasswordEncoder` BCrypt pour vérifier le mot de passe.

### 8.2. SecurityFilterChain

Le backend utilise une `SecurityFilterChain` stateless :

- CSRF désactivé pour l'API REST JWT ;
- sessions HTTP désactivées ;
- routes publiques explicitement autorisées ;
- routes joueur limitées à `ROLE_JOUEUR` ;
- routes administrateur limitées à `ROLE_ADMIN` ;
- toutes les autres routes authentifiées par défaut.

Le `JwtAuthenticationFilter`, basé sur `OncePerRequestFilter`, est placé
avant `UsernamePasswordAuthenticationFilter`.

`AuthRateLimitFilter` s'exécute avant le filtre JWT pour les trois endpoints
d'authentification. Il applique une fenêtre fixe, par adresse distante et par
endpoint, et renvoie `429` avec `Retry-After` après la limite configurée.

`JwtService` délègue à JJWT (`io.jsonwebtoken` 0.13.0) la création, la
signature HS256, le parsing et la validation de l'expiration des tokens. Le
filtre recharge le compte via `UserDetailsService`, récupère les autorités
actuelles en base et place l'utilisateur dans le `SecurityContext`.

Les URL publiques sont déclarées uniquement dans `SecurityConfig`. Le filtre
traite le header `Authorization` lorsqu'il est présent, sans dupliquer les
règles d'accès de la `SecurityFilterChain`.

### 8.3. Access token et refresh token

- access token : JWT de 60 minutes conservé avec la session Angular ;
- refresh token : JWT de 7 jours dans un cookie `HttpOnly`,
  `SameSite=Strict`, chemin `/api/auth` ;
- l'access token contient uniquement le sujet, le type d'utilisateur, le type
  de token et les claims temporels ;
- le refresh token ajoute un `jti` aléatoire ;
- les deux types de tokens portent un claim distinct et ne sont pas
  interchangeables ;
- `POST /api/auth/refresh` verrouille et révoque l'ancien `jti`, recharge le
  compte actif en base, puis enregistre le nouveau `jti` dans la même
  transaction ;
- `POST /api/auth/logout` révoque le `jti` et expire le cookie ;
- seul le `jti` et ses métadonnées sont persistés, jamais le JWT brut.

### 8.4. Autorisation métier

`@EnableMethodSecurity` active les contrôles `@PreAuthorize`.

Les controllers et services vérifient notamment :

- l'identité du joueur ;
- la propriété des participations et des dettes ;
- le rôle administrateur ;
- la portée `GLOBAL` ou `SITE` ;
- le site de rattachement d'un administrateur SITE.

Le backend reste l'autorité définitive. Les guards et les masquages Angular
ne remplacent pas ces contrôles.

### 8.5. Session Angular

`AuthFacadeService` orchestre les connexions et les déconnexions.

`AuthContextService` :

- conserve la session dans `localStorage` ;
- expose le token de l'unique session active à l'interceptor ;
- supprime l'autre type de session lors d'une connexion ;
- écoute l'événement `storage` ;
- synchronise immédiatement les différents onglets ;
- nettoie les deux sessions si un état incohérent est détecté ;
- remplace l'access token après un refresh réussi ;
- conserve temporairement une session dont l'access token est expiré afin que
  l'interceptor puisse la renouveler avec le cookie HttpOnly.

`auth.interceptor.ts` ajoute ce token uniquement aux appels internes
`/api/**`. Après une réponse `401`, il appelle `AuthRefreshService`, puis rejoue
une seule fois la requête avec le nouveau token. Un seul refresh est envoyé si
plusieurs requêtes échouent simultanément.

Si le refresh échoue, le service vérifie qu'aucune nouvelle session n'a
remplacé l'ancienne, nettoie la session concernée et redirige vers le login
joueur ou administrateur. Le logout reste localement effectif en cas d'erreur
réseau et expose alors une alerte globale.

### 8.6. Limites connues

La sécurité est complète pour le périmètre du MVP. Les limites restantes sont :

- les access tokens ne sont pas révoqués avant leur expiration ;
- le rate limiting en mémoire est local à une instance et remis à zéro au
  redémarrage ;
- un déploiement multi-instance nécessite un compteur partagé ;
- le secret JWT ne fait pas l'objet d'une rotation automatique.

La valeur JWT locale par défaut est réservée à la démonstration. Un
déploiement réel doit fournir `PADEL_JWT_SECRET` depuis l'environnement et
activer `PADEL_REFRESH_COOKIE_SECURE=true` sous HTTPS.

## 9. Base de données

Le projet utilise une base relationnelle.

Deux configurations existent :

```txt
H2 en mémoire par défaut
PostgreSQL Docker optionnel
```

### 9.1. H2 par défaut

H2 est utilisé pour le démarrage rapide du MVP.

Configuration locale :

```txt
URL      : jdbc:h2:mem:padeldb
User     : sa
Password : vide
```

Le schéma est créé par Liquibase à partir d'un changelog versionné.
Hibernate utilise `ddl-auto=validate` et vérifie la correspondance entre le
schéma et les entités JPA sans modifier les tables.

Le seed H2 est un changeset Liquibase réservé au contexte `demo` et à H2.

La migration `003-create-refresh-token-table.sql` ajoute la table
`jeton_rafraichissement` utilisée pour la rotation et la révocation serveur.

Les données de démonstration H2 sont calculées relativement à la date du jour afin de rester utilisables pendant la deuxième session.

### 9.2. PostgreSQL Docker optionnel

PostgreSQL Docker peut être lancé pour montrer une base plus réaliste.

Le backend est démarré avec le profil :

```txt
postgres
```

Commande PowerShell :

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

Le profil PostgreSQL utilise :

```txt
backend/src/main/resources/application-postgres.properties
```

Dans ce profil :

```properties
spring.sql.init.mode=never
```

Donc data.sql n'est pas exécuté sur PostgreSQL.

Les données PostgreSQL de démonstration sont chargées par :

```txt
PostgresDemoDataSeeder
```

Ce seeder Java est actif uniquement avec le profil postgres.

### 9.3. Artefacts de remise

Artefacts DB principaux :

```txt
docs/db/schema.sql
docs/db/data-demo.sql
docs/db/db-users.md
docs/db/db-users-h2.sql
docs/db/postgres-demo-seed.md
docker/postgres/init/01-create-users-and-rights.sql
```

Le fichier docs/db/schema.sql sert d'artefact de schéma relationnel pour la remise.

Le frontend n'a aucun user DB et ne connaît aucun credential DB.

### 9.4. Users DB

Pour H2 local :

```txt
sa
mot de passe vide
```

Pour PostgreSQL Docker, le projet documente plusieurs users :

```txt
padel_admin      : initialisation Docker locale
padel_migration  : création / évolution du schéma en cible
padel_app        : user applicatif utilisé par le backend
padel_readonly   : lecture seule
```

Liquibase utilise `padel_migration` pour les opérations DDL. Le backend utilise
`padel_app` pour les opérations métier. Cette séparation empêche l'application
de créer ou de modifier elle-même les tables.

---

## 10. Tests

### 10.1. Tests backend

Tests présents sur les couches attendues :

```txt
controller
service
repository
config
security
intégration backend
```

Commande :

```powershell
cd backend
.\mvnw.cmd clean test
cd ..
```

Le projet contient notamment :

```txt
@WebMvcTest pour les controllers
@DataJpaTest pour les repositories
Mockito pour les services
@SpringBootTest + MockMvc pour un happy flow d'intégration backend
```

Le test d'intégration backend vérifie un parcours complet :

```txt
connexion joueur
consultation disponibilités
création match
paiement participation organisateur
consultation réservations
```

Les scénarios qui dépendent du verrouillage réel de PostgreSQL sont isolés dans
le profil `postgresql-integration` : double paiement, paiement face à une
fermeture, paiement face au refus d'une invitation, paiement face à la veille,
recalcul des dettes et traitements d'échéance concurrents.

```powershell
cd backend
docker info
.\mvnw.cmd -Ppostgresql-integration clean verify
cd ..
```

### 10.2. Tests frontend

Tests unitaires Angular :

```powershell
cd frontend
npm.cmd run test -- --watch=false
cd ..
```

Build Angular :

```powershell
cd frontend
npm.cmd run build
cd ..
```

### 10.3. Cypress mocké

Neuf scénarios Cypress mockés valident les principaux parcours UI avec des
réponses API simulées : présentation, solde, disponibilités, match public,
invitation privée, dette, statistiques, fermeture et traitements automatiques.

```powershell
cd frontend
npm.cmd run cypress:run
cd ..
```

Ces tests sont utiles pour valider l'interface sans dépendre d'un backend lancé.

### 10.4. Cypress full stack

Deux scénarios Cypress full stack valident l'application réelle :

```txt
Angular réel
HTTP réel
Spring Boot réel
H2 réelle
```

Une seule commande démarre automatiquement Spring Boot avec H2, attend son
health check, démarre Angular, exécute Cypress puis arrête les processus
lancés :

```powershell
cd frontend
npm.cmd run cypress:run:fullstack
cd ..
```

Il n'est pas nécessaire de démarrer manuellement PostgreSQL ou le backend.

Le premier scénario vérifie les données de présentation exposées par le backend.
Le second vérifie la connexion joueur, la consultation des disponibilités, la
création d'un match et la consultation des réservations.

### 10.5. GitHub Actions

Le projet contient une CI GitHub Actions :

```txt
.github/workflows/project-quality-gates.yml
```

Elle exécute automatiquement :

```txt
backend tests
backend PostgreSQL integration tests
frontend build
frontend tests
```

Cette CI permet de montrer que les Pull Requests sont validées automatiquement avant fusion.

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
PostgreSQL Docker optionnel
OpenAPI / Springdoc
Lombok
Spring Security
BCrypt
JWT
SecurityFilterChain
OncePerRequestFilter
Method Security
Maven Wrapper
GitHub Actions
```

### Frontend

```txt
Angular
Angular Material
Angular Signals
Façades de parcours
TypeScript
Angular Router
Angular HttpClient
Angular Guards
Angular Interceptor
RxJS
Vitest
Cypress mocké
Cypress full stack
start-server-and-test
```

---
