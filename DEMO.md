# Démo — Padel Marius

## 1. Objectif de la démo

Cette démo présente le MVP Padel Marius de manière métier.

Elle doit montrer :

- que le backend démarre ;
- que le frontend démarre ;
- que l'API REST fonctionne ;
- que le frontend appelle le backend ;
- que les règles métier principales sont couvertes ;
- que les tests passent ;
- que GitHub est structuré avec issues, branches, commits et PR.

Durée cible :

```txt
5 à 10 minutes de démo métier
puis questions / réponses
```

---

## 2. Préparation avant l'examen

Avant le début de l'examen :

1. Ouvrir IntelliJ.
2. Ouvrir le dépôt GitHub dans le navigateur.
3. Ouvrir deux terminaux.
4. Démarrer le backend.
5. Démarrer le frontend.
6. Vérifier Swagger.
7. Vérifier que les tests backend passent.
8. Vérifier que le build frontend passe.

---

## 3. Commandes avant démo

### Backend

Terminal 1 :

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Vérifier :

```txt
http://localhost:8080/api/health
```

Swagger :

```txt
http://localhost:8080/swagger-ui.html
```

### Frontend

Terminal 2 :

```powershell
cd frontend
npm start
```

Ouvrir :

```txt
http://localhost:4200
```

---

## 4. Comptes à utiliser

### Joueur principal

```txt
Matricule : G1001
Mot de passe : password
```

Utilisation :

- consulter le solde ;
- consulter les disponibilités ;
- rejoindre un match public ;
- voir les réservations ;
- voir l'historique des transactions.

### Joueur avec dette

```txt
Matricule : G1002
Mot de passe : password
```

Utilisation :

- montrer une dette ouverte ;
- montrer le blocage métier ;
- payer une dette si nécessaire.

### Joueur inactif

```txt
Matricule : G9999
Mot de passe : password
```

Utilisation :

- montrer un refus de connexion.

### Admin global

```txt
Login : admin-global
Mot de passe : secret
```

Utilisation :

- dashboard admin ;
- statistiques globales ;
- fermeture globale ;
- traitement de veille ;
- traitement d'échéance.

### Admin site Bruxelles

```txt
Login : admin-bruxelles
Mot de passe : secret-site
```

Utilisation :

- montrer le rôle `SITE` ;
- expliquer qu'il est limité à son site.

### Admin site Namur

```txt
Login : admin-namur
Mot de passe : secret-site
```

Utilisation :

- montrer le deuxième administrateur de site.

---

## 5. Scénario principal de démo

### Étape 1 — Montrer le backend

Dans le navigateur :

```txt
http://localhost:8080/api/health
```

Dire :

```txt
Le backend Spring Boot expose une REST API.
Le endpoint /api/health confirme que le backend tourne.
```

Puis ouvrir :

```txt
http://localhost:8080/swagger-ui.html
```

Dire :

```txt
OpenAPI / Swagger permet de visualiser les endpoints HTTP.
```

---

### Étape 2 — Montrer le frontend

Ouvrir :

```txt
http://localhost:4200
```

Dire :

```txt
Le frontend Angular est séparé du backend.
Il appelle uniquement l'API REST.
Il ne contient pas de SQL.
Il ne se connecte pas à la base.
```

---

### Étape 3 — Connexion joueur

Aller dans :

```txt
Connexion joueur
```

Utiliser :

```txt
G1001 / password
```

Montrer :

- joueur connecté ;
- menu joueur visible ;
- routes joueur disponibles.

Dire :

```txt
Le joueur s'identifie avec son matricule.
Le mot de passe est vérifié côté backend.
```

---

### Étape 4 — Consulter le solde

Aller dans :

```txt
Mon solde
```

Montrer :

```txt
Solde crédit : 100.00 €
```

Dire :

```txt
Le solde crédit est porté par le membre.
Les paiements débitent ce solde.
Les remboursements le recréditent.
```

---

### Étape 5 — Consulter les disponibilités

Aller dans :

```txt
Organiser un match
```

Choisir :

```txt
Site : Padel Bruxelles
Date : 2026-06-20
```

Cliquer :

```txt
Voir les créneaux disponibles
```

Montrer :

- horaires du site ;
- terrains ;
- créneaux disponibles ;
- durée 1h30 ;
- logique backend.

Dire :

```txt
Les disponibilités sont calculées côté backend.
Le backend tient compte des horaires, terrains, fermetures et matches existants.
```

---

### Étape 6 — Rejoindre un match public

Aller dans :

```txt
Rejoindre un match public
```

Choisir :

```txt
Site : Padel Bruxelles
Date : 2026-06-20
```

Cliquer :

```txt
Rechercher les matches publics
```

Puis :

```txt
Rejoindre et payer 15 €
```

Montrer :

- paiement réalisé ;
- solde restant ;
- place confirmée.

Dire :

```txt
Dans un match public, premier payé = premier servi.
La validation est immédiate après paiement.
```

---

### Étape 7 — Voir les réservations

Aller dans :

```txt
Mes réservations
```

Montrer :

- match organisé ;
- match rejoint ;
- statut de participation ;
- état du match.

Dire :

```txt
Le joueur voit ses réservations sans saisir de SQL ni accéder à la base.
```

---

### Étape 8 — Historique des transactions

Aller dans :

```txt
Historique des transactions
```

