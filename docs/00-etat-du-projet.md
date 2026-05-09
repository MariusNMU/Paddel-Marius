# État du projet — Padel Marius

Dernière mise à jour : 2026-05-09  
Document de suivi rapide pour reprise par IA / Codex / développeur.

Objectif du fichier : suivre simplement ce qui est fait, ce qui reste à faire, et le prochain pas concret.  
À chaque PR mergée, mettre à jour ce fichier en déplaçant l'étape concernée vers `[FAIT]`.

---

## 1. Statuts utilisés

- `[FAIT]` : terminé, testé, mergé ou prêt à être considéré comme livré après merge de la PR courante.
- `[EN COURS]` : branche ou PR active.
- `[A FAIRE]` : pas encore commencé.
- `[A SURVEILLER]` : point technique connu, non bloquant pour le MVP.
- `[A RESPECTER]` : exigence professeur ou règle projet permanente.

---

## 2. Résumé projet

Application web de réservation de terrains de padel.

Stack projet :

- `[FAIT]` Backend : Java avec Spring Boot.
- `[FAIT]` Build backend : Maven Wrapper.
- `[FAIT]` Base de données : SQL relationnelle, H2 pour le MVP.
- `[A FAIRE]` Frontend : Angular.
- `[FAIT]` Communication prévue : frontend vers backend uniquement via API HTTP REST.
- `[A RESPECTER]` Le frontend ne doit jamais contenir de SQL.
- `[A RESPECTER]` Le frontend ne doit jamais accéder directement à la base de données.

Priorité actuelle :

1. `[FAIT]` Backend fonctionnel.
2. `[FAIT]` Tests backend visibles.
3. `[FAIT]` Réservation / paiement / dette / statistiques.
4. `[A FAIRE]` Frontend simple et démontrable.
5. `[EN COURS]` Documentation minimale mais propre.
6. `[A FAIRE]` Préparation démo finale.

---

## 3. Exigences professeur à respecter

- `[A RESPECTER]` GitHub public avec issues, branches, commits et PR.
- `[A RESPECTER]` Une branche par issue.
- `[A RESPECTER]` Commits courts, réguliers et cohérents.
- `[A RESPECTER]` Backend sous forme de REST API.
- `[A RESPECTER]` Séparation claire controller / service / repository.
- `[A RESPECTER]` Tests backend obligatoires :
  - controllers
  - services
  - repositories
- `[A RESPECTER]` Frontend Angular séparé du backend.
- `[A RESPECTER]` Aucun SQL dans Angular.
- `[A RESPECTER]` Aucun accès DB direct depuis Angular.
- `[A RESPECTER]` Accès joueur par matricule, pas de login joueur.
- `[A RESPECTER]` Admin avec rôles :
  - GLOBAL
  - SITE
- `[FAIT]` Script DB ou artefact de schéma prévu.
- `[FAIT]` Explication des users DB et droits associés prévue.
- `[A FAIRE]` Dossier d'architecture à fournir à la racine.
- `[A FAIRE]` Document d'exploitation à fournir à la racine.
- `[A FAIRE]` Démo métier orientée règles business.
- `[A RESPECTER]` Les différentes parties du projet doivent démarrer sans erreur.
- `[A RESPECTER]` Le dépôt Git doit rester propre.

---

## 4. Modèle métier retenu

Entités principales du MVP :

- `[FAIT]` Site
- `[FAIT]` Terrain
- `[FAIT]` HoraireAnnuelSite
- `[FAIT]` Fermeture
- `[FAIT]` Membre
- `[FAIT]` Administrateur
- `[FAIT]` PadelMatch
- `[FAIT]` Participation
- `[FAIT]` Paiement
- `[FAIT]` Dette
- `[FAIT]` Penalite

Règles métier principales :

- `[FAIT]` Multi-sites.
- `[FAIT]` Terrains par site.
- `[FAIT]` Horaires annuels par site.
- `[FAIT]` Fermetures globales et locales.
- `[FAIT]` Membres `GLOBAL`, `SITE`, `LIBRE`.
- `[FAIT]` Accès joueur par matricule.
- `[FAIT]` Match privé ou public.
- `[FAIT]` Match = réservation d'un terrain.
- `[FAIT]` 4 joueurs maximum par match.
- `[FAIT]` Durée match = 1h30.
- `[FAIT]` 15 minutes entre deux matches.
- `[FAIT]` Paiement simple.
- `[FAIT]` Dette organisateur si match incomplet ou pas totalement payé.
- `[FAIT]` Blocage nouvelle réservation si dette active.
- `[FAIT]` Blocage nouvelle réservation si pénalité active.
- `[FAIT]` Pénalité simple de 7 jours.
- `[FAIT]` Traitement de veille :
  - match privé incomplet devient public
  - participation non payée libérée
  - pénalité organisateur possible
- `[FAIT]` Statistiques admin.
- `[FAIT]` Authentification joueur par matricule.
- `[FAIT]` Authentification admin simple.
- `[A FAIRE]` Frontend joueur.
- `[A FAIRE]` Frontend admin.

---

## 5. Architecture backend

Structure attendue et utilisée :

- `[FAIT]` `controller`
  - gère HTTP
  - reçoit les DTO request
  - retourne les DTO response
  - ne contient pas la logique métier lourde

- `[FAIT]` `service`
  - contient la logique métier
  - orchestre les repositories
  - applique les règles de réservation, participation, paiement, dette, pénalité, statistiques, authentification

