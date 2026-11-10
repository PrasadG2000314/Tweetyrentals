package com.antigravity.rental.repository;

import com.antigravity.rental.model.vehicle.AntigravityVehicle;
import com.antigravity.rental.model.vehicle.FlyingCar;
import com.antigravity.rental.model.vehicle.Hoverboard;
import com.antigravity.rental.util.FileStorageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * COMPONENT 2: Antigravity Vehicle Management
 * CLASS: VehicleRepository (File Storage CRUD Layer)
 * ============================================================================
 * CRUD OPERATIONS IMPLEMENTED:
 * 1. Create: save(AntigravityVehicle vehicle)
 * 2. Read:   findAll(), findById(String id), findByType(String type)
 * 3. Update: update(AntigravityVehicle vehicle), updateStock(String id, int delta)
 * 4. Delete: deleteById(String id)
 *
 * OOP HIGHLIGHTS:
 * - Polymorphic Deserialization: Dynamically reconstructs Hoverboard and FlyingCar
 *   objects while treating them uniformly as `AntigravityVehicle` instances.
 * ============================================================================
 */
@Repository
public class VehicleRepository {

    @Value("${app.data.vehicles-file:data/vehicles.txt}")
    private String filePath;

    public VehicleRepository() {}

    public VehicleRepository(String filePath) {
        this.filePath = filePath;
    }

    /**
     * CREATE: Adds a new antigravity vehicle to the storage file.
     */
    public boolean save(AntigravityVehicle vehicle) {
        if (findById(vehicle.getVehicleId()).isPresent()) {
            throw new IllegalArgumentException("Vehicle ID " + vehicle.getVehicleId() + " already exists.");
        }
        return FileStorageUtil.appendLine(filePath, vehicle.toFileString());
    }

    /**
     * READ: Reads all vehicles and dynamically casts them to concrete types.
     */
    public List<AntigravityVehicle> findAll() {
        List<String> lines = FileStorageUtil.readAllLines(filePath);
        List<AntigravityVehicle> vehicles = new ArrayList<>();

        for (String line : lines) {
            AntigravityVehicle vehicle = parseVehicle(line);
            if (vehicle != null) {
                vehicles.add(vehicle);
            }
        }
        return vehicles;
    }

    /**
     * READ: Search vehicle by ID.
     */
    public Optional<AntigravityVehicle> findById(String vehicleId) {
        return findAll().stream()
                .filter(v -> v.getVehicleId().equalsIgnoreCase(vehicleId))
                .findFirst();
    }

    /**
     * READ: Filter vehicles by type (HOVERBOARD or FLYING_CAR).
     */
    public List<AntigravityVehicle> findByType(String type) {
        return findAll().stream()
                .filter(v -> v.getVehicleType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    /**
     * UPDATE: Updates complete vehicle specifications in the text file.
     */
    public boolean update(AntigravityVehicle vehicle) {
        return FileStorageUtil.updateLine(
                filePath,
                line -> {
                    String[] tokens = line.split("\\|");
                    return tokens.length > 1 && tokens[1].equalsIgnoreCase(vehicle.getVehicleId());
                },
                vehicle.toFileString()
        );
    }

    /**
     * UPDATE: Modifies stock quantity (e.g. decrement on rent, increment on return).
     */
    public boolean updateStock(String vehicleId, int stockDelta) {
        Optional<AntigravityVehicle> vehicleOpt = findById(vehicleId);
        if (vehicleOpt.isPresent()) {
            AntigravityVehicle vehicle = vehicleOpt.get();
            int newStock = vehicle.getStockQuantity() + stockDelta;
            if (newStock < 0) {
                return false;
            }
            vehicle.setStockQuantity(newStock);
            return update(vehicle);
        }
        return false;
    }

    /**
     * DELETE: Deletes / retires a vehicle from active storage.
     */
    public boolean deleteById(String vehicleId) {
        return FileStorageUtil.deleteLine(
                filePath,
                line -> {
                    String[] tokens = line.split("\\|");
                    return tokens.length > 1 && tokens[1].equalsIgnoreCase(vehicleId);
                }
        );
    }

    /**
     * Polymorphic Factory Method:
     * Parses raw file line into Hoverboard or FlyingCar.
     */
    private AntigravityVehicle parseVehicle(String line) {
        try {
            String[] tokens = line.split("\\|", -1);
            if (tokens.length < 8) return null;

            String type = tokens[0].trim().toUpperCase();
            String id = tokens[1].trim();
            String model = tokens[2].trim();
            String maker = tokens[3].trim();
            double rate = Double.parseDouble(tokens[4].trim());
            int stock = Integer.parseInt(tokens[5].trim());
            double efficiency = Double.parseDouble(tokens[6].trim());
            String status = tokens[7].trim();

            if ("HOVERBOARD".equals(type)) {
                double weightLimit = tokens.length > 8 ? Double.parseDouble(tokens[8].trim()) : 100.0;
                boolean stabilizer = tokens.length > 9 && Boolean.parseBoolean(tokens[9].trim());
                String grip = tokens.length > 10 ? tokens[10].trim() : "Standard";
                return new Hoverboard(id, model, maker, rate, stock, efficiency, status, weightLimit, stabilizer, grip);
            } else if ("FLYING_CAR".equals(type)) {
                int capacity = tokens.length > 8 ? Integer.parseInt(tokens[8].trim()) : 4;
                boolean shielding = tokens.length > 9 && Boolean.parseBoolean(tokens[9].trim());
                double hyperdrive = tokens.length > 10 ? Double.parseDouble(tokens[10].trim()) : 2.0;
                return new FlyingCar(id, model, maker, rate, stock, efficiency, status, capacity, shielding, hyperdrive);
            }
            return null;
        } catch (Exception e) {
            System.err.println("[VehicleRepository ERROR] Malformed line: " + line + " -> " + e.getMessage());
            return null;
        }
    }
}

