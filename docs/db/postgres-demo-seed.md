# Seed de démonstration PostgreSQL

## Objectif

Le seed de démonstration PostgreSQL remplit automatiquement la base Docker avec les mêmes données de démonstration que H2.

Objectif principal :

- garder H2 comme configuration par défaut ;
- permettre aussi une démo complète avec PostgreSQL Docker ;
- conserver les mêmes IDs que le frontend utilise déjà pour la démonstration.

## Activation

Le seed est exécuté uniquement lorsque le backend est lancé avec le profil Spring `postgres`.

Commande :

```powershell
docker compose up -d postgres
cd backend
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=postgres