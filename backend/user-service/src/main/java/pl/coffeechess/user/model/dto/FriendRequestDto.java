package pl.coffeechess.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FriendRequestDto(
        @NotBlank
        @Size(min = 3, max = 32)
        String nickname
) {}