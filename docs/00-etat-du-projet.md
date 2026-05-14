# État du projet — Padel Marius

Dernière mise à jour : 2026-05-14  
Document de suivi rapide pour reprise par IA / Codex / développeur.

Objectif du fichier : suivre clairement ce qui est fait, ce qui reste à faire, les limites connues, les PR importantes et le prochain pas concret.  
À chaque PR mergée, mettre à jour ce fichier en déplaçant l'étape concernée vers `[FAIT]`.

---

## 1. Statuts utilisés

- `[FAIT]` : terminé, testé, mergé ou prêt à être considéré comme livré.
- `[EN COURS]` : issue ouverte, branche active ou PR non mergée.
- `[A FAIRE]` : pas encore commencé.
- `[A SURVEILLER]` : point technique connu, non bloquant pour le MVP.
- `[A RESPECTER]` : exigence professeur ou règle projet permanente.
- `[LIMITE MVP]` : fonctionnalité volontairement simplifiée pour garder un MVP stable.

---

## 2. Résumé projet

Application web de réservation de terrains de padel.

Stack projet :

- `[FAIT]` Backend : Java avec Spring Boot.
- `[FAIT]` Build backend : Maven Wrapper.
- `[FAIT]` Base de données : SQL relationnelle, H2 en mémoire pour le MVP.
- `[FAIT]` Frontend : Angular.
- `[FAIT]` Communication : frontend vers backend uniquement via API HTTP REST.
- `[A RESPECTER]` Le frontend ne doit jamais contenir de SQL.
- `[A RESPECTER]` Le frontend ne doit jamais accéder directement à la base de données.
- `[A RESPECTER]` Le backend est le seul composant qui accède à la DB.

Priorité actuelle :

1. `[FAIT]` Backend fonctionnel.
2. `[FAIT]` Tests backend visibles.
3. `[FAIT]` Réservation / paiement / dette / statistiques.
4. `[FAIT]` Frontend simple et démontrable.
5. `[FAIT]` Inscription joueur.
6. `[FAIT]` Gestion admin des jours de fermeture.
7. `[FAIT]` Solde crédit joueur côté backend.
8. `[FAIT]` Affichage du solde crédit joueur côté frontend.
9. `[FAIT]` Matches publics listables et rejoignables avec paiement.
10. `[FAIT]` Mes réservations joueur.
11. `[FAIT]` Historique des transactions joueur.
12. `[EN COURS]` Stabilisation finale.
13. `[A FAIRE]` Documentation finale de remise.
14. `[A FAIRE]` Préparation de la démo finale.

---

## 3. Exigences professeur à respecter

- `[A RESPECTER]` GitHub public avec issues, branches, commits et PR.
- `[A RESPECTER]` Une branche par issue.
- `[A RESPECTER]` Commits courts, réguliers et cohérents.
- `[A RESPECTER]` Backend sous forme de REST API.
- `[A RESPECTER]` Séparation claire controller / service / repository.
- `[A RESPECTER]` Tests backend obligatoires :
  - controllers ;
  - services ;
  - repositories.
- `[A RESPECTER]` Frontend Angular séparé du backend.
- `[A RESPECTER]` Aucun SQL dans Angular.
- `[A RESPECTER]` Aucun accès DB direct depuis Angular.
- `[FAIT]` Accès joueur par matricule.
- `[FAIT]` Inscription joueur avec génération automatique du matricule.
- `[FAIT]` Authentification admin simple avec rôles :
  - GLOBAL ;
  - SITE.
- `[FAIT]` Script DB ou artefact de schéma prévu.
- `[FAIT]` Seed automatique H2 au démarrage backend.
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

Point de modélisation important :

- `[FAIT]` Le portefeuille virtuel est modélisé simplement par le champ `soldeCredit` du membre.
- `[LIMITE MVP]` Il n'y a pas de table séparée `credit_portefeuille` dans le modèle relationnel actuel.
- `[FAIT]` Le solde initial d'un nouveau joueur est `100.00`.

Règles métier principales :

- `[FAIT]` Multi-sites.
- `[FAIT]` Terrains par site.
- `[FAIT]` Horaires annuels par site.
- `[FAIT]` Fermetures globales et locales.
- `[FAIT]` Membres `GLOBAL`, `SITE`, `LIBRE`.
- `[FAIT]` Matricules :
  - `GLOBAL` -> `Gxxxx`
  - `SITE` -> `Sxxxx`
  - `LIBRE` -> `Lxxxx`
