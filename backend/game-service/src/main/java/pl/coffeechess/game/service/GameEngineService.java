package pl.coffeechess.game.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.coffeechess.game.kafka.GameCompletedProducer;
import pl.coffeechess.game.model.board.BoardMove;
import pl.coffeechess.game.model.board.GameBoard;
import pl.coffeechess.game.model.board.MoveValidator;
import pl.coffeechess.game.model.dto.GameUpdateDto;
import pl.coffeechess.game.model.entity.Game;
import pl.coffeechess.game.model.entity.Move;
import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.EndReason;
import pl.coffeechess.game.model.enums.GameStatus;
import pl.coffeechess.game.repository.GameRepository;
import pl.coffeechess.game.repository.MoveRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameEngineService {

    private final GameRepository gameRepository;
    private final MoveRepository moveRepository;
    private final GameCompletedProducer kafkaProducer;

    @Transactional
    public GameUpdateDto processMove(UUID gameId, UUID playerId, String moveUciRequest) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game doesn't exist"));

        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Game has already ended.");
        }

        GameBoard board = new GameBoard(game.getCurrentFen());
        Color activeColor = board.getActiveColor();

        verifyPlayerTurn(game, playerId, activeColor);
        updateTimers(game, activeColor);

        if (moveUciRequest.length() < 4) {
            throw new IllegalArgumentException("Invalid move format");
        }
        String fromSquare = moveUciRequest.substring(0, 2);
        String toSquare = moveUciRequest.substring(2, 4);
        BoardMove moveFromUci = BoardMove.fromUciNotation(moveUciRequest);

        if (!MoveValidator.isSafeLegalMove(board, fromSquare, toSquare)) {
            throw new IllegalArgumentException("Illegal move!");
        }

        board.movePiece(fromSquare, toSquare);

        if (moveFromUci.promotionPiece() != null) {
            board.promotePiece(toSquare, moveFromUci.promotionPiece(), activeColor);
        }

        Color nextColor = (activeColor == Color.WHITE) ? Color.BLACK : Color.WHITE;
        board.setActiveColor(nextColor);

        boolean isCheck = MoveValidator.isKingInCheck(board, nextColor);
        boolean hasMoves = MoveValidator.hasAnyLegalMove(board, nextColor);

        if (!hasMoves) {
            if (isCheck) {
                game.setStatus(activeColor == Color.WHITE ? GameStatus.WHITE_WINS : GameStatus.BLACK_WINS);
            } else {
                game.setStatus(GameStatus.DRAW);
            }
        }

        String newFen = board.toFen();
        game.setCurrentFen(newFen);

        String currentPgn = game.getPgnMoves() == null ? "" : game.getPgnMoves();
        game.setPgnMoves(currentPgn + (currentPgn.isEmpty() ? "" : " ") + moveUciRequest);
        game.setUpdatedAt(LocalDateTime.now());

        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            game.setEndedAt(LocalDateTime.now());
        }

        gameRepository.save(game);

        Move moveEntity = new Move();
        moveEntity.setGame(game);
        moveEntity.setUci(moveUciRequest);
        moveEntity.setFenAfter(newFen);
        moveEntity.setMoveNumber((currentPgn.split(" ").length / 2) + 1);
        moveEntity.setPlayedAt(LocalDateTime.now());
        moveRepository.save(moveEntity);

        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            kafkaProducer.publishGameCompletedEvent(game);
        }

        return new GameUpdateDto(
                game.getCurrentFen(),
                game.getWhiteTimeMs(),
                game.getBlackTimeMs(),
                game.getStatus(),
                moveUciRequest
        );
    }

    private void verifyPlayerTurn(Game game, UUID playerId, Color activeColor) {
        UUID expectedPlayerId = (activeColor == Color.WHITE) ? game.getWhitePlayerId() : game.getBlackPlayerId();
        if (!playerId.equals(expectedPlayerId)) {
            throw new IllegalArgumentException("It's not your turn!");
        }
    }

    private void updateTimers(Game game, Color activeColor) {
        if (game.getUpdatedAt() == null) return;

        long timeElapsed = Duration.between(game.getUpdatedAt(), LocalDateTime.now()).toMillis();

        if (activeColor == Color.WHITE) {
            game.setWhiteTimeMs(game.getWhiteTimeMs() - timeElapsed);
            if (game.getWhiteTimeMs() <= 0) {
                game.setStatus(GameStatus.BLACK_WINS);
                game.setEndReason(EndReason.TIME_OUT);
            }
        } else {
            game.setBlackTimeMs(game.getBlackTimeMs() - timeElapsed);
            if (game.getBlackTimeMs() <= 0) {
                game.setStatus(GameStatus.WHITE_WINS);
                game.setEndReason(EndReason.TIME_OUT);
            }
        }
    }
}