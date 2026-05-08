# État du projet — Padel Marius

Dernière mise à jour : 2026-05-08  
Document de suivi rapide pour reprise par IA / Codex / développeur.

Objectif du fichier : suivre simplement ce qui est fait, ce qui reste à faire, et le prochain pas concret.  
À chaque PR mergée, mettre à jour ce fichier en déplaçant l'étape concernée vers `[FAIT]`.

---

## 1. Statuts utilisés

- `[FAIT]` : terminé, testé, mergé ou prêt à être considéré comme livré après merge de la PR courante.
- `[EN COURS]` : branche ou PR active.
- `[A FAIRE]` : pas encore commencé.
- `[A SURVEILLER]` : point technique connu, non bloquant pour le MVP.

---

## 2. Résumé projet

Application web de réservation de terrains de padel.

Stack projet :

- Backend : Java avec Spring Boot.
- Build : Maven Wrapper.
- Base de données : SQL relationnelle, H2 pour le MVP.
- Frontend prévu : Angular.
- Communication : frontend vers backend uniquement via API HTTP REST.
- Le frontend ne doit jamais contenir de SQL.
- Le frontend ne doit jamais accéder directement à la base de données.

Priorité actuelle :

1. Backend fonctionnel.
2. Tests backend visibles.
3. Réservation / paiement / dette / statistiques.
4. Frontend simple et démontrable.
5. Documentation minimale mais propre.

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
- `[A RESPECTER]` Admin avec rôles à prévoir :
  - GLOBAL
  - SITE
- `[A RESPECTER]` Script DB ou artefact de schéma à fournir.
- `[A RESPECTER]` Explication des users DB et droits associés à fournir.
- `[A RESPECTER]` Dossier d'architecture à fournir.
- `[A RESPECTER]` Document d'exploitation à fournir.
- `[A RESPECTER]` Démo métier orientée règles business.

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
- `[FAIT]` Match privé ou public.
- `[FAIT]` Match = réservation d'un terrain.
- `[FAIT]` 4 joueurs maximum par match.
- `[FAIT]` Durée match = 1h30.
- `[FAIT]` 15 minutes entre deux matches.
- `[FAIT]` Paiement simple.
- `[FAIT]` Dette organisateur si match incomplet ou pas totalement payé.
- `[FAIT]` Blocage nouvelle réservation si dette active.
- `[FAIT]` Blocage nouvelle réservation si pénalité active.
- `[FAIT]` Traitement de veille :
  - match privé incomplet devient public
  - participation non payée libérée
  - pénalité organisateur possible
- `[FAIT]` Statistiques admin.
- `[FAIT]` Authentification simple admin.
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
  - applique les règles de réservation, participation, paiement, dette, pénalité

- `[FAIT]` `repository`
  - accès base de données via Spring Data JPA
  - pas de logique métier

- `[FAIT]` `entity`
  - modèle JPA

- `[FAIT]` `dto`
  - objets d'entrée/sortie API

Package racine :

```txt
com.padelMarius.backend
```

---

## 6. Backend — étapes réalisées

### Initialisation

- `[FAIT]` Projet Spring Boot initialisé.
- `[FAIT]` Maven Wrapper présent.
- `[FAIT]` H2 configuré pour le MVP.
- `[FAIT]` Endpoint santé créé.

Endpoint :

```http
GET /api/health
```

Tests :

- `[FAIT]` `BackendApplicationTests`
- `[FAIT]` `HealthControllerTest`

---

### Entités JPA et repositories

- `[FAIT]` Entités JPA coeur créées.
- `[FAIT]` Entités JPA complémentaires créées.
- `[FAIT]` Repositories Spring Data JPA créés.
- `[FAIT]` Tests repository de base créés.

Tests :

- `[FAIT]` `CoreRepositoryTest`
- `[FAIT]` `ComplementaryRepositoryTest`

---

### Disponibilités

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

Tests :

- `[FAIT]` `DisponibiliteServiceTest`
- `[FAIT]` `DisponibiliteControllerTest`

---

### Création de match privé/public

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

### Participations aux matches

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
- `[FAIT]` Tests renforcés ensuite.
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
- `[FAIT]` Participation libérée gérée dans les tests.

Tests :

- `[FAIT]` `ParticipationServiceTest`
- `[FAIT]` `ParticipationControllerTest`
- `[FAIT]` `ParticipationRepositoryTest`

---

### Règles membres et fenêtres de réservation

Statut :

- `[FAIT]` Fonctionnalité livrée.
- `[FAIT]` Tests service.

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

Tests :

- `[FAIT]` `ReglesReservationMembreServiceTest`

---

### Paiement simple des participations

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

