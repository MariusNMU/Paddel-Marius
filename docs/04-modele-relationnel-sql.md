# Modèle relationnel SQL du MVP

## 1. Objectif

Ce document transforme le modèle conceptuel de données du MVP en modèle relationnel exploitable pour la base SQL et le backend Java.

## 2. Conventions retenues

- noms de tables en snake_case
- clé primaire technique `id` sur chaque table
- clés étrangères nommées avec le suffixe `_id`
- types métier (catégories, statuts, rôles) stockés en `VARCHAR` dans le MVP
- table `padel_match` utilisée au lieu de `match` pour éviter un nom ambigu en SQL
- certaines règles métier seront contrôlées par le backend et pas uniquement par la base

## 3. Tables du modèle relationnel

### 3.1. Table `site`
- `id` : BIGINT, PK
- `code` : VARCHAR(20), NOT NULL, UNIQUE
- `nom` : VARCHAR(100), NOT NULL
- `adresse` : VARCHAR(255), NOT NULL
- `actif` : BOOLEAN, NOT NULL

### 3.2. Table `terrain`
- `id` : BIGINT, PK
- `site_id` : BIGINT, NOT NULL, FK vers `site(id)`
- `numero` : VARCHAR(20), NOT NULL
- `actif` : BOOLEAN, NOT NULL

**Contraintes :**
- UNIQUE (`site_id`, `numero`)

### 3.3. Table `horaire_annuel_site`
- `id` : BIGINT, PK
- `site_id` : BIGINT, NOT NULL, FK vers `site(id)`
- `annee_civile` : INT, NOT NULL
- `heure_debut_reservation` : TIME, NOT NULL
- `heure_fin_reservation` : TIME, NOT NULL

**Contraintes :**
- UNIQUE (`site_id`, `annee_civile`)

### 3.4. Table `fermeture`
- `id` : BIGINT, PK
- `date_fermeture` : DATE, NOT NULL
- `portee` : VARCHAR(20), NOT NULL
- `motif` : VARCHAR(255), NULL
- `site_id` : BIGINT, NULL, FK vers `site(id)`

**Règle métier :**
- si `portee = GLOBALE`, alors `site_id` est NULL
- si `portee = LOCALE`, alors `site_id` est obligatoire

### 3.5. Table `membre`
- `id` : BIGINT, PK
- `matricule` : VARCHAR(10), NOT NULL, UNIQUE
- `nom` : VARCHAR(100), NOT NULL
- `prenom` : VARCHAR(100), NOT NULL
- `categorie_membre` : VARCHAR(20), NOT NULL
- `site_rattachement_id` : BIGINT, NULL, FK vers `site(id)`
- `actif` : BOOLEAN, NOT NULL

**Règle métier :**
- si `categorie_membre = SITE`, alors `site_rattachement_id` est obligatoire

### 3.6. Table `administrateur`
- `id` : BIGINT, PK
- `nom` : VARCHAR(100), NOT NULL
- `prenom` : VARCHAR(100), NOT NULL
- `email_ou_login` : VARCHAR(150), NOT NULL, UNIQUE
- `role_administrateur` : VARCHAR(20), NOT NULL
- `site_id` : BIGINT, NULL, FK vers `site(id)`
- `actif` : BOOLEAN, NOT NULL

**Règle métier :**
- si `role_administrateur = SITE`, alors `site_id` est obligatoire

### 3.7. Table `padel_match`
- `id` : BIGINT, PK
- `terrain_id` : BIGINT, NOT NULL, FK vers `terrain(id)`
- `date_heure_debut` : TIMESTAMP, NOT NULL
- `date_heure_fin` : TIMESTAMP, NOT NULL
- `mode_creation` : VARCHAR(20), NOT NULL
- `visibilite_courante` : VARCHAR(20), NOT NULL
- `prix_total` : DECIMAL(10,2), NOT NULL
- `date_creation` : TIMESTAMP, NOT NULL
- `date_passage_public` : TIMESTAMP, NULL
- `etat_cycle` : VARCHAR(20), NOT NULL

**Valeur par défaut recommandée :**
- `prix_total = 60.00`

### 3.8. Table `participation`
- `id` : BIGINT, PK
- `match_id` : BIGINT, NOT NULL, FK vers `padel_match(id)`
- `membre_id` : BIGINT, NOT NULL, FK vers `membre(id)`
- `role_participation` : VARCHAR(20), NOT NULL
- `mode_entree` : VARCHAR(30), NOT NULL
- `statut_participation` : VARCHAR(30), NOT NULL
- `date_affectation` : TIMESTAMP, NOT NULL
- `date_confirmation` : TIMESTAMP, NULL
- `date_liberation` : TIMESTAMP, NULL

