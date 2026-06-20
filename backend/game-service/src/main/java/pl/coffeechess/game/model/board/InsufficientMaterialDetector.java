package pl.coffeechess.game.model.board;

import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.PieceType;
import pl.coffeechess.game.model.piece.Piece;

import java.util.ArrayList;
import java.util.List;

// wykrywa niewystarczający materiał do mata
public final class InsufficientMaterialDetector {

    private InsufficientMaterialDetector() {
    }

    public static boolean isInsufficientMaterial(GameBoard board) {
        List<Piece> whitePieces = new ArrayList<>();
        List<Piece> blackPieces = new ArrayList<>();
        List<Integer> whiteBishopSquareColors = new ArrayList<>();
        List<Integer> blackBishopSquareColors = new ArrayList<>();

        for (int row = 0; row < GameBoard.BOARD_SIZE; row++) {
            for (int col = 0; col < GameBoard.BOARD_SIZE; col++) {
                String square = "" + (char) ('a' + col) + (char) ('8' - row);
                Piece piece = board.getPieceAt(square);
                if (piece == null) {
                    continue;
                }

                PieceType type = piece.getType();
                if (type == PieceType.PAWN || type == PieceType.ROOK || type == PieceType.QUEEN) {
                    return false;
                }

                if (piece.getColor() == Color.WHITE) {
                    whitePieces.add(piece);
                    if (type == PieceType.BISHOP) {
                        whiteBishopSquareColors.add((row + col) % 2);
                    }
                } else {
                    blackPieces.add(piece);
                    if (type == PieceType.BISHOP) {
                        blackBishopSquareColors.add((row + col) % 2);
                    }
                }
            }
        }

        int w = whitePieces.size();
        int b = blackPieces.size();

        if (w == 1 && b == 1) {
            return true;
        }
        if (w == 2 && b == 1 && hasAnyMinorPiece(whitePieces)) {
            return true;
        }
        if (w == 1 && b == 2 && hasAnyMinorPiece(blackPieces)) {
            return true;
        }
        if (w == 2 && b == 2
                && whiteBishopSquareColors.size() == 1
                && blackBishopSquareColors.size() == 1
                && whiteBishopSquareColors.get(0).equals(blackBishopSquareColors.get(0))) {
            return true;
        }

        return false;
    }

    private static boolean hasAnyMinorPiece(List<Piece> pieces) {
        return pieces.stream().anyMatch(p ->
                p.getType() == PieceType.BISHOP || p.getType() == PieceType.KNIGHT);
    }
}
