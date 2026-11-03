# GMT Rent a Car — site public et espace admin

Application React (Vite) qui consomme l'API Spring Boot du dossier `gmt-rental-backend`.

## Démarrage

```powershell
npm install
npm run dev      # http://localhost:5174
npm run build
npm run lint
```

L'adresse de l'API se règle avec `VITE_API_URL` (par défaut `http://localhost:8081/api`).
En production, créez un fichier `.env.production` :

```
VITE_API_URL=https://api.votre-domaine.tn/api
```

## À renseigner avant la mise en ligne

- `src/config/site.js` : adresse, téléphone, e-mail, horaires, réseaux et conditions de location.
  Un champ vide est simplement masqué sur le site.
- `src/assets/cars/` : photos des voitures (voir `LISEZ-MOI.txt`).
