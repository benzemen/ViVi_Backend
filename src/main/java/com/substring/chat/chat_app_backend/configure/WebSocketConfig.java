package com.substring.chat.chat_app_backend.configure;

import com.substring.chat.chat_app_backend.security.WebSocketAuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket and STOMP messaging configuration.
 *
 * <h3>Architecture overview</h3>
 * <ul>
 *   <li><b>{@code /app}</b> — Application destination prefix. Messages sent by clients
 *       to destinations like {@code /app/sendMessage/roomId} are routed to
 *       {@code @MessageMapping} handler methods in controllers.</li>
 *   <li><b>{@code /topic}</b> — Broker destination prefix. The simple in-memory broker
 *       manages subscriptions to topics like {@code /topic/room/roomId} and broadcasts
 *       messages to all subscribers of that topic.</li>
 *   <li><b>{@code /user}</b> — User destination prefix. Enables sending messages to
 *       a specific user's session (e.g., error notifications, private messages).</li>
 *   <li><b>{@code /chat}</b> — STOMP/SockJS handshake endpoint. Clients establish the
 *       WebSocket connection here, with SockJS providing automatic fallback to
 *       HTTP long-polling for browsers that don't support WebSocket.</li>
 * </ul>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    public WebSocketConfig(WebSocketAuthInterceptor webSocketAuthInterceptor) {
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }

    /**
     * Configures the message broker with topic-based pub/sub and user-specific destinations.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/user");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Registers the STOMP/SockJS handshake endpoint.
     *
     * <p>Clients connect via: {@code new SockJS("https://your-server/chat")}</p>
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * Registers the JWT authentication interceptor on the inbound WebSocket channel.
     *
     * <p>This intercepts STOMP CONNECT frames to validate JWT tokens and set the
     * user's {@link java.security.Principal} on the WebSocket session.</p>
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }
}
