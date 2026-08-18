package com.substring.chat.chat_app_backend.controllers;

import com.substring.chat.chat_app_backend.entities.Message;
import com.substring.chat.chat_app_backend.payload.MessageRequest;
import com.substring.chat.chat_app_backend.services.MessageService;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket controller for real-time chat messaging via STOMP.
 *
 * <h3>Message flow</h3>
 * <ol>
 *   <li>Client sends a STOMP message to {@code /app/sendMessage/{roomId}}</li>
 *   <li>This controller receives it via {@code @MessageMapping}</li>
 *   <li>The message is persisted via {@link MessageService}</li>
 *   <li>The saved message is broadcast to {@code /topic/room/{roomId}} via {@code @SendTo}</li>
 *   <li>All clients subscribed to that topic receive the message in real-time</li>
 * </ol>
 */
@Controller
public class ChatController {

    private final MessageService messageService;
    private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;
    private final org.springframework.data.redis.listener.ChannelTopic channelTopic;

    public ChatController(MessageService messageService, 
                          org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate,
                          org.springframework.data.redis.listener.ChannelTopic channelTopic) {
        this.messageService = messageService;
        this.redisTemplate = redisTemplate;
        this.channelTopic = channelTopic;
    }

    /**
     * Handles incoming chat messages and publishes them to Redis Pub/Sub.
     *
     * <p>When JWT authentication is active, the {@code sender} field from the request
     * is overridden with the authenticated user's identity from the WebSocket session
     * {@link Principal}, preventing impersonation.</p>
     *
     * @param roomId    the target room ID from the destination path
     * @param request   the message payload
     * @param principal the authenticated user (null if auth is not yet configured)
     */
    @MessageMapping("/sendMessage/{roomId}")
    public void sendMessage(
            @DestinationVariable String roomId,
            @Valid MessageRequest request,
            Principal principal) {

        // Use authenticated identity if available; otherwise fall back to request sender
        String sender = (principal != null) ? principal.getName() : request.getSender();

        // Persist message to MongoDB
        Message savedMessage = messageService.sendMessage(roomId, sender, request.getContent());

        // Publish to Redis instead of sending directly to local STOMP broker.
        // The RedisMessageSubscriber on all connected nodes will receive this and broadcast it to STOMP.
        redisTemplate.convertAndSend(channelTopic.getTopic(), savedMessage);
    }
}
