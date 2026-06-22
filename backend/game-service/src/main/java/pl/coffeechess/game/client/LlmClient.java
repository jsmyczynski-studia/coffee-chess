package pl.coffeechess.game.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class LlmClient {

    private final RestClient llmRestClient;

    @Value("${llm.api-key:}")
    private String apiKey;

    @Value("${llm.model:openai/gpt-oss-20b}")
    private String model;

    @Value("${llm.max-tokens:40}")
    private int maxTokens;

    public LlmClient(@Qualifier("llmRestClient") RestClient llmRestClient) {
        this.llmRestClient = llmRestClient;
    }

    /**
     * Zwraca tekst odpowiedzi modelu albo null, gdy request się nie powiedzie.
     */
    public String complete(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("LLM call skipped because llm.api-key is not configured");
            return null;
        }

        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "max_tokens", maxTokens,
                    "temperature", 0.9,
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", systemPrompt
                            ),
                            Map.of(
                                    "role", "user",
                                    "content", userPrompt
                            )
                    )
            );

            GroqChatResponse response = llmRestClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(GroqChatResponse.class);

            if (response == null
                    || response.choices() == null
                    || response.choices().isEmpty()) {
                log.warn("LLM returned an empty response for model {}", model);
                return null;
            }

            Choice firstChoice = response.choices().getFirst();

            if (firstChoice == null
                    || firstChoice.message() == null
                    || firstChoice.message().content() == null
                    || firstChoice.message().content().isBlank()) {
                log.warn("LLM response did not contain message content for model {}", model);
                return null;
            }

            return firstChoice.message().content().trim();

        } catch (Exception e) {
            log.error("LLM call failed for model {}", model, e);
            return null;
        }
    }

    /**
     * Minimalny model odpowiedzi OpenAI-compatible / Groq API.
     * Pozostałe pola z odpowiedzi są ignorowane.
     */
    public record GroqChatResponse(
            List<Choice> choices
    ) {
    }

    public record Choice(
            Message message
    ) {
    }

    public record Message(
            String role,
            String content
    ) {
    }
}
