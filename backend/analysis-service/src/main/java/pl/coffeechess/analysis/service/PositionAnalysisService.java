package pl.coffeechess.analysis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.coffeechess.analysis.client.CandidateMove;
import pl.coffeechess.analysis.client.ChessApiClient;
import pl.coffeechess.analysis.exception.EngineAnalysisException;
import pl.coffeechess.analysis.model.dto.PositionAnalysisResponse;
import pl.coffeechess.analysis.model.dto.PositionCandidateDto;
import pl.coffeechess.analysis.validation.FenValidator;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PositionAnalysisService {

    private static final int DEFAULT_VARIANTS = 5;

    private final ChessApiClient chessApiClient;

    public PositionAnalysisResponse analyzePosition(String fen, Integer variants) {
        String normalizedFen = FenValidator.normalizeAndValidate(fen);
        int requestedVariants = variants == null ? DEFAULT_VARIANTS : ChessApiClient.clampVariants(variants);

        List<CandidateMove> engineCandidates;
        try {
            engineCandidates = chessApiClient.getCandidateMoves(normalizedFen, requestedVariants).block();
        } catch (RuntimeException e) {
            throw new EngineAnalysisException("Failed to analyze position with chess engine.", e);
        }

        if (engineCandidates == null || engineCandidates.isEmpty()) {
            throw new EngineAnalysisException("Engine returned no candidate moves for the position.");
        }

        List<PositionCandidateDto> candidates = engineCandidates.stream()
                .map(this::toDto)
                .toList();

        return new PositionAnalysisResponse(normalizedFen, requestedVariants, candidates);
    }

    private PositionCandidateDto toDto(CandidateMove move) {
        String continuationFirst = move.continuation() == null || move.continuation().isEmpty()
                ? null
                : move.continuation().getFirst();
        return new PositionCandidateDto(
                move.san(),
                move.uci(),
                move.eval(),
                move.mate(),
                move.winChance(),
                continuationFirst
        );
    }
}
