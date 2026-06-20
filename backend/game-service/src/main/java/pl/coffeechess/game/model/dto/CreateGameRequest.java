package pl.coffeechess.game.model.dto;

import pl.coffeechess.game.model.enums.BotDifficulty;
import pl.coffeechess.game.model.enums.Color;

import java.util.UUID;

public record CreateGameRequest(
        UUID whitePlayerId,
        UUID blackPlayerId,
        String timeControl,
        String startingFen,
        boolean vsBot,
        Color playerColor,
        BotDifficulty botDifficulty
) { }
