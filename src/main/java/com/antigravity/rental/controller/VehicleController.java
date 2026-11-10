package com.antigravity.rental.controller;

import com.antigravity.rental.model.vehicle.AntigravityVehicle;
import com.antigravity.rental.model.vehicle.FlyingCar;
import com.antigravity.rental.model.vehicle.Hoverboard;
import com.antigravity.rental.service.VehicleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * ============================================================================
 * COMPONENT 2: Antigravity Vehicle Management
 * CLASS: VehicleController (REST API Endpoints)
 * ============================================================================
 * Endpoints for vehicle creation, retrieval, updates, and retirement.
 * ============================================================================
 */
@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    /**
     * CREATE: Add new vehicle endpoint.
     */
    @PostMapping
    public ResponseEntity<?> addVehicle(@RequestBody Map<String, Object> payload) {
        try {
            String type = ((String) payload.getOrDefault("vehicleType", "HOVERBOARD")).toUpperCase();
            String id = (String) payload.getOrDefault("vehicleId", "VEH-" + System.currentTimeMillis() % 10000);
            String model = (String) payload.get("modelName");
            String maker = (String) payload.getOrDefault("manufacturer", "AeroGrav Dynamics");
            double rate = Double.parseDouble(payload.get("baseHourlyRate").toString());
            int stock = Integer.parseInt(payload.getOrDefault("stockQuantity", "1").toString());
            double efficiency = Double.parseDouble(payload.getOrDefault("gravDampenerEfficiency", "0.85").toString());

            AntigravityVehicle created;
            if ("FLYING_CAR".equals(type)) {
                int capacity = Integer.parseInt(payload.getOrDefault("passengerCapacity", "4").toString());
                boolean shielding = Boolean.parseBoolean(payload.getOrDefault("hasShielding", "true").toString());
                double hyperdrive = Double.parseDouble(payload.getOrDefault("hyperDriveRating", "2.5").toString());
                created = vehicleService.addFlyingCar(id, model, maker, rate, stock, efficiency, capacity, shielding, hyperdrive);
            } else {
                double weightLimit = Double.parseDouble(payload.getOrDefault("weightLimitKg", "110").toString());
                boolean stabilizer = Boolean.parseBoolean(payload.getOrDefault("hasQuantumStabilizer", "true").toString());
                String grip = (String) payload.getOrDefault("deckGripType", "Plasma-Grip");
                created = vehicleService.addHoverboard(id, model, maker, rate, stock, efficiency, weightLimit, stabilizer, grip);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "status", "SUCCESS",
                    "message", "Vehicle successfully added to fleet.",
                    "vehicle", mapVehicleWithPolymorphicSpecs(created)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }

    /**
     * READ: Get all vehicles, optionally filtered by type.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getVehicles(@RequestParam(required = false) String type) {
        List<AntigravityVehicle> list = (type != null && !type.trim().isEmpty())
                ? vehicleService.getVehiclesByType(type.trim())
                : vehicleService.getAllVehicles();

        List<Map<String, Object>> response = new ArrayList<>();
        for (AntigravityVehicle v : list) {
            response.add(mapVehicleWithPolymorphicSpecs(v));
        }
        return ResponseEntity.ok(response);
    }

    /**
     * READ: Get single vehicle by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getVehicleById(@PathVariable String id) {
        return vehicleService.getVehicleById(id)
                .<ResponseEntity<?>>map(v -> ResponseEntity.ok(mapVehicleWithPolymorphicSpecs(v)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "ERROR", "message", "Vehicle not found with ID: " + id)));
    }

    /**
     * UPDATE: Update vehicle stock or specifications.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicle(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        Optional<AntigravityVehicle> opt = vehicleService.getVehicleById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "ERROR", "message", "Vehicle not found"));
        }

        AntigravityVehicle v = opt.get();
        if (payload.containsKey("modelName")) v.setModelName((String) payload.get("modelName"));
        if (payload.containsKey("baseHourlyRate")) v.setBaseHourlyRate(Double.parseDouble(payload.get("baseHourlyRate").toString()));
        if (payload.containsKey("stockQuantity")) v.setStockQuantity(Integer.parseInt(payload.get("stockQuantity").toString()));
        if (payload.containsKey("status")) v.setStatus((String) payload.get("status"));
        if (payload.containsKey("gravDampenerEfficiency")) v.setGravDampenerEfficiency(Double.parseDouble(payload.get("gravDampenerEfficiency").toString()));

        if (v instanceof Hoverboard h) {
            if (payload.containsKey("weightLimitKg")) h.setWeightLimitKg(Double.parseDouble(payload.get("weightLimitKg").toString()));
            if (payload.containsKey("hasQuantumStabilizer")) h.setHasQuantumStabilizer(Boolean.parseBoolean(payload.get("hasQuantumStabilizer").toString()));
            if (payload.containsKey("deckGripType")) h.setDeckGripType((String) payload.get("deckGripType"));
        } else if (v instanceof FlyingCar fc) {
            if (payload.containsKey("passengerCapacity")) fc.setPassengerCapacity(Integer.parseInt(payload.get("passengerCapacity").toString()));
            if (payload.containsKey("hasShielding")) fc.setHasShielding(Boolean.parseBoolean(payload.get("hasShielding").toString()));
            if (payload.containsKey("hyperDriveRating")) fc.setHyperDriveRating(Double.parseDouble(payload.get("hyperDriveRating").toString()));
        }

        boolean updated = vehicleService.updateVehicle(v);
        return updated
                ? ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Vehicle updated.", "vehicle", mapVehicleWithPolymorphicSpecs(v)))
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", "ERROR", "message", "Update failed."));
    }

    /**
     * DELETE: Delete/retire vehicle by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable String id) {
        boolean deleted = vehicleService.deleteVehicle(id);
        return deleted
                ? ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Vehicle retired from fleet."))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("status", "ERROR", "message", "Vehicle ID not found."));
    }

    /**
     * Helper to project polymorphic vehicle calculations into JSON.
     */
    private Map<String, Object> mapVehicleWithPolymorphicSpecs(AntigravityVehicle v) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("vehicleId", v.getVehicleId());
        map.put("vehicleType", v.getVehicleType());
        map.put("modelName", v.getModelName());
        map.put("manufacturer", v.getManufacturer());
        map.put("baseHourlyRate", v.getBaseHourlyRate());
        map.put("effectiveHourlyRate", v.getEffectiveHourlyRate());
        map.put("stockQuantity", v.getStockQuantity());
        map.put("gravDampenerEfficiency", v.getGravDampenerEfficiency());
        map.put("status", v.getStatus());
        map.put("isRentable", v.isRentable());
        // Polymorphic dynamically calculated values
        map.put("performanceScore", v.calculatePerformanceScore());
        map.put("maxAltitudeMeters", v.calculateMaxAltitudeMeters());
        map.put("detailedSpecs", v.getDetailedSpecs());
        return map;
    }
}

