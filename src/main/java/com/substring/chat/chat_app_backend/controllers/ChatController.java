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

    public ChatController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Handles incoming chat messages and broadcasts them to the room's topic.
     *
     * <p>When JWT authentication is active, the {@code sender} field from the request
     * is overridden with the authenticated user's identity from the WebSocket session
     * {@link Principal}, preventing impersonation.</p>
     *
     * @param roomId    the target room ID from the destination path
     * @param request   the message payload
     * @param principal the authenticated user (null if auth is not yet configured)
     * @return the persisted message, broadcast to all room subscribers
     */
    @MessageMapping("/sendMessage/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public Message sendMessage(
            @DestinationVariable String roomId,
            @Valid MessageRequest request,
            Principal principal) {

        // Use authenticated identity if available; otherwise fall back to request sender
        String sender = (principal != null) ? principal.getName() : request.getSender();

        return messageService.sendMessage(roomId, sender, request.getContent());
    }
}
