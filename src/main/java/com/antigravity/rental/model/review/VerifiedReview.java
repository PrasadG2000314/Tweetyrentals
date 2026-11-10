package com.antigravity.rental.model.review;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * COMPONENT 4: Feedback and Review Management
 * CLASS: VerifiedReview (Subclass)
 * ============================================================================
 * OOP CONCEPT HIGHLIGHTS:
 * 1. Inheritance:
 *    - Extends `Review` base class for renters with validated transaction proof.
 * 2. Polymorphism:
 *    - Overrides `calculateCredibilityWeight()` providing highest weight (1.0).
 *    - Overrides `getBadge()` returning gold "Verified Pilot" status.
 * 3. Encapsulation:
 *    - Validates rentalTransactionId link and comfort rating.
 * ============================================================================
 */
public class VerifiedReview extends Review {

    private String rentalTransactionId;
    private int flightComfortScore; // 1 to 5 rating on anti-gravity dampener smoothness

    public VerifiedReview(String reviewId, String vehicleId, String userId,
                          int rating, String comment, LocalDateTime timestamp,
                          String rentalTransactionId, int flightComfortScore) {
        super(reviewId, vehicleId, userId, rating, comment, timestamp);
        setRentalTransactionId(rentalTransactionId);
        setFlightComfortScore(flightComfortScore);
    }

    @Override
    public double calculateCredibilityWeight() {
        // High credibility: verified flight transaction + dampener comfort correlation
        return 1.0;
    }

    @Override
    public String getBadge() {
        return "Verified Antigravity Renter [Tx: " + rentalTransactionId + " | Comfort: " + flightComfortScore + "/5]";
    }

    @Override
    public String getReviewType() {
        return "VERIFIED";
    }

    /**
     * VERIFIED|reviewId|vehicleId|userId|rating|comment|timestamp|rentalTransactionId|flightComfortScore
     */
    @Override
    public String toFileString() {
        String safeComment = getComment().replace("|", " - ");
        return String.join("|",
                "VERIFIED",
                getReviewId(),
                getVehicleId(),
                getUserId(),
                String.valueOf(getRating()),
                safeComment,
                getTimestamp().format(FORMATTER),
                getRentalTransactionId(),
                String.valueOf(getFlightComfortScore())
        );
    }

    public String getRentalTransactionId() {
        return rentalTransactionId;
    }

    public void setRentalTransactionId(String rentalTransactionId) {
        if (rentalTransactionId == null || rentalTransactionId.trim().isEmpty()) {
            throw new IllegalArgumentException("[Validation Error] Verified reviews require a valid Rental Transaction ID.");
        }
        this.rentalTransactionId = rentalTransactionId.trim();
    }

    public int getFlightComfortScore() {
        return flightComfortScore;
    }

    public void setFlightComfortScore(int flightComfortScore) {
        this.flightComfortScore = Math.max(1, Math.min(5, flightComfortScore));
    }
}

