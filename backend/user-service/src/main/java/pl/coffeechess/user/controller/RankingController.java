package pl.coffeechess.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.coffeechess.user.model.dto.UserProfileDto;
import pl.coffeechess.user.service.UserService;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserProfileDto>> getRanking(
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(userService.getRanking(pageable));
    }
}