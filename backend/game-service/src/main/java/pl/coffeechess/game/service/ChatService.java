package pl.coffeechess.game.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.coffeechess.game.model.dto.ChatMessageDto;
import pl.coffeechess.game.model.entity.ChatMessage;
import pl.coffeechess.game.model.entity.Game;
import pl.coffeechess.game.model.enums.ChatMessageType;
import pl.coffeechess.game.repository.ChatMessageRepository;
import pl.coffeechess.game.repository.GameRepository;

import java.util.List;
import java.util.UUID;

// czat w grze - niezależny od LLM, działa też dla gier człowiek vs człowiek
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final GameRepository gameRepository;

    @Autowired(required = false)
    private GameUpdateBroadcaster broadcaster;

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getHistory(UUID gameId) {
        return chatMessageRepository.findByGameIdOrderByCreatedAtAsc(gameId)
                .stream()
                .map(ChatMessageDto::from)
                .toList();
    }

    @Transactional
    public ChatMessageDto sendUserMessage(UUID gameId, UUID authorId, String text) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game doesn't exist"));
        if (!isParticipant(game, authorId)) {
            throw new IllegalArgumentException("Player is not a participant of this game.");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Message text is required.");
        }
        return persistAndBroadcast(gameId, authorId, ChatMessageType.USER, text.trim());
    }

    // zapisuje i rozsyła wiadomość bota (LLM)
    @Transactional
    public ChatMessageDto postBotMessage(UUID gameId, String text) {
        return persistAndBroadcast(gameId, null, ChatMessageType.BOT_LLM, text);
    }

    private ChatMessageDto persistAndBroadcast(UUID gameId, UUID authorId, ChatMessageType type, String text) {
        ChatMessage message = ChatMessage.builder()
                .gameId(gameId)
                .authorId(authorId)
                .type(type)
                .text(text)
                .build();
        chatMessageRepository.save(message);

        ChatMessageDto dto = ChatMessageDto.from(message);
        if (broadcaster != null) {
            broadcaster.broadcastChat(gameId, dto);
        }
        return dto;
    }

    private boolean isParticipant(Game game, UUID playerId) {
        if (playerId == null) {
            return false;
        }
        return playerId.equals(game.getWhitePlayerId()) || playerId.equals(game.getBlackPlayerId());
    }
}
