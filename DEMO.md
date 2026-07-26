# Démo : Padel Marius

## 1. Objectif de la démonstration

Cette démonstration présente le MVP de l'application Padel Marius.

L'objectif est de montrer les principales règles métier du projet :

```txt
réservation de terrains de padel
gestion de plusieurs sites
horaires propres à chaque site
matches publics et privés
paiement des participations
gestion des dettes
blocage d'une nouvelle réservation si une dette est ouverte
blocage d'une nouvelle réservation si une pénalité est active
annulation de matches par fermeture
remboursement sur le solde crédit
statistiques administrateur
séparation frontend / backend / base de données
tests automatisés
GitHub Actions
```

## 2. Préparation avant l'oral

Avant le début de l'examen, préparer :

```txt
backend démarré
frontend démarré
Swagger ouvert
VS Code ou IntelliJ ouvert
GitHub ouvert sur le dépôt
README.md ouvert
ARCHITECTURE.md ouvert
EXPLOITATION.md ouvert
DEMO.md ouvert
docs/db/schema.sql ouvert
docs/db/db-users.md ouvert
```

URLs utiles :

```txt
Frontend : http://localhost:4200
Backend  : http://localhost:8080
Health   : http://localhost:8080/api/health
Swagger  : http://localhost:8080/swagger-ui.html
OpenAPI  : http://localhost:8080/v3/api-docs
```

## 3. Démarrage du projet

### 3.1. Terminal 1 — backend H2

Depuis la racine du projet :

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Résultat attendu :

```txt
Tomcat started on port 8080
Started BackendApplication
```

Vérification rapide :

```txt
http://localhost:8080/api/health
```

Résultat attendu :

```json
{
  "application": "padel-backend",
  "status": "OK"
}
```

### 3.2. Terminal 2 — frontend Angular

Depuis la racine du projet :

```powershell
cd frontend
npm.cmd start
```

Ouvrir :

```txt
http://localhost:4200
```

