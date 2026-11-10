package com.antigravity.rental.model.vehicle;

/**
 * ============================================================================
 * COMPONENT 2: Antigravity Vehicle Management
 * CLASS: FlyingCar (Subclass)
 * ============================================================================
 * OOP CONCEPT HIGHLIGHTS:
 * 1. Inheritance:
 *    - Reuses `AntigravityVehicle` foundation for high-capacity atmospheric cruisers.
 * 2. Polymorphism:
 *    - Overrides `calculatePerformanceScore()` factoring in hyperdrive class rating,
 *      deflector shields, and multi-passenger thrust distribution.
 *    - Overrides `calculateMaxAltitudeMeters()` (cruises at atmospheric altitudes: 2500m - 8000m).
 * 3. Encapsulation:
 *    - Protects passenger safety limits and hyperdrive rating boundaries.
 * ============================================================================
 */
public class FlyingCar extends AntigravityVehicle {

    private int passengerCapacity;
    private boolean hasShielding;
    private double hyperDriveRating; // 1.0 to 5.0

    public FlyingCar(String vehicleId, String modelName, String manufacturer,
                     double baseHourlyRate, int stockQuantity, double gravDampenerEfficiency,
                     String status, int passengerCapacity, boolean hasShielding,
                     double hyperDriveRating) {
        super(vehicleId, modelName, manufacturer, baseHourlyRate, stockQuantity, gravDampenerEfficiency, status);
        setPassengerCapacity(passengerCapacity);
        this.hasShielding = hasShielding;
        setHyperDriveRating(hyperDriveRating);
    }

    /**
     * POLYMORPHIC IMPLEMENTATION:
     * Calculates score heavily weighted on propulsion class and canopy deflector shields.
     */
    @Override
    public double calculatePerformanceScore() {
        double dampenerWeight = getGravDampenerEfficiency() * 40.0;
        double hyperDriveScore = (hyperDriveRating / 5.0) * 35.0;
        double shieldScore = hasShielding ? 15.0 : 0.0;
        double capacityScore = (passengerCapacity >= 4) ? 10.0 : 5.0;
        return Math.min(100.0, Math.round((dampenerWeight + hyperDriveScore + shieldScore + capacityScore) * 10.0) / 10.0);
    }

    /**
     * Atmospheric corridor flight ceiling: 2,500 to 8,000 meters.
     */
    @Override
    public double calculateMaxAltitudeMeters() {
        return 2000.0 + (hyperDriveRating * 1200.0);
    }

    @Override
    public String getVehicleType() {
        return "FLYING_CAR";
    }

    @Override
    public String getDetailedSpecs() {
        return String.format("Capacity: %d Seats | Shielding: %s | Warp Class: %.1f",
                passengerCapacity, (hasShielding ? "Active Deflector" : "Unshielded"), hyperDriveRating);
    }

    /**
     * File serialization format:
     * FLYING_CAR|id|model|manufacturer|rate|stock|efficiency|status|capacity|shielding|hyperDrive
     */
    @Override
    public String toFileString() {
        return String.join("|",
                "FLYING_CAR",
                getVehicleId(),
                getModelName(),
                getManufacturer(),
                String.valueOf(getBaseHourlyRate()),
                String.valueOf(getStockQuantity()),
                String.valueOf(getGravDampenerEfficiency()),
                getStatus(),
                String.valueOf(passengerCapacity),
                String.valueOf(hasShielding),
                String.valueOf(hyperDriveRating)
        );
    }

    // Accessors and Mutators
    public int getPassengerCapacity() {
        return passengerCapacity;
    }

    public void setPassengerCapacity(int passengerCapacity) {
        if (passengerCapacity < 1 || passengerCapacity > 12) {
            throw new IllegalArgumentException("[Validation Error] Passenger capacity must be between 1 and 12.");
        }
        this.passengerCapacity = passengerCapacity;
    }

    public boolean isHasShielding() {
        return hasShielding;
    }

    public void setHasShielding(boolean hasShielding) {
        this.hasShielding = hasShielding;
    }

    public double getHyperDriveRating() {
        return hyperDriveRating;
    }

    public void setHyperDriveRating(double hyperDriveRating) {
        if (hyperDriveRating < 1.0 || hyperDriveRating > 5.0) {
            throw new IllegalArgumentException("[Validation Error] HyperDrive rating must be between 1.0 and 5.0.");
        }
        this.hyperDriveRating = hyperDriveRating;
    }
}

