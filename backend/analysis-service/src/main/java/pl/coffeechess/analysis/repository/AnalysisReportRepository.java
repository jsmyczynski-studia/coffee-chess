package pl.coffeechess.analysis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.coffeechess.analysis.model.entity.AnalysisReport;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, UUID> {

    Optional<AnalysisReport> findByGameAnalysisId(UUID gameAnalysisId);
}