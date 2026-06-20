package pl.coffeechess.game.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.coffeechess.game.model.dto.CreateGameRequest;
import pl.coffeechess.game.model.dto.GameDto;
import pl.coffeechess.game.model.dto.GameUpdateDto;
import pl.coffeechess.game.model.dto.MoveRequest;
import pl.coffeechess.game.model.entity.Game;
import pl.coffeechess.game.repository.GameRepository;
import pl.coffeechess.game.service.GameEngineService;
import pl.coffeechess.game.service.GameManagementService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameManagementService gameManagementService;
    private final GameEngineService gameEngineService;
    private final GameRepository gameRepository;

    @PostMapping
    public ResponseEntity<GameDto> createGame(@RequestBody CreateGameRequest request,
                                              @AuthenticationPrincipal Jwt jwt) {
        UUID creatorId = subjectAsUuid(jwt);
        Game game = gameManagementService.createGame(request, creatorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(GameDto.from(game));
    }

    @GetMapping("/{id}")
    public GameDto getGame(@PathVariable UUID id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Game doesn't exist"));
        return GameDto.from(game);
    }

    @GetMapping
    public List<GameDto> listGames(@RequestParam(required = false) UUID userId,
                                   @AuthenticationPrincipal Jwt jwt) {
        UUID query = userId != null ? userId : subjectAsUuid(jwt);
        return gameRepository
                .findByWhitePlayerIdOrBlackPlayerIdOrderByStartedAtDesc(query, query)
                .stream()
                .map(GameDto::from)
                .toList();
    }

    @PostMapping("/{id}/moves")
    public GameUpdateDto submitMove(@PathVariable UUID id,
                                    @RequestBody MoveRequest request,
                                    @AuthenticationPrincipal Jwt jwt) {
        if (request == null || request.move() == null || request.move().isBlank()) {
            throw new IllegalArgumentException("Invalid move format");
        }
        return gameEngineService.processMove(id, subjectAsUuid(jwt), request.move());
    }

    @PostMapping("/{id}/resign")
    public GameUpdateDto resign(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return gameManagementService.resign(id, subjectAsUuid(jwt));
    }

    @PostMapping("/{id}/draw/offer")
    public GameUpdateDto offerDraw(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return gameManagementService.offerDraw(id, subjectAsUuid(jwt));
    }

    @PostMapping("/{id}/draw/accept")
    public GameUpdateDto acceptDraw(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return gameManagementService.acceptDraw(id, subjectAsUuid(jwt));
    }

    @PostMapping("/{id}/draw/decline")
    public GameUpdateDto declineDraw(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return gameManagementService.declineDraw(id, subjectAsUuid(jwt));
    }

    private UUID subjectAsUuid(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            throw new IllegalStateException("Authenticated principal is required.");
        }
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("JWT subject is not a valid player UUID.");
        }
    }
}
