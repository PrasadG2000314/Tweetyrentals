package com.antigravity.rental.repository;

import com.antigravity.rental.model.user.AdminUser;
import com.antigravity.rental.model.user.RegularUser;
import com.antigravity.rental.model.user.User;
import com.antigravity.rental.util.FileStorageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * COMPONENT 1: User Management
 * CLASS: UserRepository (Data Access Layer via File Handling)
 * ============================================================================
 * CRUD OPERATIONS IMPLEMENTED:
 * 1. Create: save(User user)
 * 2. Read:   findAll(), findById(String id), findByUsername(String username)
 * 3. Update: update(User user)
 * 4. Delete: deleteById(String id)
 *
 * OOP HIGHLIGHTS:
 * - Abstraction: Exposes CRUD interface returning abstract `User` references.
 * - Polymorphic Deserialization: Parses raw string tokens and dynamically
 *   instantiates the appropriate concrete subclass (AdminUser or RegularUser).
 * ============================================================================
 */
@Repository
public class UserRepository {

    @Value("${app.data.users-file:data/users.txt}")
    private String filePath;

    public UserRepository() {
        // Default constructor
    }

    public UserRepository(String filePath) {
        this.filePath = filePath;
    }

    /**
     * CREATE: Appends a new User to the file.
     */
    public boolean save(User user) {
        if (findById(user.getUserId()).isPresent()) {
            throw new IllegalArgumentException("User with ID " + user.getUserId() + " already exists.");
        }
        return FileStorageUtil.appendLine(filePath, user.toFileString());
    }

    /**
     * READ: Reads and reconstructs all Users polymorphically from file.
     */
    public List<User> findAll() {
        List<String> lines = FileStorageUtil.readAllLines(filePath);
        List<User> users = new ArrayList<>();

        for (String line : lines) {
            User user = parseUser(line);
            if (user != null) {
                users.add(user);
            }
        }
        return users;
    }

    /**
     * READ: Search user by ID.
     */
    public Optional<User> findById(String userId) {
        return findAll().stream()
                .filter(u -> u.getUserId().equalsIgnoreCase(userId))
                .findFirst();
    }

    /**
     * READ: Search user by Username.
     */
    public Optional<User> findByUsername(String username) {
        return findAll().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    /**
     * UPDATE: Modifies an existing user's details.
     */
    public boolean update(User updatedUser) {
        return FileStorageUtil.updateLine(
                filePath,
                line -> {
                    String[] tokens = line.split("\\|");
                    return tokens.length > 1 && tokens[1].equalsIgnoreCase(updatedUser.getUserId());
                },
                updatedUser.toFileString()
        );
    }

    /**
     * DELETE: Deletes a user record by ID.
     */
    public boolean deleteById(String userId) {
        return FileStorageUtil.deleteLine(
                filePath,
                line -> {
                    String[] tokens = line.split("\\|");
                    return tokens.length > 1 && tokens[1].equalsIgnoreCase(userId);
                }
        );
    }

    /**
     * Polymorphic Factory Deserializer:
     * Parses pipe-delimited text line into concrete AdminUser or RegularUser.
     */
    private User parseUser(String line) {
        try {
            String[] tokens = line.split("\\|", -1);
            if (tokens.length < 5) {
                return null;
            }

            String type = tokens[0].trim().toUpperCase();
            String userId = tokens[1].trim();
            String username = tokens[2].trim();
            String email = tokens[3].trim();
            String password = tokens[4].trim();

            if ("ADMIN".equals(type)) {
                String adminKey = tokens.length > 5 ? tokens[5].trim() : "ADMIN-SECRET";
                String department = tokens.length > 6 ? tokens[6].trim() : "Fleet Operations";
                return new AdminUser(userId, username, email, password, adminKey, department);
            } else {
                boolean isVip = tokens.length > 5 && Boolean.parseBoolean(tokens[5].trim());
                int points = tokens.length > 6 ? Integer.parseInt(tokens[6].trim()) : 0;
                String phone = tokens.length > 7 ? tokens[7].trim() : "N/A";
                return new RegularUser(userId, username, email, password, isVip, points, phone);
            }
        } catch (Exception e) {
            System.err.println("[UserRepository ERROR] Skipping malformed line: " + line + " -> " + e.getMessage());
            return null;
        }
    }
}

