package pl.coffeechess.analysis.client;

import java.util.List;

public record CandidateMove(
        String san,
        String uci,
        Double eval,
        Integer mate,
        Double winChance,
        List<String> continuation
) {}