Le frontend utilise le proxy Angular pour rediriger les appels /api/** vers le backend.

## 4. Comptes de démonstration

### 4.1. Joueurs

```txt
G1001 / password
Joueur GLOBAL actif
Compte conseillé pour le parcours principal

G1002 / password
Joueur GLOBAL actif avec dette ouverte
Compte conseillé pour montrer le blocage par dette

S1001 / password
Joueur SITE Bruxelles

S1002 / password
Joueur SITE Namur

L1001 / password
Joueur LIBRE actif

L1002 / password
Joueur LIBRE avec pénalité active
Compte conseillé pour montrer le blocage par pénalité

G9999 / password
Joueur inactif
Compte conseillé pour montrer un refus de connexion
```

### 4.2. Administrateurs

```txt
admin-global / secret
Administrateur GLOBAL

admin-bruxelles / secret-site
Administrateur SITE Bruxelles

admin-namur / secret-site
Administrateur SITE Namur

admin-inactif / secret
Administrateur inactif
```

## 5. Données utiles pour la démonstration

### 5.1. Sites

```txt
1001 : Padel Bruxelles
1002 : Padel Namur
```

### 5.2. Terrains

```txt
1101 : Bruxelles T1
1102 : Bruxelles T2
1103 : Bruxelles T3

1201 : Namur T1
1202 : Namur T2
```

### 5.3. Dates de démonstration

Les données de démonstration sont relatives à la date du jour.

Cela évite d'avoir des données obsolètes pendant la deuxième session.

Repères utiles :

```txt
match public de démonstration : aujourd'hui + 3 jours
match privé de démonstration  : aujourd'hui + 4 jours
match terminé                 : aujourd'hui - 7 jours
fermeture globale démo        : aujourd'hui + 10 jours
fermeture locale démo         : aujourd'hui + 15 jours
pénalité active démo          : aujourd'hui - 1 jour à aujourd'hui + 6 jours
```

## 6. Scénario principal : 5 à 10 minutes

### Étape 1 — Montrer que le backend fonctionne

Ouvrir :

```txt
http://localhost:8080/api/health
```

À dire :

Le backend Spring Boot est démarré. Il expose une API REST utilisée par le frontend Angular.

Puis ouvrir Swagger :

```txt
http://localhost:8080/swagger-ui.html
```

À dire :

Swagger permet de visualiser les endpoints REST disponibles. Le frontend ne se connecte jamais directement à la base, il passe uniquement par ces endpoints.

### Étape 2 — Connexion joueur

Ouvrir le frontend :

```txt
http://localhost:4200
```

Aller dans l'espace joueur.

Se connecter avec :

```txt
Matricule : G1001
Mot de passe : password
```

À montrer :

```txt
connexion réussie
menu joueur visible
accès aux pages joueur
joueur identifié par son matricule métier
```

À dire :

Le joueur n'a pas de login séparé. Son matricule est son identifiant métier. Le mot de passe sert au mécanisme d'authentification, puis le backend renvoie un JWT.

### Étape 3 — Consulter le solde joueur

Depuis l'espace joueur, ouvrir :

```txt
Mon solde
```

À montrer :

```txt
matricule du joueur
solde crédit disponible
```

Le solde crédit est géré côté backend. Il est débité lors d'un paiement de participation ou de dette, et il peut être recrédité lors d'un remboursement.

### Étape 4 — Consulter les disponibilités

Ouvrir :

```txt
Organiser un match
```

Utiliser :

```txt
Site : Padel Bruxelles / 1001
Date : une date future disponible via les boutons rapides
```

Cliquer sur :

```txt
Voir les créneaux disponibles
```

À montrer :

```txt
créneaux disponibles
terrains du site
horaires propres au site
durée de match de 1h30
pause de 15 minutes entre deux matches
```

Les disponibilités sont calculées par le backend. Le calcul tient compte des horaires annuels du site, des fermetures, des terrains actifs, des matches existants et des matches annulés.

### Étape 5 — Créer un match

Depuis un créneau disponible, cliquer sur :

```txt
Utiliser ce créneau pour créer un match
```

Créer un match avec :

```txt
Type : Public
Organisateur : G1001
```

À montrer :

```txt
match créé
réservation du terrain
participation organisateur créée
état du match À venir
```

Un match correspond à une réservation de terrain. Le backend vérifie les règles métier avant d'autoriser la création.

Règles contrôlées côté backend :

```txt
terrain actif
site actif
créneau disponible
match dans le futur
fenêtre de réservation selon la catégorie du membre
absence de dette ouverte
absence de pénalité active réelle
absence de conflit horaire
```

### Étape 6 — Consulter les réservations

Ouvrir :

```txt
Mes réservations
```

À montrer :

```txt
match créé
site
terrain
date et heure
rôle Organisateur
statut de participation
état du match
```

La réservation est stockée côté backend et récupérée via l'API. Le frontend affiche les données reçues, mais ne manipule pas directement la base.

### Étape 7 — Rejoindre un match public

Se déconnecter puis se connecter avec un autre joueur actif, par exemple :

```txt
L1001 / password
```

Ouvrir :

```txt
Rejoindre un match public
```

Choisir :

```txt
Site : Padel Bruxelles / 1001
Date : date du match public de démonstration
```

Cliquer sur :

```txt
Rechercher les matches publics
```

Puis rejoindre un match public disponible.

À montrer :

```txt
liste des matches publics
places disponibles
paiement de 15 euros
participation confirmée après paiement
```

Dans un match public, le principe est premier payé, premier servi. Le joueur doit rejoindre lui-même le match public. L'organisateur ne réserve pas une place à sa place.

### Étape 8 — Montrer le blocage par dette

Se connecter avec :

```txt
G1002 / password
```

Essayer d'organiser un nouveau match.

Résultat attendu :

```txt
la création est refusée parce que ce joueur a une dette ouverte
```

À dire :

Une dette ouverte bloque l'organisation d'un nouveau match. Cette règle protège le club contre les réservations non payées.

### Étape 9 — Consulter et payer une dette

Avec le joueur :

```txt
G1002 / password
```

Ouvrir :

```txt
Mes dettes
```

À montrer :

```txt
dette ouverte
montant restant
possibilité de payer la dette
```

Payer la dette si le solde est suffisant.

Le paiement de dette débite le solde du joueur. Une dette réglée ne bloque plus une nouvelle réservation.

### Étape 10 — Montrer le blocage par pénalité

Se connecter avec :

```txt
L1002 / password
```

Essayer d'organiser un nouveau match.

Résultat attendu :

```txt
la création est refusée parce que ce joueur a une pénalité active
```

Une pénalité active bloque temporairement l'organisation d'un nouveau match. Le backend vérifie aussi la date de fin : une pénalité expirée ne doit plus bloquer le joueur.

### Étape 11 — Connexion administrateur

Aller dans :

```txt
/admin/login
```

Se connecter avec :

```txt
admin-global / secret
```

À montrer :

```txt
dashboard administrateur
rôle GLOBAL
accès aux statistiques
accès à la liste des membres
accès aux fermetures
accès aux traitements
```

Il existe deux types d'administrateurs : GLOBAL et SITE. Un administrateur GLOBAL peut voir tous les sites. Un administrateur SITE est limité à son propre site.

### Étape 12 — Statistiques administrateur

Dans l'espace administrateur, ouvrir :

```txt
Statistiques
```

Utiliser :

```txt
Période démo complète
```

ou une période relative autour de la date du jour.

À montrer :

```txt
nombre de matches
chiffre d'affaires
dettes ouvertes
taux de remplissage
participations actives
```

Les statistiques sont calculées côté backend à partir des matches, des participations, des paiements et des dettes. Les matches annulés et les paiements annulés ne doivent pas fausser le chiffre d'affaires.

### Étape 13 — Fermeture administrateur

Dans l'espace administrateur, ouvrir :

```txt
Fermetures
```

Créer une fermeture locale ou globale sur une date future.

À montrer :

```txt
création de fermeture
matches concernés annulés
remboursements crédités si des paiements existaient
disponibilités bloquées pour cette date
```

La fermeture est une règle admin. Elle peut être globale ou locale. Lorsqu'elle annule des matches à venir, les joueurs payés sont remboursés sur leur solde crédit et les paiements concernés sont annulés.

### Étape 14 — Traitements administrateur

Ouvrir :

```txt
Traitement de veille
```

À montrer :

```txt
date de traitement
matches analysés
participations libérées
matches passés publics
```

Le traitement de veille applique les règles avant les matches, notamment le passage public d'un match privé incomplet et la libération des participations non payées.

Ouvrir aussi, si disponible dans l'interface ou via Swagger :

```http
POST /api/admin/matches/traitement-echeance
```

Le traitement d'échéance fait évoluer le cycle des matches : A_VENIR vers DEMARRE, puis DEMARRE vers TERMINE. Il peut aussi déclencher les dettes et les pénalités selon l'état du match.

### Étape 15 — Montrer la documentation technique

Ouvrir rapidement :

```txt
README.md
ARCHITECTURE.md
EXPLOITATION.md
DEMO.md
docs/db/README.md
docs/db/schema.sql
docs/db/db-users.md
```

```txt
README.md présente le projet.
ARCHITECTURE.md explique les couches frontend et backend.
EXPLOITATION.md explique comment démarrer et tester le projet.
DEMO.md décrit le scénario de démonstration.
docs/db/schema.sql fournit l'artefact de schéma SQL.
docs/db/db-users.md explique les utilisateurs DB et leurs droits.
```

### Étape 16 — Montrer GitHub

Ouvrir le dépôt GitHub.

À montrer :

```txt
issues
branches
commits
pull requests
GitHub Actions
```

J'ai travaillé par issues, branches et pull requests. La branche main représente l'état stable du projet. Les dernières Pull Requests ont corrigé les règles métier, ajouté des tests d'intégration, ajouté Cypress full stack, uniformisé les erreurs API et ajouté une CI GitHub Actions.

### Étape 17 — Montrer les tests

Depuis la racine du projet :

Tests backend :

```powershell
cd backend
.\mvnw.cmd clean test
cd ..
```

Les tests backend couvrent les controllers, les services, les repositories, la sécurité, la configuration et un happy flow d'intégration backend.

Build frontend :

```powershell
cd frontend
npm.cmd run build
cd ..
```

Tests frontend :

```powershell
cd frontend
npm.cmd run test -- --watch=false
cd ..
```

Cypress mocké :

```powershell
cd frontend
npm.cmd run cypress:run
cd ..
```

Cypress full stack :

```powershell
cd frontend
npm.cmd run cypress:run:fullstack
cd ..
```

Cette commande démarre automatiquement Spring Boot avec H2, Angular et
Cypress, puis arrête les processus lancés.

Les Cypress mockés valident les principaux parcours UI avec API simulée. Le Cypress full stack valide un vrai parcours Angular vers Spring Boot et H2.

## 7. Points métier à insister pendant l'oral

### Réservation

```txt
un match est une réservation d'un terrain
un match dure 1h30
il y a 15 minutes entre deux matches
le backend vérifie les conflits horaires
```

### Membres

```txt
GLOBAL : réservation jusqu'à 21 jours avant le match, tous sites
SITE   : réservation jusqu'à 14 jours avant le match, uniquement son site
LIBRE  : réservation jusqu'à 5 jours avant le match, tous sites
```

### Paiement

```txt
un match coûte 60 euros
une participation standard coûte 15 euros
le paiement confirme la participation
le solde crédit est débité côté backend
```

### Dette

```txt
si le match n'est pas entièrement payé, l'organisateur porte le solde
une dette ouverte bloque une nouvelle réservation
une dette réglée ne bloque plus la création d'un match
```

### Pénalité

```txt
un match privé incomplet peut entraîner une pénalité
une pénalité active bloque une nouvelle réservation
une pénalité expirée ne doit plus bloquer le joueur
```

### Fermeture

```txt
une fermeture peut être globale ou locale
une fermeture annule les matches à venir concernés
les joueurs payés sont remboursés sur leur solde crédit
les matches annulés ne bloquent plus les disponibilités
```

### Administration

```txt
un administrateur GLOBAL gère tous les sites
un administrateur SITE est limité à son site
les endpoints admin exigent un JWT admin
```

## 8. Architecture à expliquer simplement

```txt
Frontend Angular
        |
        | HTTP REST / JSON via /api/**
        v
Backend Spring Boot REST API
        |
        | JPA / Repositories
        v
Base de données relationnelle
```

Le frontend ne contient aucun SQL et ne connaît aucun identifiant de base de données. Il appelle uniquement le backend via l'API REST. Le backend applique les règles métier dans ses services et accède à la base via les repositories JPA.

## 9. H2 et PostgreSQL à expliquer

### H2 par défaut

```txt
H2 est utilisé par défaut.
La base est en mémoire.
Elle démarre automatiquement avec le backend.
Le schéma est créé par la migration Liquibase initiale.
Hibernate valide le schéma sans le modifier.
Les données sont seedées par le changeset Liquibase data.sql.
Les données sont relatives à la date du jour.
```

### PostgreSQL Docker optionnel

```txt
PostgreSQL Docker est disponible en option.
Il montre une base locale plus réaliste.
Le backend utilise le profil postgres.
Le fichier data.sql n'est pas exécuté sur PostgreSQL.
Le seed PostgreSQL est fait par PostgresDemoDataSeeder.
```

Commande PostgreSQL sous PowerShell :

```powershell
docker compose up -d postgres
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

## 10. Sécurité

```text
mots de passe hachés avec BCrypt
politique de mot de passe pour les nouvelles inscriptions
connexion joueur par matricule et mot de passe
connexion administrateur par login et mot de passe
JWT signé et limité dans le temps
SecurityFilterChain stateless
filtre JWT OncePerRequestFilter
SecurityContext Spring
contrôles par rôles et @PreAuthorize
guards Angular pour la navigation
session unique synchronisée entre les onglets
backend responsable de l'autorisation définitive
```

Limites assumées pour le MVP :

```text
pas de refresh token
pas de révocation serveur d'un JWT déjà émis
secret local de démonstration à remplacer dans un déploiement réel
```

## 11. Contrat d'erreur API

Le backend renvoie les erreurs importantes avec :

```json
{
  "code": "...",
  "message": "..."
}
```

Exemples de codes :

```txt
RESSOURCE_INTROUVABLE
CONFIGURATION_METIER_INVALIDE
AUTHENTIFICATION_INVALIDE
ACCES_REFUSE
VALIDATION_INVALIDE
REQUETE_INVALIDE
JSON_INVALIDE
```

J'ai uniformisé les erreurs API pour que le frontend reçoive toujours un format simple avec un code et un message. Cela évite d'afficher des erreurs techniques brutes.

## 16. Mini check-list avant l'examen

- [ ] Backend démarré
- [ ] Frontend démarré
- [ ] Health check ouvert
- [ ] Swagger ouvert
- [ ] GitHub ouvert
- [ ] README.md ouvert
- [ ] ARCHITECTURE.md ouvert
- [ ] EXPLOITATION.md ouvert
- [ ] DEMO.md ouvert
- [ ] docs/db/schema.sql ouvert
- [ ] docs/db/db-users.md ouvert
- [ ] IntelliJ ou VS Code ouvert
- [ ] Terminal prêt pour les tests backend
- [ ] Terminal prêt pour les tests frontend
- [ ] Navigateur prêt sur http://localhost:4200
