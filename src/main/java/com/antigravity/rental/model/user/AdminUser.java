package com.antigravity.rental.model.user;

/**
 * ============================================================================
 * COMPONENT 1: User Management
 * CLASS: AdminUser (Subclass)
 * ============================================================================
 * OOP CONCEPT HIGHLIGHTS:
 * 1. Inheritance:
 *    - Extends the abstract base class `User`, inheriting base fields (userId, username,
 *      email, password) and methods while adding admin-specific fields.
 * 2. Polymorphism (Method Overriding):
 *    - Overrides `authenticate()` to enforce Two-Factor Authentication (2FA),
 *      requiring both password AND an Admin Security Key.
 * 3. Encapsulation:
 *    - adminKey and department are maintained as private fields with controlled access.
 * ============================================================================
 */
public class AdminUser extends User {

    // Subclass-specific encapsulated fields
    private String adminKey;       // 2FA Security Key
    private String department;     // e.g., "Operations", "Fleet Safety"

    public AdminUser(String userId, String username, String email, String password,
                     String adminKey, String department) {
        super(userId, username, email, password, "ADMIN");
        setAdminKey(adminKey);
        setDepartment(department);
    }

    /**
     * POLYMORPHIC IMPLEMENTATION:
     * Admin authentication enforces dual verification (password + secret admin key token).
     */
    @Override
    public boolean authenticate(String inputPassword, String extraToken) {
        if (inputPassword == null || extraToken == null) {
            return false;
        }
        boolean passwordMatch = getPassword().equals(inputPassword);
        boolean tokenMatch = this.adminKey.equals(extraToken.trim());
        return passwordMatch && tokenMatch;
    }

    /**
     * Serializes AdminUser into file-delimited format:
     * ADMIN|userId|username|email|password|adminKey|department
     */
    @Override
    public String toFileString() {
        return String.join("|",
                "ADMIN",
                getUserId(),
                getUsername(),
                getEmail(),
                getPassword(),
                getAdminKey(),
                getDepartment()
        );
    }

    @Override
    public String getUserBadge() {
        return "System Administrator (" + department + ")";
    }

    // Accessors and Mutators
    public String getAdminKey() {
        return adminKey;
    }

    public void setAdminKey(String adminKey) {
        if (adminKey == null || adminKey.trim().isEmpty()) {
            throw new IllegalArgumentException("[Validation Error] Admin security key cannot be empty.");
        }
        this.adminKey = adminKey.trim();
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = (department != null && !department.trim().isEmpty()) ? department.trim() : "General Administration";
    }
}

