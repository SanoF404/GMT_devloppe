package com.gmt.rental.service.admin;

import com.gmt.rental.dto.admin.VehicleRequest;
import com.gmt.rental.entity.Category;
import com.gmt.rental.entity.Vehicle;
import com.gmt.rental.repository.CategoryRepository;
import com.gmt.rental.repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AdminVehicleService {
    private final VehicleRepository vehicles;
    private final CategoryRepository categories;

    public AdminVehicleService(VehicleRepository vehicles, CategoryRepository categories) {
        this.vehicles = vehicles;
        this.categories = categories;
    }

    public List<Vehicle> findAll() { return vehicles.findAll(); }

    public Vehicle findById(Long id) {
        return vehicles.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Véhicule introuvable"));
    }

    public Vehicle create(VehicleRequest request) {
        Vehicle vehicle = new Vehicle();
        apply(vehicle, request);
        return vehicles.save(vehicle);
    }

    public Vehicle update(Long id, VehicleRequest request) {
        Vehicle vehicle = findById(id);
        apply(vehicle, request);
        return vehicles.save(vehicle);
    }

    public Vehicle updateStatus(Long id, String status) {
        if (!List.of("ACTIF", "INACTIF").contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Statut de véhicule invalide");
        }
        Vehicle vehicle = findById(id);
        vehicle.setStatut(status);
        return vehicles.save(vehicle);
    }

    public void deactivate(Long id) { updateStatus(id, "INACTIF"); }

    private static final List<String> TRANSMISSIONS = List.of("MANUELLE", "AUTOMATIQUE");
    private static final List<String> FUELS = List.of("ESSENCE", "DIESEL", "HYBRIDE", "ELECTRIQUE");

    private static String allowedValue(String value, List<String> allowed, String label) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim().toUpperCase();
        if (!allowed.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " n'est pas valide");
        }
        return normalized;
    }

    /** Refuse les schémas dangereux (javascript:, data:…) : seules les adresses http(s), /uploads/ et les noms de fichier passent. */
    private static boolean isSafeImageReference(String image) {
        String lower = image.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")) return true;
        if (lower.startsWith("/uploads/")) return !lower.contains("..");
        return image.matches("^[\\p{L}\\p{N} _.()\\-]+$");
    }

    private void apply(Vehicle vehicle, VehicleRequest request) {
        if (request == null || request.marque() == null || request.marque().isBlank()
                || request.modele() == null || request.modele().isBlank() || request.prixParJour() == null
                || request.categorieId() == null || request.places() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Marque, modèle, prix par jour, places et catégorie sont obligatoires");
        }
        if (request.prixParJour().signum() < 0 || request.places() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le prix doit être positif et le nombre de places supérieur à zéro");
        }
        String status = request.statut() == null || request.statut().isBlank() ? "ACTIF" : request.statut().trim().toUpperCase();
        if (!List.of("ACTIF", "INACTIF").contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Statut de véhicule invalide");
        }
        vehicle.setMarque(request.marque().trim());
        vehicle.setModele(request.modele().trim());
        String plate = request.immatriculation() == null ? "" : request.immatriculation().trim();
        if (plate.length() > 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'immatriculation est trop longue (30 caractères maximum)");
        }
        vehicle.setImmatriculation(plate.isEmpty() ? null : plate);
        if (request.annee() != null && (request.annee() < 1990 || request.annee() > java.time.Year.now().getValue() + 1)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'année du véhicule est invalide");
        }
        if (request.places() > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nombre de places est invalide");
        }
        if (request.prixParJour().compareTo(new java.math.BigDecimal("100000")) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le prix par jour est invalide");
        }
        if (request.description() != null && request.description().length() > 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La description ne doit pas dépasser 2000 caractères");
        }
        String image = request.image() == null ? "" : request.image().trim();
        if (image.length() > 500 || (!image.isEmpty() && !isSafeImageReference(image))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Image invalide : indiquez un nom de fichier, un chemin /uploads/ ou une adresse http(s)");
        }
        vehicle.setAnnee(request.annee());
        vehicle.setPlaces(request.places());
        vehicle.setTransmission(allowedValue(request.transmission(), TRANSMISSIONS, "La transmission"));
        vehicle.setCarburant(allowedValue(request.carburant(), FUELS, "Le carburant"));
        vehicle.setPrixParJour(request.prixParJour());
        vehicle.setDescription(request.description());
        vehicle.setImage(image.isEmpty() ? null : image);
        vehicle.setStatut(status);
        Category category = categories.findById(request.categorieId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Catégorie introuvable"));
        vehicle.setCategorie(category);
    }
}
