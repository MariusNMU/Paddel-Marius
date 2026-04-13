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
