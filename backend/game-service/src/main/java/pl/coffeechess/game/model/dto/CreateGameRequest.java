package pl.coffeechess.game.model.dto;

import java.util.UUID;

public record CreateGameRequest(
        UUID whitePlayerId,
        UUID blackPlayerId,
        String timeControl,
        String startingFen
) { }
