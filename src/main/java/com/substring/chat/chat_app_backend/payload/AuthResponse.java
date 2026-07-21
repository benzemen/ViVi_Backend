package com.substring.chat.chat_app_backend.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Response payload returned after successful authentication (login or registration).
 *
 * <p>Contains the JWT token that the client must include in subsequent requests
 * via the {@code Authorization: Bearer <token>} header.</p>
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String tokenType;
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;

    public static AuthResponse of(String token, String username, String email,
                                   String displayName, String avatarUrl) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(username)
                .email(email)
                .displayName(displayName)
                .avatarUrl(avatarUrl)
                .build();
    }
}
