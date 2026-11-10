package com.antigravity.rental.controller;

import com.antigravity.rental.model.rental.RentalTransaction;
import com.antigravity.rental.model.rental.policy.FineCalculator;
import com.antigravity.rental.model.user.User;
import com.antigravity.rental.service.RentalService;
import com.antigravity.rental.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ============================================================================
 * COMPONENT 3: Rental & Return Transactions
 * CLASS: RentalController (REST API Endpoints)
 * ============================================================================
 * Endpoints for booking rentals, returning vehicles, previewing fines, and purging logs.
 * ============================================================================
 */
@RestController
@RequestMapping("/api/rentals")
@CrossOrigin(origins = "*")
public class RentalController {

    private final RentalService rentalService;
    private final UserService userService;

    public RentalController(RentalService rentalService, UserService userService) {
        this.rentalService = rentalService;
        this.userService = userService;
    }

    /**
     * CREATE: Book a rental.
     */
    @PostMapping
    public ResponseEntity<?> createRental(@RequestBody Map<String, Object> payload) {
        try {
            String userId = (String) payload.get("userId");
            String vehicleId = (String) payload.get("vehicleId");
            int hours = Integer.parseInt(payload.getOrDefault("rentalDurationHours", "4").toString());

            RentalTransaction tx = rentalService.rentVehicle(userId, vehicleId, hours);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "status", "SUCCESS",
                    "message", "Rental booking confirmed.",
                    "transaction", tx
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }

    /**
     * READ: Get all rentals or filter by active status.
     */
    @GetMapping
    public ResponseEntity<List<RentalTransaction>> getRentals(@RequestParam(required = false) Boolean activeOnly) {
        if (Boolean.TRUE.equals(activeOnly)) {
            return ResponseEntity.ok(rentalService.getActiveRentals());
        }
        return ResponseEntity.ok(rentalService.getAllRentals());
    }

    /**
     * READ: Get single transaction by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getRentalById(@PathVariable String id) {
        return rentalService.getRentalById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "ERROR", "message", "Rental not found: " + id)));
    }

    /**
     * UPDATE: Complete vehicle return and calculate late fine polymorphically.
     */
    @PutMapping("/{id}/return")
    public ResponseEntity<?> returnVehicle(@PathVariable String id,
                                           @RequestBody(required = false) Map<String, Object> payload) {
        try {
            int overdueHours = 0;
            if (payload != null && payload.containsKey("overdueHours")) {
                overdueHours = Integer.parseInt(payload.get("overdueHours").toString());
            }

            RentalTransaction completedTx = rentalService.processReturn(id, overdueHours);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Vehicle successfully returned.",
                    "transaction", completedTx
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }

    /**
     * DELETE: Purge transaction log.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRentalLog(@PathVariable String id) {
        boolean deleted = rentalService.deleteRental(id);
        return deleted
                ? ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Rental log deleted."))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("status", "ERROR", "message", "Log not found."));
    }

    /**
     * Preview Polymorphic Fine Calculation endpoint (Great for demonstrating in viva).
     */
    @GetMapping("/preview-fine")
    public ResponseEntity<?> previewFine(@RequestParam String userId,
                                         @RequestParam double hourlyRate,
                                         @RequestParam long overdueHours) {
        Optional<User> userOpt = userService.getUserById(userId);
        FineCalculator calculator = rentalService.resolveFineCalculator(userOpt.orElse(null));
        double fine = calculator.calculateFine(overdueHours, hourlyRate);
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "policyApplied", calculator.getPolicyDescription(),
                "overdueHours", overdueHours,
                "hourlyRate", hourlyRate,
                "calculatedFine", fine
        ));
    }
}

