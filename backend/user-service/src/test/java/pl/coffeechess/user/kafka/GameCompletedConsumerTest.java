package pl.coffeechess.user.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.coffeechess.user.model.entity.User;
import pl.coffeechess.user.repository.EloHistoryRepository;
import pl.coffeechess.user.repository.GameHistoryRepository;
import pl.coffeechess.user.repository.UserRepository;
import pl.coffeechess.user.service.EloService;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameCompletedConsumerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EloHistoryRepository eloHistoryRepository;
    @Mock
    private GameHistoryRepository gameHistoryRepository;
    @Mock
    private EloService eloService;

    @InjectMocks
    private GameCompletedConsumer consumer;

    @Test
    void onGameCompleted_skipsBotGamesWithMissingOpponentId() {
        UUID gameId = UUID.randomUUID();
        UUID humanId = UUID.randomUUID();

        consumer.onGameCompleted(new GameCompletedEvent(
                gameId.toString(),
                humanId.toString(),
                null,
                "WHITE_WINS",
                "e2e4",
                "5+0",
                "CHECKMATE"
        ));

        verify(userRepository, never()).findById(any());
        verify(gameHistoryRepository, never()).save(any());
    }

    @Test
    void onGameCompleted_updatesEloForHumanVsHumanGame() {
        UUID gameId = UUID.randomUUID();
        UUID whiteId = UUID.randomUUID();
        UUID blackId = UUID.randomUUID();

        User white = User.builder().id(whiteId).eloRating(1200).gamesPlayed(0).build();
        User black = User.builder().id(blackId).eloRating(1200).gamesPlayed(0).build();

        when(userRepository.findById(whiteId)).thenReturn(Optional.of(white));
        when(userRepository.findById(blackId)).thenReturn(Optional.of(black));
        when(eloService.calculateEloChange(1200, 1200, 1.0)).thenReturn(16);
        when(eloService.calculateEloChange(1200, 1200, 0.0)).thenReturn(-16);

        consumer.onGameCompleted(new GameCompletedEvent(
                gameId.toString(),
                whiteId.toString(),
                blackId.toString(),
                "WHITE_WINS",
                "e2e4 e7e5",
                "10+0",
                "CHECKMATE"
        ));

        verify(userRepository).save(white);
        verify(userRepository).save(black);
        verify(gameHistoryRepository).save(any());
        verify(eloHistoryRepository, org.mockito.Mockito.times(2)).save(any());
    }
}
