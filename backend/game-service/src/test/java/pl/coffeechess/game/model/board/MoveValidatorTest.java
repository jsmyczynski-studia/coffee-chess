package pl.coffeechess.game.model.board;

import org.junit.jupiter.api.Test;
import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.piece.Bishop;
import pl.coffeechess.game.model.piece.King;
import pl.coffeechess.game.model.piece.Pawn;
import pl.coffeechess.game.model.piece.Queen;
import pl.coffeechess.game.model.piece.Rook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoveValidatorTest {

    @Test
    void pawnCanMoveOneOrTwoSquaresForwardFromStartPosition() {
        GameBoard board = GameBoard.standardSetup();

        assertThat(MoveValidator.isMoveLegal(board, "e2", "e4")).isTrue();
        assertThat(MoveValidator.isMoveLegal(board, "d7", "d5")).isTrue();
    }

    @Test
    void pawnCapturesDiagonallyOnlyWhenEnemyPieceIsPresent() {
        GameBoard board = GameBoard.empty();
        board.placePiece("e4", new Pawn(Color.WHITE));
        board.placePiece("d5", new Pawn(Color.BLACK));

        assertThat(MoveValidator.isMoveLegal(board, "e4", "d5")).isTrue();
        assertThat(MoveValidator.isMoveLegal(board, "e4", "f5")).isFalse();
    }

    @Test
    void pawnCannotMoveForwardIntoOccupiedSquare() {
        GameBoard board = GameBoard.empty();
        board.placePiece("e2", new Pawn(Color.WHITE));
        board.placePiece("e3", new Pawn(Color.BLACK));

        assertThat(MoveValidator.isMoveLegal(board, "e2", "e3")).isFalse();
    }

    @Test
    void pawnCannotMoveTwoSquaresWhenPathIsBlocked() {
        GameBoard board = GameBoard.empty();
        board.placePiece("e2", new Pawn(Color.WHITE));
        board.placePiece("e3", new Pawn(Color.BLACK));

        assertThat(MoveValidator.isMoveLegal(board, "e2", "e4")).isFalse();
    }

    @Test
    void knightCanJumpOverPieces() {
        GameBoard board = GameBoard.standardSetup();

        assertThat(MoveValidator.isMoveLegal(board, "g1", "f3")).isTrue();
    }

    @Test
    void slidingPiecesCannotJumpOverOtherPieces() {
        GameBoard board = GameBoard.standardSetup();

        assertThat(MoveValidator.isMoveLegal(board, "a1", "a4")).isFalse();
    }

    @Test
    void bishopMovesDiagonallyWhenPathIsClear() {
        GameBoard board = GameBoard.empty();
        board.placePiece("c1", new Bishop(Color.WHITE));

        assertThat(MoveValidator.isMoveLegal(board, "c1", "g5")).isTrue();
        assertThat(MoveValidator.isMoveLegal(board, "c1", "c5")).isFalse();
    }

    @Test
    void rookMovesStraightWhenPathIsClear() {
        GameBoard board = GameBoard.empty();
        board.placePiece("a1", new Rook(Color.WHITE));

        assertThat(MoveValidator.isMoveLegal(board, "a1", "a8")).isTrue();
        assertThat(MoveValidator.isMoveLegal(board, "a1", "b2")).isFalse();
    }

    @Test
    void queenMovesStraightOrDiagonallyWhenPathIsClear() {
        GameBoard board = GameBoard.empty();
        board.placePiece("d1", new Queen(Color.WHITE));

        assertThat(MoveValidator.isMoveLegal(board, "d1", "h5")).isTrue();
        assertThat(MoveValidator.isMoveLegal(board, "d1", "d8")).isTrue();
        assertThat(MoveValidator.isMoveLegal(board, "d1", "e3")).isFalse();
    }

    @Test
    void kingMovesOneSquareInAnyDirection() {
        GameBoard board = GameBoard.empty();
        board.placePiece("e1", new King(Color.WHITE));

        assertThat(MoveValidator.isMoveLegal(board, "e1", "f2")).isTrue();
        assertThat(MoveValidator.isMoveLegal(board, "e1", "e3")).isFalse();
    }

    @Test
    void shouldAllowKingSideAndQueenSideCastling() {
        GameBoard board = new GameBoard("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");

        assertThat(MoveValidator.isSafeLegalMove(board, "e1", "g1")).isTrue();
        assertThat(MoveValidator.isSafeLegalMove(board, "e1", "c1")).isTrue();

        board.setActiveColor(Color.BLACK);
        assertThat(MoveValidator.isSafeLegalMove(board, "e8", "g8")).isTrue();
        assertThat(MoveValidator.isSafeLegalMove(board, "e8", "c8")).isTrue();
    }

    @Test
    void shouldRejectCastlingWithoutRightsOrWithBlockedPath() {
        GameBoard noRights = new GameBoard("4k3/8/8/8/8/8/8/4K2R w - - 0 1");
        GameBoard blocked = new GameBoard("4k3/8/8/8/8/8/8/4KB1R w K - 0 1");

        assertThat(MoveValidator.isSafeLegalMove(noRights, "e1", "g1")).isFalse();
        assertThat(MoveValidator.isSafeLegalMove(blocked, "e1", "g1")).isFalse();
    }

    @Test
    void shouldRejectCastlingOutOfCheckOrThroughAttackedSquare() {
        GameBoard inCheck = new GameBoard("k3r3/8/8/8/8/8/8/4K2R w K - 0 1");
        GameBoard throughCheck = new GameBoard("k4r2/8/8/8/8/8/8/4K2R w K - 0 1");

        assertThat(MoveValidator.isSafeLegalMove(inCheck, "e1", "g1")).isFalse();
        assertThat(MoveValidator.isSafeLegalMove(throughCheck, "e1", "g1")).isFalse();
    }

    @Test
    void moveCannotCaptureOwnPiece() {
        GameBoard board = GameBoard.empty();
        board.placePiece("a1", new Rook(Color.WHITE));
        board.placePiece("a8", new King(Color.WHITE));

        assertThat(MoveValidator.isMoveLegal(board, "a1", "a8")).isFalse();
    }

    @Test
    void moveFromEmptySquareIsRejected() {
        GameBoard board = GameBoard.empty();

        assertThatThrownBy(() -> MoveValidator.isMoveLegal(board, "e4", "e5"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldDetectCheck() {
        GameBoard board = new GameBoard("4r3/8/8/8/8/8/8/4K3 w - - 0 1");

        assertThat(MoveValidator.isKingInCheck(board, Color.WHITE)).isTrue();
        assertThat(MoveValidator.isKingInCheck(board, Color.BLACK)).isFalse();
    }

    @Test
    void shouldDetectCheckmate() {
        String folsMateFen = "rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3";
        GameBoard board = new GameBoard(folsMateFen);

        assertThat(MoveValidator.isKingInCheck(board, Color.WHITE)).isTrue();
        assertThat(MoveValidator.hasAnyLegalMove(board, Color.WHITE)).isFalse();
    }

    @Test
    void shouldDetectStalemate() {
        String stalemateFen = "k7/2Q5/1K6/8/8/8/8/8 b - - 0 1";
        GameBoard board = new GameBoard(stalemateFen);

        assertThat(MoveValidator.isKingInCheck(board, Color.BLACK)).isFalse();
        assertThat(MoveValidator.hasAnyLegalMove(board, Color.BLACK)).isFalse();
    }

    @Test
    void shouldPreventMovingPinnedPiece() {
        String pinnedKnightFen = "4r3/8/8/8/8/8/4N3/4K3 w - - 0 1";
        GameBoard board = new GameBoard(pinnedKnightFen);

        assertThat(MoveValidator.isSafeLegalMove(board, "e2", "d4")).isFalse();
    }

    @Test
    void shouldAllowKingToEscapeCheck() {
        String checkFen = "4r3/8/8/8/8/8/8/4K3 w - - 0 1";
        GameBoard board = new GameBoard(checkFen);

        assertThat(MoveValidator.isSafeLegalMove(board, "e1", "d1")).isTrue();
        assertThat(MoveValidator.isSafeLegalMove(board, "e1", "e2")).isFalse();
    }
}
