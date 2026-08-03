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
src/app/interceptors/ ajout du JWT aux appels protégés
src/app/shared/       utilitaires de présentation et d'erreur
```

Les composants gèrent la présentation. Les façades orchestrent les parcours
avec des signaux Angular. Les clients API centralisent les appels HTTP.

## Prérequis et installation

Depuis la racine du projet :

```powershell
cd frontend
npm.cmd ci
```

Le projet utilise Node.js, npm 11.6.0, Angular 21, Angular Material et Vitest.

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
- Cypress mocké teste l'interface avec des réponses API simulées.
- Cypress full stack démarre automatiquement Spring Boot avec H2 et Angular,
  puis vérifie un parcours HTTP réel.

Pour le fonctionnement complet, les règles métier, la sécurité et la
démonstration, consulter `README.md`, `ARCHITECTURE.md`, `EXPLOITATION.md` et
`DEMO.md` à la racine du dépôt.
