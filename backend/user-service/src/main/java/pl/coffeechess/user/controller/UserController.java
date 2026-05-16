package pl.coffeechess.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.coffeechess.user.model.dto.UserProfileDto;
import pl.coffeechess.user.model.entity.GameHistory;
import pl.coffeechess.user.service.GameHistoryService;
import pl.coffeechess.user.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final GameHistoryService gameHistoryService;

    @GetMapping("/{nickname}/profile")
    public ResponseEntity<UserProfileDto> getProfile(@PathVariable String nickname) {
        return ResponseEntity.ok(userService.getProfileByNickname(nickname));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<Page<GameHistory>> getGameHistory(
            @PathVariable UUID id,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(gameHistoryService.getGameHistory(id, pageable));
    }
}