Montrer :

- paiements de participation ;
- paiements de dette si présents ;
- montants.

Dire :

```txt
Les paiements sont tracés côté backend.
Ils alimentent aussi les statistiques admin.
```

---

### Étape 9 — Dette organisateur

Se connecter avec :

```txt
G1002 / password
```

Aller dans :

```txt
Mes dettes
```

Montrer :

- dette ouverte ;
- montant restant.

Dire :

```txt
Si un match n'est pas totalement payé, l'organisateur porte le solde dû.
Une dette ouverte bloque une nouvelle réservation.
```

Si possible, montrer le paiement de dette.

---

### Étape 10 — Connexion admin

Aller dans :

```txt
Connexion admin
```

Utiliser :

```txt
admin-global / secret
```

Montrer :

- dashboard admin ;
- rôle `GLOBAL`.

Dire :

```txt
Il existe deux rôles admin : GLOBAL et SITE.
Les routes admin sont protégées côté Angular.
Les endpoints admin sont aussi contrôlés côté backend.
```

---

### Étape 11 — Statistiques admin

Aller dans :

```txt
Statistiques
```

Choisir :

```txt
Période démo complète
```

Cliquer :

```txt
Charger les statistiques
```

Montrer :

- nombre de matches ;
- paiements ;
- chiffre d'affaires ;
- dettes ouvertes ;
- taux de remplissage.

Dire :

```txt
Les statistiques sont calculées côté backend à partir de la base relationnelle.
```

---

### Étape 12 — Jours de fermeture

Aller dans :

```txt
Jours de fermeture
```

Créer une fermeture de démonstration si nécessaire.

Dire :

```txt
Une fermeture peut être globale ou locale.
Elle peut annuler les matches à venir concernés.
Les joueurs ayant payé sont remboursés sur leur solde.
```

---

### Étape 13 — Traitement de veille

Aller dans :

```txt
Traitement de veille
```

Utiliser une date rapide de démonstration.

Dire :

```txt
Le traitement de veille applique les règles à J-1 :
un match privé incomplet peut devenir public,
une place non payée peut être libérée,
une pénalité peut être créée.
```

---

## 6. Cas de refus à montrer si le temps le permet

### Joueur inactif

Utiliser :

```txt
G9999 / password
```

Résultat attendu :

```txt
Connexion refusée
```

---

### Mauvais mot de passe

Utiliser :

```txt
G1001 / mauvais
```

Résultat attendu :

```txt
Connexion refusée
```

---

### Dette ouverte

Utiliser :

```txt
G1002 / password
```

Résultat attendu :

```txt
Dette visible
Nouvelle réservation bloquée si dette active
```

---

### Admin site limité

Utiliser :

```txt
admin-bruxelles / secret-site
```

Dire :

```txt
Cet admin est limité au site Bruxelles.
Un admin SITE ne peut pas gérer globalement tous les sites.
```

---

## 7. Points techniques à montrer

### Architecture

Ouvrir :

```txt
ARCHITECTURE.md
```

Montrer :

```txt
controller -> service -> repository -> DB
```

Dire :

```txt
Les controllers sont minces.
Les services contiennent les règles métier.
Les repositories accèdent à la base.
```

---

### Exploitation

Ouvrir :

```txt
EXPLOITATION.md
```

Montrer :

- commandes backend ;
- commandes frontend ;
- tests ;
- ports ;
- H2 ;
- Swagger ;
- CORS ;
- proxy Angular.

---

### Artefacts DB

Ouvrir :

```txt
docs/db/schema.sql
docs/db/data-demo.sql
docs/db/db-users.md
```

Dire :

```txt
Le script de schéma est fourni comme artefact de remise.
Le seed H2 est automatique.
Les users DB cible sont documentés.
Le frontend n'a aucun accès DB.
```

---

### Tests

Backend :

```powershell
cd backend
.\mvnw.cmd clean test
cd ..
```

Frontend :

```powershell
cd frontend
npm run build
cd ..
```

Cypress si demandé :

```powershell
cd frontend
npm run cypress:run
cd ..
```

---

### GitHub

Montrer :

- issues ;
- branches ;
- commits ;
- pull requests ;
- merges ;
- validations.

Dire :

```txt
Chaque fonctionnalité a été développée via une issue, une branche et une PR.
```

---

## 8. Ce dont je suis le plus fier

Points possibles à dire :

```txt
La logique métier est assez complète pour un MVP.
Les règles de dette, paiement, pénalité et statistiques sont gérées côté backend.
Le frontend reste simple mais démontrable.
Les tests backend couvrent controllers, services et repositories.
Les tests frontend et Cypress existent.
La séparation frontend/backend est respectée.
```

---

## 9. Ce qui a été le plus challengeant

Points possibles à dire :

```txt
La gestion des règles métier autour des dettes et paiements.
Le passage privé vers public à J-1.
La cohérence entre solde, paiements, dettes et réservations.
La protection minimale des routes et rôles admin.
La préparation d'une démo stable.
```

---

## 10. Phrase de conclusion

```txt
Le projet est un MVP fonctionnel de réservation de terrains de padel.
Il respecte la séparation frontend/backend.
Le backend expose une API REST.
La base est relationnelle.
Les règles métier principales sont implémentées côté backend.
Les tests backend, frontend et Cypress permettent de valider les principaux parcours.
```