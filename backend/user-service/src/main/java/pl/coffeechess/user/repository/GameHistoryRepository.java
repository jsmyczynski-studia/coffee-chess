package pl.coffeechess.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.coffeechess.user.model.entity.GameHistory;

import java.util.UUID;

@Repository
public interface GameHistoryRepository extends JpaRepository<GameHistory, UUID> {

    @Query("SELECT g FROM GameHistory g WHERE g.whitePlayerId = :userId OR g.blackPlayerId = :userId ORDER BY g.playedAt DESC")
    Page<GameHistory> findAllByPlayerId(UUID userId, Pageable pageable);
}