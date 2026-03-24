package com.travel.service;

import com.travel.entity.User;
import com.travel.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z ]{2,50}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,64}$");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    public User saveUser(User user) {

        String username = user.getUsername() != null ? user.getUsername().trim() : "";
        String email = user.getEmail() != null ? user.getEmail().trim().toLowerCase() : "";
        String password = user.getPassword() != null ? user.getPassword() : "";

        if (!NAME_PATTERN.matcher(username).matches()) {
            throw new RuntimeException("Name must contain only letters and spaces (2 to 50 characters).");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new RuntimeException("Please enter a valid email address.");
        }

        if (!STRONG_PASSWORD_PATTERN.matcher(password).matches()) {
            throw new RuntimeException("Password must be at least 8 characters and include uppercase, lowercase, and a number.");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email is already registered. Please login.");
        }

        user.setUsername(username);
        user.setEmail(email);

        user.setPassword(encoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}