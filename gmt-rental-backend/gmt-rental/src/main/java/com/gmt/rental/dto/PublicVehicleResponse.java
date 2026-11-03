package com.gmt.rental.dto;

import com.gmt.rental.entity.Category;
import com.gmt.rental.entity.Vehicle;

import java.math.BigDecimal;

/** Vue publique d'un véhicule : volontairement sans l'immatriculation, réservée à l'administration. */
public record PublicVehicleResponse(
        Long id,
        String marque,
        String modele,
        Integer annee,
        Integer places,
        String transmission,
        String carburant,
        BigDecimal prixParJour,
        String description,
        String image,
        String statut,
        Category categorie
) {
    public static PublicVehicleResponse from(Vehicle vehicle) {
        if (vehicle == null) return null;
        return new PublicVehicleResponse(
                vehicle.getId(),
                vehicle.getMarque(),
                vehicle.getModele(),
                vehicle.getAnnee(),
                vehicle.getPlaces(),
                vehicle.getTransmission(),
                vehicle.getCarburant(),
                vehicle.getPrixParJour(),
                vehicle.getDescription(),
                vehicle.getImage(),
                vehicle.getStatut(),
                vehicle.getCategorie());
    }
}
