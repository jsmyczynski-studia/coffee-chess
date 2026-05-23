package pl.coffeechess.game_service.model.dto;

import pl.coffeechess.game_service.model.entity.Game.GameStatus;

public record GameUpdateDto(
        String fen,
        long whiteTimeLeftMs,
        long blackTimeLeftMs,
        GameStatus status,
        String lastMove
) {}