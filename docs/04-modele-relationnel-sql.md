# Modèle relationnel SQL du MVP

Dernière mise à jour : 2026-05-14

## 1. Objectif

Ce document transforme le modèle conceptuel de données du MVP en modèle relationnel exploitable pour la base SQL et le backend Java.

Il sert aussi d’explication pour l’examen : le professeur doit pouvoir comprendre les tables, les clés, les relations et les règles gérées par le backend.

Le script SQL de remise est disponible séparément dans :

```txt
docs/db/schema.sql
```

---

## 2. Conventions retenues

- noms de tables en `snake_case` ;
- clé primaire technique `id` sur chaque table ;
- clés étrangères nommées avec le suffixe `_id` ;
- types métier stockés en `VARCHAR` dans le MVP ;
- table `padel_match` utilisée au lieu de `match` pour éviter un nom ambigu en SQL ;
- solde virtuel joueur stocké dans `membre.solde_credit` ;
- certaines règles métier sont contrôlées par les services backend et pas uniquement par la base.

---

## 3. Tables du modèle relationnel

### 3.1. Table `site`

Colonnes :

- `id` : BIGINT, PK
- `code` : VARCHAR(20), NOT NULL, UNIQUE
- `nom` : VARCHAR(100), NOT NULL
- `adresse` : VARCHAR(255), NOT NULL
- `actif` : BOOLEAN, NOT NULL

Rôle :

- représente un club ou un lieu d’exploitation.

---

### 3.2. Table `terrain`

Colonnes :

- `id` : BIGINT, PK
- `site_id` : BIGINT, NOT NULL, FK vers `site(id)`
- `numero` : VARCHAR(20), NOT NULL
- `actif` : BOOLEAN, NOT NULL

Contraintes :

- UNIQUE (`site_id`, `numero`)

Rôle :

- représente un terrain de padel rattaché à un site.

---

### 3.3. Table `horaire_annuel_site`

Colonnes :

- `id` : BIGINT, PK
- `site_id` : BIGINT, NOT NULL, FK vers `site(id)`
- `annee_civile` : INT, NOT NULL
- `heure_debut_reservation` : TIME, NOT NULL
- `heure_fin_reservation` : TIME, NOT NULL

Contraintes :

- UNIQUE (`site_id`, `annee_civile`)

Rôle :

- définit les horaires annuels de réservation d’un site.

---

### 3.4. Table `fermeture`

Colonnes :

- `id` : BIGINT, PK
- `date_fermeture` : DATE, NOT NULL
- `portee` : VARCHAR(20), NOT NULL
- `motif` : VARCHAR(255), NULL
- `site_id` : BIGINT, NULL, FK vers `site(id)`

Règle métier :

- si `portee = GLOBALE`, alors `site_id` est NULL ;
- si `portee = LOCALE`, alors `site_id` est obligatoire.

Contrainte SQL recommandée :

```sql
CHECK (
    (portee = 'GLOBALE' AND site_id IS NULL)
    OR
    (portee = 'LOCALE' AND site_id IS NOT NULL)
)
```

Rôle :

- bloque les réservations pour une date ;
- peut annuler des matches à venir ;
- déclenche un remboursement du solde crédit pour les joueurs ayant payé.

---

### 3.5. Table `membre`

Colonnes :

- `id` : BIGINT, PK
- `matricule` : VARCHAR(10), NOT NULL, UNIQUE
- `nom` : VARCHAR(100), NOT NULL
- `prenom` : VARCHAR(100), NOT NULL
- `categorie_membre` : VARCHAR(20), NOT NULL
- `site_rattachement_id` : BIGINT, NULL, FK vers `site(id)`
- `actif` : BOOLEAN, NOT NULL
- `solde_credit` : DECIMAL(10,2), NOT NULL

Règles métier :

- si `categorie_membre = SITE`, alors `site_rattachement_id` est obligatoire ;
- si `categorie_membre = GLOBAL` ou `LIBRE`, le site de rattachement est optionnel ;
- un nouveau joueur reçoit `solde_credit = 100.00` ;
- un paiement débite le solde crédit ;
- une annulation de match payé rembourse le solde crédit ;
- le solde crédit ne doit pas devenir négatif.

Rôle :

- représente un joueur ;
- porte aussi le solde virtuel du joueur dans le MVP.

---

### 3.6. Table `administrateur`

Colonnes :

- `id` : BIGINT, PK
- `nom` : VARCHAR(100), NOT NULL
- `prenom` : VARCHAR(100), NOT NULL
- `email_ou_login` : VARCHAR(150), NOT NULL, UNIQUE
- `mot_de_passe` : VARCHAR(100), NULL dans le MVP actuel mais requis pour la connexion admin
- `role_administrateur` : VARCHAR(20), NOT NULL
- `site_id` : BIGINT, NULL, FK vers `site(id)`
- `actif` : BOOLEAN, NOT NULL