- `[FAIT]` Accès joueur par matricule.
- `[FAIT]` Inscription d'un nouveau joueur.
- `[FAIT]` Match privé ou public.
- `[FAIT]` Match = réservation d'un terrain.
- `[FAIT]` 4 joueurs maximum par match.
- `[FAIT]` Durée match = 1h30.
- `[FAIT]` 15 minutes entre deux matches.
- `[FAIT]` Paiement simple.
- `[FAIT]` Paiement par débit du solde crédit joueur.
- `[FAIT]` Dette organisateur si match incomplet ou pas totalement payé.
- `[FAIT]` Création d'une dette initiale pour l'organisateur lors de la création d'un match.
- `[FAIT]` Recalcul de la dette après paiement des participations.
- `[FAIT]` Blocage nouvelle réservation si dette active ou match organisé non totalement payé.
- `[FAIT]` Paiement d'une participation qui ajoute aussi les dettes ouvertes du joueur si nécessaire.
- `[FAIT]` Blocage nouvelle réservation si pénalité active.
- `[FAIT]` Pénalité simple de 7 jours.
- `[FAIT]` Traitement de veille :
  - match privé incomplet devient public ;
  - participation non payée libérée ;
  - pénalité organisateur possible.
- `[FAIT]` Traitement d'échéance des matches.
- `[FAIT]` Statistiques admin.
- `[FAIT]` Authentification joueur par matricule.
- `[FAIT]` Authentification admin simple.
- `[FAIT]` Gestion admin des jours de fermeture.
- `[FAIT]` Annulation des matches à venir concernés par une fermeture.
- `[FAIT]` Remboursement du solde crédit des joueurs ayant payé un match annulé par fermeture.
- `[FAIT]` Liste des matches publics disponibles.
- `[FAIT]` Rejoindre un match public et payer en une action.
- `[FAIT]` Consultation des réservations du joueur connecté.
- `[FAIT]` Consultation de l'historique des paiements du joueur connecté.
- `[FAIT]` Refus de création d'un match dans le passé ou à l'heure courante.

---

## 5. Architecture backend

Structure attendue et utilisée :

- `[FAIT]` `controller` : gère HTTP, reçoit les DTO request, retourne les DTO response, ne contient pas la logique métier lourde.
- `[FAIT]` `service` : contient la logique métier et orchestre les repositories.
- `[FAIT]` `repository` : accès base de données via Spring Data JPA, pas de logique métier.
- `[FAIT]` `entity` : modèle JPA.
- `[FAIT]` `dto` : objets d'entrée/sortie API.
- `[FAIT]` `exception` : exceptions métier, ressource introuvable, authentification.
- `[FAIT]` `config` : Clock et OpenAPI Swagger.

Package racine :

```txt
com.padelMarius.backend
```

---

## 6. Architecture frontend

Structure Angular utilisée :

- `[FAIT]` `models` : interfaces TypeScript des objets API.
- `[FAIT]` `services` : appels HTTP vers le backend.
- `[FAIT]` `pages` : composants standalone par page.
- `[FAIT]` `app.routes.ts` : routes Angular.
- `[FAIT]` `app.html` : menu principal et layout.
- `[FAIT]` `shared/api-error.util.ts` : extraction lisible des erreurs API.
- `[FAIT]` `services/auth-context.service.ts` : conservation simple de la session joueur/admin côté frontend.

Règles respectées :

- `[A RESPECTER]` Aucun SQL dans Angular.
- `[A RESPECTER]` Aucun accès DB direct.
- `[A RESPECTER]` Angular appelle uniquement les endpoints REST.
- `[A RESPECTER]` La logique métier reste côté backend.
- `[A RESPECTER]` Le frontend reste simple et démontrable.

---

## 7. Backend — étapes réalisées

### 7.1. Initialisation backend

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

### 7.2. Entités JPA coeur et repositories

Issue :

```txt
[BACK] Créer les entités JPA coeur et repositories
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

### 7.3. Entités JPA complémentaires et repositories

Issue :

```txt
[BACK] Créer les entités JPA complémentaires et repositories
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

### 7.4. Consultation des disponibilités

Issue :

```txt
[BACK] Implémenter la consultation des disponibilités
```

Statut :

- `[FAIT]` Fonctionnalité livrée.
- `[FAIT]` Tests service.
- `[FAIT]` Tests controller.
- `[FAIT]` PR mergée.

Endpoint :

```http
GET /api/disponibilites?siteId=1001&date=2026-06-20
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

### 7.5. Création de match privé/public

Issue :

```txt
[BACK] Implémenter la création de match privé et public
```

Statut :

- `[FAIT]` Fonctionnalité livrée.
- `[FAIT]` Tests service.
- `[FAIT]` Tests controller.
- `[FAIT]` Tests renforcés ensuite.
- `[FAIT]` PR mergée.
- `[FAIT]` Refus de création dans le passé ajouté ensuite.
- `[FAIT]` Création d'une dette initiale organisateur ajoutée ensuite.

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
- `[FAIT]` Pas de match organisé non totalement payé.
- `[FAIT]` Pas de conflit horaire pour l'organisateur.
- `[FAIT]` Durée fixe de 1h30.
- `[FAIT]` Prix total fixé à `60.00`.

Tests :

- `[FAIT]` `MatchCreationServiceTest`
- `[FAIT]` `MatchControllerTest`

---

### 7.6. Participations aux matches

Issue :

```txt
[BACK] Implémenter les participations aux matches
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

