package pl.coffeechess.game.model.dto;

import pl.coffeechess.game.model.entity.Game;
import pl.coffeechess.game.model.enums.BotDifficulty;
import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.EndReason;
import pl.coffeechess.game.model.enums.GameStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public record GameDto(
        UUID id,
        UUID whitePlayerId,
        UUID blackPlayerId,
        GameStatus status,
        EndReason endReason,
        String currentFen,
        String moveListUci,
        String timeControl,
        long whiteTimeMs,
        long blackTimeMs,
        Color turn,
        Color drawOfferedBy,
        boolean vsBot,
        Color botColor,
        BotDifficulty botDifficulty,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {
    public static GameDto from(Game game) {
        Color turn = null;
        if (game.getCurrentFen() != null) {
            String[] parts = game.getCurrentFen().split(" ");
            if (parts.length >= 2) {
                turn = parts[1].equals("w") ? Color.WHITE : Color.BLACK;
            }
        }

        // Report the *live* remaining time for the side to move: stored clocks are only
        // decremented when a move is made, so without deducting the time elapsed since the
        // last move the client clock would repeatedly jump back to the stale stored value.
        long whiteMs = game.getWhiteTimeMs();
        long blackMs = game.getBlackTimeMs();
        if (turn != null && game.getStatus() == GameStatus.IN_PROGRESS) {
            LocalDateTime ref = game.getUpdatedAt() != null
                    ? game.getUpdatedAt()
                    : (game.getStartedAt() != null ? game.getStartedAt() : game.getCreatedAt());
            if (ref != null) {
                long elapsed = Math.max(0L, Duration.between(ref, LocalDateTime.now()).toMillis());
                if (turn == Color.WHITE) {
                    whiteMs = Math.max(0L, whiteMs - elapsed);
                } else {
                    blackMs = Math.max(0L, blackMs - elapsed);
                }
            }
        }

        return new GameDto(
                game.getId(),
                game.getWhitePlayerId(),
                game.getBlackPlayerId(),
                game.getStatus(),
                game.getEndReason(),
                game.getCurrentFen(),
                game.getMoveListUci(),
                game.getTimeControl(),
                whiteMs,
                blackMs,
                turn,
                game.getDrawOfferedBy(),
                game.isVsBot(),
                game.getBotColor(),
                game.getBotDifficulty(),
                game.getStartedAt(),
                game.getEndedAt()
        );
    }
}
