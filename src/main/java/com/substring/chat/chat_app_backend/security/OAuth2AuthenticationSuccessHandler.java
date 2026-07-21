package com.substring.chat.chat_app_backend.security;

import com.substring.chat.chat_app_backend.entities.User;
import com.substring.chat.chat_app_backend.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Handles successful Google OAuth2 authentication.
 *
 * <p>After Google authentication succeeds, this handler:</p>
 * <ol>
 *   <li>Looks up the user by email from the OAuth2 profile</li>
 *   <li>Generates a JWT token for the user</li>
 *   <li>Redirects to the frontend with the token as a query parameter</li>
 * </ol>
 *
 * <p>The frontend should extract the token from the URL and store it
 * for subsequent API calls.</p>
 */
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    public OAuth2AuthenticationSuccessHandler(JwtTokenProvider jwtTokenProvider,
                                               UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Authentication authentication)
            throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found after OAuth2 login"));

        String token = jwtTokenProvider.generateToken(user.getUsername());

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
