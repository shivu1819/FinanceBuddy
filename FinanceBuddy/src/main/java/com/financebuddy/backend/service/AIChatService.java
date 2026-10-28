
package com.financebuddy.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Service
public class AIChatService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public AIChatService(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model:gemini-3.8-flash}") String model,
            ObjectMapper objectMapper) {

        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;

        this.restClient = RestClient.builder()
                .baseUrl(
                        "https://generativelanguage.googleapis.com/v1beta"
                )
                .build();
    }

    public String getReply(String message) {

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "Message cannot be empty."
            );
        }

        if (message.length() > 4000) {
            throw new IllegalArgumentException(
                    "Message must not exceed 4000 characters."
            );
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Gemini API key is not configured."
            );
        }

        Map<String, Object> requestBody = Map.of(
                "systemInstruction",
                Map.of(
                        "parts",
                        List.of(
                                Map.of(
                                        "text",
                                        "You are FinanceBuddy AI, a helpful " +
                                                "assistant for personal finance and " +
                                                "general questions. Explain budgeting, " +
                                                "saving, expenses, and financial concepts " +
                                                "clearly. Never guarantee investment " +
                                                "returns. Answer general questions " +
                                                "normally. Keep responses helpful and " +
                                                "easy to understand."
                                )
                        )
                ),
                "contents",
                List.of(
                        Map.of(
                                "role", "user",
                                "parts",
                                List.of(
                                        Map.of("text", message)
                                )
                        )
                )
        );

        // One retry for temporary server errors or rate limits.
        for (int attempt = 1; attempt <= 2; attempt++) {

            try {
                System.out.println(
                        "Gemini request attempt " + attempt +
                                ", model: " + model
                );

                String response = restClient.post()
                        .uri("/models/{model}:generateContent", model)
                        .header("x-goog-api-key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestBody)
                        .retrieve()
                        .body(String.class);

                if (response == null || response.isBlank()) {
                    throw new IllegalStateException(
                            "Gemini returned an empty response."
                    );
                }

                JsonNode root = objectMapper.readTree(response);

                JsonNode textNode = root.path("candidates")
                        .path(0)
                        .path("content")
                        .path("parts")
                        .path(0)
                        .path("text");

                if (textNode.isMissingNode()
                        || textNode.isNull()
                        || textNode.asText().isBlank()) {

                    String blockReason = root.path("promptFeedback")
                            .path("blockReason")
                            .asText("");

                    if (!blockReason.isBlank()) {
                        throw new IllegalStateException(
                                "Gemini blocked the request: " + blockReason
                        );
                    }

                    throw new IllegalStateException(
                            "Gemini returned no text. Please try again."
                    );
                }

                return textNode.asText();

            } catch (RestClientResponseException e) {

                int status = e.getStatusCode().value();

                System.err.println(
                        "Gemini API HTTP Status: " + status
                );
                System.err.println(
                        "Gemini API Error Body: " +
                                e.getResponseBodyAsString()
                );

                boolean retryable =
                        status == 429 ||
                                status == 500 ||
                                status == 502 ||
                                status == 503 ||
                                status == 504;

                if (retryable && attempt < 2) {
                    waitBeforeRetry();
                    continue;
                }

                throw new IllegalStateException(
                        "Gemini API request failed with HTTP status " +
                                status + ". Check the backend console.",
                        e
                );

            } catch (Exception e) {

                System.err.println(
                        "Gemini API Error: " + e.getMessage()
                );

                if (e instanceof IllegalStateException) {
                    throw (IllegalStateException) e;
                }

                throw new IllegalStateException(
                        "Unable to get a reply from Gemini. " +
                                "Check the backend console.",
                        e
                );
            }
        }

        throw new IllegalStateException(
                "Gemini is temporarily unavailable. Please try again."
        );
    }

    private void waitBeforeRetry() {
        try {
            System.out.println(
                    "Gemini temporarily unavailable. Retrying once..."
            );

            Thread.sleep(1500);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Gemini retry interrupted.", e
            );
        }
    }
}
