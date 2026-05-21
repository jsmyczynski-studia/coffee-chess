package pl.coffeechess.analysis.model.dto;

import pl.coffeechess.analysis.model.entity.MoveAnalysis;

public record MoveAnalysisDto(
        int moveNumber,
        String moveSan,
        String bestMove,
        Double evaluation,
        MoveAnalysis.MoveQuality moveQuality,
        String comment
) {}