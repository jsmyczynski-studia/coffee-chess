package pl.coffeechess.game.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.coffeechess.game.kafka.GameCompletedProducer;
import pl.coffeechess.game.model.board.BoardMove;
import pl.coffeechess.game.model.board.GameBoard;
import pl.coffeechess.game.model.board.InsufficientMaterialDetector;
import pl.coffeechess.game.model.board.MoveValidator;
import pl.coffeechess.game.model.dto.GameUpdateDto;
import pl.coffeechess.game.model.entity.Game;
import pl.coffeechess.game.model.entity.Move;
import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.EndReason;
import pl.coffeechess.game.model.enums.GameStatus;
import pl.coffeechess.game.model.enums.PieceType;
import pl.coffeechess.game.model.piece.Piece;
import pl.coffeechess.game.repository.GameRepository;
import pl.coffeechess.game.repository.MoveRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameEngineService {

    private static final int FIFTY_MOVE_RULE_HALFMOVES = 100;
    private static final int THREEFOLD_REPETITION_COUNT = 3;

    private final GameRepository gameRepository;
    private final MoveRepository moveRepository;
    private final GameCompletedProducer kafkaProducer;

    @Autowired(required = false)
    private GameUpdateBroadcaster broadcaster;

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

        if (updateTimers(game, activeColor)) {
            // gracz stracił czas przed ruchem
            game.setEndedAt(LocalDateTime.now());
            game.setUpdatedAt(LocalDateTime.now());
            gameRepository.save(game);
            kafkaProducer.publishGameCompletedEvent(game);
            GameUpdateDto flagFallDto = toUpdateDto(game, null);
            broadcast(game, flagFallDto);
            return flagFallDto;
        }

        MoveResult result = applyMove(game, board, moveUciRequest, activeColor);
        broadcast(game, result.dto());
        return result.dto();
    }

    @Transactional
    public MoveResult processBotMove(UUID gameId, String moveUciRequest) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game doesn't exist"));

        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            return null;
        }

        GameBoard board = new GameBoard(game.getCurrentFen());
        Color activeColor = board.getActiveColor();

        if (updateTimers(game, activeColor)) {
            game.setEndedAt(LocalDateTime.now());
            game.setUpdatedAt(LocalDateTime.now());
            gameRepository.save(game);
            kafkaProducer.publishGameCompletedEvent(game);
            GameUpdateDto flagFallDto = toUpdateDto(game, null);
            broadcast(game, flagFallDto);
            return new MoveResult(flagFallDto, false, activeColor);
        }

        MoveResult result = applyMove(game, board, moveUciRequest, activeColor);
        broadcast(game, result.dto());
        return result;
    }

    // stosuje zwalidowany ruch i zapisuje stan; używane przez gracza i bota
    MoveResult applyMove(Game game, GameBoard board, String moveUciRequest, Color activeColor) {
        if (moveUciRequest == null || moveUciRequest.length() < 4 || moveUciRequest.length() > 5) {
            throw new IllegalArgumentException("Invalid move format");
        }
        String fromSquare = moveUciRequest.substring(0, 2);
        String toSquare = moveUciRequest.substring(2, 4);
        BoardMove moveFromUci = BoardMove.fromUciNotation(moveUciRequest);

        if (!MoveValidator.isSafeLegalMove(board, fromSquare, toSquare)) {
            throw new IllegalArgumentException("Illegal move!");
        }

        Piece movingPiece = board.getPieceAt(fromSquare);
        boolean isPawnMove = movingPiece != null && movingPiece.getType() == PieceType.PAWN;
        boolean isCapture = board.getPieceAt(toSquare) != null;

        board.movePiece(fromSquare, toSquare);

        if (moveFromUci.promotionPiece() != null) {
            board.promotePiece(toSquare, moveFromUci.promotionPiece(), activeColor);
        }

        Color nextColor = (activeColor == Color.WHITE) ? Color.BLACK : Color.WHITE;
        board.setActiveColor(nextColor);

        if (isPawnMove || isCapture) {
            game.setHalfmoveClock(0L);
        } else {
            game.setHalfmoveClock(game.getHalfmoveClock() + 1);
        }

        String newFen = board.toFen();
        String positionKey = extractPositionKey(newFen);
        game.appendPositionHistory(positionKey);

        boolean isCheck = MoveValidator.isKingInCheck(board, nextColor);
        boolean hasMoves = MoveValidator.hasAnyLegalMove(board, nextColor);

        if (!hasMoves) {
            if (isCheck) {
                game.setStatus(activeColor == Color.WHITE ? GameStatus.WHITE_WINS : GameStatus.BLACK_WINS);
                game.setEndReason(EndReason.CHECKMATE);
            } else {
                game.setStatus(GameStatus.DRAW);
                game.setEndReason(EndReason.STALEMATE);
            }
        } else if (InsufficientMaterialDetector.isInsufficientMaterial(board)) {
            game.setStatus(GameStatus.DRAW);
            game.setEndReason(EndReason.INSUFFICIENT_MATERIAL);
        } else if (game.getHalfmoveClock() >= FIFTY_MOVE_RULE_HALFMOVES) {
            game.setStatus(GameStatus.DRAW);
            game.setEndReason(EndReason.FIFTY_MOVE_RULE);
        } else if (countOccurrences(game.getPositionHistory(), positionKey) >= THREEFOLD_REPETITION_COUNT) {
            game.setStatus(GameStatus.DRAW);
            game.setEndReason(EndReason.THREEFOLD_REPETITION);
        }

        game.setCurrentFen(newFen);

        String currentMoves = game.getMoveListUci() == null ? "" : game.getMoveListUci();
        game.setMoveListUci(currentMoves + (currentMoves.isEmpty() ? "" : " ") + moveUciRequest);
        game.setUpdatedAt(LocalDateTime.now());

        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            game.setEndedAt(LocalDateTime.now());
        }

        // nowy ruch anuluje propozycję remisu
        game.setDrawOfferedBy(null);

        gameRepository.save(game);

        Move moveEntity = new Move();
        moveEntity.setGame(game);
        moveEntity.setUci(moveUciRequest);
        moveEntity.setFenAfter(newFen);
        moveEntity.setColor(activeColor);
        moveEntity.setSan(moveUciRequest);
        moveEntity.setMoveNumber((currentMoves.split(" ").length / 2) + 1);
        moveEntity.setPlayedAt(LocalDateTime.now());
        moveRepository.save(moveEntity);

        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            kafkaProducer.publishGameCompletedEvent(game);
        }

        GameUpdateDto dto = new GameUpdateDto(
                game.getCurrentFen(),
                game.getWhiteTimeMs(),
                game.getBlackTimeMs(),
                game.getStatus(),
                moveUciRequest
        );
        return new MoveResult(dto, isCapture, activeColor);
    }

    // wynik zastosowanego ruchu wykorzystywany przez logikę bota
    public record MoveResult(GameUpdateDto dto, boolean capture, Color movedColor) { }

    private void verifyPlayerTurn(Game game, UUID playerId, Color activeColor) {
        // ruch bota nie jest powiązany z id gracza
        if (game.isVsBot() && game.getBotColor() == activeColor) {
            throw new IllegalArgumentException("It's the bot's turn!");
        }
        UUID expectedPlayerId = (activeColor == Color.WHITE) ? game.getWhitePlayerId() : game.getBlackPlayerId();
        if (expectedPlayerId == null || !expectedPlayerId.equals(playerId)) {
            throw new IllegalArgumentException("It's not your turn!");
        }
    }

    // odejmuje czas i sprawdza warunek przegranej na czas
    public boolean updateTimers(Game game, Color activeColor) {
        if (game.getUpdatedAt() == null) {
            return false;
        }

        long elapsed = Duration.between(game.getUpdatedAt(), LocalDateTime.now()).toMillis();

        long remaining;
        if (activeColor == Color.WHITE) {
            remaining = game.getWhiteTimeMs() - elapsed;
            game.setWhiteTimeMs(Math.max(remaining, 0L));
        } else {
            remaining = game.getBlackTimeMs() - elapsed;
            game.setBlackTimeMs(Math.max(remaining, 0L));
        }

        if (remaining <= 0L) {
            game.setStatus(activeColor == Color.WHITE ? GameStatus.BLACK_WINS : GameStatus.WHITE_WINS);
            game.setEndReason(EndReason.TIME_OUT);
            return true;
        }
        return false;
    }

    private GameUpdateDto toUpdateDto(Game game, String lastMove) {
        return new GameUpdateDto(
                game.getCurrentFen(),
                game.getWhiteTimeMs(),
                game.getBlackTimeMs(),
                game.getStatus(),
                lastMove
        );
    }

    private void broadcast(Game game, GameUpdateDto dto) {
        if (broadcaster != null) {
            broadcaster.broadcast(game, dto);
        }
    }

    private static String extractPositionKey(String fen) {
        if (fen == null) {
            return "";
        }
        String[] parts = fen.split(" ");
        if (parts.length >= 2) {
            return parts[0] + " " + parts[1];
        }
        return fen;
    }

    private static int countOccurrences(String history, String key) {
        if (history == null || history.isEmpty() || key == null || key.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (String part : history.split(";")) {
            if (part.equals(key)) {
                count++;
            }
        }
        return count;
    }
}
