package com.antigravity.rental.repository;

import com.antigravity.rental.model.review.PublicReview;
import com.antigravity.rental.model.review.Review;
import com.antigravity.rental.model.review.VerifiedReview;
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
 * COMPONENT 4: Feedback and Review Management
 * CLASS: ReviewRepository (File Storage CRUD Layer)
 * ============================================================================
 * CRUD OPERATIONS IMPLEMENTED:
 * 1. Create: save(Review review)
 * 2. Read:   findAll(), findById(String id), findByVehicleId(String vid), findByUserId(String uid)
 * 3. Update: update(Review review)
 * 4. Delete: deleteById(String id)
 * ============================================================================
 */
@Repository
public class ReviewRepository {

    @Value("${app.data.reviews-file:data/reviews.txt}")
    private String filePath;

    public ReviewRepository() {}

    public ReviewRepository(String filePath) {
        this.filePath = filePath;
    }

    /**
     * CREATE: Adds a review to file storage.
     */
    public boolean save(Review review) {
        if (findById(review.getReviewId()).isPresent()) {
            throw new IllegalArgumentException("Review ID " + review.getReviewId() + " already exists.");
        }
        return FileStorageUtil.appendLine(filePath, review.toFileString());
    }

    /**
     * READ: Reconstructs all reviews polymorphically.
     */
    public List<Review> findAll() {
        List<String> lines = FileStorageUtil.readAllLines(filePath);
        List<Review> list = new ArrayList<>();

        for (String line : lines) {
            Review r = parseReview(line);
            if (r != null) {
                list.add(r);
            }
        }
        return list;
    }

    /**
     * READ: Search review by ID.
     */
    public Optional<Review> findById(String reviewId) {
        return findAll().stream()
                .filter(r -> r.getReviewId().equalsIgnoreCase(reviewId))
                .findFirst();
    }

    /**
     * READ: Find all reviews for a vehicle.
     */
    public List<Review> findByVehicleId(String vehicleId) {
        return findAll().stream()
                .filter(r -> r.getVehicleId().equalsIgnoreCase(vehicleId))
                .collect(Collectors.toList());
    }

    /**
     * READ: Find all reviews submitted by a user.
     */
    public List<Review> findByUserId(String userId) {
        return findAll().stream()
                .filter(r -> r.getUserId().equalsIgnoreCase(userId))
                .collect(Collectors.toList());
    }

    /**
     * UPDATE: Edits review text or rating.
     */
    public boolean update(Review review) {
        return FileStorageUtil.updateLine(
                filePath,
                line -> {
                    String[] tokens = line.split("\\|");
                    return tokens.length > 1 && tokens[1].equalsIgnoreCase(review.getReviewId());
                },
                review.toFileString()
        );
    }

    /**
     * DELETE: Deletes inappropriate or reported review.
     */
    public boolean deleteById(String reviewId) {
        return FileStorageUtil.deleteLine(
                filePath,
                line -> {
                    String[] tokens = line.split("\\|");
                    return tokens.length > 1 && tokens[1].equalsIgnoreCase(reviewId);
                }
        );
    }

    /**
     * Polymorphic Factory Method
     */
    private Review parseReview(String line) {
        try {
            String[] tokens = line.split("\\|", -1);
            if (tokens.length < 7) return null;

            String type = tokens[0].trim().toUpperCase();
            String reviewId = tokens[1].trim();
            String vehicleId = tokens[2].trim();
            String userId = tokens[3].trim();
            int rating = Integer.parseInt(tokens[4].trim());
            String comment = tokens[5].trim();
            LocalDateTime timestamp = LocalDateTime.parse(tokens[6].trim(), Review.FORMATTER);

            if ("VERIFIED".equals(type)) {
                String txId = tokens.length > 7 ? tokens[7].trim() : "TX-UNKNOWN";
                int comfort = tokens.length > 8 ? Integer.parseInt(tokens[8].trim()) : 5;
                return new VerifiedReview(reviewId, vehicleId, userId, rating, comment, timestamp, txId, comfort);
            } else {
                String platform = tokens.length > 7 ? tokens[7].trim() : "Web Portal";
                return new PublicReview(reviewId, vehicleId, userId, rating, comment, timestamp, platform);
            }
        } catch (Exception e) {
            System.err.println("[ReviewRepository ERROR] Malformed line: " + line + " -> " + e.getMessage());
            return null;
        }
    }
}

