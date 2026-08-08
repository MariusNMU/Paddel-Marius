# Durcissement de l'authentification

Ce document décrit les choix appliqués après l'audit de sécurité, leur effet
sur le projet et leur positionnement par rapport au cours PDW.

## JWT minimal

L'access token contient uniquement :

- `sub`, avec le matricule joueur ou le login administrateur canonique ;
- `typeUtilisateur`, avec `JOUEUR` ou `ADMIN` ;
- `typeToken`, avec `ACCES` ;
- les claims temporels standard `iat` et `exp`.

Les claims `role` et `siteId` ont été retirés. À chaque requête, Spring
Security recharge le compte et ses autorités actuelles depuis la base. Le
refresh token ajoute seulement un `jti` aléatoire nécessaire à sa rotation.

## Rotation et révocation des refresh tokens

Le backend ne stocke jamais le JWT de refresh brut. Il conserve uniquement
son `jti`, son sujet, son type, son expiration et son état de révocation dans
la table `jeton_rafraichissement`.

Lors d'un refresh :

1. la signature, l'expiration et le type du JWT sont validés ;
2. la ligne correspondant au `jti` est verrouillée en écriture ;
3. l'ancien identifiant est révoqué ;
4. le compte actif est relu en base ;
5. un nouvel access token et un nouveau refresh token sont émis ;
6. le nouveau `jti` est enregistré dans la même transaction.

Deux utilisations concurrentes du même refresh token ne peuvent donc pas
réussir. Le logout révoque également le `jti` avant d'expirer le cookie et
reste idempotent si le cookie est absent, invalide ou déjà révoqué.

Après déploiement de la migration `003`, les refresh tokens émis auparavant
ne possèdent pas d'entrée serveur et sont refusés. Une reconnexion unique est
donc attendue. Les access tokens déjà émis restent valides jusqu'à leur
expiration maximale de 60 minutes.

## Limitation des tentatives

Les routes `POST /api/auth/joueur`, `POST /api/auth/admin` et
`POST /api/auth/refresh` partagent la même politique : cinq requêtes par
fenêtre de dix minutes, par adresse distante et par endpoint. La requête
suivante reçoit `429 Too Many Requests`, le code `TROP_DE_TENTATIVES` et un
header `Retry-After`.

Les valeurs sont configurables :

```properties
padel.security.auth-rate-limit.max-attempts=${PADEL_AUTH_MAX_ATTEMPTS:5}
padel.security.auth-rate-limit.window-minutes=${PADEL_AUTH_WINDOW_MINUTES:10}
```

Le filtre utilise l'adresse distante fournie par le serveur et ne fait pas
confiance directement à `X-Forwarded-For`.

## Identifiants et tailles d'entrée

Les recherches d'authentification sont insensibles à la casse. La réponse et
les JWT utilisent toujours la valeur canonique stockée en base. Les limites
des DTO et des formulaires sont alignées :

| Champ | Taille maximale |
|---|---:|
| Matricule joueur | 10 caractères |
| Login administrateur | 150 caractères |
| Mot de passe de connexion | 72 caractères |

Aucune longueur minimale n'est ajoutée au DTO de connexion : la politique de
12 caractères concerne la création d'un nouveau mot de passe et les comptes
de démonstration existants restent compatibles.

## Comportement Angular

Si le refresh échoue, Angular supprime la session concernée et redirige
immédiatement vers `/joueur` ou `/admin/login`. Un refresh ancien ne peut pas
effacer une nouvelle session créée entre-temps.

Le logout nettoie toujours la session locale et navigue vers `/accueil`. Si
le backend ne peut pas être contacté pour révoquer le refresh token, une
alerte globale l'indique sans restaurer la session locale. Les boutons de la
barre latérale utilisent le même parcours de logout que les pages de connexion.

## Positionnement PDW

| Correction | Positionnement dans le cours |
|---|---|
| JWT limité aux informations nécessaires | Critère essentiel |
| Rotation avec révocation persistante | « Aller plus loin » |
| Limite de tentatives et identifiant insensible à la casse | Bonnes pratiques enseignées |
| Tailles maximales des DTO | Durcissement propre au projet |
| Redirection après échec du refresh et alerte de logout | Gestion cohérente des erreurs et des guards |

## Limites restantes

- Les access tokens restent stateless et ne sont pas révoqués avant leur
  expiration.
- Le rate limiting est local à une instance et remis à zéro au redémarrage.
  Un déploiement multi-instance devrait utiliser un stockage partagé tel que
  Redis.
- Le projet ne fait pas de rotation automatique du secret JWT.
- En production, `PADEL_JWT_SECRET` doit être robuste et externe,
  `PADEL_REFRESH_COOKIE_SECURE=true` doit être activé et HTTPS est obligatoire.
