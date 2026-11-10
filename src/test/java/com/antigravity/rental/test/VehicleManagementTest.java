package com.antigravity.rental.test;

import com.antigravity.rental.model.vehicle.AntigravityVehicle;
import com.antigravity.rental.model.vehicle.FlyingCar;
import com.antigravity.rental.model.vehicle.Hoverboard;
import com.antigravity.rental.repository.VehicleRepository;
import com.antigravity.rental.service.VehicleService;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================================
 * UNIT TEST SUITE: Component 2 - Antigravity Vehicle Management
 * ============================================================================
 * Tests:
 * 1. Encapsulation & Dampener Efficiency Bounds (0.1 to 1.0)
 * 2. Inheritance Hierarchy & Shared Rate Calculations
 * 3. Polymorphic Flight Altitude & Performance Scoring
 * 4. File CRUD Operations & Stock Decrementing in VehicleRepository
 * ============================================================================
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VehicleManagementTest {

    private static Path tempVehicleFile;
    private VehicleRepository vehicleRepository;
    private VehicleService vehicleService;

    @BeforeEach
    public void setup() throws IOException {
        tempVehicleFile = Files.createTempFile("vehicles_test", ".txt");
        vehicleRepository = new VehicleRepository(tempVehicleFile.toString());
        vehicleService = new VehicleService(vehicleRepository);
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(tempVehicleFile);
    }

    @Test
    @Order(1)
    @DisplayName("OOP Encapsulation: Enforces physical dampener boundaries")
    public void testEncapsulationAndBounds() {
        // Efficiency below 0.1 must throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            new Hoverboard("HB-ERR", "TestBoard", "Maker", 25.0, 5, 0.05, "AVAILABLE", 80, true, "Grip");
        });

        // Efficiency above 1.0 must throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            new FlyingCar("FC-ERR", "TestCar", "Maker", 90.0, 2, 1.25, "AVAILABLE", 4, true, 3.0);
        });
    }

    @Test
    @Order(2)
    @DisplayName("OOP Polymorphism: Distinct performance and altitude limits for Hoverboard vs FlyingCar")
    public void testPolymorphicVehicleCalculations() {
        AntigravityVehicle hoverboard = new Hoverboard(
                "HB-1", "AeroBoard X", "CyberSkate", 30.0, 3, 0.90, "AVAILABLE", 110.0, true, "Plasma-Grip"
        );
        AntigravityVehicle flyingCar = new FlyingCar(
                "FC-1", "SkyCruiser Alpha", "OmniAero", 100.0, 2, 0.95, "AVAILABLE", 4, true, 4.0
        );

        // Hoverboard low-altitude safety envelope (<= 15m)
        assertTrue(hoverboard.calculateMaxAltitudeMeters() <= 15.0);
        assertEquals(15.0, hoverboard.calculateMaxAltitudeMeters());

        // Flying car high-altitude atmospheric flight (>= 2000m)
        assertTrue(flyingCar.calculateMaxAltitudeMeters() >= 2000.0);
        assertEquals(6800.0, flyingCar.calculateMaxAltitudeMeters()); // 2000 + 4.0 * 1200

        // Polymorphic scores
        assertTrue(hoverboard.calculatePerformanceScore() > 0);
        assertTrue(flyingCar.calculatePerformanceScore() > 0);
        assertEquals("HOVERBOARD", hoverboard.getVehicleType());
        assertEquals("FLYING_CAR", flyingCar.getVehicleType());
    }

    @Test
    @Order(3)
    @DisplayName("CRUD Operations: File-based Create, Read, Update Stock, and Delete for Vehicles")
    public void testVehicleFileCrudOperations() {
        // CREATE
        Hoverboard hb = new Hoverboard("HB-CRUD", "LevBoard V", "AeroGrav", 35.0, 5, 0.85, "AVAILABLE", 100, true, "Magnetic");
        boolean created = vehicleRepository.save(hb);
        assertTrue(created);

        // READ
        Optional<AntigravityVehicle> fetched = vehicleRepository.findById("HB-CRUD");
        assertTrue(fetched.isPresent());
        assertEquals("LevBoard V", fetched.get().getModelName());

        // UPDATE STOCK
        boolean stockReduced = vehicleRepository.updateStock("HB-CRUD", -2); // 5 - 2 = 3
        assertTrue(stockReduced);
        assertEquals(3, vehicleRepository.findById("HB-CRUD").get().getStockQuantity());

        // DELETE
        boolean deleted = vehicleRepository.deleteById("HB-CRUD");
        assertTrue(deleted);
        assertTrue(vehicleRepository.findById("HB-CRUD").isEmpty());
    }
}