### 7.7. Règles membres et fenêtres de réservation

Statut :

- `[FAIT]` Fonctionnalité livrée.
- `[FAIT]` Tests service.
- `[FAIT]` PR mergée.

Règles couvertes :

- `[FAIT]` Membre `GLOBAL` : matricule `Gxxxx`, peut réserver 21 jours avant, tous sites.
- `[FAIT]` Membre `SITE` : matricule `Sxxxxx`, peut réserver 14 jours avant, uniquement son site.
- `[FAIT]` Membre `LIBRE` : matricule `Lxxxxx`, peut réserver 5 jours avant, tous sites.

Test :

- `[FAIT]` `ReglesReservationMembreServiceTest`

---

### 7.8. Paiement simple des participations

Issue :

```txt
[BACK] Implémenter le paiement simple des participations
```

Statut :

- `[FAIT]` Fonctionnalité livrée.
- `[FAIT]` Tests service.
- `[FAIT]` Tests controller.
- `[FAIT]` Tests repository.
- `[FAIT]` PR mergée.
- `[FAIT]` Paiement ensuite relié au solde crédit joueur.
- `[FAIT]` Paiement ensuite enrichi pour régler aussi les dettes ouvertes du joueur.

Endpoint :

```http
POST /api/participations/{participationId}/paiements
```

Règles couvertes :

- `[FAIT]` Montant attendu : `15.00`.
- `[FAIT]` Création d'un paiement de nature `PARTICIPATION`.
- `[FAIT]` Participation passe à `CONFIRMEE`.
- `[FAIT]` Solde crédit débité.
- `[FAIT]` Dettes ouvertes du joueur réglées si nécessaire.
- `[FAIT]` Refus double paiement.
- `[FAIT]` Refus montant incorrect.
- `[FAIT]` Refus participation libérée.
- `[FAIT]` Refus participation déjà confirmée.
- `[FAIT]` Refus si solde insuffisant.

Tests :

- `[FAIT]` `PaiementServiceTest`
- `[FAIT]` `PaiementControllerTest`
- `[FAIT]` `PaiementRepositoryTest`

---

### 7.9. Dette organisateur

Issue :

```txt
[BACK] Implémenter la dette organisateur
```

Statut :

- `[FAIT]` Fonctionnalité livrée.
- `[FAIT]` Tests service.
- `[FAIT]` Tests controller.
- `[FAIT]` Tests repository liés au paiement.
- `[FAIT]` PR mergée.
- `[FAIT]` Dette initiale organisateur ajoutée ensuite.
- `[FAIT]` Recalcul de dette après paiement ajouté ensuite.

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
- `[FAIT]` Paiement de dette par débit du solde crédit.

Tests :

- `[FAIT]` `DetteServiceTest`
- `[FAIT]` `DetteControllerTest`
- `[FAIT]` `PaiementRepositoryTest`

---

### 7.10. Traitement de veille des matches

Issue :

```txt
[BACK] Implémenter le traitement de veille des matches
```

Statut :

- `[FAIT]` Fonctionnalité livrée.
- `[FAIT]` Tests service.
- `[FAIT]` Tests controller.
- `[FAIT]` Tests repository renforcés.
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
- `[FAIT]` `PadelMatchRepositoryTest`

---

### 7.11. Statistiques backend MVP

Issue :

```txt
[BACK] Ajouter les statistiques backend MVP
```

Statut :

- `[FAIT]` Endpoint admin de statistiques ajouté.
- `[FAIT]` Statistiques globales sur une période.
- `[FAIT]` Statistiques filtrées par site.
- `[FAIT]` Tests service.
- `[FAIT]` Tests controller.
- `[FAIT]` Tests repository.
- `[FAIT]` PR mergée.

Endpoints :

```http
GET /api/admin/statistiques?dateDebut=2026-05-01&dateFin=2026-06-30
GET /api/admin/statistiques?dateDebut=2026-05-01&dateFin=2026-06-30&siteId=1001
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

### 7.12. Authentification simple joueurs/admins

Issue :

```txt
[BACK] Ajouter authentification simple joueurs/admins
```

Statut :

- `[FAIT]` Authentification joueur par matricule.
- `[FAIT]` Aucun login/mot de passe requis pour les joueurs.
- `[FAIT]` Refus d'un joueur inactif.
- `[FAIT]` Authentification admin par login et mot de passe.
- `[FAIT]` Refus d'un admin inactif.
- `[FAIT]` Retour du rôle administrateur `GLOBAL` ou `SITE`.
- `[FAIT]` Retour du site administrateur si admin de site.
- `[FAIT]` Retour du solde crédit dans la réponse joueur.
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

Tests :

- `[FAIT]` `AuthServiceTest`
- `[FAIT]` `AuthControllerTest`
- `[FAIT]` `AuthRepositoryTest`

---

### 7.13. OpenAPI Swagger

Issue :

```txt
[BACK] Ajouter OpenAPI Swagger
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

