package com.antigravity.rental.repository;

import com.antigravity.rental.model.rental.RentalStatus;
import com.antigravity.rental.model.rental.RentalTransaction;
import com.antigravity.rental.util.FileStorageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * COMPONENT 3: Rental & Return Transactions
 * CLASS: RentalRepository (File Storage CRUD Layer)
 * ============================================================================
 * CRUD OPERATIONS IMPLEMENTED:
 * 1. Create: save(RentalTransaction tx)
 * 2. Read:   findAll(), findById(String id), findActiveRentals(), findByUserId(String uid)
 * 3. Update: update(RentalTransaction tx)
 * 4. Delete: deleteById(String id)
 * ============================================================================
 */
@Repository
public class RentalRepository {

    @Value("${app.data.rentals-file:data/rentals.txt}")
    private String filePath;

    public RentalRepository() {}

    public RentalRepository(String filePath) {
        this.filePath = filePath;
    }

    /**
     * CREATE: Records a new rental transaction into file.
     */
    public boolean save(RentalTransaction tx) {
        if (findById(tx.getRentalId()).isPresent()) {
            throw new IllegalArgumentException("Rental record ID " + tx.getRentalId() + " already exists.");
        }
        return FileStorageUtil.appendLine(filePath, tx.toFileString());
    }

    /**
     * READ: Reconstructs all rental transaction history.
     */
    public List<RentalTransaction> findAll() {
        List<String> lines = FileStorageUtil.readAllLines(filePath);
        List<RentalTransaction> list = new ArrayList<>();

        for (String line : lines) {
            RentalTransaction tx = parseTransaction(line);
            if (tx != null) {
                list.add(tx);
            }
        }
        return list;
    }

    /**
     * READ: Search transaction by ID.
     */
    public Optional<RentalTransaction> findById(String rentalId) {
        return findAll().stream()
                .filter(t -> t.getRentalId().equalsIgnoreCase(rentalId))
                .findFirst();
    }

    /**
     * READ: Find all currently active rentals.
     */
    public List<RentalTransaction> findActiveRentals() {
        return findAll().stream()
                .filter(t -> t.getStatus() == RentalStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    /**
     * READ: Find rentals belonging to a specific user.
     */
    public List<RentalTransaction> findByUserId(String userId) {
        return findAll().stream()
                .filter(t -> t.getUserId().equalsIgnoreCase(userId))
                .collect(Collectors.toList());
    }

    /**
     * UPDATE: Updates status, return time, fines, or totals for a rental.
     */
    public boolean update(RentalTransaction tx) {
        return FileStorageUtil.updateLine(
                filePath,
                line -> {
                    String[] tokens = line.split("\\|");
                    return tokens.length > 0 && tokens[0].equalsIgnoreCase(tx.getRentalId());
                },
                tx.toFileString()
        );
    }

    /**
     * DELETE: Purges completed or obsolete transaction log from file.
     */
    public boolean deleteById(String rentalId) {
        return FileStorageUtil.deleteLine(
                filePath,
                line -> {
                    String[] tokens = line.split("\\|");
                    return tokens.length > 0 && tokens[0].equalsIgnoreCase(rentalId);
                }
        );
    }

    /**
     * File Deserialization Helper
     */
    private RentalTransaction parseTransaction(String line) {
        try {
            String[] tokens = line.split("\\|", -1);
            if (tokens.length < 11) return null;

            String rentalId = tokens[0].trim();
            String userId = tokens[1].trim();
            String vehicleId = tokens[2].trim();
            LocalDateTime start = LocalDateTime.parse(tokens[3].trim(), RentalTransaction.DATE_TIME_FORMAT);
            LocalDateTime expected = LocalDateTime.parse(tokens[4].trim(), RentalTransaction.DATE_TIME_FORMAT);

            LocalDateTime actual = null;
            if (!"N/A".equalsIgnoreCase(tokens[5].trim()) && !tokens[5].trim().isEmpty()) {
                actual = LocalDateTime.parse(tokens[5].trim(), RentalTransaction.DATE_TIME_FORMAT);
            }

            double rate = Double.parseDouble(tokens[6].trim());
            double deposit = Double.parseDouble(tokens[7].trim());
            double fine = Double.parseDouble(tokens[8].trim());
            double total = Double.parseDouble(tokens[9].trim());
            RentalStatus status = RentalStatus.valueOf(tokens[10].trim().toUpperCase());

            return new RentalTransaction(rentalId, userId, vehicleId, start, expected, actual, rate, deposit, fine, total, status);
        } catch (Exception e) {
            System.err.println("[RentalRepository ERROR] Malformed line: " + line + " -> " + e.getMessage());
            return null;
        }
    }
}

