# État du projet

Dernière mise à jour : 2026-05-06

## 1. Résumé du projet

Application web de réservation de terrains de padel.

### Stack technique
- Backend : Java avec Spring Boot
- Base de données : SQL relationnelle
- Frontend : Angular

### Objectif
Livrer rapidement un MVP fonctionnel, propre et démontrable.

### Attentes principales du professeur
- code backend visible et sérieux
- tests backend visibles
- GitHub bien suivi avec issues, branches, commits et pull requests
- projet démontrable de bout en bout
- séparation stricte entre frontend et backend
- backend exposé sous forme de REST API
- aucun SQL ni accès direct à la base de données depuis le frontend

---

## 2. Périmètre MVP retenu

Le MVP doit couvrir au minimum :

- gestion multi-sites
- gestion des terrains par site
- gestion des horaires annuels par site
- gestion des jours de fermeture globaux et locaux
- accès joueur par matricule
- catégories de membres : GLOBAL / SITE / LIBRE
- consultation des disponibilités
- création de match privé
- création de match public
- ajout de joueurs à un match privé
- inscription à un match public
- paiement simple
- dette organisateur si match incomplet
- blocage de nouvelle réservation si dette active
- pénalité simple possible
- espace joueur
- vue admin simple
- statistiques de base

---

## 3. Règles métier validées

- le système est multi-sites
- un site possède plusieurs terrains
- chaque site possède ses propres horaires annuels
- il existe des jours de fermeture globaux et locaux
- les membres sont de type GLOBAL, SITE ou LIBRE
- l’accès joueur se fait par matricule
- un match peut être privé ou public
- un match correspond à la réservation d’un terrain
- un match contient maximum 4 joueurs
- la durée d’un match est de 1h30
- il faut 15 minutes entre deux matches
- le paiement est simple
- si le match n’est pas complet, l’organisateur peut avoir une dette
- un organisateur avec dette active ne peut pas faire une nouvelle réservation
- une pénalité simple peut être appliquée
- il existe un espace joueur
- il existe une vue admin simple
- il existe des statistiques de base

---

## 4. Documents déjà produits

### Faits et validés
- plan métier
- spécification fonctionnelle détaillée du MVP
- modélisation des données du MVP
- diagramme Mermaid du MCD
- modèle relationnel SQL du MVP
- état du projet mis à jour après les entités JPA coeur et complémentaires

### Fichiers de référence
- `docs/01-plan-metier.md`
- `docs/02-specification-fonctionnelle-mvp.md`
- `docs/03-modelisation-donnees-mvp.md`
- `docs/04-modele-relationnel-sql.md`
- `docs/00-etat-du-projet.md`

---

## 5. État GitHub actuel

### Déjà mis en place
- dépôt GitHub du projet
- logique de travail avec issues / branches / PR
- issues backend créées
- backend Spring Boot initialisé
- branche backend de base réalisée
- branche `back/entities-core` réalisée et mergée
- branche `back/entities-extra` réalisée pour les entités complémentaires
- `.gitignore` ajouté pour éviter de versionner `.idea/`, `*.iml` et `backend/target/`
- `backend/target/` retiré du suivi Git si nécessaire

### Issues backend créées ou utilisées
- `[BACK] Initialiser le backend Java et la configuration de base`
- `[BACK] Créer les entités JPA coeur et repositories`
- `[BACK] Créer les entités JPA complémentaires et repositories`
- `[TEST] Mettre en place la stratégie de tests backend`

### Branches backend importantes
- `back/init-backend-base` : initialisation backend, endpoint de santé et premiers tests
- `back/entities-core` : entités JPA coeur, repositories et tests repository
- `back/entities-extra` : entités JPA complémentaires, repositories complémentaires et tests repository complémentaires

### Commits importants réalisés
- `chore(git): ignore generated build files`
- `feat(back): add core JPA entities and repositories`
- `feat(back): add complementary JPA entities and repositories`

---

## 6. Où j’en suis maintenant

Je suis dans la phase de développement backend.

### Situation actuelle
- le métier est cadré
- le MVP est défini
- la modélisation de données est faite
- le modèle relationnel SQL est prêt côté documentation
- le backend Spring Boot est initialisé
- le endpoint `/api/health` existe
- les premiers tests backend passent
- les entités JPA coeur sont créées
- les repositories Spring Data JPA coeur sont créés
- les entités JPA complémentaires sont créées
- les repositories Spring Data JPA complémentaires sont créés
- les tests repository vérifient la persistance des entités coeur et complémentaires

