package com.antigravity.rental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * MAIN ENTRY POINT: Tweety Rentals - Antigravity Vehicle Platform
 * ============================================================================
 * University OOP Final Project - Spring Boot Application
 * Run directly in IntelliJ IDEA or via 'mvn spring-boot:run'.
 * ============================================================================
 */
@SpringBootApplication
public class AntigravityRentalApplication {

    public static void main(String[] args) {
        SpringApplication.run(AntigravityRentalApplication.class, args);
        System.out.println("==================================================================");
        System.out.println(" Tweety Rentals (Antigravity Platform) is active!");
        System.out.println(" Access Web Portal at: http://localhost:8080");
        System.out.println(" Admin Fleet Console: http://localhost:8080/admin.html");
        System.out.println(" Rentals & Returns:    http://localhost:8080/rentals.html");
        System.out.println(" Reviews & Ratings:    http://localhost:8080/reviews.html");
        System.out.println("==================================================================");
    }
}

