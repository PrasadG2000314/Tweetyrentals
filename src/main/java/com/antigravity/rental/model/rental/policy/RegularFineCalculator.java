package com.antigravity.rental.model.rental.policy;

/**
 * ============================================================================
 * COMPONENT 3: Rental & Return Transactions
 * CLASS: RegularFineCalculator
 * ============================================================================
 * OOP CONCEPT HIGHLIGHTS:
 * 1. Polymorphism (Interface Implementation):
 *    - Realizes `FineCalculator` with standard strict overdue rules:
 *      $25 fixed administrative charge + 2.0x hourly surcharge per overdue hour.
 * ============================================================================
 */
public class RegularFineCalculator implements FineCalculator {

    private static final double FIXED_ADMIN_FEE = 25.0;
    private static final double OVERDUE_MULTIPLIER = 2.0;

    @Override
    public double calculateFine(long overdueHours, double baseHourlyRate) {
        if (overdueHours <= 0) {
            return 0.0;
        }
        // Standard user: $25 breach fee + (2.0 * hourly rate * overdue hours)
        double totalFine = FIXED_ADMIN_FEE + (overdueHours * (baseHourlyRate * OVERDUE_MULTIPLIER));
        return Math.round(totalFine * 100.0) / 100.0;
    }

    @Override
    public String getPolicyDescription() {
        return "Standard Tier Penalty: $25.00 flat admin fee + 200% hourly rate per delayed hour.";
    }
}