### 7.14. Script DB et seed automatisé

Issue :

```txt
[DB] Ajouter script de schéma et seed automatisé
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
- `[FAIT]` Schéma mis à jour avec `membre.solde_credit`.

Fichiers DB :

- `[FAIT]` `docs/db/schema.sql`
- `[FAIT]` `docs/db/data-demo.sql`
- `[FAIT]` `docs/db/README.md`
- `[FAIT]` `docs/db/db-users.md`
- `[FAIT]` `docs/db/db-users-h2.sql`
- `[FAIT]` `backend/src/main/resources/data.sql`

Configuration H2 :

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:padeldb
    username: sa
    password:

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

### 7.15. Users DB et droits associés

Issue :

```txt
[DB] Documenter les users DB et leurs droits
```

Statut :

- `[FAIT]` Documentation des users DB ajoutée.
- `[FAIT]` Script H2 démonstratif des users DB ajouté.
- `[FAIT]` Explication du user `sa` H2 local ajoutée.
- `[FAIT]` Explication du user applicatif backend ajoutée.
- `[FAIT]` Explication du user migration ajoutée.
- `[FAIT]` Explication du user lecture seule ajoutée.
- `[FAIT]` Rappel ajouté : le frontend n'accède jamais à la DB.
- `[FAIT]` Rappel ajouté : aucun SQL dans le frontend.
- `[FAIT]` PR mergée.

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

### 7.16. Inscription joueur

Issue initiale :

```txt
[FULLSTACK] Ajouter inscription joueur
```

Statut :

- `[FAIT]` Backend inscription joueur.
- `[FAIT]` Frontend inscription joueur.
- `[FAIT]` Tests backend.
- `[FAIT]` PR mergée.
- `[FAIT]` Nouveau joueur créé avec `soldeCredit = 100.00`.

Endpoint :

```http
POST /api/membres/inscription
```

Règles couvertes :

- `[FAIT]` Création d’un membre `GLOBAL`.
- `[FAIT]` Création d’un membre `SITE`.
- `[FAIT]` Création d’un membre `LIBRE`.
- `[FAIT]` Génération automatique du matricule côté backend.
- `[FAIT]` Préfixe `G`, `S` ou `L` selon catégorie.
- `[FAIT]` Le joueur créé est actif par défaut.
- `[FAIT]` Le joueur créé reçoit un solde crédit initial.
- `[FAIT]` Un membre `SITE` doit avoir un site de rattachement.
- `[FAIT]` Un site inexistant est refusé.
- `[FAIT]` Le frontend affiche le matricule généré.
- `[FAIT]` Le joueur peut ensuite se connecter via son matricule.

Tests :

- `[FAIT]` `MembreInscriptionServiceTest`
- `[FAIT]` `MembreControllerTest`
- `[FAIT]` `MembreRepositoryTest`

Frontend :

- `[FAIT]` Page `Inscription joueur`.
- `[FAIT]` Route `/inscription-joueur`.
- `[FAIT]` Service Angular `MembreApiService`.
- `[FAIT]` Modèle Angular `membre.model.ts`.
- `[FAIT]` Lien de menu `Inscription joueur`.

---

### 7.17. Gestion admin des jours de fermeture

Issue :

```txt
[FULLSTACK] Ajouter la gestion admin des jours de fermeture
```

PR :

```txt
#83 [FULLSTACK] Ajouter la gestion admin des jours de fermeture
```

Statut :

- `[FAIT]` Backend fermeture admin.
- `[FAIT]` Frontend fermeture admin.
- `[FAIT]` Tests backend.
- `[FAIT]` PR #83 mergée.
- `[FAIT]` Remboursement du solde crédit ajouté ensuite.

Endpoint :

```http
POST /api/admin/fermetures
```

Règles couvertes :

- `[FAIT]` Création d’une fermeture globale.
- `[FAIT]` Création d’une fermeture locale.
- `[FAIT]` Une fermeture globale ne doit pas avoir de site.
- `[FAIT]` Une fermeture locale doit avoir un site.
- `[FAIT]` Refus des doublons de fermeture.
- `[FAIT]` Ajout de l’état de match `ANNULE`.
- `[FAIT]` Annulation des matches à venir concernés par la fermeture.
- `[FAIT]` Les disponibilités sont bloquées par les fermetures via la logique existante.
- `[FAIT]` Frontend admin pour encoder une fermeture.
- `[FAIT]` Affichage du nombre de matches annulés.
- `[FAIT]` Remboursement des participations déjà payées.
- `[FAIT]` Affichage du nombre de remboursements crédités.
- `[FAIT]` Affichage du montant total remboursé.

Tests :

- `[FAIT]` `AdminFermetureServiceTest`
- `[FAIT]` `AdminFermetureControllerTest`
- `[FAIT]` Renforcement de `PadelMatchRepositoryTest`

Frontend :

- `[FAIT]` Page `Jours de fermeture`.
- `[FAIT]` Route `/admin/fermetures`.
- `[FAIT]` Service Angular `AdminFermetureApiService`.
- `[FAIT]` Modèle Angular `fermeture.model.ts`.
- `[FAIT]` Lien de menu admin `Jours de fermeture`.

---

### 7.18. Solde crédit joueur

Issue :

```txt
#84 [BACK] Ajouter le solde joueur et le paiement par crédit
```

PR :

```txt
#86 [BACK] Ajouter le solde joueur et le paiement par crédit
```

Statut :

- `[FAIT]` Backend solde crédit livré.
- `[FAIT]` Tests backend.
- `[FAIT]` PR #86 mergée.

Endpoint :

```http
GET /api/membres/{matricule}/solde
```

Règles couvertes :

- `[FAIT]` Ajout du champ `soldeCredit` sur `Membre`.
- `[FAIT]` Solde initial de `100.00`.
- `[FAIT]` Solde retourné dans les réponses joueur.
- `[FAIT]` Débit du solde lors du paiement d'une participation.
- `[FAIT]` Débit du solde lors du paiement d'une dette.
- `[FAIT]` Remboursement du solde lors d'une fermeture qui annule un match payé.
- `[FAIT]` Refus si solde insuffisant.
- `[FAIT]` Mise à jour de `data.sql`.
- `[FAIT]` Mise à jour de `docs/db/schema.sql`.

Tests :

- `[FAIT]` `MembreSoldeServiceTest`
- `[FAIT]` `MembreControllerTest`
- `[FAIT]` tests paiement/dette/fermeture renforcés.

---

### 7.19. Frontend solde crédit joueur

Issue :

```txt
#85 [FRONT] Afficher le solde crédit joueur
```

PR :

```txt
#87 [FRONT] Afficher le solde crédit joueur
```

Statut :

- `[FAIT]` Frontend solde livré.
- `[FAIT]` Build frontend validé.
- `[FAIT]` PR #87 mergée.

Frontend :

- `[FAIT]` Modèle `solde-joueur.model.ts`.
- `[FAIT]` Service `SoldeJoueurApiService`.
- `[FAIT]` Page `Mon solde`.
- `[FAIT]` Route `/joueur/mon-solde`.
- `[FAIT]` Lien `Mon solde` dans le menu joueur.
- `[FAIT]` Utilisation du joueur connecté via `AuthContextService`.
- `[FAIT]` Aucun champ libre pour consulter le solde d'un autre joueur.
- `[FAIT]` Rappel des règles financières dans la page.

---

### 7.20. Matches publics listables et rejoignables

Issue / PR :

```txt
#91 [FULLSTACK] Lister et rejoindre les matches publics
```

Statut :

- `[FAIT]` Backend ajouté.
- `[FAIT]` Frontend ajouté.
- `[FAIT]` Tests backend.
- `[FAIT]` PR #91 mergée.

Endpoints :

```http
GET /api/matches/publics?siteId=1001&date=2026-06-20
POST /api/matches/{matchId}/participants/public/payer
```

Règles couvertes :

- `[FAIT]` Liste uniquement les matches publics.
- `[FAIT]` Filtre par site.
- `[FAIT]` Filtre par date.
- `[FAIT]` Filtre sur l'état `A_VENIR`.
- `[FAIT]` Masque les matches complets.
- `[FAIT]` Inscription publique + paiement en une action métier.
- `[FAIT]` Débit du solde joueur.
- `[FAIT]` Premier payé = premier servi.

Tests :

- `[FAIT]` `MatchPublicServiceTest`
- `[FAIT]` `MatchPublicControllerTest`
- `[FAIT]` `MatchPublicRepositoryTest`

Frontend :

- `[FAIT]` Page `Rejoindre un match public`.
- `[FAIT]` Bouton `Rejoindre et payer 15 €`.
- `[FAIT]` Affichage du paiement et du solde restant.
- `[FAIT]` Utilisation du joueur connecté.

---

### 7.21. Mes réservations joueur

Issue / PR :

```txt
#93 [FULLSTACK] Afficher les réservations du joueur
```

Statut :

- `[FAIT]` Backend ajouté.
- `[FAIT]` Frontend ajouté.
- `[FAIT]` Tests backend.
- `[FAIT]` PR #93 mergée.

Endpoint :

```http
GET /api/membres/{matricule}/reservations
```

Règles couvertes :

- `[FAIT]` Consultation des réservations du joueur connecté.
- `[FAIT]` Exclusion des participations libérées.
- `[FAIT]` Affichage des informations match / terrain / site / participation.
- `[FAIT]` Affichage du statut de participation.
- `[FAIT]` Affichage de l'état du match, dont `ANNULE`.

Tests :

- `[FAIT]` `MembreReservationControllerTest`
- `[FAIT]` service `MembreReservationService`

Frontend :

- `[FAIT]` Page `Mes réservations`.
- `[FAIT]` Modèle `reservation.model.ts`.
- `[FAIT]` Service `ReservationApiService`.
- `[FAIT]` Lien dans le menu joueur.
- `[FAIT]` Utilisation du joueur connecté via `AuthContextService`.

---

### 7.22. Historique des transactions et règles finales de dette

Issue / PR :

```txt
#99 [FIX] Finaliser les règles métier de dette et historique joueur
```

Statut :

- `[FAIT]` Backend finalisé.
- `[FAIT]` Frontend finalisé.
- `[FAIT]` PR #99 mergée.

Endpoints :

```http
GET /api/membres/{matricule}/paiements
POST /api/admin/matches/traitement-echeance
```

Règles couvertes :

- `[FAIT]` Création d'une dette initiale pour l'organisateur lors de la création d'un match.
- `[FAIT]` Blocage d'une nouvelle réservation si l'organisateur a encore un solde dû.
- `[FAIT]` Recalcul de la dette du match après chaque paiement de participation.
- `[FAIT]` Mise à jour de la dette organisateur selon le total réellement payé.
- `[FAIT]` Ajout des dettes ouvertes au paiement lorsqu'un organisateur devient joueur dans un autre match.
- `[FAIT]` Traitement d'échéance des matches.
- `[FAIT]` Refus de création d'un match dans le passé ou à l'heure courante.
- `[FAIT]` Historique des paiements d'un joueur.

Frontend :

- `[FAIT]` Onglet joueur `Historique des transactions`.
- `[FAIT]` Route `/joueur/historique-transactions`.
- `[FAIT]` Affichage des paiements de participation.
- `[FAIT]` Affichage des règlements de dette.

---

## 8. Frontend — état actuel

Pages Angular présentes dans le MVP :

- `[FAIT]` Homepage.
- `[FAIT]` Connexion joueur.
- `[FAIT]` Inscription joueur.
- `[FAIT]` Organiser un match / disponibilités.
- `[FAIT]` Créer un match via un créneau choisi.
- `[FAIT]` Rejoindre un match public.
- `[FAIT]` Mes réservations.
- `[FAIT]` Mes dettes.
- `[FAIT]` Mon solde.
- `[FAIT]` Historique des transactions.
- `[FAIT]` Connexion admin.
- `[FAIT]` Dashboard admin.
- `[FAIT]` Statistiques admin.
- `[FAIT]` Traitement de veille.
- `[FAIT]` Jours de fermeture.

Validations frontend :

```powershell
cd frontend
npm run build
cd ..
```

Résultat attendu :

```txt
Build frontend OK
```

---

## 9. Tests backend actuels

Commande à utiliser depuis le dossier `backend` :

```powershell
.\mvnw.cmd clean test
```

Résultat attendu :

```txt
BUILD SUCCESS
```

Tests controller présents ou attendus :

- `[FAIT]` `HealthControllerTest`
- `[FAIT]` `DisponibiliteControllerTest`
- `[FAIT]` `MatchControllerTest`
- `[FAIT]` `ParticipationControllerTest`
- `[FAIT]` `PaiementControllerTest`
- `[FAIT]` `DetteControllerTest`
- `[FAIT]` `TraitementVeilleControllerTest`
- `[FAIT]` `StatistiquesAdminControllerTest`
- `[FAIT]` `AuthControllerTest`
- `[FAIT]` `MembreControllerTest`
- `[FAIT]` `AdminFermetureControllerTest`
- `[FAIT]` `MatchPublicControllerTest`
- `[FAIT]` `MembreReservationControllerTest`

Tests service présents ou attendus :

- `[FAIT]` `DisponibiliteServiceTest`
- `[FAIT]` `MatchCreationServiceTest`
- `[FAIT]` `ParticipationServiceTest`
- `[FAIT]` `ReglesReservationMembreServiceTest`
- `[FAIT]` `PaiementServiceTest`
- `[FAIT]` `DetteServiceTest`
- `[FAIT]` `TraitementVeilleServiceTest`
- `[FAIT]` `StatistiquesAdminServiceTest`
- `[FAIT]` `AuthServiceTest`
- `[FAIT]` `MembreInscriptionServiceTest`
- `[FAIT]` `AdminFermetureServiceTest`
- `[FAIT]` `MembreSoldeServiceTest`
- `[FAIT]` `MatchPublicServiceTest`
- `[FAIT]` `MembreReservationService`

Tests repository présents ou attendus :

- `[FAIT]` `CoreRepositoryTest`
- `[FAIT]` `ComplementaryRepositoryTest`
- `[FAIT]` `ParticipationRepositoryTest`
- `[FAIT]` `PaiementRepositoryTest`
- `[FAIT]` `PadelMatchRepositoryTest`
- `[FAIT]` `StatistiquesRepositoryTest`
- `[FAIT]` `AuthRepositoryTest`
- `[FAIT]` `MembreRepositoryTest`
- `[FAIT]` `DemoSeedDataTest`
- `[FAIT]` `MatchPublicRepositoryTest`

Tests configuration / documentation technique :

- `[FAIT]` `OpenApiDocumentationTest`

Point à vérifier avant remise :

- `[A SURVEILLER]` Relancer les tests backend sur le `main` local après avoir intégré les dernières corrections.
- `[A SURVEILLER]` Relancer le build frontend sur le `main` local après avoir intégré les dernières corrections.

---

## 10. Endpoints backend disponibles

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

Membres :

```http
POST /api/membres/inscription
GET /api/membres/{matricule}/solde
GET /api/membres/{matricule}/reservations
GET /api/membres/{matricule}/paiements
```

Disponibilités :

```http
GET /api/disponibilites?siteId=1001&date=2026-06-20
```

Matches :

```http
POST /api/matches
GET /api/matches/publics?siteId=1001&date=2026-06-20
```

Participations :

```http
POST /api/matches/{matchId}/participants/prive
POST /api/matches/{matchId}/participants/public
POST /api/matches/{matchId}/participants/public/payer
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

