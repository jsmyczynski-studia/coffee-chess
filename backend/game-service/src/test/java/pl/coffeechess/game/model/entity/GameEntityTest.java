package pl.coffeechess.game.model.entity;

import org.junit.jupiter.api.Test;
import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.GameStatus;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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

        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertNotNull(game.getCreatedAt());
        assertNotNull(game.getMoves());
        assertTrue(game.getMoves().isEmpty());
        assertNull(game.getEndReason());
        assertNull(game.getEndedAt());
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

        assertNull(game.getUpdatedAt());
        game.preUpdate();
        assertNotNull(game.getUpdatedAt());
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

        assertEquals(1, game.getMoves().size());
        assertSame(game, game.getMoves().get(0).getGame());
    }
}
