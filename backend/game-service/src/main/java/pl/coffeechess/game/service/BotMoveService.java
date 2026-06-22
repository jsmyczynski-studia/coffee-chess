package pl.coffeechess.game.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.coffeechess.game.client.EngineClient;
import pl.coffeechess.game.model.board.GameBoard;
import pl.coffeechess.game.model.entity.Game;
import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.GameStatus;
import pl.coffeechess.game.repository.GameRepository;

import java.util.UUID;

// prowadzi ruch bota po ruchu człowieka
@Slf4j
@Service
@RequiredArgsConstructor
public class BotMoveService {

    private final GameRepository gameRepository;
    private final GameEngineService gameEngineService;
    private final EngineClient engineClient;

    @Autowired(required = false)
    private GameUpdateBroadcaster broadcaster;

    private final BotCommentService botCommentService;

    // jeśli to tura bota, pobiera ruch z silnika i go stosuje
    public void playBotTurnIfNeeded(UUID gameId) {
        try {
            BotMoveOutcome outcome = applyBotMove(gameId);
            if (outcome == null) {
                return;
            }
            if (broadcaster != null) {
                broadcaster.broadcast(outcome.game(), outcome.dto().dto());
            }
            botCommentService.commentOnBotMove(
                    outcome.game().getId(), outcome.move(), outcome.dto().capture());
        } catch (Exception e) {
            // wolne lub błędne wywołanie silnika nie może psuć stanu gry
            log.error("bot turn failed for game {}: {}", gameId, e.getMessage());
        }
    }

    @Transactional
    public BotMoveOutcome applyBotMove(UUID gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game doesn't exist"));

        if (!game.isVsBot() || game.getStatus() != GameStatus.IN_PROGRESS) {
            return null;
        }

        GameBoard board = new GameBoard(game.getCurrentFen());
        Color activeColor = board.getActiveColor();
        if (activeColor != game.getBotColor()) {
            return null;
        }

        String botMove = engineClient.getBotMove(game.getCurrentFen(), game.getBotDifficulty());
        if (botMove == null) {
            // brak ruchu z silnika - zostawiamy stan nienaruszony do ponowienia
            log.warn("engine returned no move for game {}", gameId);
            return null;
        }

        GameEngineService.MoveResult result = gameEngineService.processBotMove(gameId, botMove);
        if (result == null) {
            return null;
        }
        
        // Return an updated Game entity, we can just fetch it again since it was saved
        Game updatedGame = gameRepository.findById(gameId).orElse(game);
        return new BotMoveOutcome(updatedGame, result, botMove);
    }

    public record BotMoveOutcome(Game game, GameEngineService.MoveResult dto, String move) { }
}
