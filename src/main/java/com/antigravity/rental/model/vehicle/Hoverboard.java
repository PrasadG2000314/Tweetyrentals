package com.antigravity.rental.model.vehicle;

/**
 * ============================================================================
 * COMPONENT 2: Antigravity Vehicle Management
 * CLASS: Hoverboard (Subclass)
 * ============================================================================
 * OOP CONCEPT HIGHLIGHTS:
 * 1. Inheritance:
 *    - Inherits core antigravity chassis properties from `AntigravityVehicle`.
 * 2. Polymorphism:
 *    - Overrides `calculatePerformanceScore()` to account for personal rider agility,
 *      stabilizer feedback, and weight tolerance.
 *    - Overrides `calculateMaxAltitudeMeters()` (Hoverboards operate in low-altitude hover zones: 2m - 12m).
 * 3. Encapsulation:
 *    - Keeps rider weight limit, stabilizer status, and grip type protected.
 * ============================================================================
 */
public class Hoverboard extends AntigravityVehicle {

    private double weightLimitKg;
    private boolean hasQuantumStabilizer;
    private String deckGripType; // "Magnetic", "Plasma-Grip", "Carbon-Nano"

    public Hoverboard(String vehicleId, String modelName, String manufacturer,
                      double baseHourlyRate, int stockQuantity, double gravDampenerEfficiency,
                      String status, double weightLimitKg, boolean hasQuantumStabilizer,
                      String deckGripType) {
        super(vehicleId, modelName, manufacturer, baseHourlyRate, stockQuantity, gravDampenerEfficiency, status);
        setWeightLimitKg(weightLimitKg);
        this.hasQuantumStabilizer = hasQuantumStabilizer;
        setDeckGripType(deckGripType);
    }

    /**
     * POLYMORPHIC IMPLEMENTATION:
     * Hoverboard score rewards agility, lightweight dampeners, and quantum stability.
     */
    @Override
    public double calculatePerformanceScore() {
        double baseScore = getGravDampenerEfficiency() * 70.0;
        double stabilizerBonus = hasQuantumStabilizer ? 20.0 : 5.0;
        double weightFactor = (weightLimitKg >= 120.0) ? 10.0 : 5.0;
        return Math.min(100.0, Math.round((baseScore + stabilizerBonus + weightFactor) * 10.0) / 10.0);
    }

    /**
     * Low altitude safety envelope: 8 to 15 meters.
     */
    @Override
    public double calculateMaxAltitudeMeters() {
        return hasQuantumStabilizer ? 15.0 : 8.0;
    }

    @Override
    public String getVehicleType() {
        return "HOVERBOARD";
    }

    @Override
    public String getDetailedSpecs() {
        return String.format("Weight Limit: %.1f kg | Stabilizer: %s | Grip: %s",
                weightLimitKg, (hasQuantumStabilizer ? "Quantum-Locked" : "Standard Gyro"), deckGripType);
    }

    /**
     * File serialization format:
     * HOVERBOARD|id|model|manufacturer|rate|stock|efficiency|status|weightLimit|stabilizer|gripType
     */
    @Override
    public String toFileString() {
        return String.join("|",
                "HOVERBOARD",
                getVehicleId(),
                getModelName(),
                getManufacturer(),
                String.valueOf(getBaseHourlyRate()),
                String.valueOf(getStockQuantity()),
                String.valueOf(getGravDampenerEfficiency()),
                getStatus(),
                String.valueOf(weightLimitKg),
                String.valueOf(hasQuantumStabilizer),
                getDeckGripType()
        );
    }

    // Accessors and Mutators
    public double getWeightLimitKg() {
        return weightLimitKg;
    }

    public void setWeightLimitKg(double weightLimitKg) {
        if (weightLimitKg <= 30.0) {
            throw new IllegalArgumentException("[Validation Error] Weight limit must exceed 30 kg.");
        }
        this.weightLimitKg = weightLimitKg;
    }

    public boolean isHasQuantumStabilizer() {
        return hasQuantumStabilizer;
    }

    public void setHasQuantumStabilizer(boolean hasQuantumStabilizer) {
        this.hasQuantumStabilizer = hasQuantumStabilizer;
    }

    public String getDeckGripType() {
        return deckGripType;
    }

    public void setDeckGripType(String deckGripType) {
        this.deckGripType = (deckGripType != null && !deckGripType.trim().isEmpty())
                ? deckGripType.trim() : "Standard Grip";
    }
}

