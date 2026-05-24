package pl.coffeechess.game.model.board;

import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.PieceType;
import pl.coffeechess.game.model.piece.King;
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

    private static String toSquareString(int row, int col) {
        char file = (char) ('a' + col);
        char rank = (char) ('8' - row);
        return "" + file + rank;
    }

    public static boolean isSafeLegalMove(GameBoard board, String from, String to) {
        Piece pieceToMove = board.getPieceAt(from);
        if (pieceToMove == null || pieceToMove.getColor() != board.getActiveColor()) {
            return false;
        }

        if (!isMoveLegal(board, from, to)) {
            return false;
        }

        Piece capturedPiece = board.movePiece(from, to);
        boolean isKingSafe = !isKingInCheck(board, pieceToMove.getColor());
        board.undoMove(from, to, capturedPiece);

        return isKingSafe;
    }

    public static boolean isKingInCheck(GameBoard board, Color kingColor) {
        String kingPos = findKingPosition(board, kingColor);
        if (kingPos == null) return false;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                String oppPos = toSquareString(row, col);
                Piece piece = board.getPieceAt(oppPos);

                if (piece != null && piece.getColor() != kingColor) {
                    if (isMoveLegal(board, oppPos, kingPos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasAnyLegalMove(GameBoard board, Color color) {
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                String fromPos = toSquareString(fromRow, fromCol);
                Piece piece = board.getPieceAt(fromPos);

                if (piece != null && piece.getColor() == color) {
                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {
                            String toPos = toSquareString(toRow, toCol);

                            if (isSafeLegalMove(board, fromPos, toPos)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private static String findKingPosition(GameBoard board, Color kingColor) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                String pos = toSquareString(row, col);
                Piece piece = board.getPieceAt(pos);
                if (piece instanceof King && piece.getColor() == kingColor) {
                    return pos;
                }
            }
        }
        return null;
    }
}
