-- À exécuter une seule fois si la connexion de l'administrateur échoue avec une erreur de contrainte « check_role »
-- (cas d'une base créée avec une ancienne version qui n'acceptait pas le rôle ADMIN).
--
--   psql -U postgres -d gmt_rentacar -f db/fix-admin-role-constraint.sql
ALTER TABLE users DROP CONSTRAINT IF EXISTS check_role;

ALTER TABLE users
    ADD CONSTRAINT check_role
    CHECK (role IN ('CLIENT', 'USER', 'ADMIN', 'ROLE_ADMIN')) NOT VALID;
