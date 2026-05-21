package pl.coffeechess.analysis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pl.coffeechess.analysis.client.ChessApiClient;
import pl.coffeechess.analysis.model.entity.GameAnalysis;
import pl.coffeechess.analysis.model.entity.MoveAnalysis;
import pl.coffeechess.analysis.repository.GameAnalysisRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportGeneratorService {

    private final GameAnalysisRepository gameAnalysisRepository;
    private final ChessApiClient chessApiClient;

    @Async
    public void generateReport(GameAnalysis analysis, String pgn) {
        try {
            // TODO: parsowanie PGN → lista FEN-ów per ruch
            // Każdy FEN wysyłamy do chess-api.com i oceniamy jakość ruchu
            log.info("Generating report for gameId={}", analysis.getGameId());

            // Placeholder — właściwa logika parsowania PGN w kolejnym etapie
            analysis.setStatus(GameAnalysis.AnalysisStatus.COMPLETED);
            analysis.setCompletedAt(LocalDateTime.now());
            gameAnalysisRepository.save(analysis);

        } catch (Exception e) {
            log.error("Analysis failed for gameId={}: {}", analysis.getGameId(), e.getMessage());
            analysis.setStatus(GameAnalysis.AnalysisStatus.FAILED);
            gameAnalysisRepository.save(analysis);
        }
    }

    private MoveAnalysis.MoveQuality evaluateMoveQuality(double playedEval, double bestEval) {
        double loss = bestEval - playedEval;
        if (loss >= 3.0)  return MoveAnalysis.MoveQuality.BLUNDER;
        if (loss >= 1.5)  return MoveAnalysis.MoveQuality.MISTAKE;
        if (loss >= 0.5)  return MoveAnalysis.MoveQuality.INACCURACY;
        if (loss <= -0.1) return MoveAnalysis.MoveQuality.BRILLIANT;
        return MoveAnalysis.MoveQuality.GOOD;
    }
}