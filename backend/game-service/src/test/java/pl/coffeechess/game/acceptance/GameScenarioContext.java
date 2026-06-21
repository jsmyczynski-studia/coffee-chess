package pl.coffeechess.game.acceptance;

import io.cucumber.spring.ScenarioScope;
import org.springframework.stereotype.Component;
import pl.coffeechess.game.model.entity.Game;
import pl.coffeechess.game.repository.GameRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@ScenarioScope
public class GameScenarioContext {

    private final Map<String, UUID> players = new HashMap<>();
    private Game game;
    private Exception lastException;
    private String sanitizedComment;
    private String engineReply = "e7e5";

    public void reset() {
        players.clear();
        game = null;
        lastException = null;
        sanitizedComment = null;
        engineReply = "e7e5";
    }

    public void registerPlayer(String alias, String role) {
        players.putIfAbsent(alias, UUID.randomUUID());
    }

    public void registerPlayer(String alias) {
        players.putIfAbsent(alias, UUID.randomUUID());
    }

    public UUID playerId(String alias) {
        UUID id = players.get(alias);
        if (id == null) {
            throw new IllegalStateException("Unknown player alias: " + alias);
        }
        return id;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Exception getLastException() {
        return lastException;
    }

    public void setLastException(Exception lastException) {
        this.lastException = lastException;
    }

    public String getSanitizedComment() {
        return sanitizedComment;
    }

    public void setSanitizedComment(String sanitizedComment) {
        this.sanitizedComment = sanitizedComment;
    }

    public String getEngineReply() {
        return engineReply;
    }

    public void setEngineReply(String engineReply) {
        this.engineReply = engineReply;
    }

    public Game reloadGame(GameRepository repository) {
        if (game == null) {
            return null;
        }
        game = repository.findById(game.getId()).orElseThrow();
        return game;
    }
}
