package pl.coffeechess.game_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.coffeechess.game_service.model.entity.Game;

import java.util.UUID;

public interface GameRepository extends JpaRepository<Game, UUID> {
}
