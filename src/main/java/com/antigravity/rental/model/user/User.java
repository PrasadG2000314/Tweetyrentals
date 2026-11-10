package com.antigravity.rental.model.user;

/**
 * ============================================================================
 * COMPONENT 1: User Management
 * CLASS: User (Abstract Base Class)
 * ============================================================================
 * OOP CONCEPT HIGHLIGHTS:
 * 1. Abstraction:
 *    - Declares the abstract blueprint for all user categories in the platform.
 *    - Abstract method `authenticate()` forces derived classes to implement
 *      custom authentication logic without exposing internal rules.
 * 2. Encapsulation & Information Hiding:
 *    - All state variables are declared 'private' to prevent direct external manipulation.
 *    - Access is strictly controlled via public getter and validated setter methods.
 *    - Sensitive fields (e.g., password) are protected against illegal values.
 * ============================================================================
 */
public abstract class User {

    // Encapsulated private attributes
    private String userId;
    private String username;
    private String email;
    private String password;
    private String role; // "ADMIN" or "REGULAR"

    /**
     * Parameterized Constructor ensuring complete initialization of base state.
     */
    public User(String userId, String username, String email, String password, String role) {
        setUserId(userId);
        setUsername(username);
        setEmail(email);
        setPassword(password);
        this.role = role;
    }

    // =========================================================================
    // ABSTRACT METHODS (ABSTRACTION & POLYMORPHISM CONTRACT)
    // =========================================================================

    /**
     * Polymorphic Authentication method.
     * Subclasses (AdminUser, RegularUser) must implement their distinct
     * security verification logic.
     *
     * @param inputPassword Entered password
     * @param extraToken Optional auxiliary credential (e.g., 2FA key for Admin)
     * @return true if credentials pass subclass-specific validation
     */
    public abstract boolean authenticate(String inputPassword, String extraToken);

    /**
     * Abstract method for serializing the user instance into a delimited file format.
     * Enforces each concrete subclass to format its unique properties.
     */
    public abstract String toFileString();

    /**
     * Polymorphic method to get the descriptive user category badge.
     */
    public abstract String getUserBadge();

    // =========================================================================
    // ENCAPSULATED ACCESSORS AND MUTATORS (DATA INTEGRITY VALIDATION)
    // =========================================================================

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("[Validation Error] User ID cannot be null or empty.");
        }
        this.userId = userId.trim();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null || username.trim().length() < 3) {
            throw new IllegalArgumentException("[Validation Error] Username must be at least 3 characters long.");
        }
        this.username = username.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("[Validation Error] Invalid email address format.");
        }
        this.email = email.trim().toLowerCase();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("[Validation Error] Password must be at least 4 characters long.");
        }
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    protected void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}

