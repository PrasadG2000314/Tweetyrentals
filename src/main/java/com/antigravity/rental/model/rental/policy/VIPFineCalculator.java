package com.antigravity.rental.model.rental.policy;

/**
 * ============================================================================
 * COMPONENT 3: Rental & Return Transactions
 * CLASS: VIPFineCalculator
 * ============================================================================
 * OOP CONCEPT HIGHLIGHTS:
 * 1. Polymorphism (Interface Implementation):
 *    - Realizes `FineCalculator` with VIP privilege rules:
 *      2-hour grace period, $0 flat admin fee, and reduced 1.25x hourly surcharge.
 * ============================================================================
 */
public class VIPFineCalculator implements FineCalculator {

    private static final int GRACE_PERIOD_HOURS = 2;
    private static final double OVERDUE_MULTIPLIER = 1.25;

    @Override
    public double calculateFine(long overdueHours, double baseHourlyRate) {
        if (overdueHours <= GRACE_PERIOD_HOURS) {
            return 0.0; // Grace period privilege
        }
        long chargeableHours = overdueHours - GRACE_PERIOD_HOURS;
        // VIP user: No fixed admin fee + 1.25x discounted overdue rate
        double totalFine = chargeableHours * (baseHourlyRate * OVERDUE_MULTIPLIER);
        return Math.round(totalFine * 100.0) / 100.0;
    }

    @Override
    public String getPolicyDescription() {
        return "VIP Tier Privilege: 2-Hour free grace window, $0 flat fee, and discounted 125% hourly rate.";
    }
}

