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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

        assertNotNull(result);
        assertEquals(GameStatus.IN_PROGRESS, result.status());
        assertFalse(result.fen().contains("w"));

        verify(gameRepository).save(game);
        verify(moveRepository).save(any(Move.class));
        verifyNoInteractions(kafkaProducer);
    }

    @Test
    void shouldThrowExceptionWhenNotPlayersTurn() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                gameEngineService.processMove(gameId, blackPlayerId, "e7e5"));

        assertEquals("It's not your turn!", exception.getMessage());
        verify(gameRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionForIllegalMoveOnBoard() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                gameEngineService.processMove(gameId, whitePlayerId, "g1g4"));

        assertEquals("Illegal move!", exception.getMessage());
    }

    @Test
    void shouldDetectCheckmateAndPublishEvent() {
        game.setCurrentFen("rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR b KQkq - 1 3");
        game.setStatus(GameStatus.IN_PROGRESS);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        String almostMateFen = "rnbqkbnr/ppppp2p/8/5pp1/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2";
        game.setCurrentFen(almostMateFen);

        GameUpdateDto result = gameEngineService.processMove(gameId, whitePlayerId, "d1h5");

        assertEquals(GameStatus.WHITE_WINS, result.status());
        verify(kafkaProducer).publishGameCompletedEvent(game);
    }

    @Test
    void shouldPromotePawnToQueen() {
        String fenBeforePromotion = "8/3P4/8/8/8/8/8/8 w - - 0 1";
        game.setCurrentFen(fenBeforePromotion);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(moveRepository.save(any(Move.class))).thenReturn(new Move());

        GameUpdateDto result = gameEngineService.processMove(gameId, whitePlayerId, "d7d8q");

        assertNotNull(result);
        assertTrue(result.fen().contains("Q"), "FEN powinien zawierać białego Hetmana (Q) po promocji");
        assertFalse(result.fen().contains("P"), "Biały pionek (P) powinien zniknąć z planszy po promocji");
        assertEquals("d7d8q", result.lastMove(), "Ostatni ruch powinien poprawnie zarejestrować żądanie promocji");
    }
}