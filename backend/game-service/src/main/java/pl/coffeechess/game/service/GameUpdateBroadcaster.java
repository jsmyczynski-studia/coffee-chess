package pl.coffeechess.game.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import pl.coffeechess.game.model.dto.GameUpdateDto;
import pl.coffeechess.game.model.entity.Game;

import java.util.UUID;

// wysyła stan gry przez websocket
@Component
@RequiredArgsConstructor
public class GameUpdateBroadcaster {

    public static final String GAME_TOPIC_PREFIX = "/topic/games/";

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcast(UUID gameId, GameUpdateDto dto) {
        if (gameId == null) {
            return;
        }
        messagingTemplate.convertAndSend(GAME_TOPIC_PREFIX + gameId, dto);
    }

    public void broadcast(Game game, GameUpdateDto dto) {
        if (game == null) {
            return;
        }
        broadcast(game.getId(), dto);
    }
}
