package pl.coffeechess.game.model.dto;

import pl.coffeechess.game.model.entity.ChatMessage;
import pl.coffeechess.game.model.enums.ChatMessageType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatMessageDto(
        UUID id,
        UUID gameId,
        UUID authorId,
        ChatMessageType type,
        String text,
        LocalDateTime createdAt
) {
    public static ChatMessageDto from(ChatMessage message) {
        return new ChatMessageDto(
                message.getId(),
                message.getGameId(),
                message.getAuthorId(),
                message.getType(),
                message.getText(),
                message.getCreatedAt()
        );
    }
}