- `[FAIT]` `repository`
  - accès base de données via Spring Data JPA
  - pas de logique métier

- `[FAIT]` `entity`
  - modèle JPA

- `[FAIT]` `dto`
  - objets d'entrée/sortie API

- `[FAIT]` `exception`
  - exceptions métier
  - exceptions ressource introuvable
  - exceptions authentification

- `[FAIT]` `config`
  - Clock
  - OpenAPI Swagger

Package racine :

```txt
com.padelMarius.backend
```

---

## 6. Backend — étapes réalisées

### 6.1. Initialisation backend

Issue :

```txt
[BACK] Initialiser le backend Java et la configuration de base
```

Statut :

- `[FAIT]` Projet Spring Boot initialisé.
- `[FAIT]` Maven Wrapper présent.
- `[FAIT]` H2 configuré pour le MVP.
- `[FAIT]` Endpoint santé créé.
- `[FAIT]` Premiers tests backend créés.
- `[FAIT]` PR mergée.

Endpoint :

```http
GET /api/health
```

Tests :

- `[FAIT]` `BackendApplicationTests`
- `[FAIT]` `HealthControllerTest`

---

### 6.2. Entités JPA coeur et repositories

Issue :

```txt
[BACK] Créer les entités JPA coeur et repositories
```

Branche :

```txt
back/entities-core
```

Statut :

- `[FAIT]` Entités JPA coeur créées.
- `[FAIT]` Repositories coeur créés.
- `[FAIT]` Tests repository créés.
- `[FAIT]` PR mergée.

Entités :

- `[FAIT]` Site
- `[FAIT]` Terrain
- `[FAIT]` Membre
- `[FAIT]` PadelMatch
- `[FAIT]` Participation

Repositories :

- `[FAIT]` SiteRepository
- `[FAIT]` TerrainRepository
- `[FAIT]` MembreRepository
- `[FAIT]` PadelMatchRepository
- `[FAIT]` ParticipationRepository

Test :

- `[FAIT]` `CoreRepositoryTest`

---

### 6.3. Entités JPA complémentaires et repositories

Issue :

```txt
[BACK] Créer les entités JPA complémentaires et repositories
```

Branche :

```txt
back/entities-extra
```

Statut :

- `[FAIT]` Entités JPA complémentaires créées.
- `[FAIT]` Repositories complémentaires créés.
- `[FAIT]` Tests repository créés.
- `[FAIT]` PR mergée.

Entités :

- `[FAIT]` HoraireAnnuelSite
- `[FAIT]` Fermeture
- `[FAIT]` Administrateur
- `[FAIT]` Dette
- `[FAIT]` Penalite
- `[FAIT]` Paiement

Repositories :

- `[FAIT]` HoraireAnnuelSiteRepository
- `[FAIT]` FermetureRepository
- `[FAIT]` AdministrateurRepository
- `[FAIT]` DetteRepository
- `[FAIT]` PenaliteRepository
- `[FAIT]` PaiementRepository

Test :

- `[FAIT]` `ComplementaryRepositoryTest`

---

### 6.4. Consultation des disponibilités

Issue :

```txt
[BACK] Implémenter la consultation des disponibilités
```

Branche :

```txt
back/disponibilites
```

Statut :

- `[FAIT]` Fonctionnalité livrée.
- `[FAIT]` Tests service.
- `[FAIT]` Tests controller.
- `[FAIT]` PR mergée.

Endpoint :

```http
GET /api/disponibilites?siteId=1&date=2026-05-20
```

Règles couvertes :

- `[FAIT]` Horaires annuels du site.
- `[FAIT]` Créneaux de 1h30.
- `[FAIT]` Pause de 15 minutes entre matches.
- `[FAIT]` Terrains actifs.
- `[FAIT]` Matches déjà réservés exclus.
- `[FAIT]` Fermetures locales.
- `[FAIT]` Fermetures globales.
- `[FAIT]` Site introuvable refusé.
- `[FAIT]` Horaire annuel manquant refusé.

Tests :

- `[FAIT]` `DisponibiliteServiceTest`
- `[FAIT]` `DisponibiliteControllerTest`

---

### 6.5. Création de match privé/public

Issue :

```txt
[BACK] Implémenter la création de match privé et public
```

Branche :

```txt
back/create-match
```

Statut :

- `[FAIT]` Fonctionnalité livrée.
- `[FAIT]` Tests service.
- `[FAIT]` Tests controller.
- `[FAIT]` Tests renforcés ensuite.
- `[FAIT]` PR mergée.

Endpoint :

```http
POST /api/matches
```

Règles couvertes :

- `[FAIT]` Création match privé.
- `[FAIT]` Création match public.
- `[FAIT]` Participation organisateur créée automatiquement.
- `[FAIT]` Terrain existant et actif.
- `[FAIT]` Site actif.
- `[FAIT]` Organisateur existant et actif.
- `[FAIT]` Membre `SITE` limité à son site.
- `[FAIT]` Créneau disponible.
- `[FAIT]` Pas de dette active.
- `[FAIT]` Pas de pénalité active.
- `[FAIT]` Pas de conflit horaire pour l'organisateur.
- `[FAIT]` Durée fixe de 1h30.
- `[FAIT]` Prix total fixé à `60.00`.

Tests :

- `[FAIT]` `MatchCreationServiceTest`
- `[FAIT]` `MatchControllerTest`

