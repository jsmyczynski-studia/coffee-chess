package pl.coffeechess.game.model.board;

import org.junit.jupiter.api.Test;
import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.PieceType;
import pl.coffeechess.game.model.piece.Bishop;
import pl.coffeechess.game.model.piece.King;
import pl.coffeechess.game.model.piece.Pawn;
import pl.coffeechess.game.model.piece.Piece;
import pl.coffeechess.game.model.piece.Queen;
import pl.coffeechess.game.model.piece.Rook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameBoardTest {

    @Test
    void standardSetupPlacesPiecesInInitialChessPosition() {
        GameBoard board = GameBoard.standardSetup();

        assertPiece(board.getPieceAt("a1"), PieceType.ROOK, Color.WHITE);
        assertPiece(board.getPieceAt("b1"), PieceType.KNIGHT, Color.WHITE);
        assertPiece(board.getPieceAt("c1"), PieceType.BISHOP, Color.WHITE);
        assertPiece(board.getPieceAt("d1"), PieceType.QUEEN, Color.WHITE);
        assertPiece(board.getPieceAt("e1"), PieceType.KING, Color.WHITE);
        assertPiece(board.getPieceAt("a2"), PieceType.PAWN, Color.WHITE);

        assertPiece(board.getPieceAt("a8"), PieceType.ROOK, Color.BLACK);
        assertPiece(board.getPieceAt("b8"), PieceType.KNIGHT, Color.BLACK);
        assertPiece(board.getPieceAt("c8"), PieceType.BISHOP, Color.BLACK);
        assertPiece(board.getPieceAt("d8"), PieceType.QUEEN, Color.BLACK);
        assertPiece(board.getPieceAt("e8"), PieceType.KING, Color.BLACK);
        assertPiece(board.getPieceAt("h7"), PieceType.PAWN, Color.BLACK);

        assertThat(board.isEmpty("e4")).isTrue();
    }

    @Test
    void emptyBoardAllowsPlacingAndRemovingPieces() {
        GameBoard board = GameBoard.empty();
        Piece king = new King(Color.WHITE);

        board.placePiece("e1", king);

        assertThat(board.getPieceAt("e1")).isSameAs(king);
        assertThat(board.hasPiece("e1")).isTrue();
        assertThat(board.removePiece("e1")).isSameAs(king);
        assertThat(board.isEmpty("e1")).isTrue();
    }

    @Test
    void pawnCanMoveOneOrTwoSquaresForwardFromStartPosition() {
        GameBoard board = GameBoard.standardSetup();

        assertThat(board.movePiece("e2", "e4")).isNull();
        assertPiece(board.getPieceAt("e4"), PieceType.PAWN, Color.WHITE);

        assertThat(board.movePiece("d7", "d5")).isNull();
        assertPiece(board.getPieceAt("d5"), PieceType.PAWN, Color.BLACK);
    }

    @Test
    void pawnCapturesDiagonallyOnlyWhenEnemyPieceIsPresent() {
        GameBoard board = GameBoard.empty();
        board.placePiece("e4", new Pawn(Color.WHITE));
        board.placePiece("d5", new Pawn(Color.BLACK));

        Piece capturedPiece = board.movePiece("e4", "d5");

        assertPiece(capturedPiece, PieceType.PAWN, Color.BLACK);
        assertPiece(board.getPieceAt("d5"), PieceType.PAWN, Color.WHITE);
        assertThat(board.isEmpty("e4")).isTrue();
    }

    @Test
    void pawnCannotMoveForwardIntoOccupiedSquare() {
        GameBoard board = GameBoard.empty();
        board.placePiece("e2", new Pawn(Color.WHITE));
        board.placePiece("e3", new Pawn(Color.BLACK));

        assertThatThrownBy(() -> board.movePiece("e2", "e3"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void knightCanJumpOverPieces() {
        GameBoard board = GameBoard.standardSetup();

        board.movePiece("g1", "f3");

        assertPiece(board.getPieceAt("f3"), PieceType.KNIGHT, Color.WHITE);
        assertThat(board.isEmpty("g1")).isTrue();
    }

    @Test
    void slidingPiecesCannotJumpOverOtherPieces() {
        GameBoard board = GameBoard.standardSetup();

        assertThatThrownBy(() -> board.movePiece("a1", "a4"))
                .isInstanceOf(IllegalArgumentException.class);
        assertPiece(board.getPieceAt("a1"), PieceType.ROOK, Color.WHITE);
    }

    @Test
    void bishopMovesDiagonallyWhenPathIsClear() {
        GameBoard board = GameBoard.empty();
        board.placePiece("c1", new Bishop(Color.WHITE));

        board.movePiece("c1", "g5");

        assertPiece(board.getPieceAt("g5"), PieceType.BISHOP, Color.WHITE);
    }

    @Test
    void rookMovesStraightWhenPathIsClear() {
        GameBoard board = GameBoard.empty();
        board.placePiece("a1", new Rook(Color.WHITE));

        board.movePiece("a1", "a8");

        assertPiece(board.getPieceAt("a8"), PieceType.ROOK, Color.WHITE);
    }

    @Test
    void queenMovesStraightOrDiagonallyWhenPathIsClear() {
        GameBoard board = GameBoard.empty();
        board.placePiece("d1", new Queen(Color.WHITE));
        board.placePiece("h4", new Queen(Color.BLACK));

        board.movePiece("d1", "h5");
        board.movePiece("h4", "h1");

        assertPiece(board.getPieceAt("h5"), PieceType.QUEEN, Color.WHITE);
        assertPiece(board.getPieceAt("h1"), PieceType.QUEEN, Color.BLACK);
    }

    @Test
    void kingMovesOneSquareInAnyDirection() {
        GameBoard board = GameBoard.empty();
        board.placePiece("e1", new King(Color.WHITE));

        board.movePiece("e1", "f2");

        assertPiece(board.getPieceAt("f2"), PieceType.KING, Color.WHITE);
    }

    @Test
    void moveCannotCaptureOwnPiece() {
        GameBoard board = GameBoard.empty();
        board.placePiece("a1", new Rook(Color.WHITE));
        board.placePiece("a8", new King(Color.WHITE));

        assertThatThrownBy(() -> board.movePiece("a1", "a8"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void invalidSquaresAreRejected() {
        GameBoard board = GameBoard.empty();

        assertThatThrownBy(() -> board.getPieceAt("i1")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> board.getPieceAt("a9")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> board.getPieceAt("e10")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> board.getPieceAt(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void moveFromEmptySquareIsRejected() {
        GameBoard board = GameBoard.empty();

        assertThatThrownBy(() -> board.movePiece("e4", "e5"))
                .isInstanceOf(IllegalStateException.class);
    }

    private static void assertPiece(Piece piece, PieceType type, Color color) {
        assertThat(piece).isNotNull();
        assertThat(piece.getType()).isEqualTo(type);
        assertThat(piece.getColor()).isEqualTo(color);
    }
}
