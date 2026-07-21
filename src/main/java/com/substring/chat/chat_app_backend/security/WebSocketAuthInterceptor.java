package com.substring.chat.chat_app_backend.security;

import com.substring.chat.chat_app_backend.repositories.UserRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Intercepts STOMP CONNECT frames to authenticate WebSocket connections via JWT.
 *
 * <p>Clients must include the JWT token in the STOMP CONNECT headers:</p>
 * <pre>
 * stompClient.connect(
 *     { 'Authorization': 'Bearer eyJhbGciOi...' },
 *     onConnected,
 *     onError
 * );
 * </pre>
 *
 * <p>Once validated, the user's {@link java.security.Principal} is set on the
 * WebSocket session, making it available to {@code @MessageMapping} handlers.</p>
 */
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public WebSocketAuthInterceptor(JwtTokenProvider jwtTokenProvider,
                                     UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (jwtTokenProvider.validateToken(token)) {
                    String username = jwtTokenProvider.getUsernameFromToken(token);

                    userRepository.findByUsername(username).ifPresent(user -> {
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        user.getUsername(), null, Collections.emptyList());
                        accessor.setUser(auth);
                    });
                }
            }
        }
        return message;
    }
}
