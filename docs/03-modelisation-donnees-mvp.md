# Modélisation des données du MVP

## 1. Objectif du document

Ce document décrit les entités métier principales du MVP, leurs relations, leurs cardinalités et les contraintes métier importantes.

L’objectif est de préparer la future base de données et le backend, sans encore écrire le schéma SQL final.

## 2. Entités métier

### 2.1. Site
**Rôle métier :** représente un club ou lieu d’exploitation.

**Informations importantes :**
- idSite
- codeSite
- nom
- adresse
- actif

### 2.2. Terrain
**Rôle métier :** représente un terrain de padel réservable.

**Informations importantes :**
- idTerrain
- numeroTerrain
- actif
- site

### 2.3. HoraireAnnuelSite
**Rôle métier :** définit les heures de réservation d’un site pour une année civile.

**Informations importantes :**
- idHoraireAnnuel
- anneeCivile
- heureDebutReservation
- heureFinReservation
- site

### 2.4. Fermeture
**Rôle métier :** représente un jour où les réservations sont impossibles.

**Informations importantes :**
- idFermeture
- dateFermeture
- portee (GLOBALE ou LOCALE)
- motif
- site (optionnel)

### 2.5. Membre
**Rôle métier :** représente un joueur connu du système.

**Informations importantes :**
- idMembre
- matricule
- nom
- prenom
- categorieMembre (GLOBAL, SITE, LIBRE)
- siteRattachement (optionnel)
- actif

### 2.6. Administrateur
**Rôle métier :** représente un utilisateur d’administration.

**Informations importantes :**
- idAdministrateur
- nom
- prenom
- emailOuLogin
- roleAdministrateur (GLOBAL, SITE)
- site (optionnel)
- actif

### 2.7. Match
**Rôle métier :** représente la réservation réelle d’un terrain pour jouer un match.

**Informations importantes :**
- idMatch
- terrain
- dateHeureDebut
- modeCreation (PRIVE ou PUBLIC)
- visibiliteCourante (PRIVE ou PUBLIC)
- prixTotalApplique
- dateCreation
- datePassagePublic (optionnel)
- etatCycle (A_VENIR, DEMARRE, TERMINE)

### 2.8. Participation
**Rôle métier :** relie un membre à un match.

**Informations importantes :**
- idParticipation
- match
- membre
- roleParticipation (ORGANISATEUR, JOUEUR)
- modeEntree (CREATION, INVITATION_PRIVEE, INSCRIPTION_PUBLIQUE)
- statutParticipation (EN_ATTENTE_PAIEMENT, CONFIRMEE, LIBEREE)
- dateAffectation
- dateConfirmation
- dateLiberation

### 2.9. Paiement
**Rôle métier :** trace un encaissement métier.

**Informations importantes :**
- idPaiement
- membre
- naturePaiement (PARTICIPATION, REGLEMENT_DETTE)
- montant
- dateHeurePaiement
- statutPaiement
- participation (optionnel)
- dette (optionnel)

### 2.10. Dette
**Rôle métier :** représente le solde restant à charge de l’organisateur si le match n’est pas complet.

**Informations importantes :**
- idDette
- match
- membreResponsable
- montantInitial
- montantRestant
- dateCreation
- dateReglement
- statutDette (OUVERTE, REGLEE)

### 2.11. Pénalité
**Rôle métier :** représente la sanction appliquée à l’organisateur quand le match privé n’atteint pas le nombre requis de joueurs.

**Informations importantes :**
- idPenalite
- membre
- matchSource
- typePenalite
- motif
- dateDebut
- dateFin
- statutPenalite

## 3. Relations entre les entités

