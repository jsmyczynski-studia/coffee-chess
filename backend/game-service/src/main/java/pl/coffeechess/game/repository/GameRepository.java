package pl.coffeechess.game.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.coffeechess.game.model.entity.Game;
import pl.coffeechess.game.model.enums.GameStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {

    List<Game> findByWhitePlayerIdOrBlackPlayerIdOrderByStartedAtDesc(UUID whitePlayerId, UUID blackPlayerId);

    List<Game> findAllByStatus(GameStatus status);
}
