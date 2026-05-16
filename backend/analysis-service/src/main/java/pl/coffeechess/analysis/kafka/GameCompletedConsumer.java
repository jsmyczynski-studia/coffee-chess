package pl.coffeechess.analysis.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pl.coffeechess.analysis.service.AnalysisService;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameCompletedConsumer {

    private final AnalysisService analysisService;

    @KafkaListener(topics = "game-completed", groupId = "analysis-service-group")
    public void onGameCompleted(GameCompletedEvent event) {
        log.info("Analysis service received game-completed event for gameId={}", event.gameId());
        analysisService.triggerAnalysis(event.gameId(), event.pgn());
    }

    public record GameCompletedEvent(
            UUID gameId,
            UUID whitePlayerId,
            UUID blackPlayerId,
            String outcome,
            String pgn,
            String timeControl
    ) {}
}