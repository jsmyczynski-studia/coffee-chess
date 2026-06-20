package pl.coffeechess.analysis.model.dto;

public record PositionCandidateDto(
        String san,
        String uci,
        Double eval,
        Integer mate,
        Double winChance,
        String continuationFirst
) {}