---

### 6.6. Renforcement des tests de création de match

Issue :

```txt
[TEST] Compléter les tests de création de match
```

Branche :

```txt
test/create-match-edge-cases
```

Statut :

- `[FAIT]` Tests edge cases ajoutés.
- `[FAIT]` Validation HTTP 400 ajoutée.
- `[FAIT]` Tests service renforcés.
- `[FAIT]` PR mergée.

Points couverts :

- `[FAIT]` Terrain inactif refusé.
- `[FAIT]` Site inactif refusé.
- `[FAIT]` Organisateur inactif refusé.
- `[FAIT]` Membre `SITE` hors site refusé.
- `[FAIT]` Dette ouverte refusée.
- `[FAIT]` Pénalité active refusée.
- `[FAIT]` Créneau indisponible refusé.
- `[FAIT]` Conflit de participation refusé.
- `[FAIT]` Requête invalide refusée par le controller.

---

### 6.7. Participations aux matches

Issue :

```txt
[BACK] Implémenter les participations aux matches
```

Branche :

```txt
back/participations
```

Statut :

- `[FAIT]` Fonctionnalité livrée.
- `[FAIT]` Tests service.
- `[FAIT]` Tests controller.
- `[FAIT]` Tests repository.
- `[FAIT]` PR mergée.

Endpoints :

```http
POST /api/matches/{matchId}/participants/prive
POST /api/matches/{matchId}/participants/public
```

Règles couvertes :

- `[FAIT]` Ajouter un joueur à un match privé.
- `[FAIT]` Inscrire un joueur à un match public.
- `[FAIT]` Maximum 4 participants actifs.
- `[FAIT]` Pas de doublon match/membre.
- `[FAIT]` Pas de conflit horaire joueur.
- `[FAIT]` Membre actif obligatoire.
- `[FAIT]` Match à venir obligatoire.
- `[FAIT]` Match privé accepte invitation privée.
- `[FAIT]` Match public accepte inscription publique.
- `[FAIT]` Participation créée en attente de paiement.
- `[FAIT]` Participation libérée prise en compte.

Tests :

- `[FAIT]` `ParticipationServiceTest`
- `[FAIT]` `ParticipationControllerTest`
- `[FAIT]` `ParticipationRepositoryTest`

---

### 6.8. Règles membres et fenêtres de réservation

Statut :

- `[FAIT]` Fonctionnalité livrée.
- `[FAIT]` Tests service.
- `[FAIT]` PR mergée.

Règles couvertes :

- `[FAIT]` Membre `GLOBAL`
  - matricule `Gxxxx`
  - peut réserver 21 jours avant
  - peut réserver sur tous les sites

- `[FAIT]` Membre `SITE`
  - matricule `Sxxxxx`
  - peut réserver 14 jours avant
  - peut réserver uniquement sur son site de rattachement

- `[FAIT]` Membre `LIBRE`
  - matricule `Lxxxxx`
  - peut réserver 5 jours avant
  - peut réserver sur tous les sites

Test :

- `[FAIT]` `ReglesReservationMembreServiceTest`

---

### 6.9. Paiement simple des participations

Issue :

```txt
[BACK] Implémenter le paiement simple des participations
```

Branche :

```txt
back/paiements
```

Statut :

- `[FAIT]` Fonctionnalité livrée.
- `[FAIT]` Tests service.
- `[FAIT]` Tests controller.
- `[FAIT]` Tests repository.
- `[FAIT]` PR mergée.

Endpoint :

```http
POST /api/participations/{participationId}/paiements
```

Règles couvertes :

- `[FAIT]` Montant attendu : `15.00`.
- `[FAIT]` Création d'un paiement de nature `PARTICIPATION`.
- `[FAIT]` Participation passe à `CONFIRMEE`.
- `[FAIT]` Refus double paiement.
- `[FAIT]` Refus montant incorrect.
- `[FAIT]` Refus participation libérée.
- `[FAIT]` Refus participation déjà confirmée.

Tests :

- `[FAIT]` `PaiementServiceTest`
- `[FAIT]` `PaiementControllerTest`
- `[FAIT]` `PaiementRepositoryTest`

---

### 6.10. Dette organisateur

Issue :

```txt
[BACK] Implémenter la dette organisateur
```

Branche :

```txt
back/dettes
```

Statut :

- `[FAIT]` Fonctionnalité livrée.
- `[FAIT]` Tests service.
- `[FAIT]` Tests controller.
- `[FAIT]` Tests repository liés au paiement.
- `[FAIT]` PR mergée.

Endpoints :

```http
POST /api/matches/{matchId}/dettes/generer
GET /api/membres/{matricule}/dettes/ouvertes
POST /api/dettes/{detteId}/paiements
```

Règles couvertes :

- `[FAIT]` Génération d'une dette si un match n'est pas totalement payé.
- `[FAIT]` Calcul dette = prix total match - total paiements participation.
- `[FAIT]` Responsable = organisateur du match.
- `[FAIT]` Consultation des dettes ouvertes d'un membre.
- `[FAIT]` Paiement d'une dette.
- `[FAIT]` Dette passe à `REGLEE`.
- `[FAIT]` Paiement de nature `REGLEMENT_DETTE`.
- `[FAIT]` Blocage création de match si dette ouverte conservé.

Tests :

