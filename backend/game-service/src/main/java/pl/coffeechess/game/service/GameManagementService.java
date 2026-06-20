package pl.coffeechess.game.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.coffeechess.game.kafka.GameCompletedProducer;
import pl.coffeechess.game.model.dto.CreateGameRequest;
import pl.coffeechess.game.model.dto.GameUpdateDto;
import pl.coffeechess.game.model.entity.Game;
import pl.coffeechess.game.model.enums.BotDifficulty;
import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.EndReason;
import pl.coffeechess.game.model.enums.GameStatus;
import pl.coffeechess.game.repository.GameRepository;

import java.time.LocalDateTime;
import java.util.UUID;

// zarządza grami poza silnikiem szachowym
@Service
@RequiredArgsConstructor
public class GameManagementService {

    public static final String STANDARD_START_FEN =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private static final long DEFAULT_TIME_MS = 5L * 60L * 1000L;

    private final GameRepository gameRepository;
    private final GameCompletedProducer kafkaProducer;

    @Autowired(required = false)
    private GameUpdateBroadcaster broadcaster;

    @Transactional
    public Game createGame(CreateGameRequest request, UUID creatorId) {
        if (request.vsBot()) {
            return createBotGame(request, creatorId);
        }

        UUID white = request.whitePlayerId();
        UUID black = request.blackPlayerId();

        if (white == null && black == null) {
            // twórca gra białymi, brak przeciwnika
            white = creatorId;
        }

        if (creatorId != null && !creatorId.equals(white) && !creatorId.equals(black)) {
            throw new IllegalArgumentException("Creator must be one of the participants.");
        }

        long timeMs = parseTimeControlToMs(request.timeControl());
        String startingFen = (request.startingFen() == null || request.startingFen().isBlank())
                ? STANDARD_START_FEN
                : request.startingFen();

        GameStatus initialStatus = (white == null || black == null)
                ? GameStatus.WAITING_FOR_OPPONENT
                : GameStatus.IN_PROGRESS;

        LocalDateTime now = LocalDateTime.now();

        Game game = Game.builder()
                .whitePlayerId(white)
                .blackPlayerId(black)
                .currentFen(startingFen)
                .timeControl(request.timeControl())
                .whiteTimeMs(timeMs)
                .blackTimeMs(timeMs)
                .startedAt(now)
                .status(initialStatus)
                .moveListUci("")
                .halfmoveClock(0L)
                .positionHistory("")
                .createdAt(now)
                .build();
        return gameRepository.save(game);
    }

    // tworzy grę przeciwko botowi - człowiek wybiera kolor i poziom trudności
    @Transactional
    public Game createBotGame(CreateGameRequest request, UUID creatorId) {
        if (creatorId == null) {
            throw new IllegalArgumentException("Authenticated player is required.");
        }
        Color humanColor = request.playerColor() == null ? Color.WHITE : request.playerColor();
        Color botColor = humanColor.opposite();
        BotDifficulty difficulty = request.botDifficulty() == null ? BotDifficulty.MEDIUM : request.botDifficulty();

        UUID white = humanColor == Color.WHITE ? creatorId : null;
        UUID black = humanColor == Color.BLACK ? creatorId : null;

        long timeMs = parseTimeControlToMs(request.timeControl());
        String startingFen = (request.startingFen() == null || request.startingFen().isBlank())
                ? STANDARD_START_FEN
                : request.startingFen();
        LocalDateTime now = LocalDateTime.now();

        Game game = Game.builder()
                .whitePlayerId(white)
                .blackPlayerId(black)
                .vsBot(true)
                .botColor(botColor)
                .botDifficulty(difficulty)
                .currentFen(startingFen)
                .timeControl(request.timeControl())
                .whiteTimeMs(timeMs)
                .blackTimeMs(timeMs)
                .startedAt(now)
                .status(GameStatus.IN_PROGRESS)
                .moveListUci("")
                .halfmoveClock(0L)
                .positionHistory("")
                .createdAt(now)
                .build();
        return gameRepository.save(game);
    }

