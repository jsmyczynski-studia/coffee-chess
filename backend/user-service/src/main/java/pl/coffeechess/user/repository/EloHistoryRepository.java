package pl.coffeechess.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.coffeechess.user.model.entity.EloHistory;

import java.util.List;
import java.util.UUID;

@Repository
public interface EloHistoryRepository extends JpaRepository<EloHistory, UUID> {

    Page<EloHistory> findByUserIdOrderByRecordedAtDesc(UUID userId, Pageable pageable);
    List<EloHistory> findTop10ByUserIdOrderByRecordedAtDesc(UUID userId);
}