- `[FAIT]` `DetteServiceTest`
- `[FAIT]` `DetteControllerTest`
- `[FAIT]` `PaiementRepositoryTest`

---

### 6.11. Traitement de veille des matches

Issue :

```txt
[BACK] Implémenter le traitement de veille des matches
```

Branche :

```txt
back/traitement-veille
```

Statut :

- `[FAIT]` Fonctionnalité livrée.
- `[FAIT]` Tests service.
- `[FAIT]` Tests controller.
- `[FAIT]` PR mergée.

Endpoint :

```http
POST /api/admin/matches/traitement-veille?date=2026-05-19
```

Règles couvertes :

- `[FAIT]` Le traitement reçoit la date du jour de traitement.
- `[FAIT]` Il traite les matches du lendemain.
- `[FAIT]` Match privé incomplet à J-1 devient public.
- `[FAIT]` Participation joueur non payée devient `LIBEREE`.
- `[FAIT]` Date de libération renseignée.
- `[FAIT]` Pénalité ACTIVE de 7 jours créée pour l'organisateur responsable.
- `[FAIT]` Match public incomplet ne crée pas de pénalité.
- `[FAIT]` Pénalité déjà existante non recréée.
- `[FAIT]` Match `DEMARRE` ou `TERMINE` ignoré.
- `[FAIT]` `datePassagePublic` conservée si déjà renseignée.

Tests :

- `[FAIT]` `TraitementVeilleServiceTest`
- `[FAIT]` `TraitementVeilleControllerTest`

---

### 6.12. Renforcement des tests du traitement de veille

Issue :

```txt
[TEST] Renforcer les tests du traitement de veille
```

Branche :

```txt
test/traitement-veille-edge-cases
```

Statut :

- `[FAIT]` Test repository réel ajouté pour la recherche des matches par plage de dates.
- `[FAIT]` Test idempotence ajouté : pas de double pénalité.
- `[FAIT]` Test matches `DEMARRE` / `TERMINE` ignorés.
- `[FAIT]` Test conservation de `datePassagePublic`.
- `[FAIT]` Test controller date invalide.
- `[FAIT]` Documentation projet mise à jour.
- `[FAIT]` PR mergée.

Tests :

- `[FAIT]` `TraitementVeilleServiceTest`
- `[FAIT]` `TraitementVeilleControllerTest`
- `[FAIT]` `PadelMatchRepositoryTest`

---

### 6.13. Statistiques backend MVP

Issue :

```txt
[BACK] Ajouter les statistiques backend MVP
```

Branche :

```txt
back/stats-admin
```

Statut :

- `[FAIT]` Endpoint admin de statistiques ajouté.
- `[FAIT]` Statistiques globales sur une période.
- `[FAIT]` Statistiques filtrées par site.
- `[FAIT]` Tests service.
- `[FAIT]` Tests controller.
- `[FAIT]` Tests repository.
- `[FAIT]` PR mergée.

Endpoint global :

```http
GET /api/admin/statistiques?dateDebut=2026-05-01&dateFin=2026-05-31
```

Endpoint avec filtre site :

```http
GET /api/admin/statistiques?dateDebut=2026-05-01&dateFin=2026-05-31&siteId=1
```

Statistiques retournées :

- `[FAIT]` Nombre de matches.
- `[FAIT]` Nombre de matches à venir.
- `[FAIT]` Nombre de matches terminés.
- `[FAIT]` Nombre de paiements.
- `[FAIT]` Chiffre d'affaires.
- `[FAIT]` Nombre de dettes ouvertes.
- `[FAIT]` Montant total des dettes ouvertes.
- `[FAIT]` Nombre de participations actives.
- `[FAIT]` Capacité théorique joueurs.
- `[FAIT]` Taux de remplissage simple.

Tests :

- `[FAIT]` `StatistiquesAdminServiceTest`
- `[FAIT]` `StatistiquesAdminControllerTest`
- `[FAIT]` `StatistiquesRepositoryTest`

---

### 6.14. Authentification simple joueurs/admins

Issue :

```txt
[BACK] Ajouter authentification simple joueurs/admins
```

Branche :

```txt
back/auth-simple
```

Statut :

- `[FAIT]` Authentification joueur par matricule.
- `[FAIT]` Aucun login/mot de passe requis pour les joueurs.
- `[FAIT]` Refus d'un joueur inactif.
- `[FAIT]` Authentification admin par login et mot de passe.
- `[FAIT]` Refus d'un admin inactif.
- `[FAIT]` Retour du rôle administrateur `GLOBAL` ou `SITE`.
- `[FAIT]` Retour du site administrateur si admin de site.
- `[FAIT]` Gestion HTTP 401 pour identifiants admin invalides.
- `[FAIT]` Tests controller.
- `[FAIT]` Tests service.
- `[FAIT]` Tests repository.
- `[FAIT]` PR mergée.

Endpoints :

```http
POST /api/auth/joueur
POST /api/auth/admin
```

Exemple joueur :

```json
{
  "matricule": "G0001"
}
```

Exemple admin :

```json
{
  "login": "admin-global",
  "motDePasse": "secret"
}
```

Tests :

- `[FAIT]` `AuthServiceTest`
- `[FAIT]` `AuthControllerTest`
- `[FAIT]` `AuthRepositoryTest`

---

### 6.15. OpenAPI Swagger

Issue :

```txt
[BACK] Ajouter OpenAPI Swagger
```

Branche :