Traitements admin :

```http
POST /api/admin/matches/traitement-veille?date=2026-05-19
POST /api/admin/matches/traitement-echeance
```

Fermetures admin :

```http
POST /api/admin/fermetures
```

Statistiques admin :

```http
GET /api/admin/statistiques?dateDebut=2026-05-01&dateFin=2026-06-30
GET /api/admin/statistiques?dateDebut=2026-05-01&dateFin=2026-06-30&siteId=1001
```

---

## 11. Base de données et artefacts DB

Base MVP :

- `[FAIT]` H2 en mémoire.
- `[FAIT]` URL : `jdbc:h2:mem:padeldb`.
- `[FAIT]` User local MVP : `sa`.
- `[FAIT]` Password local MVP : vide.
- `[FAIT]` Création tables par JPA/Hibernate.
- `[FAIT]` Seed automatique via `data.sql`.
- `[FAIT]` Aucun script SQL manuel nécessaire.
- `[FAIT]` Aucun serveur DB externe nécessaire pour le MVP H2.
- `[FAIT]` Champ `membre.solde_credit` ajouté pour le solde virtuel joueur.

Fichiers DB :

- `[FAIT]` `docs/db/schema.sql`
- `[FAIT]` `docs/db/data-demo.sql`
- `[FAIT]` `docs/db/README.md`
- `[FAIT]` `docs/db/db-users.md`
- `[FAIT]` `docs/db/db-users-h2.sql`
- `[FAIT]` `backend/src/main/resources/data.sql`

