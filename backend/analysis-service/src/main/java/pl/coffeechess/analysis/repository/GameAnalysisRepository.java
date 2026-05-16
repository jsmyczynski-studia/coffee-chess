package pl.coffeechess.analysis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.coffeechess.analysis.model.entity.GameAnalysis;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameAnalysisRepository extends JpaRepository<GameAnalysis, UUID> {

    Optional<GameAnalysis> findByGameId(UUID gameId);
}