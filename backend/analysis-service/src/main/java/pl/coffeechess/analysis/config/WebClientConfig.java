package pl.coffeechess.analysis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${chess.api.url:https://chess-api.com/v1}")
    private String chessApiUrl;

    @Bean
    public WebClient chessApiWebClient() {
        return WebClient.builder()
                .baseUrl(chessApiUrl)
                .codecs(config -> config.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }
}