Users DB documentés :

- `[FAIT]` `padel_migration` : migration / schéma / DDL.
- `[FAIT]` `padel_app` : user applicatif backend / CRUD.
- `[FAIT]` `padel_readonly` : lecture seule / SELECT.
- `[FAIT]` `sa` : H2 local MVP uniquement.

---

## 12. Roadmap restante — ordre à suivre

### Priorité immédiate

1. `[EN COURS]` Stabilisation finale.
2. `[A FAIRE]` Documentation finale de remise.
3. `[A FAIRE]` Préparation de la démo finale.
4. `[A FAIRE]` Nettoyage final Git local après merge des dernières petites PR.

### Stabilisation et documentation

- `[A FAIRE]` Mettre à jour `README.md`.
- `[A FAIRE]` Créer ou finaliser `ARCHITECTURE.md`.
- `[A FAIRE]` Créer ou finaliser `EXPLOITATION.md`.
- `[A FAIRE]` Créer ou finaliser `DEMO.md`.
- `[A FAIRE]` Vérifier la démo complète backend + frontend.
- `[A FAIRE]` Vérifier que le projet démarre sans erreur.
- `[A FAIRE]` Vérifier que Swagger est accessible.
- `[A FAIRE]` Vérifier que les tests backend passent.
- `[A FAIRE]` Vérifier que le build frontend passe.
- `[A FAIRE]` Nettoyer les branches locales mergées.

