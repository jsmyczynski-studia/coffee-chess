package pl.coffeechess.user.model.dto;

import java.util.UUID;

public record RankingEntryDto(
        int position,
        UUID userId,
        String nickname,
        int eloRating,
        int gamesPlayed,
        String avatarUrl
) {}