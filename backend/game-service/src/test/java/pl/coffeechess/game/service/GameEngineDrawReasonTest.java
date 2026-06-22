package pl.coffeechess.game.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.coffeechess.game.kafka.GameCompletedProducer;
import pl.coffeechess.game.model.board.GameBoard;
import pl.coffeechess.game.model.entity.Game;
import pl.coffeechess.game.model.entity.Move;
import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.EndReason;
import pl.coffeechess.game.model.enums.GameStatus;
import pl.coffeechess.game.repository.GameRepository;
import pl.coffeechess.game.repository.MoveRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameEngineDrawReasonTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private MoveRepository moveRepository;

    @Mock
    private GameCompletedProducer kafkaProducer;

    @InjectMocks
    private GameEngineService gameEngineService;

    private Game game;

    @BeforeEach
    void setUp() {
        game = Game.builder()
                .id(UUID.randomUUID())
                .whitePlayerId(UUID.randomUUID())
                .blackPlayerId(UUID.randomUUID())
                .status(GameStatus.IN_PROGRESS)
                .whiteTimeMs(300_000)
                .blackTimeMs(300_000)
                .startedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .moveListUci("")
                .halfmoveClock(0L)
                .positionHistory("")
                .build();
        when(moveRepository.save(any(Move.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void applyMove_labelsStalemateDraw() {
        game.setCurrentFen("k7/8/2K5/1Q6/8/8/8/8 w - - 0 1");

        gameEngineService.applyMove(game, new GameBoard(game.getCurrentFen()), "b5b6", Color.WHITE);

        assertThat(game.getStatus()).isEqualTo(GameStatus.DRAW);
        assertThat(game.getEndReason()).isEqualTo(EndReason.STALEMATE);
        verify(kafkaProducer).publishGameCompletedEvent(game);
    }

    @Test
    void applyMove_labelsInsufficientMaterialDraw() {
        game.setCurrentFen("4k3/8/8/8/8/8/8/4K3 w - - 0 1");

        gameEngineService.applyMove(game, new GameBoard(game.getCurrentFen()), "e1e2", Color.WHITE);

        assertThat(game.getStatus()).isEqualTo(GameStatus.DRAW);
        assertThat(game.getEndReason()).isEqualTo(EndReason.INSUFFICIENT_MATERIAL);
    }

    @Test
    void applyMove_labelsFiftyMoveRuleDraw() {
        game.setCurrentFen("8/8/8/8/8/8/4K2R/7k w - - 0 1");
        game.setHalfmoveClock(99L);

        gameEngineService.applyMove(game, new GameBoard(game.getCurrentFen()), "h2h3", Color.WHITE);

        assertThat(game.getStatus()).isEqualTo(GameStatus.DRAW);
        assertThat(game.getEndReason()).isEqualTo(EndReason.FIFTY_MOVE_RULE);
    }

    @Test
    void applyMove_labelsThreefoldRepetitionDraw() {
        game.setCurrentFen("8/8/8/8/8/8/4K2R/7k w - - 0 1");
        game.setPositionHistory(
                "8/8/8/8/8/7R/4K3/7k b - -;8/8/8/8/8/7R/4K3/7k b - -");

        gameEngineService.applyMove(game, new GameBoard(game.getCurrentFen()), "h2h3", Color.WHITE);

        assertThat(game.getStatus()).isEqualTo(GameStatus.DRAW);
        assertThat(game.getEndReason()).isEqualTo(EndReason.THREEFOLD_REPETITION);
    }

    @Test
    void applyMove_labelsCheckmateWin() {
        game.setCurrentFen("rnbqkbnr/ppppp2p/8/5pp1/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2");

        gameEngineService.applyMove(game, new GameBoard(game.getCurrentFen()), "d1h5", Color.WHITE);

        assertThat(game.getStatus()).isEqualTo(GameStatus.WHITE_WINS);
        assertThat(game.getEndReason()).isEqualTo(EndReason.CHECKMATE);
    }
}
