package com.gmt.rental.controller;

import com.gmt.rental.dto.QuoteCreatedResponse;
import com.gmt.rental.dto.QuoteRequestDTO;
import com.gmt.rental.dto.VehicleAvailabilityResponse;
import com.gmt.rental.entity.QuoteRequest;
import com.gmt.rental.security.RateLimiter;
import com.gmt.rental.service.AvailabilityConflictException;
import com.gmt.rental.service.QuoteRequestService;
import com.gmt.rental.service.VehicleAvailabilityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/public/demandes")
public class QuoteRequestController {

    private final QuoteRequestService quoteRequestService;
    private final VehicleAvailabilityService vehicleAvailabilityService;
    // Au plus 20 demandes de devis par adresse IP et par tranche de 10 minutes.
    private final RateLimiter quoteLimiter = new RateLimiter(20, Duration.ofMinutes(10));

    public QuoteRequestController(
            QuoteRequestService quoteRequestService,
            VehicleAvailabilityService vehicleAvailabilityService) {
        this.quoteRequestService = quoteRequestService;
        this.vehicleAvailabilityService = vehicleAvailabilityService;
    }

    @GetMapping("/disponibilite")
    public VehicleAvailabilityResponse checkAvailability(
            @RequestParam Long vehiculeId,
            @RequestParam LocalDate dateDebut,
            @RequestParam LocalDate dateFin) {
        return vehicleAvailabilityService.checkAvailability(vehiculeId, dateDebut, dateFin);
    }

    @PostMapping
    public ResponseEntity<QuoteCreatedResponse> createQuote(
            @RequestBody QuoteRequestDTO dto,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            HttpServletRequest http) {

        if (!quoteLimiter.tryAcquire(http.getRemoteAddr())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Trop de demandes envoyées. Réessayez dans quelques minutes.");
        }

        QuoteRequest savedQuote =
                quoteRequestService.createQuote(dto, idempotencyKey);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(QuoteCreatedResponse.from(savedQuote));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleInvalidRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    @ExceptionHandler(AvailabilityConflictException.class)
    public ResponseEntity<VehicleAvailabilityResponse> handleAvailabilityConflict(
            AvailabilityConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new VehicleAvailabilityResponse(false, exception.getConflictingPeriods(), exception.getMessage()));
    }

}
