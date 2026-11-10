package com.antigravity.rental.model.review;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ============================================================================
 * COMPONENT 4: Feedback and Review Management
 * CLASS: Review (Abstract Base Class)
 * ============================================================================
 * OOP CONCEPT HIGHLIGHTS:
 * 1. Abstraction:
 *    - Encapsulates common feedback properties while delegating trust
 *      and credibility calculations to specialized subclasses.
 * 2. Encapsulation:
 *    - Rating is strictly validated between 1 and 5 stars.
 *    - Review text and reviewer identities are protected.
 * 3. Polymorphism:
 *    - Abstract methods `calculateCredibilityWeight()`, `getBadgeHtml()`, and `toFileString()`.
 * ============================================================================
 */
public abstract class Review {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private String reviewId;
    private String vehicleId;
    private String userId;
    private int rating; // 1 to 5 stars
    private String comment;
    private LocalDateTime timestamp;

    public Review(String reviewId, String vehicleId, String userId,
                  int rating, String comment, LocalDateTime timestamp) {
        setReviewId(reviewId);
        setVehicleId(vehicleId);
        setUserId(userId);
        setRating(rating);
        setComment(comment);
        this.timestamp = (timestamp != null) ? timestamp : LocalDateTime.now();
    }

    // =========================================================================
    // ABSTRACT METHODS (POLYMORPHIC CONTRACT)
    // =========================================================================

    /**
     * Polymorphic method to calculate credibility weight (0.0 to 1.0)
     * based on whether rental flight verification took place.
     */
    public abstract double calculateCredibilityWeight();

    /**
     * Polymorphic visual badge identifying review authenticity.
     */
    public abstract String getBadge();

    /**
     * Returns the review discriminator type string ("PUBLIC" or "VERIFIED").
     */
    public abstract String getReviewType();

    /**
     * File serialization format.
     */
    public abstract String toFileString();

    // =========================================================================
    // ENCAPSULATED ACCESSORS AND MUTATORS
    // =========================================================================

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        if (reviewId == null || reviewId.trim().isEmpty()) {
            throw new IllegalArgumentException("[Validation Error] Review ID cannot be empty.");
        }
        this.reviewId = reviewId.trim();
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("[Validation Error] User ID cannot be empty.");
        }
        this.userId = userId.trim();
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("[Validation Error] Rating must be an integer between 1 and 5.");
        }
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("[Validation Error] Review comment cannot be empty.");
        }
        this.comment = comment.trim();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

