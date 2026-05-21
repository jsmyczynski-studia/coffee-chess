package pl.coffeechess.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pl.coffeechess.user.model.dto.FriendRequestDto;
import pl.coffeechess.user.model.entity.Friendship;
import pl.coffeechess.user.service.FriendService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/request")
    public ResponseEntity<Void> sendRequest(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody FriendRequestDto dto) {
        UUID requesterId = UUID.fromString(jwt.getSubject());
        friendService.sendFriendRequest(requesterId, dto.nickname());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/request/{friendshipId}/accept")
    public ResponseEntity<Void> acceptRequest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID friendshipId) {
        UUID addresseeId = UUID.fromString(jwt.getSubject());
        friendService.acceptFriendRequest(friendshipId, addresseeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Friendship>> getFriends(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(friendService.getFriends(userId));
    }

    @GetMapping("/invites/pending")
    public ResponseEntity<List<Friendship>> getPendingInvites(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(friendService.getPendingInvites(userId));
    }
}