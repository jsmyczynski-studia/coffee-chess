package pl.coffeechess.analysis.model.dto;

import java.util.List;

public record PositionAnalysisResponse(
        String fen,
        int variants,
        List<PositionCandidateDto> candidates
) {}
