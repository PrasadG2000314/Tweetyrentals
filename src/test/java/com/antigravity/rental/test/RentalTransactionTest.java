package com.antigravity.rental.test;

import com.antigravity.rental.model.rental.RentalStatus;
import com.antigravity.rental.model.rental.RentalTransaction;
import com.antigravity.rental.model.rental.policy.FineCalculator;
import com.antigravity.rental.model.rental.policy.RegularFineCalculator;
import com.antigravity.rental.model.rental.policy.VIPFineCalculator;
import com.antigravity.rental.model.user.RegularUser;
import com.antigravity.rental.model.vehicle.Hoverboard;
import com.antigravity.rental.repository.RentalRepository;
import com.antigravity.rental.repository.UserRepository;
import com.antigravity.rental.repository.VehicleRepository;
import com.antigravity.rental.service.RentalService;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================================
 * UNIT TEST SUITE: Component 3 - Rental & Return Transactions
 * ============================================================================
 * Tests:
 * 1. Strategy Pattern Polymorphism (RegularFineCalculator vs VIPFineCalculator)
 * 2. Vehicle Availability & Fleet Inventory Locks
 * 3. Return Processing & Overdue Fine Resolution
 * 4. File CRUD Operations in RentalRepository
 * ============================================================================
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RentalTransactionTest {

    private static Path tempRentalFile;
    private static Path tempUserFile;
    private static Path tempVehicleFile;

    private RentalRepository rentalRepository;
    private UserRepository userRepository;
    private VehicleRepository vehicleRepository;
    private RentalService rentalService;

    @BeforeEach
    public void setup() throws IOException {
        tempRentalFile = Files.createTempFile("rentals_test", ".txt");
        tempUserFile = Files.createTempFile("users_rental_test", ".txt");
        tempVehicleFile = Files.createTempFile("vehicles_rental_test", ".txt");

        rentalRepository = new RentalRepository(tempRentalFile.toString());
        userRepository = new UserRepository(tempUserFile.toString());
        vehicleRepository = new VehicleRepository(tempVehicleFile.toString());

        rentalService = new RentalService(rentalRepository, vehicleRepository, userRepository);
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(tempRentalFile);
        Files.deleteIfExists(tempUserFile);
        Files.deleteIfExists(tempVehicleFile);
    }

    @Test
    @Order(1)
    @DisplayName("OOP Polymorphism (Strategy Pattern): Verifies fine calculations for Regular vs VIP tiers")
    public void testPolymorphicFineCalculators() {
        FineCalculator regularPolicy = new RegularFineCalculator();
        FineCalculator vipPolicy = new VIPFineCalculator();

        double hourlyRate = 40.0;
        long overdueHours = 3;

        // Regular: $25 fixed fee + (3 hours * $40 * 2.0) = 25 + 240 = $265.00
        double regularFine = regularPolicy.calculateFine(overdueHours, hourlyRate);
        assertEquals(265.0, regularFine, 0.01);

        // VIP: 2-hour grace period means only 1 chargeable hour, $0 fixed fee + (1 * $40 * 1.25) = $50.00
        double vipFine = vipPolicy.calculateFine(overdueHours, hourlyRate);
        assertEquals(50.0, vipFine, 0.01);

        // VIP within 2-hour grace period: $0 fine
        assertEquals(0.0, vipPolicy.calculateFine(2, hourlyRate));
    }

    @Test
    @Order(2)
    @DisplayName("Transaction Lifecycle: Rents vehicle, decrements stock, returns with fine, and restores stock")
    public void testFullRentalLifecycle() {
        // Seed user and vehicle
        RegularUser renter = new RegularUser("U-1", "alice", "alice@wonder.com", "secret", false, 0, "555-0123");
        userRepository.save(renter);

        Hoverboard vehicle = new Hoverboard("HB-RNT", "Glide Pro", "AeroGrav", 30.0, 2, 0.90, "AVAILABLE", 95, true, "Magnetic");
        vehicleRepository.save(vehicle);

        // 1. Book Rental (Stock should decrease 2 -> 1)
        RentalTransaction tx = rentalService.rentVehicle("U-1", "HB-RNT", 4);
        assertNotNull(tx.getRentalId());
        assertEquals(RentalStatus.ACTIVE, tx.getStatus());
        assertEquals(1, vehicleRepository.findById("HB-RNT").get().getStockQuantity());

        // 2. Process Return with 2 Simulated Overdue Hours
        RentalTransaction returnedTx = rentalService.processReturn(tx.getRentalId(), 2);
        assertEquals(RentalStatus.RETURNED, returnedTx.getStatus());
        assertTrue(returnedTx.getLateFine() > 0);

        // 3. Verify stock restored back to 2
        assertEquals(2, vehicleRepository.findById("HB-RNT").get().getStockQuantity());
    }

    @Test
    @Order(3)
    @DisplayName("CRUD Operations: File-based Create, Read, Update, Delete for Rentals")
    public void testRentalFileCrudOperations() {
        RentalTransaction tx = new RentalTransaction(
                "RNT-99", "U-1", "V-1",
                LocalDateTime.now(), LocalDateTime.now().plusHours(2), null,
                30.0, 60.0, 0.0, 60.0, RentalStatus.ACTIVE
        );

        // CREATE
        assertTrue(rentalRepository.save(tx));

        // READ
        assertTrue(rentalRepository.findById("RNT-99").isPresent());
        assertEquals(1, rentalRepository.findActiveRentals().size());

        // UPDATE
        tx.setStatus(RentalStatus.RETURNED);
        assertTrue(rentalRepository.update(tx));
        assertEquals(RentalStatus.RETURNED, rentalRepository.findById("RNT-99").get().getStatus());

        // DELETE
        assertTrue(rentalRepository.deleteById("RNT-99"));
        assertTrue(rentalRepository.findById("RNT-99").isEmpty());
    }
}
