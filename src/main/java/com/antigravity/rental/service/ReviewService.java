package com.antigravity.rental.service;

import com.antigravity.rental.model.rental.RentalTransaction;
import com.antigravity.rental.model.review.PublicReview;
import com.antigravity.rental.model.review.Review;
import com.antigravity.rental.model.review.VerifiedReview;
import com.antigravity.rental.repository.RentalRepository;
import com.antigravity.rental.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * COMPONENT 4: Feedback and Review Management
 * CLASS: ReviewService (Customer Experience Layer)
 * ============================================================================
 * OOP CONCEPT HIGHLIGHTS:
 * 1. Polymorphic Aggregation:
 *    - Computes weighted vehicle review sentiment by calling `calculateCredibilityWeight()`
 *      on each polymorphic `Review` object.
 * ============================================================================
 */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RentalRepository rentalRepository;

    public ReviewService(ReviewRepository reviewRepository, RentalRepository rentalRepository) {
        this.reviewRepository = reviewRepository;
        this.rentalRepository = rentalRepository;
    }

    /**
     * CREATE: Submit a public community review.
     */
    public PublicReview submitPublicReview(String vehicleId, String userId, int rating,
                                          String comment, String sourcePlatform) {
        String reviewId = "REV-" + System.currentTimeMillis() % 100000;
        PublicReview review = new PublicReview(reviewId, vehicleId, userId, rating, comment,
                LocalDateTime.now(), sourcePlatform);
        reviewRepository.save(review);
        return review;
    }

    /**
     * CREATE: Submit a verified renter review linked to transaction proof.
     */
    public VerifiedReview submitVerifiedReview(String vehicleId, String userId, int rating,
                                              String comment, String rentalTxId, int comfortScore) {
        // Optional verification check against rental history
        Optional<RentalTransaction> txOpt = rentalRepository.findById(rentalTxId);
        if (txOpt.isPresent()) {
            RentalTransaction tx = txOpt.get();
            if (!tx.getVehicleId().equalsIgnoreCase(vehicleId)) {
                throw new IllegalArgumentException("Rental transaction " + rentalTxId + " does not match vehicle " + vehicleId);
            }
        }

        String reviewId = "VREV-" + System.currentTimeMillis() % 100000;
        VerifiedReview review = new VerifiedReview(reviewId, vehicleId, userId, rating, comment,
                LocalDateTime.now(), rentalTxId, comfortScore);
        reviewRepository.save(review);
        return review;
    }

    /**
     * READ: Returns all reviews.
     */
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    /**
     * READ: Returns reviews filtered by vehicle.
     */
    public List<Review> getReviewsByVehicle(String vehicleId) {
        return reviewRepository.findByVehicleId(vehicleId);
    }

    /**
     * READ: Get single review by ID.
     */
    public Optional<Review> getReviewById(String reviewId) {
        return reviewRepository.findById(reviewId);
    }

    /**
     * POLYMORPHIC SENTIMENT SCORING:
     * Calculates weighted average rating using dynamic polymorphism.
     */
    public double calculateWeightedRating(String vehicleId) {
        List<Review> reviews = getReviewsByVehicle(vehicleId);
        if (reviews.isEmpty()) {
            return 5.0; // Default baseline score
        }

        double weightedSum = 0.0;
        double totalWeights = 0.0;

        for (Review r : reviews) {
            // Polymorphic dispatch to calculateCredibilityWeight()
            double weight = r.calculateCredibilityWeight();
            weightedSum += (r.getRating() * weight);
            totalWeights += weight;
        }

        return (totalWeights > 0)
                ? Math.round((weightedSum / totalWeights) * 10.0) / 10.0
                : 5.0;
    }

    /**
     * UPDATE: Edits review commentary and star rating.
     */
    public boolean updateReview(String reviewId, int newRating, String newComment) {
        Optional<Review> opt = reviewRepository.findById(reviewId);
        if (opt.isPresent()) {
            Review r = opt.get();
            r.setRating(newRating);
            r.setComment(newComment);
            r.setTimestamp(LocalDateTime.now());
            return reviewRepository.update(r);
        }
        return false;
    }

    /**
     * DELETE: Moderates / removes inappropriate review.
     */
    public boolean deleteReview(String reviewId) {
        return reviewRepository.deleteById(reviewId);
    }
}