```txt
back/swagger
```

Statut :

- `[FAIT]` Dépendance `springdoc-openapi-starter-webmvc-ui` ajoutée.
- `[FAIT]` Version Springdoc compatible Spring Boot 4 utilisée.
- `[FAIT]` Configuration OpenAPI ajoutée.
- `[FAIT]` Métadonnées de l'API ajoutées.
- `[FAIT]` Endpoint OpenAPI JSON disponible.
- `[FAIT]` Swagger UI disponible.
- `[FAIT]` Test backend d'accès à `/v3/api-docs`.
- `[FAIT]` Test backend d'accès à Swagger UI.
- `[FAIT]` Test manuel navigateur validé.
- `[FAIT]` PR mergée.

URLs disponibles après démarrage backend :

```http
GET /v3/api-docs
GET /swagger-ui.html
GET /swagger-ui/index.html
```

URL locale principale à montrer au professeur :

```txt
http://localhost:8080/swagger-ui.html
```

Test :

- `[FAIT]` `OpenApiDocumentationTest`

---

### 6.16. Script DB et seed automatisé

Issue :

```txt
[DB] Ajouter script de schéma et seed automatisé
```

Branche :

```txt
db/schema-seed
```

Statut :

- `[FAIT]` Artefact SQL de schéma ajouté.
- `[FAIT]` Script de données de démonstration ajouté.
- `[FAIT]` Seed automatique ajouté au démarrage backend.
- `[FAIT]` Seed désactivé dans les tests standards.
- `[FAIT]` Test dédié ajouté pour valider le script `data.sql`.
- `[FAIT]` Documentation DB ajoutée dans `docs/db/README.md`.
- `[FAIT]` Test manuel via Swagger validé.
- `[FAIT]` PR mergée.

Fichiers ajoutés :

- `[FAIT]` `docs/db/schema.sql`
- `[FAIT]` `docs/db/data-demo.sql`
- `[FAIT]` `docs/db/README.md`
- `[FAIT]` `backend/src/main/resources/data.sql`
- `[FAIT]` `backend/src/test/resources/application.yml`
- `[FAIT]` `backend/src/test/java/com/padelMarius/backend/repository/DemoSeedDataTest.java`

Configuration importante :

```yaml
spring:
  jpa:
    defer-datasource-initialization: true

  sql:
    init:
      mode: always
```

Données utiles pour la démo :

- `[FAIT]` joueur global : `G1001`
- `[FAIT]` joueur avec dette ouverte : `G1002`
- `[FAIT]` joueur inactif : `G9999`
- `[FAIT]` admin global : `admin-global` / `secret`
- `[FAIT]` admin site Bruxelles : `admin-bruxelles` / `secret-site`

Test :

- `[FAIT]` `DemoSeedDataTest`

---

### 6.17. Users DB et droits associés

Issue :

```txt
[DB] Documenter les users DB et leurs droits
```

Branche :

```txt
db/users-droits
```

Statut :

- `[FAIT]` Documentation des users DB ajoutée ou en cours de commit dans la PR courante.
- `[FAIT]` Script H2 démonstratif des users DB ajouté ou en cours de commit dans la PR courante.
- `[FAIT]` Explication du user `sa` H2 local ajoutée.
- `[FAIT]` Explication du user applicatif backend ajoutée.
- `[FAIT]` Explication du user migration ajoutée.
- `[FAIT]` Explication du user lecture seule ajoutée.
- `[FAIT]` Rappel ajouté : le frontend n'accède jamais à la DB.
- `[FAIT]` Rappel ajouté : aucun SQL dans le frontend.
- `[EN COURS]` PR à créer, merger puis nettoyer localement.

Fichiers ajoutés :

- `[FAIT]` `docs/db/db-users.md`
- `[FAIT]` `docs/db/db-users-h2.sql`

Fichiers modifiés :

- `[FAIT]` `docs/db/README.md`
- `[FAIT]` `docs/00-etat-du-projet.md`

Choix documenté :

- `padel_migration` : schéma / migration.
- `padel_app` : backend applicatif, droits CRUD.
- `padel_readonly` : lecture seule.
- `sa` : H2 local MVP uniquement.

Réponse courte pour l'examen :

```txt
Le frontend n'a aucun accès DB.
Le backend est le seul composant qui accède à la DB.
En H2 MVP, sa est utilisé pour automatiser le démarrage.
En configuration cible, le backend utilise padel_app avec droits CRUD uniquement.
Les changements de schéma sont faits par padel_migration.
```

---

## 7. Tests backend actuels

Commande à utiliser depuis le dossier `backend` :

```powershell
.\mvnw.cmd clean test
```

Dernier résultat attendu :

- `[FAIT]` Build Maven : `BUILD SUCCESS`.
- `[FAIT]` Tests backend : OK.
- `[FAIT]` Aucun échec.
- `[FAIT]` Aucune erreur.

Tests controller présents :

- `[FAIT]` `HealthControllerTest`
- `[FAIT]` `DisponibiliteControllerTest`
- `[FAIT]` `MatchControllerTest`
- `[FAIT]` `ParticipationControllerTest`
- `[FAIT]` `PaiementControllerTest`
- `[FAIT]` `DetteControllerTest`
- `[FAIT]` `TraitementVeilleControllerTest`
- `[FAIT]` `StatistiquesAdminControllerTest`
- `[FAIT]` `AuthControllerTest`

Tests service présents :

