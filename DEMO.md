# Démonstration — Padel Marius

## 1. Objectif de la démonstration

Cette démonstration présente le MVP de l'application Padel Marius.

L'objectif est de montrer les principales règles métier du projet :

- réservation de terrains de padel ;
- gestion de plusieurs sites ;
- horaires propres à chaque site ;
- matches publics et privés ;
- paiement des participations ;
- gestion des dettes ;
- blocage d'une nouvelle réservation si une dette est ouverte ;
- blocage d'une nouvelle réservation si une pénalité est active ;
- statistiques administrateur ;
- séparation frontend / backend / base de données.

La démonstration dure environ 5 à 10 minutes.

---

## 2. Prérequis avant la démonstration

Avant de commencer la démonstration, les deux applications doivent être démarrées.

### Backend

Depuis la racine du projet :
powershell 
``` 
cd backend
.\mvnw.cmd spring-boot:run

Le backend démarre sur :

http://localhost:8080

Vérification rapide :

http://localhost:8080/api/health

Swagger est disponible ici :

http://localhost:8080/swagger-ui.html
Frontend

Dans un deuxième terminal :

cd frontend
npm install
npm start

Le frontend démarre sur :

http://localhost:4200
```
##3. Comptes de démonstration 
```
Joueurs
G1001 / password
Joueur GLOBAL actif

G1002 / password
Joueur GLOBAL actif avec dette ouverte

S1001 / password
Joueur SITE Bruxelles

S1002 / password
Joueur SITE Namur

L1001 / password
Joueur LIBRE actif

L1002 / password
Joueur LIBRE avec pénalité active

G9999 / password
Joueur inactif
Administrateurs
admin-global / secret
Administrateur GLOBAL

admin-bruxelles / secret-site
Administrateur SITE Bruxelles

admin-namur / secret-site
Administrateur SITE Namur

admin-inactif / secret
Administrateur inactif 
```
## 4. Données utiles pour la démonstration
Sites
1001 : Padel Bruxelles
1002 : Padel Namur
Terrains
1101 : Bruxelles T1
1102 : Bruxelles T2
1103 : Bruxelles T3

1201 : Namur T1
1202 : Namur T2
Dates utiles
2026-06-20
Date utile pour consulter les disponibilités et les matches publics.

