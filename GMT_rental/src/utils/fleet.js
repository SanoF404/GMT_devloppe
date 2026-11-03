// Les véhicules sont enregistrés un par un (chaque voiture a sa plaque), mais un client
// choisit un modèle : on regroupe donc les voitures identiques pour l'affichage.
export function modelKey(vehicle) {
  return `${vehicle?.marque ?? ""}|${vehicle?.modele ?? ""}`.trim().toLowerCase();
}

export function groupByModel(vehicles) {
  const groups = new Map();
  for (const vehicle of Array.isArray(vehicles) ? vehicles : []) {
    const key = modelKey(vehicle);
    const group = groups.get(key);
    if (group) {
      group.count += 1;
      group.ids.push(vehicle.id);
      const price = Number(vehicle.prixParJour);
      if (Number.isFinite(price) && (!Number.isFinite(Number(group.prixParJour)) || price < Number(group.prixParJour))) {
        group.prixParJour = vehicle.prixParJour;
      }
    } else {
      groups.set(key, { ...vehicle, count: 1, ids: [vehicle.id] });
    }
  }
  return [...groups.values()];
}

// Les valeurs enregistrées en base sont en majuscules (AUTOMATIQUE, ESSENCE) : on les affiche en « Automatique ».
export function titleCase(value) {
  return typeof value === "string" && value ? value.charAt(0).toUpperCase() + value.slice(1).toLowerCase() : "—";
}
