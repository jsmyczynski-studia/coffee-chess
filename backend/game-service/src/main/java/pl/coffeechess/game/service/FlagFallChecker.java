package pl.coffeechess.game.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.coffeechess.game.kafka.GameCompletedProducer;
import pl.coffeechess.game.model.board.GameBoard;
import pl.coffeechess.game.model.dto.GameUpdateDto;
import pl.coffeechess.game.model.entity.Game;
import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.EndReason;
import pl.coffeechess.game.model.enums.GameStatus;
import pl.coffeechess.game.repository.GameRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// cyklicznie sprawdza czy komuś skończył się czas
@Component
@Slf4j
public class FlagFallChecker {

    private final GameRepository gameRepository;
    private final GameCompletedProducer kafkaProducer;
    private final FlagFallChecker self;
    private final GameUpdateBroadcaster broadcaster;

    public FlagFallChecker(GameRepository gameRepository,
                           GameCompletedProducer kafkaProducer,
                           @Lazy FlagFallChecker self,
                           @Autowired(required = false) GameUpdateBroadcaster broadcaster) {
        this.gameRepository = gameRepository;
        this.kafkaProducer = kafkaProducer;
        this.self = self;
        this.broadcaster = broadcaster;
    }

    @Scheduled(fixedDelayString = "${game-service.flag-fall.check-interval-ms:1000}")
    public void checkAll() {
        List<Game> liveGames;
        try {
            liveGames = gameRepository.findAllByStatus(GameStatus.IN_PROGRESS);
        } catch (Exception e) {
            log.debug("FlagFallChecker: could not load games: {}", e.getMessage());
            return;
        }
        for (Game game : liveGames) {
            try {
                self.checkOne(game.getId());
            } catch (Exception e) {
                log.warn("FlagFallChecker: error checking game {}: {}", game.getId(), e.getMessage());
            }
        }
    }

    @Transactional
    public void checkOne(UUID gameId) {
        Game game = gameRepository.findById(gameId).orElse(null);
        if (game == null || game.getStatus() != GameStatus.IN_PROGRESS) {
            return;
        }

        Color activeColor;
        try {
            activeColor = new GameBoard(game.getCurrentFen()).getActiveColor();
        } catch (Exception e) {
            return;
        }

        LocalDateTime referenceTime = game.getUpdatedAt() != null
                ? game.getUpdatedAt()
                : (game.getStartedAt() != null ? game.getStartedAt() : game.getCreatedAt());
        if (referenceTime == null) {
            return;
        }

        long elapsed = Duration.between(referenceTime, LocalDateTime.now()).toMillis();
        long activeClock = activeColor == Color.WHITE ? game.getWhiteTimeMs() : game.getBlackTimeMs();
        long remaining = activeClock - elapsed;
        if (remaining > 0L) {
            return;
        }

        // gracz przegrywa na czas. zerujemy zegar
        if (activeColor == Color.WHITE) {
            game.setWhiteTimeMs(0L);
            game.setStatus(GameStatus.BLACK_WINS);
        } else {
            game.setBlackTimeMs(0L);
            game.setStatus(GameStatus.WHITE_WINS);
        }
        game.setEndReason(EndReason.TIME_OUT);
        game.setEndedAt(LocalDateTime.now());
        game.setUpdatedAt(LocalDateTime.now());
        game.setDrawOfferedBy(null);
        gameRepository.save(game);

        kafkaProducer.publishGameCompletedEvent(game);

        if (broadcaster != null) {
            broadcaster.broadcast(game, new GameUpdateDto(
                    game.getCurrentFen(),
                    game.getWhiteTimeMs(),
                    game.getBlackTimeMs(),
                    game.getStatus(),
                    null
            ));
        }
    }
}
