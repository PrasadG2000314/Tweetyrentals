package com.antigravity.rental.model.rental;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ============================================================================
 * COMPONENT 3: Rental & Return Transactions
 * CLASS: RentalTransaction
 * ============================================================================
 * OOP CONCEPT HIGHLIGHTS:
 * 1. Encapsulation:
 *    - All financial and scheduling fields are private.
 *    - Ensures transaction status can only transition through recognized states
 *      (e.g., ACTIVE -> RETURNED).
 * 2. Information Hiding:
 *    - Parsing and string serialization mechanisms hide raw pipe-delimited
 *      file structure from consumers.
 * ============================================================================
 */
public class RentalTransaction {

    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private String rentalId;
    private String userId;
    private String vehicleId;
    private LocalDateTime rentalStartTime;
    private LocalDateTime expectedReturnTime;
    private LocalDateTime actualReturnTime; // null if still ACTIVE
    private double hourlyRate;
    private double depositAmount;
    private double lateFine;
    private double totalCharged;
    private RentalStatus status;

    public RentalTransaction(String rentalId, String userId, String vehicleId,
                             LocalDateTime rentalStartTime, LocalDateTime expectedReturnTime,
                             LocalDateTime actualReturnTime, double hourlyRate,
                             double depositAmount, double lateFine, double totalCharged,
                             RentalStatus status) {
        setRentalId(rentalId);
        setUserId(userId);
        setVehicleId(vehicleId);
        this.rentalStartTime = rentalStartTime;
        this.expectedReturnTime = expectedReturnTime;
        this.actualReturnTime = actualReturnTime;
        this.hourlyRate = hourlyRate;
        this.depositAmount = depositAmount;
        this.lateFine = Math.max(0.0, lateFine);
        this.totalCharged = Math.max(0.0, totalCharged);
        this.status = (status != null) ? status : RentalStatus.ACTIVE;
    }

    /**
     * Serializes transaction to pipe-delimited format:
     * rentalId|userId|vehicleId|startTime|expectedTime|actualTime|rate|deposit|lateFine|totalCharged|status
     */
    public String toFileString() {
        String actualStr = (actualReturnTime != null) ? actualReturnTime.format(DATE_TIME_FORMAT) : "N/A";
        return String.join("|",
                rentalId,
                userId,
                vehicleId,
                rentalStartTime.format(DATE_TIME_FORMAT),
                expectedReturnTime.format(DATE_TIME_FORMAT),
                actualStr,
                String.valueOf(hourlyRate),
                String.valueOf(depositAmount),
                String.valueOf(lateFine),
                String.valueOf(totalCharged),
                status.name()
        );
    }

    // Accessors and Mutators
    public String getRentalId() {
        return rentalId;
    }

    public void setRentalId(String rentalId) {
        if (rentalId == null || rentalId.trim().isEmpty()) {
            throw new IllegalArgumentException("[Validation Error] Rental ID cannot be empty.");
        }
        this.rentalId = rentalId.trim();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("[Validation Error] User ID cannot be empty.");
        }
        this.userId = userId.trim();
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        if (vehicleId == null || vehicleId.trim().isEmpty()) {
            throw new IllegalArgumentException("[Validation Error] Vehicle ID cannot be empty.");
        }
        this.vehicleId = vehicleId.trim();
    }

    public LocalDateTime getRentalStartTime() {
        return rentalStartTime;
    }

    public void setRentalStartTime(LocalDateTime rentalStartTime) {
        this.rentalStartTime = rentalStartTime;
    }

    public LocalDateTime getExpectedReturnTime() {
        return expectedReturnTime;
    }

    public void setExpectedReturnTime(LocalDateTime expectedReturnTime) {
        this.expectedReturnTime = expectedReturnTime;
    }

    public LocalDateTime getActualReturnTime() {
        return actualReturnTime;
    }

    public void setActualReturnTime(LocalDateTime actualReturnTime) {
        this.actualReturnTime = actualReturnTime;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = Math.max(0.0, hourlyRate);
    }

    public double getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(double depositAmount) {
        this.depositAmount = Math.max(0.0, depositAmount);
    }

    public double getLateFine() {
        return lateFine;
    }

    public void setLateFine(double lateFine) {
        this.lateFine = Math.max(0.0, lateFine);
    }

    public double getTotalCharged() {
        return totalCharged;
    }

    public void setTotalCharged(double totalCharged) {
        this.totalCharged = Math.max(0.0, totalCharged);
    }

    public RentalStatus getStatus() {
        return status;
    }

    public void setStatus(RentalStatus status) {
        this.status = status;
    }
}

