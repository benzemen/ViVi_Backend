package com.substring.chat.chat_app_backend.services;

/**
 * Conversation summarization service interface.
 *
 * <p>Provides a "catch me up" feature — when a user joins a room with a long history,
 * this service generates a concise summary of recent messages using an LLM.</p>
 */
public interface SummarizationService {

    /**
     * Generates a summary of the most recent messages in a room.
     *
     * <p>Messages hidden by the given user are excluded from the summary,
     * so messages the user has "deleted" from their view will not appear.</p>
     *
     * @param roomId       the room identifier
     * @param messageCount the number of recent messages to summarize
     * @param username     the requesting user's username (nullable); used to
     *                     exclude messages hidden by that user
     * @return the generated summary text, or an empty string if unavailable
     */
    String summarizeRecentMessages(String roomId, int messageCount, String username);
}
