package com.substring.chat.chat_app_backend.security;

import com.substring.chat.chat_app_backend.entities.User;
import com.substring.chat.chat_app_backend.repositories.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Custom OAuth2 user service that handles Google OAuth2 login.
 *
 * <p>When a user authenticates via Google, this service:</p>
 * <ol>
 *   <li>Loads the user info from Google's OAuth2 endpoint</li>
 *   <li>Checks if a user with this Google ID already exists in the database</li>
 *   <li>If not, creates a new user record from the Google profile</li>
 *   <li>If yes, updates the existing user's profile (name, avatar) from Google</li>
 * </ol>
 */
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String googleId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        // Find existing user by Google ID, or by email (to link accounts)
        User user = userRepository.findByGoogleId(googleId)
                .or(() -> userRepository.findByEmail(email))
                .orElse(null);

        if (user == null) {
            // First-time Google login: create new user
            user = new User();
            user.setEmail(email);
            user.setUsername(email.split("@")[0]); // derive username from email
            user.setDisplayName(name);
            user.setGoogleId(googleId);
            user.setAvatarUrl(picture);
            user.setAuthProvider(User.AuthProvider.GOOGLE);
            user.setCreatedAt(LocalDateTime.now());

            // Handle username collision
            if (userRepository.existsByUsername(user.getUsername())) {
                user.setUsername(user.getUsername() + "_" + System.currentTimeMillis() % 10000);
            }
        } else {
            // Existing user: update profile from Google
            user.setGoogleId(googleId);
            user.setDisplayName(name);
            user.setAvatarUrl(picture);
        }

        userRepository.save(user);
        return oAuth2User;
    }
}
