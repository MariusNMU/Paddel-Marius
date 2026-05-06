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

### Fait ou en cours de finalisation GitHub
- modèle relationnel SQL du MVP

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
- plusieurs issues de documentation créées
- issues backend créées

### Issues backend créées
- `[BACK] Initialiser le backend Java et la configuration de base`
- `[BACK] Créer les entités JPA et les repositories`
- `[TEST] Mettre en place la stratégie de tests backend`

### Branche backend actuelle
- `back/init-backend-base`

### Point à vérifier sur GitHub
- vérifier si la PR du modèle relationnel SQL a bien été ouverte et mergée
- si besoin, finaliser ou nettoyer les branches de documentation

---

## 6. Où j’en suis maintenant

Je passe de la phase de documentation à la phase de développement backend.

### Situation actuelle
- le métier est cadré
- le MVP est défini
- la modélisation de données est faite
- le modèle relationnel SQL est prêt ou presque prêt côté documentation
- les issues backend existent
- la branche backend de départ existe
- le backend Spring Boot n’est pas encore réellement généré ou codé

### Choix techniques déjà fixés
- backend : Spring Boot
- build tool : Maven
- base de démarrage : H2 pour aller vite
- dépendances prévues :
  - Spring Web
  - Spring Data JPA
  - Validation
  - H2 Database
  - Lombok

---

## 7. Ce que je dois faire maintenant

### Priorité immédiate
Initialiser le backend Spring Boot.

### Étapes concrètes immédiates
1. générer le projet Spring Boot avec Spring Initializr
2. mettre le projet dans le dossier `backend`
3. ouvrir le projet dans l’IDE
4. configurer `application.yml`
5. créer l’arborescence de base :
   - `config`
   - `controller`
   - `dto`
   - `entity`
   - `repository`
   - `service`
6. ajouter un endpoint simple `/api/health`
7. faire démarrer l’application
8. ajouter les premiers tests :
   - test de démarrage du contexte
   - test controller sur `/api/health`
9. commit / push / PR de la branche `back/init-backend-base`

---

## 8. Ce que je ferai juste après

Après l’initialisation du backend :

1. créer les entités JPA principales
2. créer les repositories
3. écrire les premiers tests repository
4. implémenter la consultation des disponibilités
5. implémenter la création de match privé/public
6. implémenter les paiements, la dette et les statistiques
7. connecter ensuite un frontend Angular simple

---

## 9. Priorité absolue pour la semaine restante

Le projet doit avancer plus vite que prévu.

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

## 10. Tests backend obligatoires

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

## 11. Règles de travail GitHub

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

## 12. Planning sprint final

### Objectif global
Finir un MVP démontrable en environ une semaine.

### Ordre réel de travail
1. initialisation backend
2. entités JPA + repositories
3. services réservation
4. paiements et dette
5. statistiques
6. frontend minimal
7. stabilisation et démo

---

## 13. Dernier point d’arrêt

Le prochain travail concret est :

- me mettre sur la branche `back/init-backend-base`
- générer le projet Spring Boot
- configurer le backend
- créer `/api/health`
- écrire les 2 premiers tests
- pousser le premier vrai code backend sur GitHub

- ## Contraintes professorales supplémentaires confirmées
- aucun accès DB dans le frontend
- backend REST API obligatoire
- séparation stricte controller / service / repository
- tests backend obligatoires sur controller, service, repository
- Git obligatoire avec issues, branches, commits, PR
- script DB à remettre
- démo orientée métier
- users DB avec droits spécifiques à expliquer
- pas de login user, accès joueur par matricule uniquement
