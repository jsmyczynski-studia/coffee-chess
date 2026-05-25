package pl.coffeechess.game.model.board;

import org.junit.jupiter.api.Test;
import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.piece.Bishop;
import pl.coffeechess.game.model.piece.King;
import pl.coffeechess.game.model.piece.Pawn;
import pl.coffeechess.game.model.piece.Queen;
import pl.coffeechess.game.model.piece.Rook;

import static org.junit.jupiter.api.Assertions.*;

class MoveValidatorTest {

    @Test
    void pawnCanMoveOneOrTwoSquaresForwardFromStartPosition() {
        GameBoard board = GameBoard.standardSetup();

        assertTrue(MoveValidator.isMoveLegal(board, "e2", "e4"));
        assertTrue(MoveValidator.isMoveLegal(board, "d7", "d5"));
    }

    @Test
    void pawnCapturesDiagonallyOnlyWhenEnemyPieceIsPresent() {
        GameBoard board = GameBoard.empty();
        board.placePiece("e4", new Pawn(Color.WHITE));
        board.placePiece("d5", new Pawn(Color.BLACK));

        assertTrue(MoveValidator.isMoveLegal(board, "e4", "d5"));
        assertFalse(MoveValidator.isMoveLegal(board, "e4", "f5"));
    }

    @Test
    void pawnCannotMoveForwardIntoOccupiedSquare() {
        GameBoard board = GameBoard.empty();
        board.placePiece("e2", new Pawn(Color.WHITE));
        board.placePiece("e3", new Pawn(Color.BLACK));

        assertFalse(MoveValidator.isMoveLegal(board, "e2", "e3"));
    }

    @Test
    void pawnCannotMoveTwoSquaresWhenPathIsBlocked() {
        GameBoard board = GameBoard.empty();
        board.placePiece("e2", new Pawn(Color.WHITE));
        board.placePiece("e3", new Pawn(Color.BLACK));

        assertFalse(MoveValidator.isMoveLegal(board, "e2", "e4"));
    }

    @Test
    void knightCanJumpOverPieces() {
        GameBoard board = GameBoard.standardSetup();

        assertTrue(MoveValidator.isMoveLegal(board, "g1", "f3"));
    }

    @Test
    void slidingPiecesCannotJumpOverOtherPieces() {
        GameBoard board = GameBoard.standardSetup();

        assertFalse(MoveValidator.isMoveLegal(board, "a1", "a4"));
    }

    @Test
    void bishopMovesDiagonallyWhenPathIsClear() {
        GameBoard board = GameBoard.empty();
        board.placePiece("c1", new Bishop(Color.WHITE));

        assertTrue(MoveValidator.isMoveLegal(board, "c1", "g5"));
        assertFalse(MoveValidator.isMoveLegal(board, "c1", "c5"));
    }

    @Test
    void rookMovesStraightWhenPathIsClear() {
        GameBoard board = GameBoard.empty();
        board.placePiece("a1", new Rook(Color.WHITE));

        assertTrue(MoveValidator.isMoveLegal(board, "a1", "a8"));
        assertFalse(MoveValidator.isMoveLegal(board, "a1", "b2"));
    }

    @Test
    void queenMovesStraightOrDiagonallyWhenPathIsClear() {
        GameBoard board = GameBoard.empty();
        board.placePiece("d1", new Queen(Color.WHITE));

        assertTrue(MoveValidator.isMoveLegal(board, "d1", "h5"));
        assertTrue(MoveValidator.isMoveLegal(board, "d1", "d8"));
        assertFalse(MoveValidator.isMoveLegal(board, "d1", "e3"));
    }

    @Test
    void kingMovesOneSquareInAnyDirection() {
        GameBoard board = GameBoard.empty();
        board.placePiece("e1", new King(Color.WHITE));

        assertTrue(MoveValidator.isMoveLegal(board, "e1", "f2"));
        assertFalse(MoveValidator.isMoveLegal(board, "e1", "e3"));
    }

    @Test
    void moveCannotCaptureOwnPiece() {
        GameBoard board = GameBoard.empty();
        board.placePiece("a1", new Rook(Color.WHITE));
        board.placePiece("a8", new King(Color.WHITE));

        assertFalse(MoveValidator.isMoveLegal(board, "a1", "a8"));
    }

    @Test
    void moveFromEmptySquareIsRejected() {
        GameBoard board = GameBoard.empty();

        assertThrows(IllegalStateException.class, () -> MoveValidator.isMoveLegal(board, "e4", "e5"));
    }

    @Test
    void shouldDetectCheckmate() {
        String folsMateFen = "rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3";
        GameBoard board = new GameBoard(folsMateFen);

        assertTrue(MoveValidator.isKingInCheck(board, Color.WHITE));

        assertFalse(MoveValidator.hasAnyLegalMove(board, Color.WHITE));
    }

    @Test
    void shouldDetectStalemate() {
        String stalemateFen = "k7/2Q5/1K6/8/8/8/8/8 b - - 0 1";
        GameBoard board = new GameBoard(stalemateFen);

        assertFalse(MoveValidator.isKingInCheck(board, Color.BLACK));

        assertFalse(MoveValidator.hasAnyLegalMove(board, Color.BLACK));
    }

    @Test
    void shouldPreventMovingPinnedPiece() {
        String pinnedKnightFen = "4r3/8/8/8/8/8/4N3/4K3 w - - 0 1";
        GameBoard board = new GameBoard(pinnedKnightFen);

        assertFalse(MoveValidator.isSafeLegalMove(board, "e2", "d4"));
    }

    @Test
    void shouldAllowKingToEscapeCheck() {
        String checkFen = "4r3/8/8/8/8/8/8/4K3 w - - 0 1";
        GameBoard board = new GameBoard(checkFen);

        assertTrue(MoveValidator.isSafeLegalMove(board, "e1", "d1"));

        assertFalse(MoveValidator.isSafeLegalMove(board, "e1", "e2"));
    }
}
