package com.antigravity.rental.model.rental.policy;

/**
 * ============================================================================
 * COMPONENT 3: Rental & Return Transactions
 * INTERFACE: FineCalculator (Abstraction & Polymorphism)
 * ============================================================================
 * OOP CONCEPT HIGHLIGHTS:
 * 1. Abstraction:
 *    - Declares the abstract specification for overdue penalty determination.
 *    - The caller does not need to know the specific penalty rate or grace rules.
 * 2. Polymorphism (Strategy Pattern):
 *    - Different implementations (RegularFineCalculator vs VIPFineCalculator)
 *      provide dynamically interchangeable fine calculation algorithms at runtime.
 * ============================================================================
 */
public interface FineCalculator {

    /**
     * Calculates the overdue fee.
     *
     * @param overdueHours Number of hours past expected return schedule
     * @param baseHourlyRate Vehicle's effective hourly rate
     * @return Calculated penalty in credits / dollars
     */
    double calculateFine(long overdueHours, double baseHourlyRate);

    /**
     * Returns descriptive explanation of policy terms.
     */
    String getPolicyDescription();
}

