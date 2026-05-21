package pl.coffeechess.user.model.dto;

import java.util.UUID;

public record EloUpdateDto(
        UUID gameId,
        UUID whitePlayerId,
        UUID blackPlayerId,
        String outcome,       // "WHITE_WINS" | "BLACK_WINS" | "DRAW"
        String timeControl,
        String pgn
) {}