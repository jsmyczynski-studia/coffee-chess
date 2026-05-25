package pl.coffeechess.game.model.dto;

import pl.coffeechess.game.model.enums.GameStatus;

public record GameUpdateDto(
        String fen,
        Long whiteTimeMs,
        Long blackTimeMs,
        GameStatus status,
        String lastMove
) { }