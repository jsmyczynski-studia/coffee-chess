package pl.coffeechess.game.acceptance.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.But;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import pl.coffeechess.game.acceptance.GameScenarioContext;
import pl.coffeechess.game.client.EngineClient;
import pl.coffeechess.game.model.dto.CreateGameRequest;
import pl.coffeechess.game.model.dto.GameUpdateDto;
import pl.coffeechess.game.model.entity.Game;
import pl.coffeechess.game.model.enums.BotDifficulty;
import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.EndReason;
import pl.coffeechess.game.model.enums.GameStatus;
import pl.coffeechess.game.repository.GameRepository;
import pl.coffeechess.game.service.BotMoveService;
import pl.coffeechess.game.service.FlagFallChecker;
import pl.coffeechess.game.service.GameEngineService;
import pl.coffeechess.game.service.GameManagementService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class GameFlowSteps {

    @Autowired
    private GameScenarioContext context;

    @Autowired
    private GameManagementService gameManagementService;

    @Autowired
    private GameEngineService gameEngineService;

    @Autowired
    private BotMoveService botMoveService;

    @Autowired
    private FlagFallChecker flagFallChecker;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private EngineClient engineClient;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void resetScenario() {
        context.reset();
        when(engineClient.getBotMove(anyString(), any())).thenAnswer(invocation -> context.getEngineReply());
    }

    @Given("two registered players exist:")
    public void twoRegisteredPlayersExist(DataTable table) {
        for (Map<String, String> row : table.asMaps()) {
            context.registerPlayer(row.get("alias"), row.get("role"));
        }
    }

    @Given("a registered human player Alice")
    public void aRegisteredHumanPlayerAlice() {
        context.registerPlayer("Alice");
        context.registerPlayer("Bob");
    }

    @Given("an active game between Alice and Bob")
    public void anActiveGameBetweenAliceAndBob() {
        ensurePlayersRegistered();
        CreateGameRequest request = new CreateGameRequest(
                context.playerId("Alice"),
                context.playerId("Bob"),
                "5+0",
                null,
                false,
                null,
                null
        );
        context.setGame(gameManagementService.createGame(request, context.playerId("Alice")));
    }

    @Given("an active game between Alice and Bob with FEN {string}")
    public void anActiveGameWithFen(String fen) {
        anActiveGameBetweenAliceAndBob();
        Game game = context.getGame();
        game.setCurrentFen(fen);
        gameRepository.save(game);
        context.reloadGame(gameRepository);
    }

    @Given("a vs-bot game where Alice plays white")
    public void aVsBotGameWhereAlicePlaysWhite() {
        context.registerPlayer("Alice");
        CreateGameRequest request = new CreateGameRequest(
                null,
                null,
                "5+0",
                null,
                true,
                Color.WHITE,
                BotDifficulty.EASY
        );
        context.setGame(gameManagementService.createGame(request, context.playerId("Alice")));
    }

    @Given("the engine will reply with {string}")
    public void theEngineWillReplyWith(String move) {
        context.setEngineReply(move);
        when(engineClient.getBotMove(anyString(), any())).thenReturn(move);
    }

    @Given("Alice has played e2e4")
    public void aliceHasPlayedE2e4() {
        playMove("Alice", "e2e4");
    }

    @Given("Alice has almost no time left on the clock")
    public void aliceHasAlmostNoTimeLeft() {
        Game game = context.reloadGame(gameRepository);
        expireWhiteClock(game.getId(), 5_000, 6);
    }

    @Given("Alice's clock has already expired")
    public void aliceClockHasExpired() {
        Game game = context.reloadGame(gameRepository);
        expireWhiteClock(game.getId(), 3_000, 10);
    }

    private void expireWhiteClock(UUID gameId, long whiteTimeMs, long secondsAgo) {
        jdbcTemplate.update(
                "UPDATE games SET white_time_ms = ?, updated_at = ? WHERE id = ?",
                whiteTimeMs,
                java.sql.Timestamp.valueOf(LocalDateTime.now().minusSeconds(secondsAgo)),
                gameId
        );
        context.setGame(context.reloadGame(gameRepository));
    }

    @When("Alice starts a standard game against Bob")
    public void aliceStartsStandardGame() {
        anActiveGameBetweenAliceAndBob();
    }

    @When("{word} plays the following moves:")
    public void playerPlaysMoves(String player, DataTable table) {
        for (Map<String, String> row : table.asMaps()) {
            playMove(player, row.get("move"));
        }
    }

    @When("{word} plays {word}")
    public void playerPlaysSingleMove(String player, String move) {
        playMove(player, move);
        if (context.reloadGame(gameRepository).isVsBot() && "Alice".equals(player)) {
            when(engineClient.getBotMove(anyString(), any())).thenReturn(context.getEngineReply());
            botMoveService.playBotTurnIfNeeded(context.getGame().getId());
            context.reloadGame(gameRepository);
        }
    }

    @When("{word} attempts to play {string}")
    public void playerAttemptsMove(String player, String move) {
        String actualMove = move;
        try {
            gameEngineService.processMove(context.getGame().getId(), context.playerId(player), actualMove);
            context.setLastException(null);
        } catch (Exception ex) {
            context.setLastException(ex);
        }
        context.reloadGame(gameRepository);
    }

    @When("Alice resigns the game")
    public void aliceResigns() {
        GameUpdateDto update = gameManagementService.resign(context.getGame().getId(), context.playerId("Alice"));
        context.reloadGame(gameRepository);
        assertThat(update.status()).isNotEqualTo(GameStatus.IN_PROGRESS);
    }

    @When("Alice offers a draw")
    public void aliceOffersDraw() {
        gameManagementService.offerDraw(context.getGame().getId(), context.playerId("Alice"));
        context.reloadGame(gameRepository);
    }

    @When("Bob accepts the draw offer")
    public void bobAcceptsDraw() {
        gameManagementService.acceptDraw(context.getGame().getId(), context.playerId("Bob"));
        context.reloadGame(gameRepository);
    }

    @When("the flag fall checker runs")
    public void flagFallCheckerRuns() {
        flagFallChecker.checkOne(context.getGame().getId());
        context.reloadGame(gameRepository);
    }

    @Then("the game status should be {word}")
    public void gameStatusShouldBe(String status) {
        Game game = context.reloadGame(gameRepository);
        assertThat(game.getStatus()).isEqualTo(GameStatus.valueOf(status));
    }

    @Then("the end reason should be {word}")
    public void endReasonShouldBe(String reason) {
        Game game = context.reloadGame(gameRepository);
        assertThat(game.getEndReason()).isEqualTo(EndReason.valueOf(reason));
    }

    @Then("it should be Alice's turn")
    public void itShouldBeAliceTurn() {
        Game game = context.getGame();
        assertThat(game.getCurrentFen()).contains(" w ");
    }

    @Then("the move list should contain:")
    public void moveListShouldContain(DataTable table) {
        Game game = context.reloadGame(gameRepository);
        List<String> expected = table.asMaps().stream().map(row -> row.get("move")).toList();
        assertThat(game.getMoveListUci()).isNotNull();
        for (String move : expected) {
            assertThat(game.getMoveListUci()).contains(move);
        }
    }

    @Then("the move is rejected with message {string}")
    public void moveIsRejectedWithMessage(String message) {
        assertThat(context.getLastException())
                .isNotNull()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
    }

    @Then("the bot should reply with a legal move")
    public void botShouldReplyWithLegalMove() {
        Game game = context.reloadGame(gameRepository);
        assertThat(game.getMoveListUci()).contains("e7e5");
        assertThat(game.getMoveListUci()).contains("e2e4");
    }

    @And("the game should not have ended")
    public void gameShouldNotHaveEnded() {
        assertThat(context.getGame().getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    @But("the game should not be a checkmate")
    public void gameShouldNotBeCheckmate() {
        assertThat(context.getGame().getEndReason()).isNotEqualTo(EndReason.CHECKMATE);
    }

    private void playMove(String playerAlias, String move) {
        UUID playerId = context.playerId(playerAlias);
        gameEngineService.processMove(context.getGame().getId(), playerId, move);
        context.reloadGame(gameRepository);
        context.setLastException(null);
    }

    private void ensurePlayersRegistered() {
        context.registerPlayer("Alice");
        context.registerPlayer("Bob");
    }
}
