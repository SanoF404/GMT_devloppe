package com.gmt.rental.dto;

import com.gmt.rental.entity.QuoteRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Réponse renvoyée au client après l'envoi d'une demande : aucune donnée interne (plaque, clé technique). */
public record QuoteCreatedResponse(
        Long id,
        String nom,
        String prenom,
        String email,
        String telephone,
        PublicVehicleResponse vehicule,
        LocalDate dateDebut,
        LocalDate dateFin,
        String lieuPriseEnCharge,
        String lieuRestitution,
        String message,
        BigDecimal prixTotal,
        String statut,
        LocalDateTime dateCreation
) {
    public static QuoteCreatedResponse from(QuoteRequest quote) {
        return new QuoteCreatedResponse(
                quote.getId(),
                quote.getNom(),
                quote.getPrenom(),
                quote.getEmail(),
                quote.getTelephone(),
                PublicVehicleResponse.from(quote.getVehicule()),
                quote.getDateDebut(),
                quote.getDateFin(),
                quote.getLieuPriseEnCharge(),
                quote.getLieuRestitution(),
                quote.getMessage(),
                quote.getPrixTotal(),
                quote.getStatut(),
                quote.getDateCreation());
    }
}
