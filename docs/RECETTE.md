# Recette GMT Rental — checklist

Cette recette se fait **uniquement** sur un environnement jetable : PostgreSQL de recette (base `gmt_recette`),
jamais sur la base réelle, sans seed ni migration sur une base existante, sans déploiement.

Légende : [ ] non testé · [x] validé · [!] échoué (noter le fichier et le message d'erreur)

## 1. PostgreSQL de recette

- [ ] Créer une base vide `gmt_recette` (nom distinct de la base réelle).
- [ ] Démarrer le backend avec `DATABASE_URL` pointant sur `gmt_recette` : Hibernate crée le schéma (`GMT_DDL_AUTO=update`).
- [ ] Exporter le schéma réel : `pg_dump -U postgres -d gmt_recette --schema-only > schema-recette.sql`.
- [ ] Vérifier que `db/fleet-seed.sql` s'exécute deux fois sans erreur et donne 11 véhicules et 3 catégories.
- [ ] Vérifier que le seed ne supprime ni `users` ni `settings` ni `admin_preferences`.
- [ ] Vérifier que `db/fix-admin-role-constraint.sql` s'exécute deux fois sans erreur et refuse un rôle inconnu.
- [ ] Vérifier le verrou `findModelGroupForUpdate` (requête `FOR UPDATE ... ORDER BY id`) sur le schéma réel.

## 2. Cookies et CORS

- [ ] Connexion depuis le site de recette : cookie `gmt_session` présent, `HttpOnly`, `Secure`, `SameSite` conforme à la configuration.
- [ ] Le cookie n'est pas lisible par `document.cookie` (console navigateur).
- [ ] Une origine non autorisée reçoit une erreur CORS.
- [ ] Les requêtes admin envoient `X-Requested-With` (onglet Réseau).

## 3. Configuration de production (contrôle, sans déploiement)

- [ ] Sans `GMT_JWT_SECRET` (ou < 32 octets) : le backend refuse de démarrer.
- [ ] Sans `GMT_ADMIN_PASSWORD` (ou < 8 caractères) : le démarrage échoue si l'e-mail admin est défini.
- [ ] Sans `RESEND_API_KEY` : avertissement dans les logs, les demandes de devis restent enregistrées.
- [ ] `VITE_API_URL` défini dans le build : le site n'appelle plus `localhost`.
- [ ] Aucun fichier `.env` ni secret dans `git status` ni dans `dist/`.

## 4. Recette fonctionnelle

Connexion et session
- [ ] Connexion admin avec des identifiants valides : accès au tableau de bord.
- [ ] Mauvais mot de passe : message d'erreur générique, pas de détail sur l'existence du compte.
- [ ] Rechargement de page : la session persiste.
- [ ] Déconnexion : retour sur `/admin/login`, cookie effacé.
- [ ] Session expirée (après la durée configurée) : redirection vers la connexion.

Accès et autorisations
- [ ] Accès direct à `/admin/...` sans session : redirection vers la connexion.
- [ ] Compte client (`USER`) : aucun accès à `/api/admin/**` (403 ou 401).
- [ ] Désactivation d'un compte admin pendant une session active : son accès API est refusé à la requête suivante.

Protection CSRF
- [ ] Requête POST admin avec cookie sans `X-Requested-With` : 403.
- [ ] Même requête avec l'en-tête : acceptée.

Demandes de devis
- [ ] Demande publique valide : enregistrée, numéro d'accusé affiché.
- [ ] Double envoi (même clé d'idempotence) : une seule demande créée.
- [ ] Plus de 5 demandes pour la même adresse en une heure : 429.
- [ ] Acceptation d'une demande : le véhicule est réservé, aucune double réservation sur le même modèle.

Notifications et e-mails
- [ ] Notification admin créée à chaque nouvelle demande.
- [ ] Test d'envoi d'e-mail depuis le paramétrage : statut « accepté » ou erreur explicite.
- [ ] E-mail de vérification : envoi, saisie du code, compte vérifié.

## 5. Résultats

| Point | État | Fichier | Test exécuté | Résultat |
|---|---|---|---|---|
| Seed sur PostgreSQL | non testé sur le schéma Hibernate réel | `db/fleet-seed.sql` | | |
| Script contrainte de rôle | non testé sur le schéma réel | `db/fix-admin-role-constraint.sql` | | |
| Cookie et CORS | non testé en navigateur | `AuthCookies.java`, `SecurityConfig.java` | | |
| Protection CSRF | testé (backend) | `JwtAuthenticationFilter.java` | `AdminSessionCookieIntegrationTests` | 1 test, OK (Maven, 35 tests) |
| Désactivation de compte | testé (backend) | `JwtAuthenticationFilter.java` | `AdminUserDeactivationIntegrationTests` | 4 tests, OK |
| Demandes de devis et acceptation | testé (backend, H2) | `QuoteRequestService.java` | `QuoteAcceptanceIntegrationTests` | 6 tests, OK |
| E-mails | testé (backend, mocks) | `EmailService.java` | `EmailServiceTests`, `QuoteEmailNotificationServiceTests` | 8 tests, OK |
| Navigateur, session, rechargement | non testé | `AuthContext.jsx` | | |