### Dette organisateur

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

### Traitement de veille des matches

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
- `[FAIT]` `PadelMatchRepositoryTest`

---

### Renforcement des tests du traitement de veille

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
- `[FAIT]` Documentation projet mise à jour avec ce fichier.

Commit prévu :

```txt
test(back): cover pre-match processing edge cases
```

PR prévue :

```txt
[TEST] Renforcer les tests du traitement de veille
```

---

## 7. Tests backend actuels

Commande à utiliser depuis le dossier `backend` :

```powershell
.\mvnw.cmd clean test
```

Dernier résultat connu :

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

Tests service présents :

- `[FAIT]` `DisponibiliteServiceTest`
- `[FAIT]` `MatchCreationServiceTest`
- `[FAIT]` `ParticipationServiceTest`
- `[FAIT]` `ReglesReservationMembreServiceTest`
- `[FAIT]` `PaiementServiceTest`
- `[FAIT]` `DetteServiceTest`
- `[FAIT]` `TraitementVeilleServiceTest`

Tests repository présents :

- `[FAIT]` `CoreRepositoryTest`
- `[FAIT]` `ComplementaryRepositoryTest`
- `[FAIT]` `ParticipationRepositoryTest`
- `[FAIT]` `PaiementRepositoryTest`
- `[FAIT]` `PadelMatchRepositoryTest`

---

## 8. Endpoints backend disponibles

Santé :

```http
GET /api/health
```

Disponibilités :

```http
GET /api/disponibilites?siteId=1&date=2026-05-20
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

---

## 9. Roadmap restante — ordre à suivre

### Prochaine étape immédiate

Issue :

```txt
[BACK] Ajouter les statistiques backend MVP
```

Branche :

```txt
back/stats-admin
```

Commit prévu :

```txt
feat(back): add admin statistics endpoints
```

PR prévue :

```txt
[BACK] Ajouter les statistiques backend MVP
```

Statut :

- `[FAIT]` Statistiques backend MVP.

Objectifs :

- `[FAIT]` Nombre de matches.
- `[FAIT]` Chiffre d'affaires.
- `[FAIT]` Dettes ouvertes.
- `[FAIT]` Nombre de paiements.
- `[FAIT]` Taux de remplissage simple.
- `[FAIT]` Vue globale admin.
- `[FAIT]` Vue filtrable par site si possible.

---

---

### Authentification simple joueurs/admins

Issue :

```txt
[BACK] Ajouter authentification simple joueurs/admins
back/auth-simple
Statut :

[FAIT] Authentification joueur par matricule.
[FAIT] Aucun login/mot de passe requis pour les joueurs.
[FAIT] Refus d'un joueur inactif.
[FAIT] Authentification admin par login et mot de passe.
[FAIT] Refus d'un admin inactif.
[FAIT] Retour du rôle administrateur GLOBAL ou SITE.
[FAIT] Retour du site administrateur si admin de site.
[FAIT] Gestion HTTP 401 pour identifiants admin invalides.
[FAIT] Tests controller.
[FAIT] Tests service.
[FAIT] Tests repository.

Endpoints :

POST /api/auth/joueur
POST /api/auth/admin

Exemple joueur :

{
  "matricule": "G0001"
}

Exemple admin :

{
  "login": "admin-global",
  "motDePasse": "secret"
}

### Étapes backend restantes

- `[FAIT]` Ajouter les statistiques backend MVP.
- `[FAIT]` Ajouter authentification simple joueurs/admins.
- `[A FAIRE]` Ajouter OpenAPI / Swagger.
- `[A FAIRE]` Ajouter script de schéma ou artefact DB.
- `[A FAIRE]` Ajouter seed de données de démonstration.
- `[A FAIRE]` Documenter les users DB et leurs droits.
- `[A FAIRE]` Vérifier ou nettoyer les incohérences de configuration.
- `[A FAIRE]` Préparer scénario de démo backend.

---

### Étapes frontend restantes

- `[A FAIRE]` Initialiser Angular.
- `[A FAIRE]` Créer structure frontend simple.
- `[A FAIRE]` Configurer routing.
- `[A FAIRE]` Configurer `HttpClient`.
- `[A FAIRE]` Créer services Angular pour appels API.
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
- `[A FAIRE]` Afficher erreurs API clairement.
- `[A FAIRE]` Garder interface simple, lisible, démontrable.

---

### Documentation restante

- `[A FAIRE]` `README.md`
- `[A FAIRE]` `ARCHITECTURE.md`
- `[A FAIRE]` `EXPLOITATION.md`
- `[A FAIRE]` `DEMO.md`
- `[A FAIRE]` `docs/db/schema.sql` ou `docs/db/schema.md`
- `[A FAIRE]` `docs/db/db-users.md`

