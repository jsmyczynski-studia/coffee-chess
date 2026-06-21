package pl.coffeechess.game.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.coffeechess.game.kafka.GameCompletedProducer;
import pl.coffeechess.game.model.entity.Game;
import pl.coffeechess.game.model.enums.EndReason;
import pl.coffeechess.game.model.enums.GameStatus;
import pl.coffeechess.game.repository.GameRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlagFallCheckerTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private GameCompletedProducer kafkaProducer;

    @InjectMocks
    private FlagFallChecker flagFallChecker;

    @Test
    void checkOne_declaresFlagFallWhenActiveClockExpired() {
        UUID gameId = UUID.randomUUID();
        Game game = Game.builder()
                .id(gameId)
                .whitePlayerId(UUID.randomUUID())
                .blackPlayerId(UUID.randomUUID())
                .currentFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
                .status(GameStatus.IN_PROGRESS)
                .whiteTimeMs(3_000)
                .blackTimeMs(300_000)
                .updatedAt(LocalDateTime.now().minusSeconds(5))
                .build();

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        flagFallChecker.checkOne(gameId);

        assertThat(game.getStatus()).isEqualTo(GameStatus.BLACK_WINS);
        assertThat(game.getEndReason()).isEqualTo(EndReason.TIME_OUT);
        assertThat(game.getWhiteTimeMs()).isZero();
        verify(gameRepository).save(game);
        verify(kafkaProducer).publishGameCompletedEvent(game);
    }

    @Test
    void checkOne_doesNothingWhileActivePlayerStillHasTime() {
        UUID gameId = UUID.randomUUID();
        Game game = Game.builder()
                .id(gameId)
                .whitePlayerId(UUID.randomUUID())
                .blackPlayerId(UUID.randomUUID())
                .currentFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
                .status(GameStatus.IN_PROGRESS)
                .whiteTimeMs(60_000)
                .blackTimeMs(300_000)
                .updatedAt(LocalDateTime.now().minusSeconds(1))
                .build();

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        flagFallChecker.checkOne(gameId);

        assertThat(game.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(game.getEndReason()).isNull();
        verify(gameRepository, never()).save(game);
        verify(kafkaProducer, never()).publishGameCompletedEvent(game);
    }
}
