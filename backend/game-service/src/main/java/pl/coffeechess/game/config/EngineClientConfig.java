package pl.coffeechess.game.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

// blokujący klient HTTP dla ruchów bota - szybki, oddzielny od reaktywnego klienta analizy
@Configuration
public class EngineClientConfig {

    @Value("${chess.api.url:https://chess-api.com/v1}")
    private String chessApiUrl;

    @Value("${chess.api.timeout-seconds:10}")
    private int timeoutSeconds;

    @Bean("engineRestClient")
    public RestClient engineRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        return RestClient.builder()
                .baseUrl(chessApiUrl)
                .requestFactory(factory)
                .build();
    }

    @Value("${llm.base-url:https://api.openai.com/v1}")
    private String llmBaseUrl;

    @Value("${llm.timeout-seconds:8}")
    private int llmTimeoutSeconds;

    @Bean("llmRestClient")
    public RestClient llmRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(llmTimeoutSeconds));
        factory.setReadTimeout(Duration.ofSeconds(llmTimeoutSeconds));
        return RestClient.builder()
                .baseUrl(llmBaseUrl)
                .requestFactory(factory)
                .build();
    }
}