### Choix techniques déjà fixés
- backend : Spring Boot
- build tool : Maven
- base de démarrage : H2 pour aller vite
- dépendances utilisées :
    - Spring Web MVC
    - Spring Data JPA
    - Validation
    - H2 Database
    - Lombok
- package racine : `com.padelMarius.backend`

---

## 7. Travail terminé — Jour 1

### Objectif
Initialiser le backend Spring Boot.

### Réalisé
- projet Spring Boot placé dans le dossier `backend`
- configuration Maven en place
- configuration H2/JPA en place
- application Spring Boot démarrable
- endpoint `/api/health` créé
- test de démarrage du contexte créé
- test controller du endpoint `/api/health` créé
- premiers tests backend passants

### Tests connus
- `BackendApplicationTests`
- `HealthControllerTest`

### Résultat
- `BUILD SUCCESS`

---

## 8. Travail terminé — Jour 2

### Date
2026-05-06

### Issue
`[BACK] Créer les entités JPA coeur et repositories`

### Branche
`back/entities-core`

### Objectif
Créer les entités JPA coeur du MVP et leurs repositories.

### Entités JPA créées
- `Site`
- `Terrain`
- `Membre`
- `PadelMatch`
- `Participation`

### Enums métier créés
- `CategorieMembre`
- `ModeCreation`
- `VisibiliteMatch`
- `EtatCycleMatch`
- `RoleParticipation`
- `ModeEntreeParticipation`
- `StatutParticipation`

### Repositories créés
- `SiteRepository`
- `TerrainRepository`
- `MembreRepository`
- `PadelMatchRepository`
- `ParticipationRepository`

### Test repository créé
- `CoreRepositoryTest`

### Vérifications couvertes par le test repository
- persistance d’un site et d’un terrain
- recherche d’un site par code
- recherche des terrains par site
- persistance d’un membre
- recherche d’un membre par matricule
- persistance d’un match
- persistance d’une participation
- comptage des participations d’un match
- vérification de l’existence d’une participation par couple match/membre

### Résultat de test local
- `Tests run: 5`
- `Failures: 0`
- `Errors: 0`
- `Skipped: 0`
- `BUILD SUCCESS`

### État du Jour 2
Le programme technique et GitHub du Jour 2 est terminé si la PR `back/entities-core` a été mergée dans `main`.

---

## 9. Travail terminé — Jour 3

### Date
2026-05-06

### Issue
`[BACK] Créer les entités JPA complémentaires et repositories`

### Branche
`back/entities-extra`

### Objectif
Créer les entités JPA complémentaires du MVP et leurs repositories.

### Entités JPA créées
- `HoraireAnnuelSite`
- `Fermeture`
- `Administrateur`
- `Dette`
- `Penalite`
- `Paiement`

### Enums métier créés
- `PorteeFermeture`
- `RoleAdministrateur`
- `NaturePaiement`
- `StatutPaiement`
- `StatutDette`
- `StatutPenalite`

### Repositories créés
- `HoraireAnnuelSiteRepository`
- `FermetureRepository`
- `AdministrateurRepository`
- `DetteRepository`
- `PenaliteRepository`
- `PaiementRepository`

### Test repository créé
- `ComplementaryRepositoryTest`

### Vérifications couvertes par le test repository
- persistance d’un horaire annuel par site et par année
- recherche d’un horaire annuel par site et année civile
- persistance d’une fermeture globale
- persistance d’une fermeture locale rattachée à un site
- recherche d’un administrateur par email ou login
- recherche des administrateurs par rôle et par site
- persistance d’une dette liée à un match et à un membre responsable
- recherche d’une dette ouverte par membre responsable
- persistance d’une pénalité liée à un membre et à un match source
- recherche d’une pénalité active par membre
- persistance d’un paiement de participation
- persistance d’un paiement de dette
- recherche d’un paiement par participation et par dette

### Résultat de test local
- `Tests run: 10`
- `Failures: 0`
- `Errors: 0`
- `Skipped: 0`
- `BUILD SUCCESS`

### État du Jour 3
Le programme technique du Jour 3 est terminé.

Le Jour 3 est considéré complètement terminé dans le workflow GitHub seulement si :
- la branche `back/entities-extra` est poussée sur GitHub ;
- la Pull Request vers `main` est ouverte ;
- la PR est relue ;
- la PR est mergée dans `main` ;
- le poste local est revenu sur `main` ;
- `main` local a été mis à jour avec `git pull` ;
- les tests repassent sur `main`.

---

## 10. Ce que je dois faire maintenant