2026-05-01 à 2026-06-30
Période utile pour les statistiques administrateur.
``` ```
## 5. Scénario de démonstration conseillé 
```
Étape 1 — Montrer que le backend fonctionne

Ouvrir dans le navigateur :

http://localhost:8080/api/health

Résultat attendu :

{
  "application": "padel-backend",
  "status": "OK"
}

Puis ouvrir Swagger :

http://localhost:8080/swagger-ui.html


Le backend expose une API REST.
Le frontend Angular consomme uniquement cette API.
Le frontend ne se connecte jamais directement à la base de données.
Étape 2 — Connexion joueur

Ouvrir le frontend :

http://localhost:4200

Aller dans l'espace joueur.

Se connecter avec :

Matricule : G1001
Mot de passe : password

À montrer :

connexion réussie ;
menu joueur visible ;
accès aux pages joueur ;
le joueur est identifié par son matricule métier.



Le matricule reste l'identifiant métier principal du joueur.
Le backend stocke uniquement un hash BCrypt du mot de passe.
Après connexion, le backend renvoie un JWT MVP.
Étape 3 — Consulter le solde joueur

Depuis l'espace joueur, ouvrir :

Mon solde

À montrer :

matricule du joueur ;
solde crédit disponible.


Le paiement d'une participation ou d'une dette débite le solde crédit.
Le frontend affiche seulement les données reçues par l'API.
La logique métier reste dans le backend.
Étape 4 — Consulter les disponibilités

Ouvrir :

Organiser un match

Utiliser :

Site : Padel Bruxelles / 1001
Date : 2026-06-20

Cliquer sur :

Voir les créneaux disponibles

À montrer :

créneaux disponibles ;
terrains du site ;
horaires propres au site ;
durée de match de 1h30 ;
pause de 15 minutes entre les matches.


Le backend calcule les disponibilités.
Il tient compte des horaires annuels du site, des fermetures, des terrains actifs et des matches déjà réservés.
Étape 5 — Créer un match

Depuis un créneau disponible, créer un match.

Exemple :

Terrain : Bruxelles T3
Date : 2026-06-20
Créneau : 13:15 - 14:45
Type : PUBLIC ou PRIVE
Organisateur : G1001

À montrer :

création du match ;
réservation du terrain ;
participation organisateur créée.



Un match correspond à une réservation de terrain.
Le backend vérifie les règles métier avant d'autoriser la création.

Règles contrôlées :

terrain actif ;
site actif ;
créneau disponible ;
match dans le futur ;
fenêtre de réservation selon la catégorie du membre ;
pas de dette ouverte ;
pas de pénalité active ;
pas de conflit horaire.
Étape 6 — Rejoindre un match public

Se connecter avec un joueur actif qui n'est pas déjà dans le match public, par exemple :

L1001 / password

Ouvrir :

Rejoindre un match public

Utiliser :

Site : Padel Bruxelles / 1001
Date : 2026-06-20

Cliquer sur :

Rechercher les matches publics

Puis rejoindre un match public disponible.

À montrer :

liste des matches publics ;
places disponibles ;
paiement de 15 euros ;
validation immédiate après paiement.


Dans un match public, le principe est : premier payé, premier servi.
Le joueur doit rejoindre lui-même le match public.
L'organisateur ne réserve pas à sa place.
Étape 7 — Montrer le blocage par dette

Se connecter avec :

G1002 / password

Essayer d'organiser un nouveau match.

Résultat attendu :

La création est refusée parce que ce joueur a une dette ouverte.

Une dette ouverte bloque l'organisation d'un nouveau match.
Cette règle protège le club contre les réservations non payées.
Étape 8 — Consulter et payer une dette

Avec le joueur :

G1002 / password

Ouvrir :

Mes dettes

À montrer :

dette ouverte ;
montant restant ;
possibilité de payer la dette.

Payer la dette si le solde est suffisant.

Le paiement de dette débite le solde du joueur.
Une dette réglée ne bloque plus une nouvelle réservation.
Étape 9 — Montrer le blocage par pénalité

Se connecter avec :

L1002 / password

Essayer d'organiser un nouveau match.

Résultat attendu :

La création est refusée parce que ce joueur a une pénalité active.

Une pénalité active bloque temporairement l'organisation d'un nouveau match.
Dans le MVP, une pénalité simple dure 7 jours.
Étape 10 — Connexion administrateur

Aller dans :

/admin/login

Se connecter avec :

admin-global / secret

À montrer :

dashboard administrateur ;
rôle GLOBAL ;
accès aux statistiques ;
accès à la liste des membres ;
accès aux fermetures ;
accès aux traitements.


Il existe deux types d'administrateurs : GLOBAL et SITE.
Un administrateur GLOBAL peut voir tous les sites.
Un administrateur SITE est limité à son site.
Étape 11 — Statistiques administrateur

Dans l'espace administrateur, ouvrir :

Statistiques

Utiliser :

Date début : 2026-05-01
Date fin   : 2026-06-30

Charger les statistiques.

À montrer :

nombre de matches ;
chiffre d'affaires ;
dettes ouvertes ;
taux de remplissage ;
participations actives.

Les statistiques sont calculées côté backend.
Elles s'appuient sur les matches, les participations, les paiements et les dettes.
Étape 12 — Administrateur de site

Se déconnecter de l'administrateur global.

Se connecter avec :

admin-bruxelles / secret-site

À montrer :

administrateur SITE ;
accès limité au site Bruxelles ;
impossibilité de gérer globalement tous les sites.

Le backend vérifie les droits administrateur.
Un administrateur SITE ne peut agir que sur son propre site.
Étape 13 — Montrer la documentation technique

Ouvrir rapidement les fichiers :

README.md
ARCHITECTURE.md
EXPLOITATION.md
docs/db/README.md
docs/db/schema.sql
docs/db/db-users.md



README.md présente le projet.
ARCHITECTURE.md explique les couches frontend et backend.
EXPLOITATION.md explique comment démarrer et tester le projet.
docs/db/schema.sql fournit l'artefact de schéma SQL.
docs/db/db-users.md explique les utilisateurs DB et leurs droits.
```
Étape 14 — Montrer GitHub
```

Ouvrir le dépôt GitHub.

À montrer :

issues ;
branches ;
commits ;
pull requests ;
historique de progression.


Chaque fonctionnalité importante a été développée via une issue, une branche et une pull request.
Les commits sont réguliers et découpés par fonctionnalité.
Étape 15 — Montrer les tests

Depuis la racine du projet :

Tests backend
cd backend
.\mvnw.cmd clean test
cd ..


Les tests backend couvrent les controllers, les services, les repositories, la sécurité et la configuration.
Build frontend
cd frontend
npm run build
cd ..
Tests unitaires frontend
cd frontend
npm run test
cd ..
Tests Cypress
cd frontend
npm run cypress:run
cd ..


Les tests Cypress valident les principaux happy flows côté frontend.
6. Points métier à insister pendant l'oral
Réservation
Un match est une réservation d'un terrain.
Un match dure 1h30.
Il y a 15 minutes entre deux matches.
Membres
GLOBAL : réservation jusqu'à 21 jours avant le match, tous sites.
SITE   : réservation jusqu'à 14 jours avant le match, uniquement son site.
LIBRE  : réservation jusqu'à 5 jours avant le match, tous sites.
Paiement
Un match coûte 60 euros.
Une participation standard coûte 15 euros.
Le paiement confirme la participation.
Dette
Si le match n'est pas entièrement payé, l'organisateur porte le solde.
Une dette ouverte bloque une nouvelle réservation.
Pénalité
Un match privé incomplet peut entraîner une pénalité.
Une pénalité active bloque une nouvelle réservation.
Administration
Un administrateur GLOBAL gère tous les sites.
Un administrateur SITE est limité à son site.
7. Architecture à expliquer simplement
Frontend Angular
        |
        | HTTP REST / JSON
        v
Backend Spring Boot
        |
        | JPA / Repositories
        v
Base de données SQL

Le frontend ne contient aucun SQL.
Le frontend ne possède aucun user DB.
Le frontend appelle uniquement l'API REST du backend.
Le backend est le seul composant applicatif qui accède à la base de données.
8. Sécurité à expliquer simplement
Les joueurs se connectent avec matricule + mot de passe.
Les administrateurs se connectent avec login + mot de passe.
Les mots de passe sont stockés sous forme de hash BCrypt.
Après connexion, le backend génère un JWT MVP.
Le frontend ajoute ce JWT dans le header Authorization.
Les routes Angular sont protégées par des guards.
Les opérations administrateur vérifient le rôle côté backend.

Header utilisé :

Authorization: Bearer <token>
```
9. Base de données 
``` 
Base utilisée par défaut :

H2 en mémoire
Seed automatique au démarrage
Aucun script SQL manuel à exécuter pour la démo locale

Artefacts fournis :

docs/db/schema.sql
docs/db/data-demo.sql
docs/db/db-users.md
docs/db/db-users-h2.sql
docker/postgres/init/01-create-users-and-rights.sql

Users DB prévus :

sa              : user H2 local pour la démo
padel_admin     : initialisation Docker PostgreSQL
padel_migration : création / évolution du schéma
padel_app       : user applicatif backend avec droits CRUD
padel_readonly  : user lecture seule


Pour le MVP local, H2 simplifie la démonstration.
Pour une cible plus réaliste, la documentation prévoit des users DB séparés.
Le frontend n'a aucun accès direct à la base.
10. Fin de démonstration


Le MVP couvre les règles principales demandées :
multi-sites, terrains, horaires, fermetures, membres, réservations, paiements, dettes, pénalités, statistiques et séparation frontend/backend.

Le backend respecte une architecture controller / service / repository.
Le frontend Angular communique uniquement avec l'API REST.
Les tests backend sont présents sur controllers, services et repositories.
La base relationnelle est documentée avec un artefact SQL et une explication des users DB.
```