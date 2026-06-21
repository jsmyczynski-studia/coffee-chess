package pl.coffeechess.game.model.piece;

import org.junit.jupiter.api.Test;
import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.PieceType;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(whitePiece.isWhite()).isTrue();
        assertThat(whitePiece.isBlack()).isFalse();
        assertThat(blackPiece.isBlack()).isTrue();
        assertThat(blackPiece.isWhite()).isFalse();
    }

    @Test
    void piecesKnowTheirBasicMovePatterns() {
        assertThat(new King(Color.WHITE).canMove(7, 4, 6, 5, false)).isTrue();
        assertThat(new King(Color.WHITE).canMove(7, 4, 5, 4, false)).isFalse();

        assertThat(new Queen(Color.WHITE).canMove(7, 3, 3, 7, false)).isTrue();
        assertThat(new Queen(Color.WHITE).canMove(7, 3, 0, 3, false)).isTrue();
        assertThat(new Queen(Color.WHITE).canMove(7, 3, 5, 4, false)).isFalse();

        assertThat(new Rook(Color.WHITE).canMove(7, 0, 0, 0, false)).isTrue();
        assertThat(new Rook(Color.WHITE).canMove(7, 0, 5, 2, false)).isFalse();

        assertThat(new Bishop(Color.WHITE).canMove(7, 2, 3, 6, false)).isTrue();
        assertThat(new Bishop(Color.WHITE).canMove(7, 2, 3, 2, false)).isFalse();

        assertThat(new Knight(Color.WHITE).canMove(7, 6, 5, 5, false)).isTrue();
        assertThat(new Knight(Color.WHITE).canMove(7, 6, 6, 6, false)).isFalse();

        assertThat(new Pawn(Color.WHITE).canMove(6, 4, 4, 4, false)).isTrue();
        assertThat(new Pawn(Color.WHITE).canMove(6, 4, 5, 5, true)).isTrue();
        assertThat(new Pawn(Color.WHITE).canMove(6, 4, 5, 5, false)).isFalse();
        assertThat(new Pawn(Color.BLACK).canMove(1, 4, 3, 4, false)).isTrue();
        assertThat(new Pawn(Color.BLACK).canMove(1, 4, 2, 5, true)).isTrue();
    }

    @Test
    void slidingPiecesRequireClearPath() {
        assertThat(new Queen(Color.WHITE).requiresClearPath()).isTrue();
        assertThat(new Rook(Color.WHITE).requiresClearPath()).isTrue();
        assertThat(new Bishop(Color.WHITE).requiresClearPath()).isTrue();
        assertThat(new King(Color.WHITE).requiresClearPath()).isFalse();
        assertThat(new Knight(Color.WHITE).requiresClearPath()).isFalse();
        assertThat(new Pawn(Color.WHITE).requiresClearPath()).isFalse();
    }

    private static void assertPiece(Piece piece, Color color, PieceType type) {
        assertThat(piece.getColor()).isEqualTo(color);
        assertThat(piece.getType()).isEqualTo(type);
    }
}
