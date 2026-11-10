package com.antigravity.rental.service;

import com.antigravity.rental.model.user.AdminUser;
import com.antigravity.rental.model.user.RegularUser;
import com.antigravity.rental.model.user.User;
import com.antigravity.rental.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * COMPONENT 1: User Management
 * CLASS: UserService (Business Logic Layer)
 * ============================================================================
 * Implements CRUD workflows and delegates persistence to UserRepository.
 * Coordinates polymorphic authentication.
 * ============================================================================
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * CREATE: Registers a new regular user.
     */
    public User registerRegularUser(String userId, String username, String email, String password, String phone) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }
        RegularUser user = new RegularUser(userId, username, email, password, false, 0, phone);
        userRepository.save(user);
        return user;
    }

    /**
     * CREATE: Registers a new administrator user.
     */
    public User registerAdminUser(String userId, String username, String email, String password,
                                  String adminKey, String department) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }
        AdminUser admin = new AdminUser(userId, username, email, password, adminKey, department);
        userRepository.save(admin);
        return admin;
    }

    /**
     * READ: Get all registered users.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * READ: Find user by identifier.
     */
    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }

    /**
     * READ: Find user by username.
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * UPDATE: Update user profile information.
     */
    public boolean updateUser(User user) {
        return userRepository.update(user);
    }

    /**
     * DELETE: Remove user account by ID.
     */
    public boolean deleteUser(String userId) {
        return userRepository.deleteById(userId);
    }

    /**
     * POLYMORPHIC AUTHENTICATION:
     * Dispatches authentication to the runtime type of User.
     * If AdminUser -> checks 2FA secret key.
     * If RegularUser -> checks standard password.
     */
    public Optional<User> authenticate(String username, String password, String securityToken) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Polymorphic dispatch
            if (user.authenticate(password, securityToken)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
}

