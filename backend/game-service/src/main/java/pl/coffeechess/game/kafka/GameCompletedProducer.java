package pl.coffeechess.game.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pl.coffeechess.game.model.entity.Game;

@Component
@RequiredArgsConstructor
public class GameCompletedProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_NAME = "game-completed";

    public void publishGameCompletedEvent(Game game) {
        String outcome = switch (game.getStatus()) {
            case WHITE_WINS -> "WHITE_WON";
            case BLACK_WINS -> "BLACK_WON";
            case DRAW -> "DRAW";
            case ABORTED -> "ABORTED";
            default -> throw new IllegalStateException("Unexpected value: " + game.getStatus());
        };

        GameCompletedEvent payload = new GameCompletedEvent(
                game.getId().toString(),
                game.getWhitePlayerId().toString(),
                game.getBlackPlayerId().toString(),
                outcome,
                game.getPgnMoves()
        );

        kafkaTemplate.send(TOPIC_NAME, game.getId().toString(), payload);
    }

    public record GameCompletedEvent(
            String gameId,
            String whitePlayerId,
            String blackPlayerId,
            String outcome,
            String pgn
    ) {}
}