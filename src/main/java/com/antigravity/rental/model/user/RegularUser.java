package com.antigravity.rental.model.user;

/**
 * ============================================================================
 * COMPONENT 1: User Management
 * CLASS: RegularUser (Subclass)
 * ============================================================================
 * OOP CONCEPT HIGHLIGHTS:
 * 1. Inheritance:
 *    - Extends `User` base class, reusing identity & credential logic.
 * 2. Polymorphism (Method Overriding):
 *    - Overrides `authenticate()` using single-factor credential verification
 *      (extra token is optional for regular users).
 * 3. Encapsulation:
 *    - Tracks VIP membership status, phone number, and loyalty points securely.
 * ============================================================================
 */
public class RegularUser extends User {

    // Subclass-specific encapsulated fields
    private boolean isVip;
    private int loyaltyPoints;
    private String phoneNumber;

    public RegularUser(String userId, String username, String email, String password,
                       boolean isVip, int loyaltyPoints, String phoneNumber) {
        super(userId, username, email, password, "REGULAR");
        this.isVip = isVip;
        setLoyaltyPoints(loyaltyPoints);
        setPhoneNumber(phoneNumber);
    }

    /**
     * POLYMORPHIC IMPLEMENTATION:
     * Regular users authenticate via password directly.
     */
    @Override
    public boolean authenticate(String inputPassword, String extraToken) {
        if (inputPassword == null) {
            return false;
        }
        return getPassword().equals(inputPassword);
    }

    /**
     * Serializes RegularUser into file-delimited format:
     * REGULAR|userId|username|email|password|isVip|loyaltyPoints|phoneNumber
     */
    @Override
    public String toFileString() {
        return String.join("|",
                "REGULAR",
                getUserId(),
                getUsername(),
                getEmail(),
                getPassword(),
                String.valueOf(isVip),
                String.valueOf(loyaltyPoints),
                getPhoneNumber()
        );
    }

    @Override
    public String getUserBadge() {
        return isVip ? "VIP Member (Points: " + loyaltyPoints + ")" : "Standard Renter";
    }

    // Accessors and Mutators
    public boolean isVip() {
        return isVip;
    }

    public void setVip(boolean vip) {
        isVip = vip;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = Math.max(0, loyaltyPoints); // Points cannot be negative
    }

    public void addLoyaltyPoints(int points) {
        if (points > 0) {
            this.loyaltyPoints += points;
            if (this.loyaltyPoints >= 100) {
                this.isVip = true; // Auto-upgrade to VIP after 100 points
            }
        }
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = (phoneNumber != null) ? phoneNumber.trim() : "N/A";
    }
}

