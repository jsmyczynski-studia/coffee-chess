package pl.coffeechess.game.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pl.coffeechess.game.model.enums.BotDifficulty;

@Slf4j
@Component
public class EngineClient {

    private final RestClient engineRestClient;
    private final ObjectMapper objectMapper;

    public EngineClient(
            @Qualifier("engineRestClient") RestClient engineRestClient,
            ObjectMapper objectMapper
    ) {
        this.engineRestClient = engineRestClient;
        this.objectMapper = objectMapper;
    }

    // zwraca ruch bota w notacji UCI albo null, gdy silnik zawiedzie
    public String getBotMove(String fen, BotDifficulty difficulty) {
        EngineResponse response = analyzePosition(fen, difficulty);
        return response == null ? null : response.move();
    }

    public EngineResponse analyzePosition(String fen, BotDifficulty difficulty) {
        return analyze(fen, difficulty, "");
    }

    public EngineResponse analyzeMove(String fen, BotDifficulty difficulty, String move) {
        return analyze(fen, difficulty, move);
    }

    private EngineResponse analyze(String fen, BotDifficulty difficulty, String searchMoves) {
        try {
            String rawResponse = engineRestClient.post()
                    .uri("")
                    .body(new EngineRequest(
                            fen,
                            difficulty.getDepth(),
                            difficulty.getSkill(),
                            searchMoves
                    ))
                    .retrieve()
                    .body(String.class);

            if (rawResponse == null || rawResponse.isBlank()) {
                log.warn("Engine returned an empty response for fen={}", fen);
                return null;
            }

            log.debug("Engine raw response for fen={}: {}", fen, rawResponse);

            EngineResponse response = objectMapper.readValue(
                    rawResponse,
                    EngineResponse.class
            );

            if (response == null || response.move() == null || response.move().isBlank()) {
                log.warn("Engine response has no move for fen={}: {}", fen, rawResponse);
                return null;
            }

            return response;

        } catch (Exception e) {
            log.error("Engine analysis failed for fen={}", fen, e);
            return null;
        }
    }

    public record EngineRequest(
            String fen,
            int depth,
            int maxThinkingTime,
            String searchmoves
    ) {
    }

    public record EngineResponse(
            String move,
            String san,
            Double eval,
            String text
    ) {
    }
}
