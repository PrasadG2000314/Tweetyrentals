package com.antigravity.rental.controller;

import com.antigravity.rental.model.user.AdminUser;
import com.antigravity.rental.model.user.RegularUser;
import com.antigravity.rental.model.user.User;
import com.antigravity.rental.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ============================================================================
 * COMPONENT 1: User Management
 * CLASS: UserController (REST API Controller)
 * ============================================================================
 * Exposes endpoints for CRUD operations and polymorphic authentication.
 * ============================================================================
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * CREATE: Register user endpoint.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> payload) {
        try {
            String role = payload.getOrDefault("role", "REGULAR").toUpperCase();
            String userId = payload.getOrDefault("userId", "USR-" + System.currentTimeMillis() % 10000);
            String username = payload.get("username");
            String email = payload.get("email");
            String password = payload.get("password");

            User createdUser;
            if ("ADMIN".equals(role)) {
                String adminKey = payload.getOrDefault("adminKey", "ANTIGRAV-SECURE-KEY");
                String department = payload.getOrDefault("department", "Fleet Operations");
                createdUser = userService.registerAdminUser(userId, username, email, password, adminKey, department);
            } else {
                String phone = payload.getOrDefault("phoneNumber", "555-0199");
                createdUser = userService.registerRegularUser(userId, username, email, password, phone);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "status", "SUCCESS",
                    "message", "User registered successfully.",
                    "user", createdUser
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }

    /**
     * READ: Get all users.
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * READ: Get user by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "ERROR", "message", "User not found with ID: " + id)));
    }

    /**
     * UPDATE: Update user profile.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody Map<String, String> payload) {
        Optional<User> existingOpt = userService.getUserById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "ERROR", "message", "User not found"));
        }

        User user = existingOpt.get();
        if (payload.containsKey("email")) user.setEmail(payload.get("email"));
        if (payload.containsKey("username")) user.setUsername(payload.get("username"));
        if (payload.containsKey("password")) user.setPassword(payload.get("password"));

        if (user instanceof RegularUser regUser) {
            if (payload.containsKey("phoneNumber")) regUser.setPhoneNumber(payload.get("phoneNumber"));
            if (payload.containsKey("isVip")) regUser.setVip(Boolean.parseBoolean(payload.get("isVip")));
            if (payload.containsKey("loyaltyPoints")) regUser.setLoyaltyPoints(Integer.parseInt(payload.get("loyaltyPoints")));
        } else if (user instanceof AdminUser adminUser) {
            if (payload.containsKey("department")) adminUser.setDepartment(payload.get("department"));
            if (payload.containsKey("adminKey")) adminUser.setAdminKey(payload.get("adminKey"));
        }

        boolean success = userService.updateUser(user);
        return success
                ? ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "User updated successfully.", "user", user))
                : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", "ERROR", "message", "Failed to update user."));
    }

    /**
     * DELETE: Delete user by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "User deleted successfully."));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("status", "ERROR", "message", "User ID not found."));
        }
    }

    /**
     * POLYMORPHIC AUTHENTICATION ENDPOINT
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        String securityToken = credentials.get("securityToken"); // Required only for ADMIN

        Optional<User> authenticatedUser = userService.authenticate(username, password, securityToken);
        if (authenticatedUser.isPresent()) {
            User user = authenticatedUser.get();
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Login successful.",
                    "role", user.getRole(),
                    "userId", user.getUserId(),
                    "username", user.getUsername(),
                    "badge", user.getUserBadge()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "status", "ERROR",
                    "message", "Invalid credentials or missing Admin 2FA Security Key."
            ));
        }
    }
}

