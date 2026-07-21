package com.substring.chat.chat_app_backend.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request payload for sending a chat message via WebSocket or REST.
 *
 * <p>Contains the message content, sender identity, and target room.
 * The {@code sender} field will be overridden by the authenticated user's
 * identity once JWT authentication is enabled.</p>
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequest {

    @NotBlank(message = "Message content must not be blank")
    private String content;

    @NotBlank(message = "Sender must not be blank")
    private String sender;

    @NotBlank(message = "Room ID must not be blank")
    private String roomId;
}
