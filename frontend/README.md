# Frontend Angular — Padel Marius

Ce dossier contient l'interface Angular du projet. Le frontend ne contient ni
SQL ni accès direct à la base de données : il communique uniquement avec l'API
REST Spring Boot par des URLs relatives `/api/**`.

## Architecture

```txt
src/app/pages/        composants et écrans standalone
src/app/services/     clients HTTP, façades et états partagés
src/app/models/       contrats TypeScript des DTO
src/app/guards/       protection des routes joueur et administrateur
src/app/interceptors/ ajout et renouvellement du token actif pour l'API interne
src/app/shared/       utilitaires de présentation et d'erreur
```

Les composants gèrent la présentation. Les façades orchestrent les parcours
avec des signaux Angular. Les clients API centralisent les appels HTTP.

L'interceptor limite le header Bearer aux URLs relatives `/api/**`. Après un
`401`, il partage un seul appel de refresh entre les requêtes simultanées,
remplace l'access token puis rejoue une fois la requête initiale. Le refresh
token reste dans un cookie `HttpOnly` et n'est jamais accessible au code
Angular.

Si le refresh échoue, la session concernée est supprimée et l'utilisateur est
redirigé vers le login joueur ou administrateur. Le logout nettoie toujours
la session locale ; une erreur réseau de révocation est affichée dans une
alerte globale. Les formulaires appliquent les mêmes tailles maximales que les
DTO backend et les identifiants sont acceptés sans distinction de casse.

## Prérequis et installation

Depuis la racine du projet :

```powershell
cd frontend
npm.cmd ci
```

Le projet utilise Node.js 22.12.0 ou plus récent dans la branche 22, npm
11.6.0, Angular 21, Angular Material et Vitest. Les champs `packageManager` et
`engines` de `package.json` documentent ces versions reproductibles.

## Démarrage

Le backend doit être accessible sur `http://localhost:8080`.

```powershell
npm.cmd start
```

Ouvrir `http://localhost:4200`. Le fichier `proxy.conf.json` redirige les appels
`/api/**` vers le backend.

## Validation

```powershell
npm.cmd run build
npm.cmd run test -- --watch=false
npm.cmd run cypress:run
npm.cmd run cypress:run:fullstack
```

- Le build vérifie la compilation de production.
- Les tests unitaires couvrent composants, façades, clients API, guards et
  interceptor.
- Les neuf scénarios Cypress mockés testent les principaux parcours de
  l'interface avec des réponses API simulées.
- Cypress full stack démarre automatiquement Spring Boot avec H2 et Angular,
  puis vérifie deux scénarios HTTP réels.

Pour le fonctionnement complet, les règles métier, la sécurité et la
démonstration, consulter `README.md`, `ARCHITECTURE.md`, `EXPLOITATION.md` et
`DEMO.md` à la racine du dépôt.