- `[FAIT]` `DisponibiliteServiceTest`
- `[FAIT]` `MatchCreationServiceTest`
- `[FAIT]` `ParticipationServiceTest`
- `[FAIT]` `ReglesReservationMembreServiceTest`
- `[FAIT]` `PaiementServiceTest`
- `[FAIT]` `DetteServiceTest`
- `[FAIT]` `TraitementVeilleServiceTest`
- `[FAIT]` `StatistiquesAdminServiceTest`
- `[FAIT]` `AuthServiceTest`

Tests repository présents :

- `[FAIT]` `CoreRepositoryTest`
- `[FAIT]` `ComplementaryRepositoryTest`
- `[FAIT]` `ParticipationRepositoryTest`
- `[FAIT]` `PaiementRepositoryTest`
- `[FAIT]` `PadelMatchRepositoryTest`
- `[FAIT]` `StatistiquesRepositoryTest`
- `[FAIT]` `AuthRepositoryTest`
- `[FAIT]` `DemoSeedDataTest`

Tests configuration / documentation technique :

- `[FAIT]` `OpenApiDocumentationTest`

---

## 8. Endpoints backend disponibles

Santé :

```http
GET /api/health
```

Swagger / OpenAPI :

```http
GET /v3/api-docs
GET /swagger-ui.html
GET /swagger-ui/index.html
```

Authentification :

```http
POST /api/auth/joueur
POST /api/auth/admin
```

Disponibilités :

```http
GET /api/disponibilites?siteId=1001&date=2026-06-20
```

Matches :

```http
POST /api/matches
```

Participations :

```http
POST /api/matches/{matchId}/participants/prive
POST /api/matches/{matchId}/participants/public
```

Paiements :

```http
POST /api/participations/{participationId}/paiements
```

Dettes :

```http
POST /api/matches/{matchId}/dettes/generer
GET /api/membres/{matricule}/dettes/ouvertes
POST /api/dettes/{detteId}/paiements
```

Traitement admin :

```http
POST /api/admin/matches/traitement-veille?date=2026-05-19
```

Statistiques admin :

```http
GET /api/admin/statistiques?dateDebut=2026-05-01&dateFin=2026-06-30
GET /api/admin/statistiques?dateDebut=2026-05-01&dateFin=2026-06-30&siteId=1001
```

---

## 9. Base de données et artefacts DB

Base MVP :

- `[FAIT]` H2 en mémoire.
- `[FAIT]` URL : `jdbc:h2:mem:padeldb`.
- `[FAIT]` User local MVP : `sa`.
- `[FAIT]` Password local MVP : vide.
- `[FAIT]` Création tables par JPA/Hibernate.
- `[FAIT]` Seed automatique via `data.sql`.

Fichiers DB :

- `[FAIT]` `docs/db/schema.sql`
- `[FAIT]` `docs/db/data-demo.sql`
- `[FAIT]` `docs/db/README.md`
- `[FAIT]` `docs/db/db-users.md`
- `[FAIT]` `docs/db/db-users-h2.sql`
- `[FAIT]` `backend/src/main/resources/data.sql`

Users DB documentés :

- `[FAIT]` `padel_migration`
  - rôle : migration / schéma
  - droits : DDL
  - utilisé uniquement pour créer ou modifier le schéma

- `[FAIT]` `padel_app`
  - rôle : user applicatif backend
  - droits : CRUD sur les tables métier
  - pas de droits DDL

- `[FAIT]` `padel_readonly`
  - rôle : lecture seule
  - droits : SELECT uniquement

- `[FAIT]` `sa`
  - rôle : H2 local MVP uniquement
  - acceptable pour démo locale in-memory
  - non recommandé pour base persistante / production

---

## 10. Roadmap restante — ordre à suivre

### Prochaine étape immédiate

Si la PR actuelle DB users/droits n'est pas encore terminée :

- `[EN COURS]` Lancer les tests backend.
- `[EN COURS]` Commit.
- `[EN COURS]` Push.
- `[EN COURS]` Créer la PR.
- `[EN COURS]` Vérifier les fichiers changés.
- `[EN COURS]` Merger.
- `[EN COURS]` Nettoyer localement.

Si la PR actuelle est mergée :

- `[A FAIRE]` Commencer l'issue suivante :

```txt
[FRONT] Initialiser Angular et la structure frontend
```

Branche :

```txt
front/init-angular
```

Commit prévu :

```txt
feat(front): initialize angular frontend structure
```

PR prévue :

```txt
[FRONT] Initialiser Angular et la structure frontend
```

---

### Étapes backend restantes

- `[FAIT]` Ajouter les statistiques backend MVP.
- `[FAIT]` Ajouter authentification simple joueurs/admins.
- `[FAIT]` Ajouter OpenAPI / Swagger.
- `[FAIT]` Ajouter script de schéma ou artefact DB.
- `[FAIT]` Ajouter seed de données de démonstration.
- `[FAIT]` Documenter les users DB et leurs droits.
- `[A SURVEILLER]` Vérifier ou nettoyer les incohérences de configuration.
- `[A FAIRE]` Préparer scénario de démo backend.
- `[A FAIRE]` Préparer documentation finale de remise.

---

### Étapes frontend restantes

