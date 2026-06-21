package pl.coffeechess.user.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pl.coffeechess.user.model.entity.EloHistory;
import pl.coffeechess.user.model.entity.GameHistory;
import pl.coffeechess.user.model.entity.User;
import pl.coffeechess.user.repository.EloHistoryRepository;
import pl.coffeechess.user.repository.GameHistoryRepository;
import pl.coffeechess.user.repository.UserRepository;
import pl.coffeechess.user.service.EloService;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameCompletedConsumer {

    private final UserRepository userRepository;
    private final EloHistoryRepository eloHistoryRepository;
    private final GameHistoryRepository gameHistoryRepository;
    private final EloService eloService;

    @KafkaListener(topics = "game-completed", groupId = "user-service-group")
    public void onGameCompleted(GameCompletedEvent event) {
        log.info("Received game-completed event for gameId={}", event.gameId());

        UUID gameId = UUID.fromString(event.gameId());
        UUID whitePlayerId = parsePlayerId(event.whitePlayerId());
        UUID blackPlayerId = parsePlayerId(event.blackPlayerId());

        if (whitePlayerId == null || blackPlayerId == null) {
            log.info("Skipping Elo update for gameId={} (not a ranked human-vs-human game)", gameId);
            return;
        }

        if ("ABORTED".equals(event.outcome())) {
            log.info("Skipping Elo update for aborted gameId={}", gameId);
            return;
        }

        User white = userRepository.findById(whitePlayerId).orElseThrow();
        User black = userRepository.findById(blackPlayerId).orElseThrow();

        double whiteScore = switch (event.outcome()) {
            case "WHITE_WINS" -> 1.0;
            case "BLACK_WINS" -> 0.0;
            case "DRAW" -> 0.5;
            default -> throw new IllegalArgumentException("Unsupported outcome for Elo: " + event.outcome());
        };

        int whiteChange = eloService.calculateEloChange(white.getEloRating(), black.getEloRating(), whiteScore);
        int blackChange = eloService.calculateEloChange(black.getEloRating(), white.getEloRating(), 1.0 - whiteScore);

        saveEloHistory(white, black, gameId, whiteChange, whiteScore);
        saveEloHistory(black, white, gameId, blackChange, 1.0 - whiteScore);

        white.setEloRating(Math.max(100, white.getEloRating() + whiteChange));
        black.setEloRating(Math.max(100, black.getEloRating() + blackChange));
        white.setGamesPlayed(white.getGamesPlayed() + 1);
        black.setGamesPlayed(black.getGamesPlayed() + 1);

        userRepository.save(white);
        userRepository.save(black);

        gameHistoryRepository.save(GameHistory.builder()
                .gameId(gameId)
                .whitePlayerId(whitePlayerId)
                .blackPlayerId(blackPlayerId)
                .outcome(GameHistory.GameOutcome.valueOf(event.outcome()))
                .whiteEloChange(whiteChange)
                .blackEloChange(blackChange)
                .timeControl(event.timeControl())
                .playedAt(LocalDateTime.now())
                .build());
    }

    private static UUID parsePlayerId(String playerId) {
        return playerId == null || playerId.isBlank() ? null : UUID.fromString(playerId);
    }

    private void saveEloHistory(User player, User opponent, UUID gameId, int eloChange, double score) {
        EloHistory.GameResult result = score == 1.0
                ? EloHistory.GameResult.WIN
                : score == 0.0 ? EloHistory.GameResult.LOSS : EloHistory.GameResult.DRAW;

        eloHistoryRepository.save(EloHistory.builder()
                .userId(player.getId())
                .gameId(gameId)
                .eloBefore(player.getEloRating())
                .eloChange(eloChange)
                .eloAfter(player.getEloRating() + eloChange)
                .opponentElo(opponent.getEloRating())
                .result(result)
                .build());
    }
}
