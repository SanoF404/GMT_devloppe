# Schéma PostgreSQL : sauvegarde, baseline et migrations

Statut : **procédure**. Les migrations versionnées (Flyway) ne sont pas encore ajoutées.
Elles seront créées à partir d'un schéma réel exporté par Hibernate, jamais à la main.

## Règles
- Ne jamais lancer de seed, de migration ou de `ddl-auto` sur la base réelle sans sauvegarde et accord explicite.
- Les tests destructifs se font uniquement sur une base jetable (`gmt_recette`).

## 1. Sauvegarde (avant toute opération sur une base existante)
```powershell
pg_dump -U postgres -d gmt_rentacar -F c -f sauvegarde-gmt_rentacar-AAAA-MM-JJ.dump
```
Vérifier que le fichier existe et n'est pas vide avant de continuer.

## 2. Exporter le schéma réel attendu (sur base jetable uniquement)
```powershell
psql -U postgres -c "CREATE DATABASE gmt_recette;"
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/gmt_recette"
$env:DATABASE_USERNAME="postgres"
$env:DATABASE_PASSWORD=""
$env:GMT_JWT_SECRET="<au moins 32 caractères aléatoires, non réutilisés>"
$env:GMT_DDL_AUTO="update"
.\mvnw.cmd spring-boot:run     # attendre « Started », puis Ctrl+C
pg_dump -U postgres -d gmt_recette --schema-only -f schema-hibernate.sql
```

## 3. Baseline (étape suivante, après validation du schéma exporté)
- `V1__baseline.sql` = `schema-hibernate.sql`, validé sur base jetable.
- Sur une base réelle existante : `baseline-on-migrate` (marque la version sans rejouer le DDL).
- Puis `GMT_DDL_AUTO=validate` : Hibernate vérifie que le schéma correspond aux entités.

## 4. Scripts existants
- `db/fleet-seed.sql` : **destructif** (supprime devis, notifications, historique, véhicules, catégories). Interdit sur la base réelle sans décision explicite et sauvegarde.
- `db/fix-admin-role-constraint.sql` : idempotent, ajoute une contrainte NOT VALID sur `users.role`. À appliquer seulement si la connexion admin échoue sur une base ancienne.
