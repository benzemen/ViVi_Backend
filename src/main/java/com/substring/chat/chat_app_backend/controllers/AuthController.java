package com.substring.chat.chat_app_backend.controllers;

import com.substring.chat.chat_app_backend.payload.AuthRequest;
import com.substring.chat.chat_app_backend.payload.AuthResponse;
import com.substring.chat.chat_app_backend.payload.RegisterRequest;
import com.substring.chat.chat_app_backend.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user authentication.
 *
 * <h3>Endpoints</h3>
 * <ul>
 *   <li>{@code POST /api/v1/auth/register} — Register with email + password</li>
 *   <li>{@code POST /api/v1/auth/login} — Login with email + password, returns JWT</li>
 * </ul>
 *
 * <p>Google OAuth2 login is initiated by navigating to {@code /oauth2/authorization/google}
 * (handled by Spring Security, not this controller).</p>
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user account.
     *
     * @param request the registration details (username, email, password)
     * @return 201 Created with JWT token and user info
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param request the login credentials (email, password)
     * @return 200 OK with JWT token and user info
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
