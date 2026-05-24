package pl.coffeechess.game.model.piece;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.PieceType;

@Getter
@RequiredArgsConstructor
public abstract class Piece {

    private final Color color;

    public abstract PieceType getType();

    public abstract boolean canMove(int sourceRow, int sourceColumn, int targetRow, int targetColumn, boolean targetOccupied);

    public boolean requiresClearPath() {
        return false;
    }

    public boolean isWhite() {
        return color == Color.WHITE;
    }

    public boolean isBlack() {
        return color == Color.BLACK;
    }
}
