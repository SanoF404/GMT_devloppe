package com.gmt.rental.controller;

import com.gmt.rental.dto.PublicVehicleResponse;
import com.gmt.rental.service.VehicleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicules")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    // GET /api/vehicules
    @GetMapping
    public List<PublicVehicleResponse> getAllVehicles(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) Long categorieId) {

        return vehicleService.searchPublicVehicles(statut, categorieId).stream()
                .map(PublicVehicleResponse::from)
                .toList();
    }

    // GET /api/vehicules/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PublicVehicleResponse> getVehicleById(@PathVariable Long id) {

        return vehicleService.getPublicVehicleById(id)
                .map(vehicle -> ResponseEntity.ok(PublicVehicleResponse.from(vehicle)))
                .orElse(ResponseEntity.notFound().build());
    }
}
