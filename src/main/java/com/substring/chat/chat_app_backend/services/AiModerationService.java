package com.substring.chat.chat_app_backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * AI-powered content moderation using an LLM Chat Completions API (e.g. Groq).
 */
@Service
@ConditionalOnProperty(name = "app.moderation.enabled", havingValue = "true")
public class AiModerationService implements ModerationService {

    @Value("${app.moderation.api-key}")
    private String apiKey;

    @Value("${app.moderation.api-url:https://api.groq.com/openai/v1/chat/completions}")
    private String apiUrl;

    @Value("${app.moderation.model:llama3-8b-8192}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean isContentAllowed(String content) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content",
                                    "You are a strict moderation assistant. Classify the user's message. " +
                                    "If the message contains severe hate speech, explicit illegal content, or severe harassment, " +
                                    "respond with EXACTLY the word 'FLAGGED'. Otherwise, respond with EXACTLY the word 'SAFE'. " +
                                    "Say nothing else."),
                            Map.of("role", "user", "content", content)
                    ),
                    "max_tokens", 10,
                    "temperature", 0.0
            );
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, request, Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> choices =
                        (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String reply = (String) message.get("content");
                    return reply == null || !reply.trim().toUpperCase().contains("FLAGGED");
                }
            }
            return true;
        } catch (Exception ex) {
            // Fail-open
            return true;
        }
    }

    @Override
    public String filterContent(String content) {
        return content;
    }
}
