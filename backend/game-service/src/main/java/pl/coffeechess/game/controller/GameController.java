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
import pl.coffeechess.game.model.dto.ChatMessageDto;
import pl.coffeechess.game.model.dto.CreateGameRequest;
import pl.coffeechess.game.model.dto.GameDto;
import pl.coffeechess.game.model.dto.GameUpdateDto;
import pl.coffeechess.game.model.dto.MoveDto;
import pl.coffeechess.game.model.dto.MoveRequest;
import pl.coffeechess.game.model.dto.SendChatRequest;
import pl.coffeechess.game.model.entity.Game;
import pl.coffeechess.game.repository.GameRepository;
import pl.coffeechess.game.service.BotMoveService;
import pl.coffeechess.game.service.ChatService;
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
    private final BotMoveService botMoveService;
    private final ChatService chatService;
    private final GameRepository gameRepository;

    @PostMapping
    public ResponseEntity<GameDto> createGame(@RequestBody CreateGameRequest request,
                                              @AuthenticationPrincipal Jwt jwt) {
        UUID creatorId = subjectAsUuid(jwt);
        Game game = gameManagementService.createGame(request, creatorId);
        // Jeśli bot gra białymi, musi wykonać pierwszy ruch od razu — inaczej czeka
        // bez końca, bo człowiek (czarne) nie może ruszyć się pierwszy.
        botMoveService.playBotTurnIfNeeded(game.getId());
        Game latest = gameRepository.findById(game.getId()).orElse(game);
        return ResponseEntity.status(HttpStatus.CREATED).body(GameDto.from(latest));
    }

    @GetMapping("/{id}")
    public GameDto getGame(@PathVariable UUID id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Game doesn't exist"));
        return GameDto.from(game);
    }

    @GetMapping("/{id}/moves")
    public List<MoveDto> getMoves(@PathVariable UUID id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Game doesn't exist"));
        return game.getMoves().stream().map(MoveDto::from).toList();
    }

    @GetMapping("/{id}/legal-moves")
    public List<String> getLegalMoves(@PathVariable UUID id,
                                      @RequestParam String from,
                                      @AuthenticationPrincipal Jwt jwt) {
        return gameEngineService.getLegalDestinations(id, subjectAsUuid(jwt), from);
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
        gameEngineService.processMove(id, subjectAsUuid(jwt), request.move());
        // po ruchu człowieka bot odpowiada automatycznie
        botMoveService.playBotTurnIfNeeded(id);
        // Zwracamy najnowszy stan gry (po ewentualnym ruchu bota), aby klient nie musiał
        // polegać wyłącznie na websocket — pojedyncze wywołanie HTTP daje pełny aktualny stan.
        Game latest = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Game doesn't exist"));
        return GameUpdateDto.from(latest);
    }

    @GetMapping("/{id}/chat")
    public List<ChatMessageDto> getChat(@PathVariable UUID id) {
        return chatService.getHistory(id);
    }

    @PostMapping("/{id}/chat")
    public ChatMessageDto sendChat(@PathVariable UUID id,
                                   @RequestBody SendChatRequest request,
                                   @AuthenticationPrincipal Jwt jwt) {
        String text = request == null ? null : request.text();
        return chatService.sendUserMessage(id, subjectAsUuid(jwt), text);
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
