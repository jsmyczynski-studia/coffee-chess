package pl.coffeechess.game.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.coffeechess.game.client.EngineClient;
import pl.coffeechess.game.model.board.GameBoard;
import pl.coffeechess.game.model.entity.Game;
import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.GameStatus;
import pl.coffeechess.game.repository.GameRepository;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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

    @Autowired(required = false)
    private TrashTalkService trashTalkService;

    @Autowired(required = false)
    private ChatService chatService;

    @Value("${bot.trash-talk.enabled:true}")
    private boolean trashTalkEnabled;

    @Value("${bot.trash-talk.probability:0.4}")
    private double trashTalkProbability;

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
            maybeTrashTalk(outcome);
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

        GameEngineService.MoveResult result = gameEngineService.applyMove(game, board, botMove, activeColor);
        return new BotMoveOutcome(game, result, botMove);
    }

    // czasem bot wrzuca zaczepny komentarz - po biciu lub losowo
    private void maybeTrashTalk(BotMoveOutcome outcome) {
        if (!trashTalkEnabled || trashTalkService == null || chatService == null) {
            return;
        }
        boolean trigger = outcome.dto().capture()
                || ThreadLocalRandom.current().nextDouble() < trashTalkProbability;
        if (!trigger) {
            return;
        }
        try {
            String context = buildContext(outcome);
            String remark = trashTalkService.generateRemark(context);
            if (remark != null && !remark.isBlank()) {
                chatService.postBotMessage(outcome.game().getId(), remark);
            }
        } catch (Exception e) {
            // błąd LLM nigdy nie może blokować ani psuć ruchu
            log.error("trash talk failed for game {}: {}", outcome.game().getId(), e.getMessage());
        }
    }

    private String buildContext(BotMoveOutcome outcome) {
        String captured = outcome.dto().capture() ? " and captured a piece" : "";
        return "the bot just played " + outcome.move() + captured
                + " against its opponent give a short trash talk remark";
    }

    public record BotMoveOutcome(Game game, GameEngineService.MoveResult dto, String move) { }
}