**Contraintes :**
- UNIQUE (`match_id`, `membre_id`)

### 3.9. Table `paiement`
- `id` : BIGINT, PK
- `membre_id` : BIGINT, NOT NULL, FK vers `membre(id)`
- `nature_paiement` : VARCHAR(30), NOT NULL
- `montant` : DECIMAL(10,2), NOT NULL
- `date_heure_paiement` : TIMESTAMP, NOT NULL
- `statut_paiement` : VARCHAR(20), NOT NULL
- `participation_id` : BIGINT, NULL, FK vers `participation(id)`
- `dette_id` : BIGINT, NULL, FK vers `dette(id)`

**Contraintes :**
- UNIQUE (`participation_id`)
- UNIQUE (`dette_id`)

**Règle métier :**
- un paiement concerne soit une participation, soit une dette, jamais les deux en même temps

### 3.10. Table `dette`
- `id` : BIGINT, PK
- `match_id` : BIGINT, NOT NULL, FK vers `padel_match(id)`
- `membre_responsable_id` : BIGINT, NOT NULL, FK vers `membre(id)`
- `montant_initial` : DECIMAL(10,2), NOT NULL
- `montant_restant` : DECIMAL(10,2), NOT NULL
- `date_creation` : TIMESTAMP, NOT NULL
- `date_reglement` : TIMESTAMP, NULL
- `statut_dette` : VARCHAR(20), NOT NULL

**Contraintes :**
- UNIQUE (`match_id`)

### 3.11. Table `penalite`
- `id` : BIGINT, PK
- `membre_id` : BIGINT, NOT NULL, FK vers `membre(id)`
- `match_source_id` : BIGINT, NOT NULL, FK vers `padel_match(id)`
- `type_penalite` : VARCHAR(50), NOT NULL
- `motif` : VARCHAR(255), NOT NULL
- `date_debut` : TIMESTAMP, NOT NULL
- `date_fin` : TIMESTAMP, NOT NULL
- `statut_penalite` : VARCHAR(20), NOT NULL

## 4. Résumé des relations

- `terrain.site_id` -> `site.id`
- `horaire_annuel_site.site_id` -> `site.id`
- `fermeture.site_id` -> `site.id`
- `membre.site_rattachement_id` -> `site.id`
- `administrateur.site_id` -> `site.id`
- `padel_match.terrain_id` -> `terrain.id`
- `participation.match_id` -> `padel_match.id`
- `participation.membre_id` -> `membre.id`
- `paiement.membre_id` -> `membre.id`
- `paiement.participation_id` -> `participation.id`
- `paiement.dette_id` -> `dette.id`
- `dette.match_id` -> `padel_match.id`
- `dette.membre_responsable_id` -> `membre.id`
- `penalite.membre_id` -> `membre.id`
- `penalite.match_source_id` -> `padel_match.id`

## 5. Contraintes d’unicité importantes

- `site.code` unique
- `membre.matricule` unique
- `administrateur.email_ou_login` unique
- (`terrain.site_id`, `terrain.numero`) unique
- (`horaire_annuel_site.site_id`, `horaire_annuel_site.annee_civile`) unique
- (`participation.match_id`, `participation.membre_id`) unique
- `dette.match_id` unique
- `paiement.participation_id` unique dans le MVP
- `paiement.dette_id` unique dans le MVP

## 6. Règles gérées surtout par le backend

Les règles suivantes ne seront pas portées uniquement par SQL. Elles seront surtout contrôlées dans les services backend :

- maximum 4 participations par match
- exactement 1 organisateur par match
- interdiction de chevauchement de deux matches sur un même terrain
- respect des 15 minutes entre deux matches sur un même terrain
- interdiction pour un joueur d’être inscrit à deux matches qui se chevauchent
- respect de la fenêtre de réservation selon la catégorie du membre
- blocage d’une nouvelle réservation si dette active
- blocage d’une nouvelle réservation si pénalité active
- transformation d’un match privé en public à J-1
- libération d’une place non payée à J-1
- calcul de la dette de l’organisateur si le total payé est inférieur à 60 euros

## 7. Ordre de création technique recommandé

1. `site`
2. `terrain`
3. `horaire_annuel_site`
4. `fermeture`
5. `membre`
6. `administrateur`
7. `padel_match`
8. `participation`
9. `dette`
10. `penalite`
11. `paiement`

## 8. Conclusion

Ce modèle relationnel est volontairement simple pour accélérer le démarrage du backend tout en couvrant les besoins métier du MVP.
