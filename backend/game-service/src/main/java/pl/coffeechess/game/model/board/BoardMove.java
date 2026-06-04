package pl.coffeechess.game.model.board;

import pl.coffeechess.game.model.enums.PieceType;

public record BoardMove(int fromRow, int fromCol, int toRow, int toCol, PieceType promotionPiece) {

    public static BoardMove fromUciNotation(String uci) {
        if (uci == null || (uci.length() != 4 && uci.length() != 5)) {
            throw new IllegalArgumentException("Niepoprawny format ruchu: " + uci);
        }

        int fromCol = uci.charAt(0) - 'a';
        int fromRow = 8 - Character.getNumericValue(uci.charAt(1));

        int toCol = uci.charAt(2) - 'a';
        int toRow = 8 - Character.getNumericValue(uci.charAt(3));

        PieceType promotion = null;
        if (uci.length() == 5) {
            promotion = switch (uci.charAt(4)) {
                case 'q' -> PieceType.QUEEN;
                case 'r' -> PieceType.ROOK;
                case 'b' -> PieceType.BISHOP;
                case 'n' -> PieceType.KNIGHT;
                default -> null;
            };
        }

        return new BoardMove(fromRow, fromCol, toRow, toCol, promotion);
    }
}