package com.substring.chat.chat_app_backend.services;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * No-op implementation of {@link SummarizationService}.
 *
 * <p>Active when {@code app.summarization.enabled} is {@code false} (the default).
 * Returns an empty string indicating no summary is available.</p>
 */
@Service
@ConditionalOnProperty(name = "app.summarization.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpSummarizationService implements SummarizationService {

    @Override
    public String summarizeRecentMessages(String roomId, int messageCount, String username) {
        return "";
    }
}
