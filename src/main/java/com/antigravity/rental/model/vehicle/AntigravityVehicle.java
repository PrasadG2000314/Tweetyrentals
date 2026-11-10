package com.antigravity.rental.model.vehicle;

/**
 * ============================================================================
 * COMPONENT 2: Antigravity Vehicle Management
 * CLASS: AntigravityVehicle (Abstract Base Class)
 * ============================================================================
 * OOP CONCEPT HIGHLIGHTS:
 * 1. Abstraction:
 *    - Captures the universal characteristics and capabilities of an antigravity
 *      propulsion vehicle while leaving vehicle-specific physics calculations
 *      (e.g., flight ceiling, performance rating) to concrete subclasses.
 * 2. Encapsulation & Information Hiding:
 *    - All state properties (rates, dampeners, stock counts) are strictly private.
 *    - Mutators enforce physical constraints (e.g., dampener efficiency must be between 0.1 and 1.0).
 * 3. Polymorphism:
 *    - Defines abstract methods `calculatePerformanceScore()`, `calculateMaxAltitudeMeters()`,
 *      and `toFileString()` that each vehicle category must realize.
 * ============================================================================
 */
public abstract class AntigravityVehicle {

    private String vehicleId;
    private String modelName;
    private String manufacturer;
    private double baseHourlyRate;
    private int stockQuantity;
    private double gravDampenerEfficiency; // Value between 0.1 and 1.0 (anti-inertia rating)
    private String status; // "AVAILABLE", "MAINTENANCE", "RETIRED"

    public AntigravityVehicle(String vehicleId, String modelName, String manufacturer,
                              double baseHourlyRate, int stockQuantity,
                              double gravDampenerEfficiency, String status) {
        setVehicleId(vehicleId);
        setModelName(modelName);
        setManufacturer(manufacturer);
        setBaseHourlyRate(baseHourlyRate);
        setStockQuantity(stockQuantity);
        setGravDampenerEfficiency(gravDampenerEfficiency);
        setStatus(status);
    }

    // =========================================================================
    // ABSTRACT METHODS (POLYMORPHIC CONTRACT)
    // =========================================================================

    /**
     * Polymorphic method to compute the operational performance score
     * based on vehicle-specific propulsion mechanics.
     */
    public abstract double calculatePerformanceScore();

    /**
     * Polymorphic method returning the maximum certified operational altitude in meters.
     */
    public abstract double calculateMaxAltitudeMeters();

    /**
     * Returns the distinct vehicle category discriminator string.
     */
    public abstract String getVehicleType();

    /**
     * Returns serialized string for file persistence.
     */
    public abstract String toFileString();

    /**
     * Returns human-readable summary of specialized hardware specs.
     */
    public abstract String getDetailedSpecs();

    // =========================================================================
    // CONCRETE LOGIC (CODE REUSE VIA INHERITANCE)
    // =========================================================================

    /**
     * Calculates the effective hourly rental rate taking dampener efficiency into account.
     */
    public double getEffectiveHourlyRate() {
        // High dampener efficiency reduces kinetic stress, slightly lowering wear cost
        return Math.round((baseHourlyRate * (1.2 - (gravDampenerEfficiency * 0.2))) * 100.0) / 100.0;
    }

    public boolean isRentable() {
        return stockQuantity > 0 && "AVAILABLE".equalsIgnoreCase(status);
    }

    // =========================================================================
    // ENCAPSULATED ACCESSORS & MUTATORS
    // =========================================================================

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        if (vehicleId == null || vehicleId.trim().isEmpty()) {
            throw new IllegalArgumentException("[Validation Error] Vehicle ID cannot be empty.");
        }
        this.vehicleId = vehicleId.trim();
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new IllegalArgumentException("[Validation Error] Model name cannot be empty.");
        }
        this.modelName = modelName.trim();
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = (manufacturer != null) ? manufacturer.trim() : "Unknown Labs";
    }

    public double getBaseHourlyRate() {
        return baseHourlyRate;
    }

    public void setBaseHourlyRate(double baseHourlyRate) {
        if (baseHourlyRate <= 0) {
            throw new IllegalArgumentException("[Validation Error] Base hourly rate must be positive.");
        }
        this.baseHourlyRate = baseHourlyRate;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = Math.max(0, stockQuantity);
    }

    public double getGravDampenerEfficiency() {
        return gravDampenerEfficiency;
    }

    public void setGravDampenerEfficiency(double gravDampenerEfficiency) {
        if (gravDampenerEfficiency < 0.1 || gravDampenerEfficiency > 1.0) {
            throw new IllegalArgumentException("[Validation Error] Gravitational Dampener Efficiency must be between 0.1 and 1.0.");
        }
        this.gravDampenerEfficiency = gravDampenerEfficiency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = (status != null && !status.trim().isEmpty()) ? status.trim().toUpperCase() : "AVAILABLE";
    }
}