Règle métier :

- si `role_administrateur = SITE`, alors `site_id` est obligatoire ;
- si `role_administrateur = GLOBAL`, alors `site_id` peut être NULL.

Rôle :

- représente un administrateur global ou un administrateur de site.

---

### 3.7. Table `padel_match`

Colonnes :

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

Valeur par défaut recommandée :

- `prix_total = 60.00`

Valeurs métier de `etat_cycle` :

- `A_VENIR`
- `DEMARRE`
- `TERMINE`
- `ANNULE`

Rôle :

- représente une réservation de terrain.

Règles backend associées :

- durée fixe de 1h30 ;
- 15 minutes entre deux matches ;
- refus de création dans le passé ou à l’heure courante ;
- exclusion des matches annulés des parcours de réservation ;
- passage éventuel d’un match privé vers public à J-1.

---

### 3.8. Table `participation`

Colonnes :

- `id` : BIGINT, PK
- `match_id` : BIGINT, NOT NULL, FK vers `padel_match(id)`
- `membre_id` : BIGINT, NOT NULL, FK vers `membre(id)`
- `role_participation` : VARCHAR(20), NOT NULL
- `mode_entree` : VARCHAR(30), NOT NULL
- `statut_participation` : VARCHAR(30), NOT NULL
- `date_affectation` : TIMESTAMP, NOT NULL
- `date_confirmation` : TIMESTAMP, NULL
- `date_liberation` : TIMESTAMP, NULL

Contraintes :

- UNIQUE (`match_id`, `membre_id`)

Rôle :

- relie un membre à un match.

Règles backend associées :

- maximum 4 participations actives par match ;
- exactement 1 organisateur par match ;
- une participation libérée ne compte plus comme active ;
- une inscription publique doit être payée pour être confirmée.

---

### 3.9. Table `dette`

Colonnes :

- `id` : BIGINT, PK
- `match_id` : BIGINT, NOT NULL, FK vers `padel_match(id)`
- `membre_responsable_id` : BIGINT, NOT NULL, FK vers `membre(id)`
- `montant_initial` : DECIMAL(10,2), NOT NULL
- `montant_restant` : DECIMAL(10,2), NOT NULL
- `date_creation` : TIMESTAMP, NOT NULL
- `date_reglement` : TIMESTAMP, NULL
- `statut_dette` : VARCHAR(20), NOT NULL

Contraintes :

- UNIQUE (`match_id`)

Rôle :

- représente le montant restant à payer par l’organisateur.

Règles backend associées :

- une dette initiale peut être créée dès la création du match ;
- la dette est recalculée après paiement des participations ;
- une dette ouverte bloque une nouvelle organisation de match ;
- une dette réglée ne bloque plus ;
- un paiement de dette débite le solde crédit du membre responsable.

---

### 3.10. Table `penalite`

Colonnes :

- `id` : BIGINT, PK
- `membre_id` : BIGINT, NOT NULL, FK vers `membre(id)`
- `match_source_id` : BIGINT, NOT NULL, FK vers `padel_match(id)`
- `type_penalite` : VARCHAR(50), NOT NULL
- `motif` : VARCHAR(255), NOT NULL
- `date_debut` : TIMESTAMP, NOT NULL
- `date_fin` : TIMESTAMP, NOT NULL
- `statut_penalite` : VARCHAR(20), NOT NULL

Rôle :

- représente une sanction appliquée à l’organisateur.

Règle backend associée :

- une pénalité active bloque l’organisation d’un nouveau match pendant 7 jours.

---

### 3.11. Table `paiement`

Colonnes :

- `id` : BIGINT, PK
- `membre_id` : BIGINT, NOT NULL, FK vers `membre(id)`
- `nature_paiement` : VARCHAR(30), NOT NULL
- `montant` : DECIMAL(10,2), NOT NULL
- `date_heure_paiement` : TIMESTAMP, NOT NULL
- `statut_paiement` : VARCHAR(20), NOT NULL
- `participation_id` : BIGINT, NULL, FK vers `participation(id)`
- `dette_id` : BIGINT, NULL, FK vers `dette(id)`

Contraintes :

- UNIQUE (`participation_id`)
- UNIQUE (`dette_id`)

Règle métier :

- un paiement concerne soit une participation, soit une dette, jamais les deux en même temps.

Contrainte SQL recommandée :

```sql
CHECK (
    (participation_id IS NOT NULL AND dette_id IS NULL)
    OR
    (participation_id IS NULL AND dette_id IS NOT NULL)
)
```

