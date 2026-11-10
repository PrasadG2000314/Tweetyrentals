package com.antigravity.rental.service;

import com.antigravity.rental.model.rental.RentalStatus;
import com.antigravity.rental.model.rental.RentalTransaction;
import com.antigravity.rental.model.rental.policy.FineCalculator;
import com.antigravity.rental.model.rental.policy.RegularFineCalculator;
import com.antigravity.rental.model.rental.policy.VIPFineCalculator;
import com.antigravity.rental.model.user.RegularUser;
import com.antigravity.rental.model.user.User;
import com.antigravity.rental.model.vehicle.AntigravityVehicle;
import com.antigravity.rental.repository.RentalRepository;
import com.antigravity.rental.repository.UserRepository;
import com.antigravity.rental.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * COMPONENT 3: Rental & Return Transactions
 * CLASS: RentalService (Transaction Orchestrator)
 * ============================================================================
 * OOP CONCEPT HIGHLIGHTS:
 * 1. Polymorphism (Strategy Pattern Usage):
 *    - Dynamically selects either `VIPFineCalculator` or `RegularFineCalculator`
 *      at runtime based on user membership tier.
 * 2. Information Hiding & Encapsulation:
 *    - Shields return fee algorithms and stock synchronization from controllers.
 * ============================================================================
 */
@Service
public class RentalService {

    private final RentalRepository rentalRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public RentalService(RentalRepository rentalRepository,
                         VehicleRepository vehicleRepository,
                         UserRepository userRepository) {
        this.rentalRepository = rentalRepository;
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    /**
     * CREATE: Initiates a new rental transaction with vehicle stock lock.
     */
    public RentalTransaction rentVehicle(String userId, String vehicleId, int rentalDurationHours) {
        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Validate vehicle availability
        AntigravityVehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + vehicleId));

        if (!vehicle.isRentable()) {
            throw new IllegalStateException("Vehicle " + vehicle.getModelName() + " is currently unavailable or out of stock.");
        }

        // Decrement vehicle inventory
        vehicleRepository.updateStock(vehicleId, -1);

        String rentalId = "RNT-" + System.currentTimeMillis() % 100000;
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime expectedReturn = start.plusHours(Math.max(1, rentalDurationHours));
        double effectiveRate = vehicle.getEffectiveHourlyRate();
        double deposit = Math.round((effectiveRate * 2.0) * 100.0) / 100.0;
        double initialTotal = Math.round((effectiveRate * rentalDurationHours) * 100.0) / 100.0;

        RentalTransaction tx = new RentalTransaction(
                rentalId,
                user.getUserId(),
                vehicle.getVehicleId(),
                start,
                expectedReturn,
                null,
                effectiveRate,
                deposit,
                0.0,
                initialTotal,
                RentalStatus.ACTIVE
        );

        rentalRepository.save(tx);
        return tx;
    }

    /**
     * READ: Returns all rentals.
     */
    public List<RentalTransaction> getAllRentals() {
        return rentalRepository.findAll();
    }

    /**
     * READ: Returns active rentals.
     */
    public List<RentalTransaction> getActiveRentals() {
        return rentalRepository.findActiveRentals();
    }

    /**
     * READ: Find by transaction ID.
     */
    public Optional<RentalTransaction> getRentalById(String rentalId) {
        return rentalRepository.findById(rentalId);
    }

    /**
     * UPDATE: Completes vehicle return, triggers polymorphic fine calculation,
     * restores fleet stock, and increments user loyalty points.
     */
    public RentalTransaction processReturn(String rentalId, int simulatedOverdueHours) {
        RentalTransaction tx = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new IllegalArgumentException("Rental record not found: " + rentalId));

        if (tx.getStatus() != RentalStatus.ACTIVE) {
            throw new IllegalStateException("Transaction is already closed with status: " + tx.getStatus());
        }

        // Look up user to determine fine calculation policy (Polymorphism)
        User user = userRepository.findById(tx.getUserId())
                .orElse(null);

        // POLYMORPHIC STRATEGY SELECTION
        FineCalculator fineCalculator = resolveFineCalculator(user);

        // Determine overdue hours
        long overdueHours = simulatedOverdueHours;
        if (overdueHours <= 0) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(tx.getExpectedReturnTime())) {
                overdueHours = Duration.between(tx.getExpectedReturnTime(), now).toHours();
            }
        }

        // Polymorphic method call
        double fine = fineCalculator.calculateFine(overdueHours, tx.getHourlyRate());
        tx.setLateFine(fine);
        tx.setActualReturnTime(LocalDateTime.now().plusHours(simulatedOverdueHours));
        tx.setTotalCharged(Math.round((tx.getTotalCharged() + fine) * 100.0) / 100.0);
        tx.setStatus(RentalStatus.RETURNED);

        // Save transaction update
        rentalRepository.update(tx);

        // Restore vehicle stock
        vehicleRepository.updateStock(tx.getVehicleId(), 1);

        // Award loyalty points to regular users
        if (user instanceof RegularUser regularUser) {
            regularUser.addLoyaltyPoints(15);
            userRepository.update(regularUser);
        }

        return tx;
    }

    /**
     * DELETE: Purge completed or cancelled logs.
     */
    public boolean deleteRental(String rentalId) {
        return rentalRepository.deleteById(rentalId);
    }

    /**
     * Polymorphic Strategy Resolver
     */
    public FineCalculator resolveFineCalculator(User user) {
        if (user instanceof RegularUser regularUser && regularUser.isVip()) {
            return new VIPFineCalculator();
        }
        return new RegularFineCalculator();
    }
}

