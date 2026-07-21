package com.substring.chat.chat_app_backend.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Represents a registered user in the system.
 *
 * <p>Supports two authentication methods:</p>
 * <ul>
 *   <li><b>Local registration</b> — email + password (hashed with BCrypt)</li>
 *   <li><b>Google OAuth2</b> — linked via {@code googleId}</li>
 * </ul>
 *
 * <p>A user who registers locally can later link their Google account,
 * and a Google-authenticated user has their profile populated from Google's
 * user info endpoint.</p>
 */
@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    private String id;

    /** Unique username used for login and display in chat. */
    @Indexed(unique = true)
    private String username;

    /** Email address. Unique across all users. */
    @Indexed(unique = true)
    private String email;

    /** BCrypt-hashed password. Null for Google-only authenticated users. */
    private String passwordHash;

    /** Display name shown in the chat UI. */
    private String displayName;

    /** Google OAuth2 subject ID. Null for local-only users. */
    @Indexed(unique = true, sparse = true)
    private String googleId;

    /** URL to the user's profile picture (from Google or uploaded). */
    private String avatarUrl;

    /** The authentication provider used to create this account. */
    private AuthProvider authProvider;

    /** When this user account was created. */
    private LocalDateTime createdAt;

    public enum AuthProvider {
        LOCAL, GOOGLE
    }

    /**
     * Constructs a locally-registered user.
     */
    public User(String username, String email, String passwordHash, String displayName) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.authProvider = AuthProvider.LOCAL;
        this.createdAt = LocalDateTime.now();
    }
}
