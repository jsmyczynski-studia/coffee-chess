package pl.coffeechess.user.model.dto;

import pl.coffeechess.user.model.entity.Friendship;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Friendship view returned to the client. Includes the resolved nicknames so the UI can
 * display readable names instead of raw UUIDs.
 */
public record FriendshipDto(
        UUID id,
        UUID requesterId,
        String requesterNickname,
        UUID addresseeId,
        String addresseeNickname,
        Friendship.FriendshipStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
