-- ============================================================================
-- GMT Rent a Car : flotte réelle (11 véhicules)
--
-- Ce script SUPPRIME les catégories, véhicules, demandes de devis, notifications
-- et historiques de location existants, puis charge la flotte réelle.
-- Les comptes administrateurs et les paramètres (taux EUR/DT) ne sont pas touchés.
--
-- Avant de l'exécuter :
--   1. Démarrez le backend au moins une fois (Hibernate crée les tables).
--   2. Sauvegardez la base :  pg_dump -U postgres gmt_rentacar > sauvegarde.sql
--
-- Exécution (PowerShell) :
--   psql -U postgres -d gmt_rentacar -f db/fleet-seed.sql
-- ============================================================================

BEGIN;

ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS immatriculation VARCHAR(30);

-- 1. Nettoyage des données de test
TRUNCATE TABLE
    admin_notifications,
    email_deliveries,
    quote_requests,
    rental_history,
    vehicle_images,
    vehicles,
    categories
RESTART IDENTITY CASCADE;

-- Optionnel : supprime aussi les clients de test créés par d'anciennes demandes de devis.
-- Décommentez uniquement si aucun vrai client n'est encore enregistré.
-- DELETE FROM users WHERE UPPER(role) IN ('USER', 'ROLE_USER', 'CLIENT');

-- 2. Catégories
INSERT INTO categories (nom, description, actif) VALUES
    ('Citadine',    'Compactes et économiques, idéales en ville.',  TRUE),
    ('Berline',     'Berlines confortables pour les trajets routiers.', TRUE),
    ('SUV compact', 'SUV urbains, position de conduite surélevée.',   TRUE);

-- 3. Véhicules
-- Les immatriculations « TEMP-… » sont provisoires : remplacez-les depuis
-- l'espace admin (Véhicules > Modifier) avec les vraies plaques.
-- Colonne image : nom du fichier placé dans GMT_rental/src/assets/cars/ (sans extension).
INSERT INTO vehicles (marque, modele, immatriculation, nombre_places, transmission, carburant,
                      prix_jour_dt, description, image, statut, categorie_id)
SELECT f.marque, f.modele, f.immatriculation, f.places, f.transmission, f.carburant,
       f.prix, f.description, f.image, 'ACTIF', c.id
FROM (VALUES
    ('MG',      'ZS',      'TEMP-MGZS-01',   5, 'AUTOMATIQUE', 'ESSENCE', 180.00, 'SUV compact, 5 places, essence, boîte automatique.', 'mg-zs',       'SUV compact'),
    ('MG',      'ZS',      'TEMP-MGZS-02',   5, 'AUTOMATIQUE', 'ESSENCE', 180.00, 'SUV compact, 5 places, essence, boîte automatique.', 'mg-zs',       'SUV compact'),
    ('Honda',   'City',    'TEMP-CITY-01',   5, 'AUTOMATIQUE', 'ESSENCE', 150.00, 'Berline, 5 places, essence, boîte automatique.',     'honda-city', 'Berline'),
    ('Honda',   'City',    'TEMP-CITY-02',   5, 'AUTOMATIQUE', 'ESSENCE', 150.00, 'Berline, 5 places, essence, boîte automatique.',     'honda-city', 'Berline'),
    ('Peugeot', '2008',    'TEMP-2008-01',   5, 'AUTOMATIQUE', 'ESSENCE', 180.00, 'SUV compact, 5 places, essence, boîte automatique.', 'peugeot-2008', 'SUV compact'),
    ('Peugeot', '2008',    'TEMP-2008-02',   5, 'AUTOMATIQUE', 'ESSENCE', 180.00, 'SUV compact, 5 places, essence, boîte automatique.', 'peugeot-2008', 'SUV compact'),
    ('Kia',     'Stonic',  'TEMP-STONIC-01', 5, 'MANUELLE',    'ESSENCE', 130.00, 'SUV compact, 5 places, essence, boîte manuelle.',     'kia-stonic', 'SUV compact'),
    ('Kia',     'Stonic',  'TEMP-STONIC-02', 5, 'MANUELLE',    'ESSENCE', 130.00, 'SUV compact, 5 places, essence, boîte manuelle.',     'kia-stonic', 'SUV compact'),
    ('Kia',     'Stonic',  'TEMP-STONIC-03', 5, 'MANUELLE',    'ESSENCE', 130.00, 'SUV compact, 5 places, essence, boîte manuelle.',     'kia-stonic', 'SUV compact'),
    ('Kia',     'Picanto', 'TEMP-PICANTO-01',5, 'AUTOMATIQUE', 'ESSENCE', 110.00, 'Citadine, 5 places, essence, boîte automatique.',    'kia-picanto','Citadine'),
    ('Kia',     'Picanto', 'TEMP-PICANTO-02',5, 'AUTOMATIQUE', 'ESSENCE', 110.00, 'Citadine, 5 places, essence, boîte automatique.',    'kia-picanto','Citadine')
) AS f(marque, modele, immatriculation, places, transmission, carburant, prix, description, image, categorie)
JOIN categories c ON c.nom = f.categorie;

COMMIT;

-- Contrôle rapide
SELECT marque, modele, immatriculation, transmission, prix_jour_dt FROM vehicles ORDER BY id;
