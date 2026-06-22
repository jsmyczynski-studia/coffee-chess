package pl.coffeechess.game.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pl.coffeechess.game.analysis.MoveQuality;
import pl.coffeechess.game.analysis.MoveQualityClassifier;
import pl.coffeechess.game.client.EngineClient;
import pl.coffeechess.game.model.enums.BotDifficulty;
import pl.coffeechess.game.model.enums.Color;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotCommentService {

    private final EngineClient engineClient;
    private final TrashTalkService trashTalkService;
    private final ChatService chatService;

    @Value("${bot.trash-talk.enabled:true}")
    private boolean enabled;

    @Value("${bot.trash-talk.probability:0.4}")
    private double probability;

    @Async
    public void commentOnBotMove(UUID gameId, String move, boolean capture) {
        //if (!enabled || (!capture && ThreadLocalRandom.current().nextDouble() >= probability)) {
         //   return;
        //}
        //try {
            //String captured = capture ? " and captured a piece" : "";
            //String comment = trashTalkService.generateRemark(
             //       "the bot just played " + move + captured + " give a short trash talk remark");
            //if (comment != null && !comment.isBlank()) {
            //    chatService.postBotMessage(gameId, comment);
            //}
        //} catch (Exception e) {
        //    log.warn("Could not comment on bot move in game {}: {}", gameId, e.getMessage());
        //}
    }

    @Async
    public void commentOnPlayerMove(UUID gameId, String fenBefore, String move,
                                    Color playerColor, BotDifficulty difficulty) {
        if (!enabled) return;
        try {
            EngineClient.EngineResponse best = engineClient.analyzePosition(fenBefore, difficulty);
            EngineClient.EngineResponse played = engineClient.analyzeMove(fenBefore, difficulty, move);
            if (best == null || played == null) return;

            MoveQuality quality = classify(best, played, playerColor);
            String comment = trashTalkService.generateMoveComment(move, quality.name());
            if (comment != null && !comment.isBlank()) {
                chatService.postBotMessage(gameId, comment);
            }
        } catch (Exception e) {
            log.warn("Could not comment on player move in game {}: {}", gameId, e.getMessage());
        }
    }

    @Async
    public void replyToPlayerMessage(UUID gameId, String message) {
        if (!enabled) return;
        try {
            String reply = trashTalkService.generateChatReply(message);
            if (reply != null && !reply.isBlank()) {
                chatService.postBotMessage(gameId, reply);
            }
        } catch (Exception e) {
            log.warn("Could not reply to chat in game {}: {}", gameId, e.getMessage());
        }
    }

    private MoveQuality classify(EngineClient.EngineResponse best,
                                 EngineClient.EngineResponse played,
                                 Color playerColor) {
        if (best.eval() == null || played.eval() == null) {
            return best.move().equalsIgnoreCase(played.move()) ? MoveQuality.BRILLIANT : MoveQuality.GOOD;
        }
        double perspective = playerColor == Color.WHITE ? 1.0 : -1.0;
        return MoveQualityClassifier.classify(played.eval() * perspective, best.eval() * perspective);
    }
}
