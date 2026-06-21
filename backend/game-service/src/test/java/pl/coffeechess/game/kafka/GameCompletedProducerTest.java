package pl.coffeechess.game.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import pl.coffeechess.game.model.entity.Game;
import pl.coffeechess.game.model.enums.EndReason;
import pl.coffeechess.game.model.enums.GameStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GameCompletedProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private GameCompletedProducer producer;

    @Test
    void publishGameCompletedEvent_sendsCanonicalPayload() {
        UUID gameId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID whiteId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID blackId = UUID.fromString("33333333-3333-3333-3333-333333333333");

        Game game = Game.builder()
                .id(gameId)
                .whitePlayerId(whiteId)
                .blackPlayerId(blackId)
                .status(GameStatus.WHITE_WINS)
                .endReason(EndReason.CHECKMATE)
                .moveListUci("e2e4 e7e5 g1f3")
                .timeControl("5+0")
                .build();

        producer.publishGameCompletedEvent(game);

        ArgumentCaptor<GameCompletedEvent> captor = ArgumentCaptor.forClass(GameCompletedEvent.class);
        verify(kafkaTemplate).send(eq("game-completed"), eq(gameId.toString()), captor.capture());

        GameCompletedEvent event = captor.getValue();
        assertThat(event.gameId()).isEqualTo(gameId.toString());
        assertThat(event.whitePlayerId()).isEqualTo(whiteId.toString());
        assertThat(event.blackPlayerId()).isEqualTo(blackId.toString());
        assertThat(event.outcome()).isEqualTo("WHITE_WINS");
        assertThat(event.moveListUci()).isEqualTo("e2e4 e7e5 g1f3");
        assertThat(event.timeControl()).isEqualTo("5+0");
        assertThat(event.endReason()).isEqualTo("CHECKMATE");
    }

    @Test
    void publishGameCompletedEvent_rejectsInProgressGames() {
        Game game = Game.builder()
                .id(UUID.randomUUID())
                .status(GameStatus.IN_PROGRESS)
                .build();

        assertThatThrownBy(() -> producer.publishGameCompletedEvent(game))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("IN_PROGRESS");
    }
}