---

## 13. Points techniques à surveiller

- `[A SURVEILLER]` Le warning Java agent pendant les tests n'est pas bloquant si `BUILD SUCCESS`.
- `[A SURVEILLER]` Vérifier cohérence entre `application.yml` et éventuels autres fichiers de config.
- `[A SURVEILLER]` Vérifier que la version Java utilisée localement est compatible avec le projet.
- `[A SURVEILLER]` Le package `com.padelMarius.backend` fonctionne, même si la majuscule n'est pas conventionnelle en Java.
- `[A SURVEILLER]` Ne pas ajouter `backend/target/`.
- `[A SURVEILLER]` Ne pas ajouter `frontend/dist/`.
- `[A SURVEILLER]` Ne pas ajouter `.idea/` ou fichiers IDE.
- `[A SURVEILLER]` Ne pas mélanger nettoyage Git avec une feature métier.
- `[A SURVEILLER]` Ne pas modifier plusieurs sujets dans une même PR.
- `[A SURVEILLER]` Garder les PR petites et lisibles.
- `[A SURVEILLER]` Garder les tests backend passants après chaque merge.
- `[A SURVEILLER]` Garder le build frontend passant après chaque merge.
- `[A SURVEILLER]` Le frontend doit rester simple et ne jamais contenir de SQL.
- `[A SURVEILLER]` Le solde crédit est un choix MVP simple ; ne pas ajouter maintenant une table de mouvements financiers séparée sans nouvelle décision.

