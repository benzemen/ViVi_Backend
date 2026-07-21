package com.substring.chat.chat_app_backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a message is blocked by the content moderation system.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ContentModerationException extends RuntimeException {

    public ContentModerationException(String reason) {
        super("Message blocked by moderation: " + reason);
    }
}
