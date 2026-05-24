package pl.coffeechess.game.model.piece;

import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.PieceType;

public class Bishop extends Piece {

    public Bishop(Color color) {
        super(color);
    }

    @Override
    public PieceType getType() {
        return PieceType.BISHOP;
    }

    @Override
    public boolean canMove(int sourceRow, int sourceColumn, int targetRow, int targetColumn, boolean targetOccupied) {
        return Math.abs(targetRow - sourceRow) == Math.abs(targetColumn - sourceColumn)
                && sourceRow != targetRow;
    }

    @Override
    public boolean requiresClearPath() {
        return true;
    }
}
