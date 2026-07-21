package com.substring.chat.chat_app_backend.services;

/**
 * Content moderation service interface.
 *
 * <p>Provides a pluggable abstraction for message content moderation.
 * The default implementation ({@link NoOpModerationService}) allows all content through.
 * When {@code app.moderation.enabled=true}, the {@link AiModerationService}
 * implementation is activated, which calls an external AI API to classify content.</p>
 */
public interface ModerationService {

    /**
     * Checks whether the given content is allowed to be posted.
     *
     * @param content the message content to check
     * @return {@code true} if the content is acceptable, {@code false} if it should be blocked
     */
    boolean isContentAllowed(String content);

    /**
     * Filters the given content, potentially masking or modifying inappropriate parts.
     *
     * <p>Unlike {@link #isContentAllowed(String)}, this method does not block the message
     * but may sanitize it (e.g., replacing profanity with asterisks).</p>
     *
     * @param content the message content to filter
     * @return the filtered content
     */
    String filterContent(String content);
}
