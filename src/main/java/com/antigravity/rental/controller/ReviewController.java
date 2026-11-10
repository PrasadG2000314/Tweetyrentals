package com.antigravity.rental.controller;

import com.antigravity.rental.model.review.PublicReview;
import com.antigravity.rental.model.review.Review;
import com.antigravity.rental.model.review.VerifiedReview;
import com.antigravity.rental.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * ============================================================================
 * COMPONENT 4: Feedback and Review Management
 * CLASS: ReviewController (REST API Endpoints)
 * ============================================================================
 * Endpoints for submitting public/verified reviews, listing, editing, and deleting.
 * ============================================================================
 */
@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * CREATE: Submit review (automatically detects whether PUBLIC or VERIFIED).
     */
    @PostMapping
    public ResponseEntity<?> submitReview(@RequestBody Map<String, Object> payload) {
        try {
            String vehicleId = (String) payload.get("vehicleId");
            String userId = (String) payload.get("userId");
            int rating = Integer.parseInt(payload.get("rating").toString());
            String comment = (String) payload.get("comment");
            String reviewType = (String) payload.getOrDefault("reviewType", "PUBLIC");

            Review review;
            if ("VERIFIED".equalsIgnoreCase(reviewType)) {
                String txId = (String) payload.get("rentalTransactionId");
                int comfort = Integer.parseInt(payload.getOrDefault("flightComfortScore", "5").toString());
                review = reviewService.submitVerifiedReview(vehicleId, userId, rating, comment, txId, comfort);
            } else {
                String source = (String) payload.getOrDefault("sourcePlatform", "Web Portal");
                review = reviewService.submitPublicReview(vehicleId, userId, rating, comment, source);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "status", "SUCCESS",
                    "message", "Review posted successfully.",
                    "review", mapReview(review)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }

    /**
     * READ: Get reviews, optionally filtered by vehicleId.
     */
    @GetMapping
    public ResponseEntity<?> getReviews(@RequestParam(required = false) String vehicleId) {
        List<Review> reviews = (vehicleId != null && !vehicleId.trim().isEmpty())
                ? reviewService.getReviewsByVehicle(vehicleId)
                : reviewService.getAllReviews();

        List<Map<String, Object>> response = new ArrayList<>();
        for (Review r : reviews) {
            response.add(mapReview(r));
        }

        if (vehicleId != null && !vehicleId.trim().isEmpty()) {
            double weightedAvg = reviewService.calculateWeightedRating(vehicleId);
            return ResponseEntity.ok(Map.of(
                    "vehicleId", vehicleId,
                    "weightedAverageRating", weightedAvg,
                    "reviews", response
            ));
        }

        return ResponseEntity.ok(response);
    }

    /**
     * READ: Get single review by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReviewById(@PathVariable String id) {
        return reviewService.getReviewById(id)
                .<ResponseEntity<?>>map(r -> ResponseEntity.ok(mapReview(r)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "ERROR", "message", "Review not found: " + id)));
    }

    /**
     * UPDATE: Edit review comment and rating.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReview(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        try {
            int rating = Integer.parseInt(payload.get("rating").toString());
            String comment = (String) payload.get("comment");
            boolean updated = reviewService.updateReview(id, rating, comment);
            return updated
                    ? ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Review updated."))
                    : ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("status", "ERROR", "message", "Review not found."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }

    /**
     * DELETE: Delete inappropriate review.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable String id) {
        boolean deleted = reviewService.deleteReview(id);
        return deleted
                ? ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Review moderated and removed."))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("status", "ERROR", "message", "Review ID not found."));
    }

    private Map<String, Object> mapReview(Review r) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("reviewId", r.getReviewId());
        map.put("vehicleId", r.getVehicleId());
        map.put("userId", r.getUserId());
        map.put("rating", r.getRating());
        map.put("comment", r.getComment());
        map.put("timestamp", r.getTimestamp().format(Review.FORMATTER));
        map.put("reviewType", r.getReviewType());
        map.put("badge", r.getBadge());
        map.put("credibilityWeight", r.calculateCredibilityWeight());

        if (r instanceof VerifiedReview vr) {
            map.put("rentalTransactionId", vr.getRentalTransactionId());
            map.put("flightComfortScore", vr.getFlightComfortScore());
        } else if (r instanceof PublicReview pr) {
            map.put("sourcePlatform", pr.getSourcePlatform());
        }
        return map;
    }
}

