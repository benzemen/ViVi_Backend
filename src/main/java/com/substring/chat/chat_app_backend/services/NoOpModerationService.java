package com.substring.chat.chat_app_backend.services;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * No-op implementation of {@link ModerationService}.
 *
 * <p>Active when {@code app.moderation.enabled} is {@code false} (the default).
 * All content is allowed through without modification.</p>
 */
@Service
@ConditionalOnProperty(name = "app.moderation.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpModerationService implements ModerationService {

    @Override
    public boolean isContentAllowed(String content) {
        return true;
    }

    @Override
    public String filterContent(String content) {
        return content;
    }
}
