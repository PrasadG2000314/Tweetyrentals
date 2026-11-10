package com.antigravity.rental.model.review;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * COMPONENT 4: Feedback and Review Management
 * CLASS: PublicReview (Subclass)
 * ============================================================================
 * OOP CONCEPT HIGHLIGHTS:
 * 1. Inheritance:
 *    - Extends `Review` base class for generic public submissions without verified rental logs.
 * 2. Polymorphism:
 *    - Overrides `calculateCredibilityWeight()` returning a lower baseline score (0.45).
 *    - Overrides `getBadge()` designating "Community Observer".
 * ============================================================================
 */
public class PublicReview extends Review {

    private String sourcePlatform;

    public PublicReview(String reviewId, String vehicleId, String userId,
                        int rating, String comment, LocalDateTime timestamp,
                        String sourcePlatform) {
        super(reviewId, vehicleId, userId, rating, comment, timestamp);
        setSourcePlatform(sourcePlatform);
    }

    @Override
    public double calculateCredibilityWeight() {
        return 0.45; // Moderate trust weight because rental transaction was not verified
    }

    @Override
    public String getBadge() {
        return "Public Community Review (" + sourcePlatform + ")";
    }

    @Override
    public String getReviewType() {
        return "PUBLIC";
    }

    /**
     * PUBLIC|reviewId|vehicleId|userId|rating|comment|timestamp|sourcePlatform
     */
    @Override
    public String toFileString() {
        // Replace pipe characters in comment to prevent file delimiter corruption
        String safeComment = getComment().replace("|", " - ");
        return String.join("|",
                "PUBLIC",
                getReviewId(),
                getVehicleId(),
                getUserId(),
                String.valueOf(getRating()),
                safeComment,
                getTimestamp().format(FORMATTER),
                getSourcePlatform()
        );
    }

    public String getSourcePlatform() {
        return sourcePlatform;
    }

    public void setSourcePlatform(String sourcePlatform) {
        this.sourcePlatform = (sourcePlatform != null && !sourcePlatform.trim().isEmpty())
                ? sourcePlatform.trim() : "Web Portal";
    }
}

