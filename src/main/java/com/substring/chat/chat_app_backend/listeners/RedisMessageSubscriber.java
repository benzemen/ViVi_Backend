package com.substring.chat.chat_app_backend.listeners;

import com.substring.chat.chat_app_backend.entities.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisMessageSubscriber {

    private final SimpMessagingTemplate messagingTemplate;

    public RedisMessageSubscriber(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Receives messages from the Redis Pub/Sub topic and broadcasts them
     * to the local STOMP broker.
     */
    public void onMessage(Message message) {
        System.out.println("[RedisMessageSubscriber] Received message for room: " + message.getRoomId());
        messagingTemplate.convertAndSend("/topic/room/" + message.getRoomId(), message);
    }
}
