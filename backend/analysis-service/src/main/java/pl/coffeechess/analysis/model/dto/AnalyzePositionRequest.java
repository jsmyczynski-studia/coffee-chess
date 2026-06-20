package pl.coffeechess.analysis.model.dto;

public record AnalyzePositionRequest(
        String fen,
        Integer variants
) {}
