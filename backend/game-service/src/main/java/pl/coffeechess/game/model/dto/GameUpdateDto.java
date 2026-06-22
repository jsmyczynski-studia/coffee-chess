package pl.coffeechess.game.model.dto;

import pl.coffeechess.game.model.entity.Game;
import pl.coffeechess.game.model.enums.GameStatus;

public record GameUpdateDto(
        String fen,
        Long whiteTimeMs,
        Long blackTimeMs,
        GameStatus status,
        String lastMove
) {
    public static GameUpdateDto from(Game game) {
        String moveList = game.getMoveListUci();
        String lastMove = null;
        if (moveList != null && !moveList.isBlank()) {
            String[] parts = moveList.trim().split("\\s+");
            lastMove = parts[parts.length - 1];
        }
        return new GameUpdateDto(
                game.getCurrentFen(),
                game.getWhiteTimeMs(),
                game.getBlackTimeMs(),
                game.getStatus(),
                lastMove
        );
    }
}