- un Site possède plusieurs Terrains
- un Site possède plusieurs HorairesAnnuelsSite
- un Site peut avoir plusieurs Fermetures locales
- un Site peut être le site de rattachement de plusieurs Membres
- un Site peut avoir plusieurs Administrateurs de site
- un Terrain appartient à un Site
- un Terrain peut accueillir plusieurs Matches
- un Match se joue sur un seul Terrain
- un Match possède plusieurs Participations
- une Participation relie un Membre à un Match
- une Participation peut avoir un Paiement
- un Match peut générer une Dette
- une Dette concerne un Membre responsable
- une Dette peut avoir un Paiement de règlement
- un Membre peut avoir plusieurs Pénalités
- une Pénalité est liée à un Match source

## 4. Cardinalités

- un Site possède de 1 à N Terrains
- un Terrain appartient à 1 seul Site

- un Site possède de 1 à N HorairesAnnuelsSite
- un HoraireAnnuelSite appartient à 1 seul Site

- un Site possède de 0 à N Fermetures locales
- une Fermeture locale concerne 1 seul Site
- une Fermeture globale n’est liée à aucun Site

- un Site peut être le site de rattachement de 0 à N Membres
- un Membre a 0 ou 1 Site de rattachement

- un Site peut avoir 0 à N Administrateurs
- un Administrateur de site appartient à 1 seul Site
- un Administrateur global n’est lié à aucun Site

- un Terrain peut accueillir 0 à N Matches
- un Match se joue sur 1 seul Terrain

- un Match possède de 1 à 4 Participations
- une Participation appartient à 1 seul Match

- un Membre peut avoir 0 à N Participations
- une Participation concerne 1 seul Membre

- une Participation a 0 ou 1 Paiement
- un Paiement de participation concerne 1 seule Participation

- un Match peut générer 0 ou 1 Dette
- une Dette concerne 1 seul Match

- un Membre peut avoir 0 à N Dettes
- une Dette concerne 1 seul Membre responsable

- une Dette a 0 ou 1 Paiement de règlement
- un Paiement de dette concerne 1 seule Dette

- un Membre peut avoir 0 à N Pénalités
- une Pénalité concerne 1 seul Membre

- un Match peut être la source de 0 ou 1 Pénalité
- une Pénalité provient de 1 seul Match

## 5. Contraintes métier importantes

### 5.1. Contraintes d’unicité
- le matricule d’un membre est unique
- le code d’un site est unique
- le numéro d’un terrain est unique dans un site
- il n’existe qu’un horaire annuel par couple (site, année)
- une fermeture globale est unique pour une date
- une fermeture locale est unique pour un couple (site, date)

### 5.2. Contraintes sur les membres et administrateurs
- un membre de type SITE doit avoir un site de rattachement
- un membre GLOBAL ou LIBRE n’a pas besoin de site de rattachement
- un administrateur SITE doit être rattaché à un site
- un administrateur GLOBAL n’est pas rattaché à un site précis

### 5.3. Contraintes sur les matches et participations
- un match dure 1h30
- il doit y avoir 15 minutes entre deux matches sur un même terrain
- un match ne peut jamais dépasser 4 joueurs
- un match doit avoir exactement 1 organisateur
- un membre ne peut apparaître qu’une seule fois dans un même match
- un membre ne peut pas être inscrit à deux matches qui se chevauchent

### 5.4. Contraintes sur les paiements
- un match coûte 60 euros
- une part standard vaut 15 euros
- un paiement concerne soit une participation, soit une dette
- une place publique est confirmée uniquement après paiement
- une place non payée peut être libérée

### 5.5. Contraintes sur les dettes et pénalités
- un match peut générer une dette si le montant total payé est inférieur à 60 euros
- une dette ouverte bloque la création d’un nouveau match pour l’organisateur concerné
- une pénalité peut bloquer la création d’un nouveau match pendant 7 jours

## 6. Cas métier couverts

Le modèle permet de représenter :
- un match privé
- un match public
- la bascule d’un match privé vers public
- la libération d’une place non payée
- la dette de l’organisateur
- l’administration multisite

## 7. Schéma visuel

Image à ajouter après l’upload du fichier `docs/diagrammes/mcd-mvp.png`.
