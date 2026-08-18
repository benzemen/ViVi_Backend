package com.substring.chat.chat_app_backend.services;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class PresenceService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    public PresenceService(RedisTemplate<String, Object> redisTemplate, org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    public void userConnected(String username, String sessionId) {
        if (username == null || sessionId == null) return;
        
        String userSessionsKey = "user_sessions:" + username;
        redisTemplate.opsForSet().add(userSessionsKey, sessionId);
        // Set a TTL so stale sessions expire automatically if a disconnect event is missed
        redisTemplate.expire(userSessionsKey, 24, TimeUnit.HOURS);

        broadcastPresence(username, "ONLINE");
    }

    public void userDisconnected(String username, String sessionId) {
        if (username == null || sessionId == null) return;
        
        String userSessionsKey = "user_sessions:" + username;
        redisTemplate.opsForSet().remove(userSessionsKey, sessionId);

        if (!isUserOnline(username)) {
            broadcastPresence(username, "OFFLINE");
        }
    }

    public boolean isUserOnline(String username) {
        if (username == null) return false;
        
        String userSessionsKey = "user_sessions:" + username;
        Long size = redisTemplate.opsForSet().size(userSessionsKey);
        return size != null && size > 0;
    }

    public java.util.List<String> getOnlineUsers() {
        java.util.Set<String> keys = redisTemplate.keys("user_sessions:*");
        java.util.List<String> onlineUsers = new java.util.ArrayList<>();
        if (keys != null) {
            for (String key : keys) {
                Long size = redisTemplate.opsForSet().size(key);
                if (size != null && size > 0) {
                    onlineUsers.add(key.substring("user_sessions:".length()));
                }
            }
        }
        return onlineUsers;
    }

    private void broadcastPresence(String username, String status) {
        java.util.Map<String, String> payload = java.util.Map.of(
            "userId", username,
            "status", status
        );
        messagingTemplate.convertAndSend("/topic/presence", payload);
    }
}