Contenu attendu :

- `[A FAIRE]` Architecture backend.
- `[A FAIRE]` Architecture frontend.
- `[A FAIRE]` Séparation controller / service / repository.
- `[A FAIRE]` Outils et frameworks utilisés.
- `[A FAIRE]` URL Swagger.
- `[A FAIRE]` Commande pour lancer backend.
- `[A FAIRE]` Commande pour lancer frontend.
- `[A FAIRE]` Commande pour lancer tests backend.
- `[A FAIRE]` Informations DB.
- `[A FAIRE]` Données de démonstration.
- `[A FAIRE]` Scénario de démo 5 à 10 minutes.

---

## 10. Planning sprint final synthétique

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
- `[A FAIRE]` 16 — Ajouter OpenAPI Swagger.
- `[A FAIRE]` 17 — Ajouter script DB / schéma / seed.
- `[A FAIRE]` 18 — Documenter users DB et droits.
- `[A FAIRE]` 19 — Initialiser frontend Angular.
- `[A FAIRE]` 20 — Implémenter espace joueur MVP.
- `[A FAIRE]` 21 — Implémenter vue admin MVP.
- `[A FAIRE]` 22 — Stabiliser le MVP final.
- `[A FAIRE]` 23 — Préparer documentation de remise.
- `[A FAIRE]` 24 — Préparer démo finale.

---

## 11. Points techniques à surveiller

- `[A SURVEILLER]` Le warning Java agent pendant les tests n'est pas bloquant si `BUILD SUCCESS`.
- `[A SURVEILLER]` Vérifier cohérence entre `application.yml` et `application.properties` si les deux existent.
- `[A SURVEILLER]` Vérifier la version Java demandée par les consignes avant remise.
- `[A SURVEILLER]` Le package `com.padelMarius.backend` fonctionne, même si la majuscule n'est pas conventionnelle en Java.
- `[A SURVEILLER]` Ne pas ajouter `backend/target/`.
- `[A SURVEILLER]` Ne pas ajouter `.idea/` ou fichiers IDE.
- `[A SURVEILLER]` Ne pas mélanger nettoyage Git avec une feature métier.
- `[A SURVEILLER]` Prévoir un artefact DB clair pour la remise.
- `[A SURVEILLER]` Prévoir une explication simple des users DB et droits.

---

## 12. Règles GitHub à suivre

Pour chaque nouvelle fonctionnalité :

1. Créer une issue.
2. Créer une branche depuis `main`.
3. Coder uniquement le périmètre de l'issue.
4. Ajouter les tests backend nécessaires.
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

---

## 13. Prochaine action concrète

Si la PR actuelle d'authentification simple n'est pas encore terminée :

- `[EN COURS]` Lancer les tests.
- `[EN COURS]` Commit.
- `[EN COURS]` Push.
- `[EN COURS]` Créer la PR.
- `[EN COURS]` Merger.
- `[EN COURS]` Nettoyer localement.

Si la PR actuelle est mergée :

- `[A FAIRE]` Commencer l'issue suivante :

```txt
[BACK] Ajouter OpenAPI Swagger


```

## 14. Scénario de démo cible

Démo métier courte :

1. `[A FAIRE]` Démarrer backend.
2. `[A FAIRE]` Montrer `/api/health` ou Swagger.
3. `[A FAIRE]` Consulter les disponibilités.
4. `[A FAIRE]` Créer un match public ou privé.
5. `[A FAIRE]` Ajouter un participant.
6. `[A FAIRE]` Payer une participation.
7. `[A FAIRE]` Générer une dette si match non payé entièrement.
8. `[A FAIRE]` Montrer que la dette bloque une nouvelle réservation.
9. `[A FAIRE]` Payer la dette.
10. `[A FAIRE]` Lancer le traitement de veille.
11. `[A FAIRE]` Montrer les statistiques admin.
12. `[A FAIRE]` Montrer GitHub : issues, branches, commits, PR.
13. `[A FAIRE]` Montrer les tests backend qui passent.
14. `[A FAIRE]` Montrer architecture et exploitation.

---

## 15. Dernier point d'arrêt

Dernier état connu :

- Backend métier très avancé.
- Dette organisateur terminée.
- Traitement de veille terminé.
- Renforcement des tests du traitement de veille en cours de finalisation dans la PR courante.
- Prochaine vraie feature : statistiques backend MVP.
- Commande de validation backend :

```powershell
cd backend
.\mvnw.cmd clean test
```

Résultat attendu :

```txt
BUILD SUCCESS
```