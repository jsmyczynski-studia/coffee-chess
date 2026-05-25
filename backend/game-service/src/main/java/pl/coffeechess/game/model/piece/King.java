package pl.coffeechess.game.model.piece;

import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.PieceType;

public class King extends Piece {

    public King(Color color) {
        super(color);
    }

    @Override
    public PieceType getType() {
        return PieceType.KING;
    }

    @Override
    public boolean canMove(int sourceRow, int sourceColumn, int targetRow, int targetColumn, boolean targetOccupied) {
        int rowDelta = Math.abs(targetRow - sourceRow);
        int columnDelta = Math.abs(targetColumn - sourceColumn);
        return rowDelta <= 1 && columnDelta <= 1 && (rowDelta != 0 || columnDelta != 0);
    }

    @Override
    public char getFenChar() {
        return isWhite() ? 'K' : 'k';
    }
}
