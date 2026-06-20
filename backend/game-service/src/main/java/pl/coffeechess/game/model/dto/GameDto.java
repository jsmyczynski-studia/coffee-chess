package pl.coffeechess.game.model.dto;

import pl.coffeechess.game.model.entity.Game;
import pl.coffeechess.game.model.enums.BotDifficulty;
import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.EndReason;
import pl.coffeechess.game.model.enums.GameStatus;

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
        return new GameDto(
                game.getId(),
                game.getWhitePlayerId(),
                game.getBlackPlayerId(),
                game.getStatus(),
                game.getEndReason(),
                game.getCurrentFen(),
                game.getMoveListUci(),
                game.getTimeControl(),
                game.getWhiteTimeMs(),
                game.getBlackTimeMs(),
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
