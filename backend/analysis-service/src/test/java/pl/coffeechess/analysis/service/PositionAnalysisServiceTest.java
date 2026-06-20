package pl.coffeechess.analysis.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.coffeechess.analysis.client.CandidateMove;
import pl.coffeechess.analysis.client.ChessApiClient;
import pl.coffeechess.analysis.exception.EngineAnalysisException;
import pl.coffeechess.analysis.exception.InvalidFenException;
import pl.coffeechess.analysis.model.dto.PositionAnalysisResponse;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PositionAnalysisServiceTest {

    private static final String START_FEN =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    @Mock
    private ChessApiClient chessApiClient;

    @InjectMocks
    private PositionAnalysisService positionAnalysisService;

    @Test
    void analyzePosition_returnsMappedCandidates() {
        when(chessApiClient.getCandidateMoves(eq(START_FEN), eq(3)))
                .thenReturn(Mono.just(List.of(
                        new CandidateMove("e4", "e2e4", 0.27, null, 52.1, List.of("e7e5", "g1f3")),
                        new CandidateMove("d4", "d2d4", 0.22, null, 51.4, List.of())
                )));

        PositionAnalysisResponse response = positionAnalysisService.analyzePosition(START_FEN, 3);

        assertThat(response.fen()).isEqualTo(START_FEN);
        assertThat(response.variants()).isEqualTo(3);
        assertThat(response.candidates()).hasSize(2);
        assertThat(response.candidates().get(0).san()).isEqualTo("e4");
        assertThat(response.candidates().get(0).uci()).isEqualTo("e2e4");
        assertThat(response.candidates().get(0).eval()).isEqualTo(0.27);
        assertThat(response.candidates().get(0).continuationFirst()).isEqualTo("e7e5");
        assertThat(response.candidates().get(1).continuationFirst()).isNull();
    }

    @Test
    void analyzePosition_defaultsVariantsToFive() {
        when(chessApiClient.getCandidateMoves(eq(START_FEN), eq(5)))
                .thenReturn(Mono.just(List.of(
                        new CandidateMove("Nf3", "g1f3", 0.35, null, 53.0, List.of())
                )));

        PositionAnalysisResponse response = positionAnalysisService.analyzePosition(START_FEN, null);

        assertThat(response.variants()).isEqualTo(5);
    }

    @Test
    void analyzePosition_rejectsInvalidFen() {
        assertThatThrownBy(() -> positionAnalysisService.analyzePosition("not-a-fen", 5))
                .isInstanceOf(InvalidFenException.class);
    }

    @Test
    void analyzePosition_failsWhenEngineReturnsNothing() {
        when(chessApiClient.getCandidateMoves(eq(START_FEN), eq(5)))
                .thenReturn(Mono.just(List.of()));

        assertThatThrownBy(() -> positionAnalysisService.analyzePosition(START_FEN, 5))
                .isInstanceOf(EngineAnalysisException.class)
                .hasMessage("Engine returned no candidate moves for the position.");
    }
}
