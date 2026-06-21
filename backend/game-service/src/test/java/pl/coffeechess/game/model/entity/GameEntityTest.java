package pl.coffeechess.game.model.entity;

import org.junit.jupiter.api.Test;
import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.GameStatus;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GameEntityTest {

    @Test
    void builderDefaultsAreApplied() {
        Game game = Game.builder()
                .whitePlayerId(UUID.randomUUID())
                .blackPlayerId(UUID.randomUUID())
                .currentFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
                .whiteTimeMs(300_000)
                .blackTimeMs(300_000)
                .startedAt(LocalDateTime.now())
                .build();

        assertThat(game.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(game.getCreatedAt()).isNotNull();
        assertThat(game.getMoves()).isNotNull().isEmpty();
        assertThat(game.getEndReason()).isNull();
        assertThat(game.getEndedAt()).isNull();
    }

    @Test
    void preUpdateSetsUpdatedAt() {
        Game game = Game.builder()
                .whitePlayerId(UUID.randomUUID())
                .blackPlayerId(UUID.randomUUID())
                .currentFen("startfen")
                .whiteTimeMs(0)
                .blackTimeMs(0)
                .startedAt(LocalDateTime.now())
                .build();

        assertThat(game.getUpdatedAt()).isNull();
        game.preUpdate();
        assertThat(game.getUpdatedAt()).isNotNull();
    }

    @Test
    void moveCanBeAddedToGameCollection() {
        Game game = Game.builder()
                .whitePlayerId(UUID.randomUUID())
                .blackPlayerId(UUID.randomUUID())
                .currentFen("startfen")
                .whiteTimeMs(0)
                .blackTimeMs(0)
                .startedAt(LocalDateTime.now())
                .build();

        Move move = Move.builder()
                .game(game)
                .moveNumber(1)
                .color(Color.WHITE)
                .san("e4")
                .uci("e2e4")
                .fenAfter("...")
                .playedAt(LocalDateTime.now())
                .build();

        game.getMoves().add(move);

        assertThat(game.getMoves()).hasSize(1);
        assertThat(game.getMoves().get(0).getGame()).isSameAs(game);
    }
}