- `[A FAIRE]` Initialiser Angular.
- `[A FAIRE]` Créer structure frontend simple.
- `[A FAIRE]` Configurer routing.
- `[A FAIRE]` Configurer `HttpClient`.
- `[A FAIRE]` Créer services Angular pour appels API.
- `[A FAIRE]` Créer modèles TypeScript.
- `[A FAIRE]` Créer page accueil.
- `[A FAIRE]` Créer accès joueur par matricule.
- `[A FAIRE]` Créer consultation disponibilités.
- `[A FAIRE]` Créer création de match.
- `[A FAIRE]` Créer liste matches publics.
- `[A FAIRE]` Créer mes réservations.
- `[A FAIRE]` Créer mes dettes.
- `[A FAIRE]` Créer login admin simple.
- `[A FAIRE]` Créer dashboard admin simple.
- `[A FAIRE]` Créer écran traitement de veille admin.
- `[A FAIRE]` Créer écran statistiques admin.
- `[A FAIRE]` Afficher erreurs API clairement.
- `[A FAIRE]` Garder interface simple, lisible, démontrable.

---

### Documentation restante

- `[A FAIRE]` `README.md`
- `[A FAIRE]` `ARCHITECTURE.md`
- `[A FAIRE]` `EXPLOITATION.md`
- `[A FAIRE]` `DEMO.md`
- `[FAIT]` `docs/db/schema.sql`
- `[FAIT]` `docs/db/data-demo.sql`
- `[FAIT]` `docs/db/db-users.md`
- `[FAIT]` `docs/db/db-users-h2.sql`

Contenu attendu dans la documentation finale :

- `[A FAIRE]` Architecture backend.
- `[A FAIRE]` Architecture frontend.
- `[A FAIRE]` Séparation controller / service / repository.
- `[A FAIRE]` Outils et frameworks utilisés.
- `[A FAIRE]` URL Swagger.
- `[A FAIRE]` Commande pour lancer backend.
- `[A FAIRE]` Commande pour lancer frontend.
- `[A FAIRE]` Commande pour lancer tests backend.
- `[A FAIRE]` Commande pour lancer tests frontend si présents.
- `[A FAIRE]` Informations DB.
- `[A FAIRE]` Credentials H2 / demo.
- `[A FAIRE]` Données de démonstration.
- `[A FAIRE]` Scénario de démo 5 à 10 minutes.

---

## 11. Planning sprint final synthétique

- `[FAIT]` 01 — Initialiser backend.
- `[FAIT]` 02 — Créer entités JPA coeur + repositories.
- `[FAIT]` 03 — Créer entités JPA complémentaires + repositories.
- `[FAIT]` 04 — Implémenter disponibilités.
- `[FAIT]` 05 — Implémenter création match privé/public.
- `[FAIT]` 06 — Renforcer tests création match.
- `[FAIT]` 07 — Implémenter participations.
- `[FAIT]` 08 — Renforcer tests participations.
- `[FAIT]` 09 — Implémenter règles membres et fenêtres de réservation.
- `[FAIT]` 10 — Implémenter paiement simple des participations.
- `[FAIT]` 11 — Implémenter dette organisateur.
- `[FAIT]` 12 — Implémenter traitement de veille des matches.
- `[FAIT]` 13 — Renforcer tests traitement de veille.
- `[FAIT]` 14 — Ajouter statistiques backend MVP.
- `[FAIT]` 15 — Ajouter authentification simple joueurs/admins.
- `[FAIT]` 16 — Ajouter OpenAPI Swagger.
- `[FAIT]` 17 — Ajouter script DB / schéma / seed.
- `[FAIT]` 18 — Documenter users DB et droits.
- `[A FAIRE]` 19 — Initialiser frontend Angular.
- `[A FAIRE]` 20 — Implémenter espace joueur MVP.
- `[A FAIRE]` 21 — Implémenter vue admin MVP.
- `[A FAIRE]` 22 — Stabiliser le MVP final.
- `[A FAIRE]` 23 — Préparer documentation de remise.
- `[A FAIRE]` 24 — Préparer démo finale.

---

## 12. Points techniques à surveiller

- `[A SURVEILLER]` Le warning Java agent pendant les tests n'est pas bloquant si `BUILD SUCCESS`.
- `[A SURVEILLER]` Vérifier cohérence entre `application.yml` et `application.properties` si les deux existent.
- `[A SURVEILLER]` Vérifier que la version Java utilisée localement est compatible avec le projet.
- `[A SURVEILLER]` Le package `com.padelMarius.backend` fonctionne, même si la majuscule n'est pas conventionnelle en Java.
- `[A SURVEILLER]` Ne pas ajouter `backend/target/`.
- `[A SURVEILLER]` Ne pas ajouter `.idea/` ou fichiers IDE.
- `[A SURVEILLER]` Ne pas mélanger nettoyage Git avec une feature métier.
- `[A SURVEILLER]` Ne pas modifier plusieurs sujets dans une même PR.
- `[A SURVEILLER]` Garder les PR petites et lisibles.
- `[A SURVEILLER]` Garder les tests backend passants après chaque merge.
- `[A SURVEILLER]` Le user H2 `sa` est acceptable pour MVP local, mais pas comme choix cible de production.
- `[A SURVEILLER]` Les mots de passe admin du seed sont des mots de passe de démonstration.
- `[A SURVEILLER]` Le frontend doit rester simple et ne jamais contenir de SQL.

---

## 13. Règles GitHub à suivre

Pour chaque nouvelle fonctionnalité :

