package pl.coffeechess.game_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.coffeechess.game_service.kafka.GameCompletedProducer;
import pl.coffeechess.game_service.model.domain.Board;
import pl.coffeechess.game_service.model.domain.Move;
import pl.coffeechess.game_service.model.domain.Position;
import pl.coffeechess.game_service.model.domain.pieces.King;
import pl.coffeechess.game_service.model.domain.pieces.Piece;
import pl.coffeechess.game_service.model.dto.GameUpdateDto;
import pl.coffeechess.game_service.model.entity.Game;
import pl.coffeechess.game_service.repository.GameRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameEngineService {

    private final GameRepository gameRepository;
    private final GameCompletedProducer kafkaProducer;

    @Transactional
    public GameUpdateDto processMove(UUID gameId, UUID playerId, String moveUciRequest) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game doesn't exist"));

        if (game.getStatus() != Game.GameStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Game has already ended.");
        }

        boolean isWhiteTurn = game.getFen().contains(" w ");
        verifyPlayerTurn(game, playerId, isWhiteTurn);
        updateTimers(game, isWhiteTurn);

        Board board = new Board(game.getFen());
        Move move = Move.fromUciNotation(moveUciRequest);

        if (!isLegalMove(board, move)) {
            throw new IllegalArgumentException("Illegal move!");
        }

        board.doMove(move);
        board.setWhiteToMove(!isWhiteTurn);
        boolean nextPlayerIsWhite = board.isWhiteToMove();

        boolean isCheck = isKingInCheck(board, nextPlayerIsWhite);
        boolean hasMoves = hasAnyLegalMove(board, nextPlayerIsWhite);

        if (!hasMoves) {
            if (isCheck) {
                game.setStatus(isWhiteTurn ? Game.GameStatus.WHITE_WON : Game.GameStatus.BLACK_WON);
            } else {
                game.setStatus(Game.GameStatus.DRAW);
            }
        }

        String newFen = board.toFen();
        game.setFen(newFen);

        String currentPgn = game.getPgn() == null ? "" : game.getPgn();
        game.setPgn(currentPgn + (currentPgn.isEmpty() ? "" : " ") + moveUciRequest);
        game.setLastMoveTimestamp(Instant.now());

        gameRepository.save(game);

        if (game.getStatus() != Game.GameStatus.IN_PROGRESS) {
            kafkaProducer.publishGameCompletedEvent(game);
        }

        return new GameUpdateDto(
                game.getFen(),
                game.getWhiteTimeLeftMs(),
                game.getBlackTimeLeftMs(),
                game.getStatus(),
                moveUciRequest
        );
    }

    private void verifyPlayerTurn(Game game, UUID playerId, boolean isWhiteTurn) {
        UUID expectedPlayerId = isWhiteTurn ? game.getWhitePlayerId() : game.getBlackPlayerId();
        if (!playerId.equals(expectedPlayerId)) {
            throw new IllegalArgumentException("It's not your turn!");
        }
    }

    private void updateTimers(Game game, boolean isWhiteTurn) {
        if (game.getLastMoveTimestamp() == null) return;

        long timeElapsed = Duration.between(game.getLastMoveTimestamp(), Instant.now()).toMillis();

        if (isWhiteTurn) {
            game.setWhiteTimeLeftMs(game.getWhiteTimeLeftMs() - timeElapsed);
            if (game.getWhiteTimeLeftMs() <= 0) game.setStatus(Game.GameStatus.BLACK_WON);
        } else {
            game.setBlackTimeLeftMs(game.getBlackTimeLeftMs() - timeElapsed);
            if (game.getBlackTimeLeftMs() <= 0) game.setStatus(Game.GameStatus.WHITE_WON);
        }
    }

    private boolean isLegalMove(Board board, Move move) {
        Piece pieceToMove = board.getPieceAt(move.from());

        if (pieceToMove == null) {
            return false;
        }
        if (pieceToMove.isWhite() != board.isWhiteToMove()) {
            return false;
        }

        List<Move> pseudoLegalMoves = pieceToMove.getPseudoLegalMoves(board, move.from());
        if (!pseudoLegalMoves.contains(move)) {
            return false;
        }

        Piece capturedPiece = board.doMove(move);

        boolean isKingSafe = !isKingInCheck(board, pieceToMove.isWhite());

        board.undoMove(move, capturedPiece);

        return isKingSafe;
    }

    private boolean isKingInCheck(Board board, boolean isWhiteKing) {
        Position kingPosition = findKingPosition(board, isWhiteKing);
        if (kingPosition == null) return false;

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece piece = board.getPieceAt(new Position(x, y));

                if (piece != null && piece.isWhite() != isWhiteKing) {
                    List<Move> opponentMoves = piece.getPseudoLegalMoves(board, new Position(x, y));

                    for (Move opponentMove : opponentMoves) {
                        if (opponentMove.to().equals(kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean hasAnyLegalMove(Board board, boolean isWhite) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Position startPosition = new Position(x, y);
                Piece piece = board.getPieceAt(startPosition);

                if (piece != null && piece.isWhite() == isWhite) {
                    List<Move> pseudoMoves = piece.getPseudoLegalMoves(board, startPosition);

                    for (Move pseudoMove : pseudoMoves) {
                        if (isLegalMove(board, pseudoMove)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private Position findKingPosition(Board board, boolean isWhiteKing) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece piece = board.getPieceAt(new Position(x, y));
                if (piece instanceof King && piece.isWhite() == isWhiteKing) {
                    return new Position(x, y);
                }
            }
        }
        return null;
    }
}