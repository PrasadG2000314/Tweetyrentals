package com.antigravity.rental.test;

import com.antigravity.rental.model.user.AdminUser;
import com.antigravity.rental.model.user.RegularUser;
import com.antigravity.rental.model.user.User;
import com.antigravity.rental.repository.UserRepository;
import com.antigravity.rental.service.UserService;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ============================================================================
 * UNIT TEST SUITE: Component 1 - User Management
 * ============================================================================
 * Tests:
 * 1. Encapsulation & Mutator Validation
 * 2. Inheritance Hierarchy
 * 3. Polymorphic Authentication (Single-Factor vs 2FA Key)
 * 4. File CRUD Operations in UserRepository
 * ============================================================================
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserManagementTest {

    private static Path tempUserFile;
    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    public void setup() throws IOException {
        tempUserFile = Files.createTempFile("users_test", ".txt");
        userRepository = new UserRepository(tempUserFile.toString());
        userService = new UserService(userRepository);
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(tempUserFile);
    }

    @Test
    @Order(1)
    @DisplayName("OOP Encapsulation: Validates username and email constraints")
    public void testEncapsulationAndValidation() {
        // Test short username exception
        assertThrows(IllegalArgumentException.class, () -> {
            new RegularUser("U1", "ab", "valid@email.com", "pass123", false, 0, "12345");
        });

        // Test invalid email format exception
        assertThrows(IllegalArgumentException.class, () -> {
            new RegularUser("U2", "validUser", "invalidEmailNoAtSymbol", "pass123", false, 0, "12345");
        });

        // Test valid creation
        RegularUser user = new RegularUser("U3", "validUser", "test@domain.com", "pass123", false, 0, "12345");
        assertEquals("validUser", user.getUsername());
        assertEquals("test@domain.com", user.getEmail());
    }

    @Test
    @Order(2)
    @DisplayName("OOP Polymorphism: RegularUser vs AdminUser Authentication")
    public void testPolymorphicAuthentication() {
        User regular = new RegularUser("U10", "john_doe", "john@test.com", "mypass", false, 0, "555-0100");
        User admin = new AdminUser("U20", "admin_root", "root@test.com", "superpass", "SEC-999-KEY", "DevOps");

        // Regular user should authenticate with just password (token is ignored)
        assertTrue(regular.authenticate("mypass", null));
        assertFalse(regular.authenticate("wrongpass", null));

        // Admin user MUST provide both correct password AND matching 2FA secret key
        assertTrue(admin.authenticate("superpass", "SEC-999-KEY"));
        assertFalse(admin.authenticate("superpass", "WRONG-KEY"));
        assertFalse(admin.authenticate("wrongpass", "SEC-999-KEY"));
    }

    @Test
    @Order(3)
    @DisplayName("CRUD Operations: File-based Create, Read, Update, Delete for Users")
    public void testUserFileCrudOperations() {
        // CREATE
        User admin = new AdminUser("ADM-1", "chief_admin", "chief@antigrav.com", "admPass", "KEY-443", "Security");
        boolean created = userRepository.save(admin);
        assertTrue(created, "User should be created in file");

        // READ
        Optional<User> fetched = userRepository.findById("ADM-1");
        assertTrue(fetched.isPresent(), "User should be found in file");
        assertEquals("chief_admin", fetched.get().getUsername());
        assertEquals("ADMIN", fetched.get().getRole());

        // UPDATE
        admin.setEmail("chief_updated@antigrav.com");
        boolean updated = userRepository.update(admin);
        assertTrue(updated, "User details should update in file");
        assertEquals("chief_updated@antigrav.com", userRepository.findById("ADM-1").get().getEmail());

        // DELETE
        boolean deleted = userRepository.deleteById("ADM-1");
        assertTrue(deleted, "User should be deleted from file");
        assertTrue(userRepository.findById("ADM-1").isEmpty(), "User should no longer exist");
    }
}
