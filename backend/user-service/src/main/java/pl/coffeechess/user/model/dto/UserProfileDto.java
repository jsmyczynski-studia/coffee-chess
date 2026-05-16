package pl.coffeechess.user.model.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserProfileDto(
        UUID id,
        String nickname,
        int eloRating,
        int gamesPlayed,
        int gamesWon,
        int gamesLost,
        int gamesDrawn,
        String avatarUrl,
        LocalDateTime createdAt
) {}