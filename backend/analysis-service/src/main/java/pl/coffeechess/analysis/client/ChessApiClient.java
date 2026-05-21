package pl.coffeechess.analysis.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChessApiClient {

    private final WebClient chessApiWebClient;

    @Value("${chess.api.timeout-seconds:15}")
    private int timeoutSeconds;

    @Value("${chess.api.analysis-depth:16}")
    private int analysisDepth;

    public Mono<ChessApiResponse> getBestMove(String fen) {
        return chessApiWebClient.post()
                .uri("/move")
                .bodyValue(new ChessApiRequest(fen, analysisDepth))
                .retrieve()
                .bodyToMono(ChessApiResponse.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .doOnError(e -> log.error("chess-api.com error for fen={}: {}", fen, e.getMessage()));
    }

    public record ChessApiRequest(String fen, int depth) {}

    public record ChessApiResponse(
            String move,
            String san,
            Double eval,
            String text
    ) {}
}