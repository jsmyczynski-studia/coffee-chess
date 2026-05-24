package pl.coffeechess.game.model.piece;

import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.PieceType;

public class Knight extends Piece {

    public Knight(Color color) {
        super(color);
    }

    @Override
    public PieceType getType() {
        return PieceType.KNIGHT;
    }

    @Override
    public boolean canMove(int sourceRow, int sourceColumn, int targetRow, int targetColumn, boolean targetOccupied) {
        int rowDelta = Math.abs(targetRow - sourceRow);
        int columnDelta = Math.abs(targetColumn - sourceColumn);
        return (rowDelta == 2 && columnDelta == 1)
                || (rowDelta == 1 && columnDelta == 2);
    }

    @Override
    public char getFenChar() {
        return isWhite() ? 'N' : 'n';
    }
}
