package pl.coffeechess.game.model.piece;

import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.PieceType;

public class Pawn extends Piece {

    public Pawn(Color color) {
        super(color);
    }

    @Override
    public PieceType getType() {
        return PieceType.PAWN;
    }

    @Override
    public boolean canMove(int sourceRow, int sourceColumn, int targetRow, int targetColumn, boolean targetOccupied) {
        int direction = getColor() == Color.WHITE ? -1 : 1;
        int startRow = getColor() == Color.WHITE ? 6 : 1;
        int rowDelta = targetRow - sourceRow;
        int columnDelta = targetColumn - sourceColumn;

        if (columnDelta == 0 && rowDelta == direction && !targetOccupied) {
            return true;
        }

        if (columnDelta == 0 && sourceRow == startRow && rowDelta == 2 * direction && !targetOccupied) {
            return true;
        }

        return Math.abs(columnDelta) == 1 && rowDelta == direction && targetOccupied;
    }
}
