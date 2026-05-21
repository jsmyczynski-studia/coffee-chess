package pl.coffeechess.analysis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.coffeechess.analysis.client.ChessApiClient;
import pl.coffeechess.analysis.model.dto.AnalysisReportDto;
import pl.coffeechess.analysis.model.dto.MoveAnalysisDto;
import pl.coffeechess.analysis.model.entity.GameAnalysis;
import pl.coffeechess.analysis.model.entity.MoveAnalysis;
import pl.coffeechess.analysis.repository.GameAnalysisRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final GameAnalysisRepository gameAnalysisRepository;
    private final ChessApiClient chessApiClient;
    private final ReportGeneratorService reportGeneratorService;

    @Transactional
    public void triggerAnalysis(UUID gameId, String pgn) {
        if (gameAnalysisRepository.findByGameId(gameId).isPresent()) {
            log.info("Analysis already exists for gameId={}", gameId);
            return;
        }

        GameAnalysis analysis = GameAnalysis.builder()
                .gameId(gameId)
                .status(GameAnalysis.AnalysisStatus.IN_PROGRESS)
                .build();

        gameAnalysisRepository.save(analysis);
        log.info("Started analysis for gameId={}", gameId);

        // Analiza asynchroniczna — każdy ruch wysyłany do chess-api.com
        reportGeneratorService.generateReport(analysis, pgn);
    }

    public AnalysisReportDto getReport(UUID gameId) {
        GameAnalysis analysis = gameAnalysisRepository.findByGameId(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Analysis not found for gameId: " + gameId));

        List<MoveAnalysisDto> moves = analysis.getMoveAnalyses().stream()
                .map(m -> new MoveAnalysisDto(
                        m.getMoveNumber(),
                        m.getMoveSan(),
                        m.getBestMove(),
                        m.getEvaluation(),
                        m.getMoveQuality(),
                        m.getComment()))
                .toList();

        return new AnalysisReportDto(
                analysis.getGameId(),
                analysis.getStatus(),
                analysis.getBlunders(),
                analysis.getMistakes(),
                analysis.getInaccuracies(),
                analysis.getWhiteAccuracy(),
                analysis.getBlackAccuracy(),
                null,
                moves,
                analysis.getCompletedAt()
        );
    }
}