package com.antigravity.rental.service;

import com.antigravity.rental.model.vehicle.AntigravityVehicle;
import com.antigravity.rental.model.vehicle.FlyingCar;
import com.antigravity.rental.model.vehicle.Hoverboard;
import com.antigravity.rental.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * COMPONENT 2: Antigravity Vehicle Management
 * CLASS: VehicleService (Fleet Operations Layer)
 * ============================================================================
 * Coordinates vehicle additions, stock validations, and performance inspections.
 * ============================================================================
 */
@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * CREATE: Adds a new Hoverboard to the fleet.
     */
    public Hoverboard addHoverboard(String id, String model, String maker, double rate, int stock,
                                   double efficiency, double weightLimit, boolean stabilizer, String grip) {
        Hoverboard hoverboard = new Hoverboard(id, model, maker, rate, stock, efficiency, "AVAILABLE",
                weightLimit, stabilizer, grip);
        vehicleRepository.save(hoverboard);
        return hoverboard;
    }

    /**
     * CREATE: Adds a new Flying Car cruiser to the fleet.
     */
    public FlyingCar addFlyingCar(String id, String model, String maker, double rate, int stock,
                                 double efficiency, int capacity, boolean shielding, double hyperdrive) {
        FlyingCar car = new FlyingCar(id, model, maker, rate, stock, efficiency, "AVAILABLE",
                capacity, shielding, hyperdrive);
        vehicleRepository.save(car);
        return car;
    }

    /**
     * READ: Returns all antigravity vehicles in inventory.
     */
    public List<AntigravityVehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    /**
     * READ: Look up a vehicle by ID.
     */
    public Optional<AntigravityVehicle> getVehicleById(String vehicleId) {
        return vehicleRepository.findById(vehicleId);
    }

    /**
     * READ: Filter fleet by vehicle category (HOVERBOARD / FLYING_CAR).
     */
    public List<AntigravityVehicle> getVehiclesByType(String type) {
        return vehicleRepository.findByType(type);
    }

    /**
     * UPDATE: Modifies vehicle status and details.
     */
    public boolean updateVehicle(AntigravityVehicle vehicle) {
        return vehicleRepository.update(vehicle);
    }

    /**
     * UPDATE: Changes stock count for a vehicle.
     */
    public boolean updateStock(String vehicleId, int delta) {
        return vehicleRepository.updateStock(vehicleId, delta);
    }

    /**
     * DELETE: Retires/removes a vehicle from inventory.
     */
    public boolean deleteVehicle(String vehicleId) {
        return vehicleRepository.deleteById(vehicleId);
    }
}

