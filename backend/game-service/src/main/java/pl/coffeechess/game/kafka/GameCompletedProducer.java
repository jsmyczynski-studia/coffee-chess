package pl.coffeechess.game.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pl.coffeechess.game.model.entity.Game;
import pl.coffeechess.game.model.enums.GameStatus;

@Component
@RequiredArgsConstructor
public class GameCompletedProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_NAME = "game-completed";

    public void publishGameCompletedEvent(Game game) {
        GameStatus status = game.getStatus();
        if (status != GameStatus.WHITE_WINS
                && status != GameStatus.BLACK_WINS
                && status != GameStatus.DRAW
                && status != GameStatus.ABORTED) {
            throw new IllegalStateException("Cannot publish game-completed for status: " + status);
        }

        GameCompletedEvent payload = new GameCompletedEvent(
                game.getId().toString(),
                game.getWhitePlayerId() == null ? null : game.getWhitePlayerId().toString(),
                game.getBlackPlayerId() == null ? null : game.getBlackPlayerId().toString(),
                status.name(),
                game.getMoveListUci(),
                game.getTimeControl(),
                game.getEndReason() == null ? null : game.getEndReason().name()
        );

        kafkaTemplate.send(TOPIC_NAME, game.getId().toString(), payload);
    }
}
