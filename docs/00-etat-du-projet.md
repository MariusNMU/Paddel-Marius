# État du projet

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

## 3. Contraintes professorales confirmées

- le backend doit être une REST API obligatoire
- le frontend ne doit jamais accéder directement à la base de données
- le frontend ne contient jamais de SQL
- le backend doit respecter une séparation claire controller / service / repository
- les tests backend sont obligatoires sur controllers, services et repositories
- Git est obligatoire avec issues
- branche par issue
- commits réguliers et cohérents
- démo orientée métier / business
- un script DB ou artefact de schéma doit être prévu à la remise
- il faudra pouvoir expliquer les users DB et les droits associés
- pas de login nécessaire pour les users, uniquement le matricule

---

## 4. Règles métier validées

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

## 5. Documents déjà produits

### Faits et validés
- plan métier
- spécification fonctionnelle détaillée du MVP
- modélisation des données du MVP
- diagramme Mermaid du MCD
- modèle relationnel SQL du MVP

### Fichiers de référence
- `docs/00-etat-du-projet.md`
- `docs/03-modelisation-donnees-mvp.md`
- `docs/04-modele-relationnel-sql.md`
- `recommandations professeur sgbd (backend).docx`
- `settings.docx`

---

## 6. État GitHub actuel

### Déjà mis en place
- dépôt GitHub du projet
- logique de travail avec issues / branches / PR
- issues backend créées
- branche backend d’initialisation créée

### Issues backend créées
- `[BACK] Initialiser le backend Java et la configuration de base`
- `[BACK] Créer les entités JPA et les repositories`
- `[TEST] Mettre en place la stratégie de tests backend`

### Bloc backend initialisation
- backend Spring Boot initialisé
- package racine : `com.padelMarius.backend`
- configuration Maven / H2 / JPA en place
- endpoint `/api/health` créé
- premiers tests backend passants

### Point à vérifier sur GitHub
- vérifier si la PR du bloc backend initial a bien été mergée
- si oui, créer ou utiliser la branche suivante pour les entités JPA
- si non, finaliser le merge avant de passer au bloc suivant

---

## 7. Où j’en suis maintenant

Je suis entré dans la phase de développement backend.

### Situation actuelle
- le métier est cadré
- le MVP est défini
- la modélisation de données est faite
- le modèle relationnel SQL est prêt
- le backend Spring Boot de base fonctionne
- les premiers tests backend passent
- la prochaine vraie étape est le bloc entités JPA + repositories

### Choix techniques déjà fixés
- backend : Spring Boot
- package racine : `com.padelMarius.backend`
- build tool : Maven
- base de démarrage : H2 pour aller vite
- dépendances utilisées :
  - Spring Web
  - Spring Data JPA
  - Validation
  - H2 Database
  - Lombok

---

## 8. Ce que je dois faire maintenant

### Priorité immédiate
Créer les entités JPA et les repositories du MVP.

### Bloc de travail suivant
1. vérifier que le bloc `back/init-backend-base` est bien poussé et mergé
2. ouvrir ou utiliser l’issue :
   - `[BACK] Créer les entités JPA et les repositories`
3. créer la branche :
   - `back/entities-repositories`
4. créer les premières entités cœur :
   - `Site`
   - `Terrain`
   - `Membre`
   - `PadelMatch`
   - `Participation`
5. créer les repositories associés
6. écrire les premiers tests repository
7. faire commit / push / PR du bloc

---

## 9. Ce que je ferai juste après

Après les entités JPA et repositories :

1. ajouter les entités complémentaires :
   - `HoraireAnnuelSite`
   - `Fermeture`
   - `Administrateur`
   - `Paiement`
   - `Dette`
   - `Penalite`
2. compléter les repositories
3. implémenter la consultation des disponibilités
4. implémenter la création de match privé/public
5. implémenter les paiements, la dette et les statistiques
6. connecter ensuite un frontend Angular simple

---

## 10. Priorité absolue pour les deux prochaines semaines

### Priorité de travail
1. backend fonctionnel
2. tests backend visibles
3. réservations / paiement / dette / stats
4. frontend simple mais démontrable
5. documentation minimale mais propre

### Ce qu’il faut simplifier si besoin
- design frontend avancé
- sécurité complexe
- notifications
- bonus non essentiels
- options d’ergonomie secondaires

---

## 11. Tests backend obligatoires

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

---

## 12. Règles de travail GitHub

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

## 13. Architecture technique cible

Architecture minimale obligatoire :

Frontend Angular
-> appels HTTP
Backend Spring Boot REST API
-> Controller
-> Service
-> Repository
-> Base de données relationnelle SQL

### Règles strictes
- aucun accès DB dans le frontend
- aucun SQL dans le frontend
- controller = HTTP + validation + DTO
- service = logique métier
- repository = accès aux données

---

## 14. Livrables de remise à prévoir

- code source complet
- accès GitHub
- analyse / documentation
- tests backend visibles
- artefact DB :
  - changelog / schéma
  - ou `schema.sql`
- capacité à expliquer les users DB et leurs droits
- démonstration orientée métier

---

## 15. Planning sprint final

### Objectif global
Finir un MVP démontrable en environ deux semaines.

### Ordre réel de travail
1. initialisation backend
2. entités JPA + repositories
3. services réservation
4. paiements et dette
5. statistiques
6. frontend minimal
7. stabilisation et démo

---

## 16. Dernier point d’arrêt

Le dernier bloc terminé est :

- initialisation backend Spring Boot
- configuration `application.yml`
- création de `/api/health`
- exécution locale OK
- premiers tests backend OK

Le prochain travail concret est :

- vérifier / finaliser la PR du bloc d’initialisation backend
- passer sur l’issue entités JPA + repositories
- créer la branche `back/entities-repositories`
- coder les premières entités
- écrire les premiers tests repository