1. Créer une issue.
2. Créer une branche depuis `main`.
3. Coder uniquement le périmètre de l'issue.
4. Ajouter les tests backend nécessaires si l'étape touche le backend.
5. Lancer les tests.
6. Commit.
7. Push.
8. Créer une PR.
9. Vérifier les fichiers changés.
10. Merger.
11. Supprimer la branche distante.
12. Revenir sur `main`.
13. Pull.
14. Lancer les tests.
15. Supprimer la branche locale.
16. Vérifier que le dépôt est propre.

Commandes de base :

```powershell
git checkout main
git pull origin main
git status --short
cd backend
.\mvnw.cmd clean test
cd ..
```

Règle de suppression de branche locale :

```powershell
git branch -d nom-de-branche
git fetch --prune
```

---

## 14. Prochaine action concrète

### Si la PR actuelle DB users/droits n'est pas encore terminée

Issue :

```txt
[DB] Documenter les users DB et leurs droits
```

Branche :

```txt
db/users-droits
```

Commit prévu :

```txt
docs(db): document database users and permissions
```

PR prévue :

```txt
[DB] Documenter les users DB et leurs droits
```

À faire :

- `[EN COURS]` Vérifier le contenu de `docs/db/db-users.md`.
- `[EN COURS]` Vérifier le contenu de `docs/db/db-users-h2.sql`.
- `[EN COURS]` Mettre à jour `docs/db/README.md`.
- `[EN COURS]` Remplacer entièrement ce fichier `docs/00-etat-du-projet.md`.
- `[EN COURS]` Lancer les tests backend.
- `[EN COURS]` Commit.
- `[EN COURS]` Push.
- `[EN COURS]` Créer la PR.
- `[EN COURS]` Merger.
- `[EN COURS]` Nettoyer localement.

Commande de validation :

```powershell
cd backend
.\mvnw.cmd clean test
cd ..
```

### Si la PR actuelle est mergée

Commencer l'issue suivante :

```txt
[FRONT] Initialiser Angular et la structure frontend
```

Branche :

```txt
front/init-angular
```

Commit prévu :

```txt
feat(front): initialize angular frontend structure
```

PR prévue :

```txt
[FRONT] Initialiser Angular et la structure frontend
```

---

## 15. Scénario de démo cible

Démo métier courte :

1. `[A FAIRE]` Démarrer backend.
2. `[A FAIRE]` Montrer `/api/health`.
3. `[A FAIRE]` Montrer Swagger.
4. `[A FAIRE]` Se connecter comme joueur avec `G1001`.
5. `[A FAIRE]` Se connecter comme admin avec `admin-global` / `secret`.
6. `[A FAIRE]` Consulter les disponibilités.
7. `[A FAIRE]` Créer un match public ou privé.
8. `[A FAIRE]` Ajouter un participant.
9. `[A FAIRE]` Payer une participation.
10. `[A FAIRE]` Générer une dette si match non payé entièrement.
11. `[A FAIRE]` Montrer que la dette bloque une nouvelle réservation.
12. `[A FAIRE]` Payer la dette.
13. `[A FAIRE]` Lancer le traitement de veille.
14. `[A FAIRE]` Montrer les statistiques admin.
15. `[A FAIRE]` Montrer les fichiers DB :
  - `schema.sql`
  - `data-demo.sql`
  - `db-users.md`
16. `[A FAIRE]` Expliquer que le frontend n'a aucun accès DB.
17. `[A FAIRE]` Montrer GitHub : issues, branches, commits, PR.
18. `[A FAIRE]` Montrer les tests backend qui passent.
19. `[A FAIRE]` Montrer architecture et exploitation.

---

## 16. Données de démonstration utiles

Joueurs :

- `G1001` : membre GLOBAL actif.
- `G1002` : membre GLOBAL actif avec dette ouverte.
- `S1001` : membre SITE rattaché à Bruxelles.
- `S1002` : membre SITE rattaché à Namur.
- `L1001` : membre LIBRE actif.
- `L1002` : membre LIBRE actif avec pénalité active.
- `G9999` : membre inactif.

Admins :

- `admin-global` / `secret`
  - rôle : GLOBAL

- `admin-bruxelles` / `secret-site`
  - rôle : SITE
  - site : Bruxelles

Sites :

- `BRU` : Padel Bruxelles.
- `NAM` : Padel Namur.

Endpoints rapides pour démo :

```http
GET /api/health
GET /swagger-ui.html
POST /api/auth/joueur
POST /api/auth/admin
GET /api/disponibilites?siteId=1001&date=2026-06-20
GET /api/admin/statistiques?dateDebut=2026-05-01&dateFin=2026-06-30
```

---

## 17. Dernier point d'arrêt

Dernier état connu :

- Backend métier très avancé.
- Tests backend complets sur les couches principales.
- Dette organisateur terminée.
- Traitement de veille terminé.
- Statistiques admin terminées.
- Authentification simple joueurs/admins terminée.
- Swagger/OpenAPI terminé.
- Script DB / seed automatisé terminé.
- Documentation users DB / droits en cours de finalisation dans la PR courante.
- Prochaine vraie étape après merge : initialiser le frontend Angular.

Commande de validation backend :

```powershell
cd backend
.\mvnw.cmd clean test
cd ..
```

Résultat attendu :

```txt
BUILD SUCCESS
```

Objectif suivant :

```txt
[FRONT] Initialiser Angular et la structure frontend
```