    @Transactional
    public GameUpdateDto resign(UUID gameId, UUID playerId) {
        Game game = loadActiveGame(gameId);
        Color resigning = playerColor(game, playerId);

        game.setStatus(resigning == Color.WHITE ? GameStatus.BLACK_WINS : GameStatus.WHITE_WINS);
        game.setEndReason(EndReason.RESIGNATION);
        game.setEndedAt(LocalDateTime.now());
        game.setUpdatedAt(LocalDateTime.now());
        game.setDrawOfferedBy(null);

        gameRepository.save(game);
        kafkaProducer.publishGameCompletedEvent(game);

        GameUpdateDto dto = toUpdateDto(game);
        broadcast(game, dto);
        return dto;
    }

    @Transactional
    public GameUpdateDto offerDraw(UUID gameId, UUID playerId) {
        Game game = loadActiveGame(gameId);
        Color offerer = playerColor(game, playerId);

        if (game.getDrawOfferedBy() == offerer) {
            throw new IllegalArgumentException("You already have a pending draw offer.");
        }

        game.setDrawOfferedBy(offerer);
        game.setUpdatedAt(LocalDateTime.now());
        gameRepository.save(game);

        GameUpdateDto dto = toUpdateDto(game);
        broadcast(game, dto);
        return dto;
    }

    @Transactional
    public GameUpdateDto acceptDraw(UUID gameId, UUID playerId) {
        Game game = loadActiveGame(gameId);
        Color accepter = playerColor(game, playerId);

        Color offerer = game.getDrawOfferedBy();
        if (offerer == null) {
            throw new IllegalArgumentException("No draw offer pending.");
        }
        if (offerer == accepter) {
            throw new IllegalArgumentException("Cannot accept your own draw offer.");
        }

        game.setStatus(GameStatus.DRAW);
        game.setEndReason(EndReason.AGREEMENT);
        game.setEndedAt(LocalDateTime.now());
        game.setUpdatedAt(LocalDateTime.now());
        game.setDrawOfferedBy(null);

        gameRepository.save(game);
        kafkaProducer.publishGameCompletedEvent(game);

        GameUpdateDto dto = toUpdateDto(game);
        broadcast(game, dto);
        return dto;
    }

    @Transactional
    public GameUpdateDto declineDraw(UUID gameId, UUID playerId) {
        Game game = loadActiveGame(gameId);
        Color decliner = playerColor(game, playerId);

        Color offerer = game.getDrawOfferedBy();
        if (offerer == null) {
            throw new IllegalArgumentException("No draw offer pending.");
        }
        if (offerer == decliner) {
            throw new IllegalArgumentException("Cannot decline your own draw offer.");
        }

        game.setDrawOfferedBy(null);
        game.setUpdatedAt(LocalDateTime.now());
        gameRepository.save(game);

        GameUpdateDto dto = toUpdateDto(game);
        broadcast(game, dto);
        return dto;
    }

    private Game loadActiveGame(UUID gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game doesn't exist"));
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Game has already ended.");
        }
        return game;
    }

    private Color playerColor(Game game, UUID playerId) {
        if (playerId == null) {
            throw new IllegalArgumentException("Player is not a participant of this game.");
        }
        if (playerId.equals(game.getWhitePlayerId())) return Color.WHITE;
        if (playerId.equals(game.getBlackPlayerId())) return Color.BLACK;
        throw new IllegalArgumentException("Player is not a participant of this game.");
    }

    private GameUpdateDto toUpdateDto(Game game) {
        return new GameUpdateDto(
                game.getCurrentFen(),
                game.getWhiteTimeMs(),
                game.getBlackTimeMs(),
                game.getStatus(),
                null
        );
    }

    private void broadcast(Game game, GameUpdateDto dto) {
        if (broadcaster != null) {
            broadcaster.broadcast(game, dto);
        }
    }

    // parsuje czas gry na milisekundy
    private long parseTimeControlToMs(String tc) {
        if (tc == null || tc.isBlank()) {
            return DEFAULT_TIME_MS;
        }
        String minutesPart = tc;
        int plusIndex = tc.indexOf('+');
        if (plusIndex >= 0) {
            minutesPart = tc.substring(0, plusIndex);
        }
        try {
            long minutes = Long.parseLong(minutesPart.trim());
            if (minutes <= 0) {
                return DEFAULT_TIME_MS;
            }
            return minutes * 60L * 1000L;
        } catch (NumberFormatException e) {
            return DEFAULT_TIME_MS;
        }
    }
}
