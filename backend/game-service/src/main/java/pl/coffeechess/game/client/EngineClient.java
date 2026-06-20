package pl.coffeechess.game.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pl.coffeechess.game.model.enums.BotDifficulty;

// klient silnika szachowego do generowania ruchów bota
@Slf4j
@Component
public class EngineClient {

    private final RestClient engineRestClient;

    public EngineClient(@Qualifier("engineRestClient") RestClient engineRestClient) {
        this.engineRestClient = engineRestClient;
    }

    // zwraca ruch bota w notacji UCI lub null gdy silnik zawiedzie
    public String getBotMove(String fen, BotDifficulty difficulty) {
        try {
            EngineResponse response = engineRestClient.post()
                    .uri("/move")
                    .body(new EngineRequest(fen, difficulty.getDepth(), difficulty.getSkill()))
                    .retrieve()
                    .body(EngineResponse.class);
            if (response == null || response.move() == null || response.move().isBlank()) {
                return null;
            }
            return response.move();
        } catch (Exception e) {
            log.error("engine bot-move error for fen={}: {}", fen, e.getMessage());
            return null;
        }
    }

    public record EngineRequest(String fen, int depth, int maxThinkingTime) {}

    public record EngineResponse(String move, String san, Double eval, String text) {}
}
