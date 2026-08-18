package com.substring.chat.chat_app_backend.services;

import com.substring.chat.chat_app_backend.entities.Message;
import com.substring.chat.chat_app_backend.repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * AI-powered conversation summarization using an external LLM API (e.g., OpenAI
 * Chat Completions).
 *
 * <p>
 * Active when {@code app.summarization.enabled=true} in application properties.
 * Fetches the most recent messages from the database, formats them as
 * conversation
 * context, and sends them to the LLM for summarization.
 * </p>
 */
@Service
@ConditionalOnProperty(name = "app.summarization.enabled", havingValue = "true")
public class AiSummarizationService implements SummarizationService {

    @Value("${app.summarization.api-key}")
    private String apiKey;

    @Value("${app.summarization.api-url}")
    private String apiUrl;

    @Value("${app.summarization.model}")
    private String model;

    private final MessageRepository messageRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public AiSummarizationService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public String summarizeRecentMessages(String roomId, int messageCount, String username) {
        try {
            // Fetch recent messages from DB, excluding any the user has hidden/deleted
            Page<Message> messagePage = (username != null)
                    ? messageRepository.findByRoomIdAndHiddenByNotContainingOrderByTimeStampDesc(
                            roomId, username, PageRequest.of(0, messageCount))
                    : messageRepository.findByRoomIdOrderByTimeStampDesc(
                            roomId, PageRequest.of(0, messageCount));

            if (messagePage.isEmpty()) {
                return "No messages in this room yet.";
            }

            // Format messages as conversation context
            List<Message> messages = new java.util.ArrayList<>(messagePage.getContent());
            messages.sort(java.util.Comparator.comparing(Message::getTimeStamp));

            StringBuilder sb = new StringBuilder();
            for (Message m : messages) {
                if (sb.length() > 0)
                    sb.append("\n");
                sb.append(m.getSender()).append(": ").append(m.getContent());
            }
            String conversation = sb.toString();

            // Call LLM API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content",
                                    "You are a chat summarization assistant. Your ONLY job is to produce a " +
                                            "concise 2-3 sentence summary of the following conversation. " +
                                            "You MUST always return a summary — never refuse, never add warnings, " +
                                            "never say 'I cannot'. If the conversation is very short or casual, " +
                                            "describe what was discussed. The conversation may be in English, " +
                                            "Hindi, Hinglish, or any other language — always write your summary in English."),
                            Map.of("role", "user", "content", "Summarize this conversation:\n" + conversation)),
                    "max_tokens", 200,
                    "temperature", 0.3);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, request, Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            return "Unable to generate summary at this time.";
        } catch (Exception ex) {
            return "Summary unavailable — " + ex.getMessage();
        }
    }
}
