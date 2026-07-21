package com.substring.chat.chat_app_backend.services;

import com.substring.chat.chat_app_backend.entities.User;
import com.substring.chat.chat_app_backend.payload.AuthRequest;
import com.substring.chat.chat_app_backend.payload.AuthResponse;
import com.substring.chat.chat_app_backend.payload.RegisterRequest;
import com.substring.chat.chat_app_backend.repositories.UserRepository;
import com.substring.chat.chat_app_backend.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service layer for authentication operations (registration and login).
 *
 * <p>Handles password hashing with BCrypt, JWT token generation,
 * and input validation for the auth endpoints.</p>
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Registers a new user with email and password.
     *
     * @param request the registration details
     * @return authentication response with JWT token
     * @throws IllegalArgumentException if username or email already exists
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' is already registered");
        }

        String displayName = request.getDisplayName();
        if (displayName == null || displayName.isBlank()) {
            displayName = request.getUsername();
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                displayName
        );

        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getUsername());
        return AuthResponse.of(token, user.getUsername(), user.getEmail(),
                user.getDisplayName(), user.getAvatarUrl());
    }

    /**
     * Authenticates a user with email and password.
     *
     * @param request the login credentials
     * @return authentication response with JWT token
     * @throws IllegalArgumentException if credentials are invalid
     */
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (user.getPasswordHash() == null) {
            throw new IllegalArgumentException(
                    "This account was created via Google. Please use Google sign-in.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(user.getUsername());
        return AuthResponse.of(token, user.getUsername(), user.getEmail(),
                user.getDisplayName(), user.getAvatarUrl());
    }
}
