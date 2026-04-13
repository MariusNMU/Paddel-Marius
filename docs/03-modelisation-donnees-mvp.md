# Modélisation des données du MVP

## 1. Objectif du document
Ce document décrit les entités métier principales du MVP, leurs relations, leurs cardinalités et les contraintes métier importantes.

## 2. Entités métier
### Site
Décrire son rôle.

### Terrain
Décrire son rôle.

### HoraireAnnuelSite
Décrire son rôle.

### Fermeture
Décrire son rôle.

### Membre
Décrire son rôle.

### Administrateur
Décrire son rôle.

### Match
Décrire son rôle.

### Participation
Décrire son rôle.

### Paiement
Décrire son rôle.

### Dette
Décrire son rôle.

### Pénalité
Décrire son rôle.

## 3. Relations entre les entités
- Site -> Terrain
- Site -> HoraireAnnuelSite
- Site -> Fermeture
- Site -> Membre
- Site -> Administrateur
- Terrain -> Match
- Match -> Participation
- Membre -> Participation
- Participation -> Paiement
- Match -> Dette
- Membre -> Dette
- Dette -> Paiement
- Membre -> Pénalité
- Match -> Pénalité

## 4. Cardinalités
À compléter pour chaque relation.

## 5. Contraintes métier importantes
- 4 joueurs maximum par match
- un seul organisateur par match
- un match dure 1h30
- 15 minutes entre deux matches
- dette bloquante
- pénalité de 7 jours
- etc.

## 6. Cas métier couverts
- match privé
- match public
- bascule privé vers public
- place impayée libérée
- dette organisateur
- administration multisite

- 7
- erDiagram
    SITE {
        int idSite PK
        string codeSite UK
        string nom
        string adresse
        boolean actif
    }

    TERRAIN {
        int idTerrain PK
        string numeroTerrain
        boolean actif
        int idSite FK
    }

    HORAIRE_ANNUEL_SITE {
        int idHoraireAnnuel PK
        int anneeCivile
        string heureDebutReservation
        string heureFinReservation
        int idSite FK
    }

    FERMETURE {
        int idFermeture PK
        date dateFermeture
        string portee
        string motif
        int idSite FK
    }

    MEMBRE {
        int idMembre PK
        string matricule UK
        string nom
        string prenom
        string categorieMembre
        int idSiteRattachement FK
        boolean actif
    }

    ADMINISTRATEUR {
        int idAdministrateur PK
        string nom
        string prenom
        string emailOuLogin UK
        string roleAdministrateur
        int idSite FK
        boolean actif
    }

    MATCH {
        int idMatch PK
        datetime dateHeureDebut
        string modeCreation
        string visibiliteCourante
        decimal prixTotalApplique
        datetime dateCreation
        datetime datePassagePublic
        string etatCycle
        int idTerrain FK
    }

    PARTICIPATION {
        int idParticipation PK
        string roleParticipation
        string modeEntree
        string statutParticipation
        datetime dateAffectation
        datetime dateConfirmation
        datetime dateLiberation
        int idMatch FK
        int idMembre FK
    }

    PAIEMENT {
        int idPaiement PK
        string naturePaiement
        decimal montant
        datetime dateHeurePaiement
        string statutPaiement
        int idMembre FK
        int idParticipation FK
        int idDette FK
    }

    DETTE {
        int idDette PK
        decimal montantInitial
        decimal montantRestant
        datetime dateCreation
        datetime dateReglement
        string statutDette
        int idMatch FK
        int idMembreResponsable FK
    }

    PENALITE {
        int idPenalite PK
        string typePenalite
        string motif
        datetime dateDebut
        datetime dateFin
        string statutPenalite
        int idMembre FK
        int idMatchSource FK
    }

    SITE ||--|{ TERRAIN : possede
    SITE ||--|{ HORAIRE_ANNUEL_SITE : definit
    SITE o|--o{ FERMETURE : concerne_si_locale
    SITE o|--o{ MEMBRE : rattache
    SITE o|--o{ ADMINISTRATEUR : rattache_si_site

    TERRAIN ||--o{ MATCH : accueille

    MATCH ||--|{ PARTICIPATION : contient
    MEMBRE ||--o{ PARTICIPATION : participe

    PARTICIPATION o|--o| PAIEMENT : reglee_par
    MEMBRE ||--o{ PAIEMENT : effectue

    MATCH ||--o| DETTE : genere
    MEMBRE ||--o{ DETTE : supporte
    DETTE o|--o| PAIEMENT : reglee_par

    MATCH ||--o| PENALITE : declenche
    MEMBRE ||--o{ PENALITE : recoit
