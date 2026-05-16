package pl.coffeechess.analysis.model.dto;

import pl.coffeechess.analysis.model.entity.GameAnalysis;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AnalysisReportDto(
        UUID gameId,
        GameAnalysis.AnalysisStatus status,
        int blunders,
        int mistakes,
        int inaccuracies,
        Double whiteAccuracy,
        Double blackAccuracy,
        String summary,
        List<MoveAnalysisDto> moves,
        LocalDateTime completedAt
) {}