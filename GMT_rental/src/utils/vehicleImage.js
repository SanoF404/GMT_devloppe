import { API_BASE_URL } from "../services/api";

// Les photos des véhicules se déposent dans src/assets/cars/ (jpg, png ou webp).
// Le champ « image » d'un véhicule peut contenir le nom du fichier avec ou sans extension
// (ex. « mg-zs » ou « mg-zs.jpg »), une URL complète ou un chemin /uploads/.
const bundledCarImages = import.meta.glob("../assets/cars/*.{jpg,jpeg,png,webp,avif}", {
  eager: true,
  query: "?url",
  import: "default",
});

const carImages = Object.entries(bundledCarImages).reduce((images, [path, url]) => {
  const filename = path.split("/").pop().toLowerCase();
  images[filename] = url;
  images[filename.replace(/\.[^.]+$/, "")] = url;
  return images;
}, {});

export function resolveVehicleImage(source) {
  if (typeof source !== "string" || !source.trim()) return "";
  const value = source.trim();
  if (/^\/uploads\//i.test(value)) return `${API_BASE_URL.replace(/\/api\/?$/, "")}${value}`;
  if (/^(https?:|data:|blob:)/i.test(value)) return value;

  const filename = value.replace(/\\/g, "/").split("/").pop();
  let decoded = filename;
  try { decoded = decodeURIComponent(filename); } catch { /* nom de fichier conservé tel quel */ }
  const key = decoded.toLowerCase();
  return carImages[key] || carImages[key.replace(/\.[^.]+$/, "")] || "";
}
