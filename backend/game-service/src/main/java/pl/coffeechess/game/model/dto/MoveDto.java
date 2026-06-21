package pl.coffeechess.game.model.dto;

import pl.coffeechess.game.model.entity.Move;

public record MoveDto(
        int moveNumber,
        String color,
        String san,
        String uci,
        String fenAfter
) {
    public static MoveDto from(Move move) {
        return new MoveDto(
                move.getMoveNumber(),
                move.getColor().name(),
                move.getSan(),
                move.getUci(),
                move.getFenAfter()
        );
    }
}
