package pl.coffeechess.game.model.piece;

import org.junit.jupiter.api.Test;
import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.PieceType;

import static org.junit.jupiter.api.Assertions.*;

class PieceTest {

    @Test
    void piecesExposeColorAndType() {
        assertPiece(new King(Color.WHITE), Color.WHITE, PieceType.KING);
        assertPiece(new Queen(Color.BLACK), Color.BLACK, PieceType.QUEEN);
        assertPiece(new Rook(Color.WHITE), Color.WHITE, PieceType.ROOK);
        assertPiece(new Bishop(Color.BLACK), Color.BLACK, PieceType.BISHOP);
        assertPiece(new Knight(Color.WHITE), Color.WHITE, PieceType.KNIGHT);
        assertPiece(new Pawn(Color.BLACK), Color.BLACK, PieceType.PAWN);
    }

    @Test
    void colorHelpersMatchPieceColor() {
        Piece whitePiece = new Pawn(Color.WHITE);
        Piece blackPiece = new Pawn(Color.BLACK);

        assertTrue(whitePiece.isWhite());
        assertFalse(whitePiece.isBlack());
        assertTrue(blackPiece.isBlack());
        assertFalse(blackPiece.isWhite());
    }

    @Test
    void piecesKnowTheirBasicMovePatterns() {
        assertTrue(new King(Color.WHITE).canMove(7, 4, 6, 5, false));
        assertFalse(new King(Color.WHITE).canMove(7, 4, 5, 4, false));

        assertTrue(new Queen(Color.WHITE).canMove(7, 3, 3, 7, false));
        assertTrue(new Queen(Color.WHITE).canMove(7, 3, 0, 3, false));
        assertFalse(new Queen(Color.WHITE).canMove(7, 3, 5, 4, false));

        assertTrue(new Rook(Color.WHITE).canMove(7, 0, 0, 0, false));
        assertFalse(new Rook(Color.WHITE).canMove(7, 0, 5, 2, false));

        assertTrue(new Bishop(Color.WHITE).canMove(7, 2, 3, 6, false));
        assertFalse(new Bishop(Color.WHITE).canMove(7, 2, 3, 2, false));

        assertTrue(new Knight(Color.WHITE).canMove(7, 6, 5, 5, false));
        assertFalse(new Knight(Color.WHITE).canMove(7, 6, 6, 6, false));

        assertTrue(new Pawn(Color.WHITE).canMove(6, 4, 4, 4, false));
        assertTrue(new Pawn(Color.WHITE).canMove(6, 4, 5, 5, true));
        assertFalse(new Pawn(Color.WHITE).canMove(6, 4, 5, 5, false));
        assertTrue(new Pawn(Color.BLACK).canMove(1, 4, 3, 4, false));
        assertTrue(new Pawn(Color.BLACK).canMove(1, 4, 2, 5, true));
    }

    @Test
    void slidingPiecesRequireClearPath() {
        assertTrue(new Queen(Color.WHITE).requiresClearPath());
        assertTrue(new Rook(Color.WHITE).requiresClearPath());
        assertTrue(new Bishop(Color.WHITE).requiresClearPath());
        assertFalse(new King(Color.WHITE).requiresClearPath());
        assertFalse(new Knight(Color.WHITE).requiresClearPath());
        assertFalse(new Pawn(Color.WHITE).requiresClearPath());
    }

    private static void assertPiece(Piece piece, Color color, PieceType type) {
        assertEquals(color, piece.getColor());
        assertEquals(type, piece.getType());
    }
}
