package pl.coffeechess.game.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.coffeechess.game.model.entity.Move;

import java.util.List;
import java.util.UUID;

@Repository
public interface MoveRepository extends JpaRepository<Move, UUID> {

    List<Move> findByGameIdOrderByMoveNumberAscColorAsc(UUID gameId);
}
