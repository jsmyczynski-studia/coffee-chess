package pl.coffeechess.game.model.piece;

import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.PieceType;

public class Rook extends Piece {

    public Rook(Color color) {
        super(color);
    }

    @Override
    public PieceType getType() {
        return PieceType.ROOK;
    }

    @Override
    public boolean canMove(int sourceRow, int sourceColumn, int targetRow, int targetColumn, boolean targetOccupied) {
        return (sourceRow == targetRow || sourceColumn == targetColumn)
                && (sourceRow != targetRow || sourceColumn != targetColumn);
    }

    @Override
    public boolean requiresClearPath() {
        return true;
    }
}