Rôle :

- trace les paiements de participation ;
- trace les règlements de dette ;
- alimente l’historique des transactions joueur ;
- alimente les statistiques admin.

---

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

---

## 5. Contraintes d’unicité importantes

- `site.code` unique ;
- `membre.matricule` unique ;
- `administrateur.email_ou_login` unique ;
- (`terrain.site_id`, `terrain.numero`) unique ;
- (`horaire_annuel_site.site_id`, `horaire_annuel_site.annee_civile`) unique ;
- (`participation.match_id`, `participation.membre_id`) unique ;
- `dette.match_id` unique ;
- `paiement.participation_id` unique dans le MVP ;
- `paiement.dette_id` unique dans le MVP.

---

## 6. Règles gérées surtout par le backend

Les règles suivantes ne sont pas portées uniquement par SQL. Elles sont surtout contrôlées dans les services backend :

- maximum 4 participations actives par match ;
- exactement 1 organisateur par match ;
- interdiction de chevauchement de deux matches sur un même terrain ;
- respect des 15 minutes entre deux matches sur un même terrain ;
- interdiction pour un joueur d’être inscrit à deux matches qui se chevauchent ;
- respect de la fenêtre de réservation selon la catégorie du membre ;
- refus de création d’un match dans le passé ou à l’heure courante ;
- blocage d’une nouvelle réservation si dette active ;
- blocage d’une nouvelle réservation si match organisé non totalement payé ;
- blocage d’une nouvelle réservation si pénalité active ;
- transformation d’un match privé en public à J-1 ;
- libération d’une place non payée à J-1 ;
- calcul et recalcul de la dette de l’organisateur ;
- débit du solde crédit lors d’un paiement de participation ;
- débit du solde crédit lors d’un paiement de dette ;
- refus de paiement si solde crédit insuffisant ;
- remboursement du solde crédit lors d’une fermeture ;
- liste des matches publics disponibles ;
- inscription et paiement direct dans un match public ;
- historique des paiements d’un joueur ;
- statistiques métier admin.

---

## 7. Ordre de création technique recommandé

Ordre logique des tables :

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

Justification :

- `site` doit exister avant les terrains, horaires, fermetures locales, membres de site et administrateurs de site ;
- `terrain` doit exister avant `padel_match` ;
- `membre` doit exister avant `participation`, `dette`, `penalite` et `paiement` ;
- `padel_match` doit exister avant `participation`, `dette` et `penalite` ;
- `participation` et `dette` doivent exister avant les paiements qui les référencent.

---

## 8. Équivalent SQL synthétique

Ce résumé n’est pas le script complet de remise.  
Le script complet est dans `docs/db/schema.sql`.

```sql
CREATE TABLE membre (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    matricule VARCHAR(10) NOT NULL UNIQUE,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    categorie_membre VARCHAR(20) NOT NULL,
    site_rattachement_id BIGINT,
    actif BOOLEAN NOT NULL,
    solde_credit DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_membre_site_rattachement
        FOREIGN KEY (site_rattachement_id) REFERENCES site(id)
);
```

Point important pour l’oral :

```txt
Le solde joueur est un champ du membre.
Les paiements et remboursements modifient ce solde via les services backend.
Le frontend ne modifie jamais la base directement.
```

---

## 9. Choix DB et sécurité

Base utilisée pour le MVP :

- H2 en mémoire ;
- démarrage automatique avec le backend ;
- seed automatique via `data.sql` ;
- aucun script SQL manuel à exécuter pour la démo.

User local MVP :

- `sa`
- mot de passe vide
- acceptable uniquement pour H2 local de démonstration.

Choix cible expliqué dans la documentation DB :

- `padel_migration` : création et évolution du schéma ;
- `padel_app` : user applicatif backend avec droits CRUD ;
- `padel_readonly` : lecture seule ;
- aucun user DB pour le frontend.

Phrase à savoir dire :

```txt
Le frontend n'a aucun accès à la base.
Le backend est le seul composant qui se connecte à la DB.
En cible, le backend utilise un user applicatif avec droits CRUD, pas un user avec tous les droits.
```

---

## 10. Conclusion

Ce modèle relationnel est volontairement simple pour accélérer la livraison du MVP.

Il couvre les besoins essentiels :

- sites ;
- terrains ;
- horaires ;
- fermetures ;
- joueurs ;
- administrateurs ;
- matches ;
- participations ;
- paiements ;
- dettes ;
- pénalités ;
- solde crédit joueur ;
- statistiques ;
- historique des transactions.

La logique métier complexe reste dans les services backend.  
La base porte les relations, les clés étrangères et les contraintes simples.  
Le frontend Angular consomme uniquement l’API REST et ne contient aucun SQL.