### Priorité immédiate
Clôturer administrativement le Jour 3 sur GitHub et préparer le dépôt local pour le prochain bloc.

### Étapes concrètes immédiates
1. vérifier que la branche active est `back/entities-extra`
2. vérifier que les tests Maven passent avec `Tests run: 10`
3. vérifier `git status`
4. committer le code du Jour 3 si ce n’est pas encore fait
5. pousser la branche `back/entities-extra`
6. ouvrir la Pull Request vers `main`
7. vérifier l’onglet `Files changed`
8. confirmer que la PR ne contient pas `.idea/`, `*.iml` ou `backend/target/`
9. confirmer que `ComplementaryRepositoryTest.java` est bien dans `src/test/java`
10. merger la PR si tout est propre
11. revenir localement sur `main`
12. faire `git pull`
13. supprimer la branche locale `back/entities-extra`
14. relancer les tests sur `main`

---

## 11. Ce que je ferai juste après

Après le Jour 3, le prochain bloc logique est la couche service métier.

### Prochain objectif recommandé
Créer les premiers services métier pour la réservation.

### Priorité technique du prochain bloc
- créer une couche `service`
- garder les controllers minces
- placer les règles métier dans les services
- utiliser les repositories uniquement pour l’accès aux données
- ajouter des tests unitaires de service

### Premières règles métier à implémenter
- durée fixe d’un match : 1h30
- 15 minutes entre deux matches sur un même terrain
- interdiction de chevauchement de deux matches sur un même terrain
- maximum 4 joueurs par match
- exactement 1 organisateur par match
- blocage de création si dette ouverte
- blocage de création si pénalité active

---

## 12. Priorité absolue pour la semaine restante

Le projet doit avancer plus vite que prévu.

### Priorité de travail
1. backend fonctionnel
2. tests backend visibles
3. réservations / paiement / dette / statistiques
4. frontend simple mais démontrable
5. documentation minimale mais propre

### Ce qu’il faut simplifier si besoin
- design frontend avancé
- sécurité complexe
- notifications
- bonus non essentiels
- options d’ergonomie secondaires

---

## 13. Tests backend obligatoires

Le backend devra montrer des tests dans les 3 couches suivantes :

### Repository
- recherches simples
- persistance des entités
- requêtes de base utiles au métier

### Service
- règles métier principales
- validation des réservations
- blocage si dette
- calcul de dette
- logique de participation

### Controller
- endpoints principaux
- cas valides
- cas refusés
- réponses HTTP attendues

### État actuel des tests
- tests controller : démarrés avec `HealthControllerTest`
- tests repository : présents avec `CoreRepositoryTest` et `ComplementaryRepositoryTest`
- tests service : à créer dans les prochains blocs métier
- total actuel confirmé localement : `Tests run: 10`, `Failures: 0`, `Errors: 0`

---

## 14. Règles de travail GitHub

- 1 issue = 1 objectif clair
- 1 branche = 1 issue
- 1 PR = 1 bloc cohérent
- commits courts et explicites
- merge seulement quand le bloc est propre
- pour le backend : pas de feature sans tests associés

### Format attendu dans l’accompagnement IA
Quand on travaille sur GitHub, toujours donner :
- titre de l’issue
- nom de branche
- message de commit
- titre de PR
- description de PR

Quand on travaille sur le backend, toujours donner :
- fichiers à créer
- emplacement exact
- code complet si possible
- tests à écrire

---

## 15. Planning sprint final

### Objectif global
Finir un MVP démontrable en environ une semaine.

### Ordre réel de travail
1. initialisation backend — fait
2. entités JPA coeur + repositories — fait
3. entités JPA complémentaires + repositories — fait techniquement
4. services réservation
5. paiements et dette
6. statistiques
7. frontend minimal
8. stabilisation et démo

---

## 16. Dernier point d’arrêt

Le dernier point d’arrêt confirmé est :

- branche `back/entities-extra` utilisée pour le Jour 3
- entités JPA complémentaires créées
- repositories complémentaires créés
- `ComplementaryRepositoryTest` créé au bon endroit : `backend/src/test/java/com/padelMarius/backend/repository/ComplementaryRepositoryTest.java`
- tests Maven passants avec 10 tests
- aucun fichier `.idea/` ou `backend/target/` ne doit être commité

Le prochain travail concret, après merge du Jour 3, est :

- revenir localement sur `main`
- faire `git pull`
- supprimer la branche locale `back/entities-extra`
- relancer les tests Maven sur `main`
- créer la prochaine issue backend service
- démarrer une nouvelle branche pour les premiers services métier de réservation
