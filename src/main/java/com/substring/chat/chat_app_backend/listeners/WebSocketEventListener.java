package com.substring.chat.chat_app_backend.listeners;

import com.substring.chat.chat_app_backend.services.PresenceService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
public class WebSocketEventListener {

    private final PresenceService presenceService;

    public WebSocketEventListener(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();
        
        System.out.println("[WebSocketEventListener] SessionConnectEvent triggered. Principal: " + user);
        
        String username = null;
        if (user != null) {
            username = user.getName();
        } else {
            // Fallback to custom native header "username" passed from frontend
            username = accessor.getFirstNativeHeader("username");
            System.out.println("[WebSocketEventListener] Extracted username from native header: " + username);
        }

        if (username != null) {
            String sessionId = accessor.getSessionId();
            // We need to store the username in session attributes so DisconnectEvent can find it!
            if (accessor.getSessionAttributes() != null) {
                accessor.getSessionAttributes().put("username", username);
            }
            presenceService.userConnected(username, sessionId);
        } else {
            System.out.println("[WebSocketEventListener] WARNING: Could not extract username for session " + accessor.getSessionId());
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();
        
        String username = null;
        if (user != null) {
            username = user.getName();
        } else if (accessor.getSessionAttributes() != null) {
            // Retrieve from session attributes populated during connect
            username = (String) accessor.getSessionAttributes().get("username");
        }

        System.out.println("[WebSocketEventListener] SessionDisconnectEvent triggered. Extracted username: " + username);

        if (username != null) {
            String sessionId = accessor.getSessionId();
            presenceService.userDisconnected(username, sessionId);
        }
    }
}
