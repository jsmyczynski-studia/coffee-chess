package pl.coffeechess.analysis.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class ChessApiClient {

    private static final int MIN_VARIANTS = 1;
    private static final int MAX_VARIANTS = 5;
    private static final int MIN_DEPTH = 1;
    private static final int MAX_DEPTH = 16;

    private final WebClient chessApiWebClient;
    private final ObjectMapper objectMapper;
    private final int timeoutSeconds;
    private final int analysisDepth;

    public ChessApiClient(WebClient chessApiWebClient,
                          ObjectMapper objectMapper,
                          @Value("${chess.api.timeout-seconds:15}") int timeoutSeconds,
                          @Value("${chess.api.analysis-depth:16}") int analysisDepth) {
        this.chessApiWebClient = chessApiWebClient;
        this.objectMapper = objectMapper;
        this.timeoutSeconds = timeoutSeconds;
        this.analysisDepth = analysisDepth;
    }

    /**
     * Returns up to {@code variants} engine lines for the given FEN (variants clamped to 1–5).
     */
    public Mono<List<CandidateMove>> getCandidateMoves(String fen, int variants) {
        int clampedVariants = clampVariants(variants);
        int depth = clampDepth(analysisDepth);
        ChessApiRequest request = new ChessApiRequest(fen, clampedVariants, depth);

        return chessApiWebClient.post()
                .uri("")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .map(body -> parseCandidates(body))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .doOnError(e -> log.error("chess-api.com error for fen={}: {}", fen, e.getMessage()));
    }

    public record ChessApiRequest(String fen, int variants, int depth) {}

    public static int clampVariants(int variants) {
        return Math.clamp(variants, MIN_VARIANTS, MAX_VARIANTS);
    }

    static int clampDepth(int depth) {
        return Math.clamp(depth, MIN_DEPTH, MAX_DEPTH);
    }

    private List<CandidateMove> parseCandidates(String body) {
        if (body == null || body.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            if (root.isArray()) {
                List<CandidateMove> candidates = new ArrayList<>();
                root.forEach(node -> candidates.add(mapCandidate(node)));
                return candidates;
            }
            if (root.isObject()) {
                JsonNode nested = root.get("variants");
                if (nested != null && nested.isArray()) {
                    List<CandidateMove> candidates = new ArrayList<>();
                    nested.forEach(node -> candidates.add(mapCandidate(node)));
                    return candidates;
                }
                return List.of(mapCandidate(root));
            }
        } catch (Exception e) {
            log.error("Failed to parse chess-api.com response: {}", e.getMessage());
        }
        return List.of();
    }

    private CandidateMove mapCandidate(JsonNode node) {
        String san = textOrNull(node, "san");
        String uci = textOrNull(node, "move");
        if (uci == null) {
            uci = textOrNull(node, "lan");
        }

        Double eval = null;
        if (node.hasNonNull("eval") && node.get("eval").isNumber()) {
            eval = node.get("eval").doubleValue();
        }

        Double winChance = null;
        if (node.hasNonNull("winChance") && node.get("winChance").isNumber()) {
            winChance = node.get("winChance").doubleValue();
        }

        return new CandidateMove(
                san,
                uci,
                eval,
                parseMate(node.get("mate")),
                winChance,
                parseContinuation(node.get("continuationArr"))
        );
    }

    private static Integer parseMate(JsonNode mateNode) {
        if (mateNode == null || mateNode.isNull()) {
            return null;
        }
        if (mateNode.isNumber()) {
            return mateNode.intValue();
        }
        if (mateNode.isTextual()) {
            try {
                return Integer.parseInt(mateNode.asText().trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static List<String> parseContinuation(JsonNode continuationNode) {
        if (continuationNode == null || !continuationNode.isArray()) {
            return List.of();
        }
        List<String> moves = new ArrayList<>();
        continuationNode.forEach(entry -> {
            if (entry.isTextual()) {
                moves.add(entry.asText());
            }
        });
        return Collections.unmodifiableList(moves);
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || !value.isTextual()) {
            return null;
        }
        String text = value.asText().trim();
        return text.isEmpty() ? null : text;
    }
}
