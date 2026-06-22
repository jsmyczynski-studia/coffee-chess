package pl.coffeechess.game.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.coffeechess.game.client.EngineClient;
import pl.coffeechess.game.model.enums.BotDifficulty;
import pl.coffeechess.game.model.enums.Color;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BotCommentServiceTest {

    @Mock
    private EngineClient engineClient;
    @Mock
    private TrashTalkService trashTalkService;
    @Mock
    private ChatService chatService;

    @InjectMocks
    private BotCommentService botCommentService;

    @BeforeEach
    void enableComments() {
        ReflectionTestUtils.setField(botCommentService, "enabled", true);
        ReflectionTestUtils.setField(botCommentService, "probability", 1.0);
    }

    @Test
    void commentsOnPlayerMoveUsingEngineQuality() {
        UUID gameId = UUID.randomUUID();
        String fen = "position-before-move";
        EngineClient.EngineResponse best = new EngineClient.EngineResponse("d2d4", "d4", 1.0, "best");
        EngineClient.EngineResponse played = new EngineClient.EngineResponse("e2e4", "e4", -1.0, "played");

        when(engineClient.analyzePosition(fen, BotDifficulty.MEDIUM)).thenReturn(best);
        when(engineClient.analyzeMove(fen, BotDifficulty.MEDIUM, "e2e4")).thenReturn(played);
        when(trashTalkService.generateMoveComment("e2e4", "MISTAKE"))
                .thenReturn("mozna bylo zagrac lepiej");

        botCommentService.commentOnPlayerMove(
                gameId, fen, "e2e4", Color.WHITE, BotDifficulty.MEDIUM);

        verify(chatService).postBotMessage(gameId, "mozna bylo zagrac lepiej");
    }

    @Test
    void repliesToPlayerChatMessage() {
        UUID gameId = UUID.randomUUID();
        when(trashTalkService.generateChatReply("dobry ruch"))
                .thenReturn("jeszcze zobaczymy");

        botCommentService.replyToPlayerMessage(gameId, "dobry ruch");

        verify(chatService).postBotMessage(gameId, "jeszcze zobaczymy");
    }

    @Test
    void commentsAfterBotMove() {
        UUID gameId = UUID.randomUUID();
        when(trashTalkService.generateRemark(
                "the bot just played e7e5 and captured a piece give a short trash talk remark"))
                .thenReturn("to musialo bolec");

        botCommentService.commentOnBotMove(gameId, "e7e5", true);

        verify(chatService).postBotMessage(gameId, "to musialo bolec");
    }
}
