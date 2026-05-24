package pl.coffeechess.game.model.board;

import pl.coffeechess.game.model.enums.PieceType;
import pl.coffeechess.game.model.piece.Piece;

public final class MoveValidator {

    private MoveValidator() {
    }

    public static void validateMove(GameBoard board, String from, String to) {
        if (!isMoveLegal(board, from, to)) {
            throw new IllegalArgumentException("Illegal move from " + from + " to " + to);
        }
    }

    public static boolean isMoveLegal(GameBoard board, String from, String to) {
        Piece piece = board.getPieceAt(from);
        if (piece == null) {
            throw new IllegalStateException("No piece at source square: " + from);
        }

        Piece targetPiece = board.getPieceAt(to);
        if (targetPiece != null && targetPiece.getColor() == piece.getColor()) {
            return false;
        }

        int sourceRow = board.getRow(from);
        int sourceColumn = board.getColumn(from);
        int targetRow = board.getRow(to);
        int targetColumn = board.getColumn(to);
        boolean targetOccupied = targetPiece != null;

        if (!piece.canMove(sourceRow, sourceColumn, targetRow, targetColumn, targetOccupied)) {
            return false;
        }

        return !requiresClearPath(piece, sourceRow, targetRow) || board.isPathClear(from, to);
    }

    private static boolean requiresClearPath(Piece piece, int sourceRow, int targetRow) {
        return piece.requiresClearPath()
                || (piece.getType() == PieceType.PAWN && Math.abs(targetRow - sourceRow) == 2);
    }
}
