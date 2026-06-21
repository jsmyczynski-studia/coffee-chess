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
import pl.coffeechess.game.model.enums.GameStatus;
import pl.coffeechess.game.repository.GameRepository;
import pl.coffeechess.game.repository.MoveRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameEngineServiceTest {

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
    private UUID blackPlayerId;

    @BeforeEach
    void setUp() {
        gameId = UUID.randomUUID();
        whitePlayerId = UUID.randomUUID();
        blackPlayerId = UUID.randomUUID();

        game = Game.builder()
                .id(gameId)
                .whitePlayerId(whitePlayerId)
                .blackPlayerId(blackPlayerId)
                .currentFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
                .status(GameStatus.IN_PROGRESS)
                .whiteTimeMs(300_000)
                .blackTimeMs(300_000)
                .updatedAt(LocalDateTime.now())
                .moveListUci("")
                .build();
    }

    @Test
    void shouldProcessValidMoveAndChangeTurn() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(moveRepository.save(any(Move.class))).thenReturn(new Move());

        GameUpdateDto result = gameEngineService.processMove(gameId, whitePlayerId, "e2e4");

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(result.fen()).doesNotContain(" w ");

        verify(gameRepository).save(game);
        verify(moveRepository).save(any(Move.class));
        verifyNoInteractions(kafkaProducer);
    }

    @Test
    void shouldThrowExceptionWhenNotPlayersTurn() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        assertThatThrownBy(() -> gameEngineService.processMove(gameId, blackPlayerId, "e7e5"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("It's not your turn!");

        verify(gameRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionForIllegalMoveOnBoard() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        assertThatThrownBy(() -> gameEngineService.processMove(gameId, whitePlayerId, "g1g4"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Illegal move!");
    }

    @Test
    void shouldDetectCheckmateAndPublishEvent() {
        String almostMateFen = "rnbqkbnr/ppppp2p/8/5pp1/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2";
        game.setCurrentFen(almostMateFen);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(moveRepository.save(any(Move.class))).thenReturn(new Move());

        GameUpdateDto result = gameEngineService.processMove(gameId, whitePlayerId, "d1h5");

        assertThat(result.status()).isEqualTo(GameStatus.WHITE_WINS);
        verify(kafkaProducer).publishGameCompletedEvent(game);
    }

    @Test
    void shouldPromotePawnToQueen() {
        String fenBeforePromotion = "8/3P4/8/8/8/8/8/8 w - - 0 1";
        game.setCurrentFen(fenBeforePromotion);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(moveRepository.save(any(Move.class))).thenReturn(new Move());

        GameUpdateDto result = gameEngineService.processMove(gameId, whitePlayerId, "d7d8q");

        assertThat(result.fen()).contains("Q").doesNotContain("P");
        assertThat(result.lastMove()).isEqualTo("d7d8q");
    }
}
