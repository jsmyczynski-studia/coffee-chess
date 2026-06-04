package pl.coffeechess.game.model.piece;

import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.PieceType;

public class Queen extends Piece {

    public Queen(Color color) {
        super(color);
    }

    @Override
    public PieceType getType() {
        return PieceType.QUEEN;
    }

    @Override
    public boolean canMove(int sourceRow, int sourceColumn, int targetRow, int targetColumn, boolean targetOccupied) {
        int rowDelta = Math.abs(targetRow - sourceRow);
        int columnDelta = Math.abs(targetColumn - sourceColumn);
        return (sourceRow == targetRow || sourceColumn == targetColumn || rowDelta == columnDelta)
                && (rowDelta != 0 || columnDelta != 0);
    }

    @Override
    public boolean requiresClearPath() {
        return true;
    }

    @Override
    public char getFenChar() {
        return isWhite() ? 'Q' : 'q';
    }
}
