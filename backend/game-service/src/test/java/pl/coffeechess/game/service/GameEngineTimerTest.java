package pl.coffeechess.game.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.coffeechess.game.kafka.GameCompletedProducer;
import pl.coffeechess.game.model.dto.GameUpdateDto;
import pl.coffeechess.game.model.entity.Game;
import pl.coffeechess.game.model.entity.Move;
import pl.coffeechess.game.model.enums.EndReason;
import pl.coffeechess.game.model.enums.GameStatus;
import pl.coffeechess.game.repository.GameRepository;
import pl.coffeechess.game.repository.MoveRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameEngineTimerTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private MoveRepository moveRepository;

    @Mock
    private GameCompletedProducer kafkaProducer;

    @InjectMocks
    private GameEngineService gameEngineService;

    private Game game;
    private UUID gameId;
    private UUID whitePlayerId;

    @BeforeEach
    void setUp() {
        gameId = UUID.randomUUID();
        whitePlayerId = UUID.randomUUID();

        game = Game.builder()
                .id(gameId)
                .whitePlayerId(whitePlayerId)
                .blackPlayerId(UUID.randomUUID())
                .currentFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
                .status(GameStatus.IN_PROGRESS)
                .whiteTimeMs(5_000)
                .blackTimeMs(300_000)
                .updatedAt(LocalDateTime.now().minusSeconds(6))
                .moveListUci("")
                .build();
    }

    @Test
    void processMove_decrementsClockAndEndsGameOnFlagFall() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        GameUpdateDto result = gameEngineService.processMove(gameId, whitePlayerId, "e2e4");

        assertThat(result.status()).isEqualTo(GameStatus.BLACK_WINS);
        assertThat(game.getEndReason()).isEqualTo(EndReason.TIME_OUT);
        assertThat(game.getWhiteTimeMs()).isZero();
        verify(kafkaProducer).publishGameCompletedEvent(game);
        verify(moveRepository, never()).save(any());
    }

    @Test
    void processMove_decrementsActiveClockWhenMoveIsPlayedInTime() {
        game.setUpdatedAt(LocalDateTime.now().minus(500, ChronoUnit.MILLIS));
        game.setWhiteTimeMs(10_000);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(moveRepository.save(any())).thenReturn(new Move());

        gameEngineService.processMove(gameId, whitePlayerId, "e2e4");

        assertThat(game.getWhiteTimeMs()).isLessThan(10_000);
        assertThat(game.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(game.getEndReason()).isNull();
    }
}