---

## 14. Règles GitHub à suivre

Pour chaque nouvelle fonctionnalité ou mise à jour documentaire :

1. Créer une issue.
2. Créer une branche depuis `main`.
3. Coder uniquement le périmètre de l'issue.
4. Ajouter les tests backend nécessaires si l'étape touche le backend.
5. Lancer les tests backend.
6. Lancer le build frontend si l'étape touche Angular.
7. Commit.
8. Push.
9. Créer une PR.
10. Vérifier les fichiers changés.
11. Merger.
12. Supprimer la branche distante si souhaité.
13. Revenir sur `main`.
14. Pull.
15. Lancer les tests.
16. Lancer le build frontend.
17. Supprimer la branche locale.
18. Vérifier que le dépôt est propre.

Commandes de base :

```powershell
git checkout main
git pull origin main
git status --short
cd backend
.\mvnw.cmd clean test
cd ..
cd frontend
npm run build
cd ..
```

---

## 15. Scénario de démo cible

Démo métier courte :

1. `[A FAIRE]` Démarrer backend.
2. `[A FAIRE]` Démarrer frontend.
3. `[A FAIRE]` Montrer `/api/health`.
4. `[A FAIRE]` Montrer Swagger.
5. `[A FAIRE]` Se connecter comme joueur avec `G1001`.
6. `[A FAIRE]` Montrer le solde joueur.
7. `[A FAIRE]` Créer un nouveau joueur via la page `Inscription joueur`.
8. `[A FAIRE]` Se connecter avec le nouveau matricule.
9. `[A FAIRE]` Consulter les disponibilités.
10. `[A FAIRE]` Créer un match public ou privé.
11. `[A FAIRE]` Rejoindre un match public et payer.
12. `[A FAIRE]` Montrer la baisse du solde après paiement.
13. `[A FAIRE]` Ouvrir `Mes réservations`.
14. `[A FAIRE]` Ouvrir `Historique des transactions`.
15. `[A FAIRE]` Générer ou montrer une dette organisateur.
16. `[A FAIRE]` Montrer que la dette bloque une nouvelle réservation.
17. `[A FAIRE]` Payer la dette.
18. `[A FAIRE]` Se connecter comme admin avec `admin-global` / `secret`.
19. `[A FAIRE]` Lancer le traitement de veille.
20. `[A FAIRE]` Lancer le traitement d'échéance.
21. `[A FAIRE]` Créer une fermeture via `Jours de fermeture`.
22. `[A FAIRE]` Montrer que les matches concernés sont annulés.
23. `[A FAIRE]` Montrer que les joueurs payés sont remboursés sur leur solde.
24. `[A FAIRE]` Montrer les statistiques admin.
25. `[A FAIRE]` Montrer les fichiers DB :
- `schema.sql`
- `data-demo.sql`
- `db-users.md`
26. `[A FAIRE]` Expliquer que le frontend n'a aucun accès DB.
27. `[A FAIRE]` Montrer GitHub : issues, branches, commits, PR.
28. `[A FAIRE]` Montrer les tests backend qui passent.
29. `[A FAIRE]` Montrer architecture et exploitation.

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

- `1001` / `BRU` : Padel Bruxelles.
- `1002` / `NAM` : Padel Namur.

Endpoints rapides pour démo :

```http
GET /api/health
GET /swagger-ui.html
POST /api/auth/joueur
POST /api/auth/admin
POST /api/membres/inscription
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

## 17. Dernier point d'arrêt

Dernier état connu :

- Backend métier très avancé.
- Frontend MVP démontrable.
- Tests backend complets sur les couches principales.
- Inscription joueur terminée.
- Gestion admin des jours de fermeture terminée.
- Dette organisateur terminée et renforcée.
- Traitement de veille terminé.
- Traitement d'échéance ajouté.
- Statistiques admin terminées.
- Authentification simple joueurs/admins terminée.
- Swagger/OpenAPI terminé.
- Script DB / seed automatisé terminé.
- Documentation users DB / droits terminée.
- Solde crédit joueur terminé.
- Frontend `Mon solde` terminé.
- Matches publics listables et rejoignables avec paiement terminés.
- Mes réservations terminé.
- Historique des transactions terminé.
- PR #99 `[FIX] Finaliser les règles métier de dette et historique joueur` mergée.
- Prochaine vraie étape : stabilisation finale + documentation de remise.

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

Commande de validation frontend :

```powershell
cd frontend
npm run build
cd ..
```

Résultat attendu :

```txt
Build frontend OK
```

Objectif suivant immédiat :

```txt
[DOC] Mettre à jour les documents projet après les dernières PR
```
