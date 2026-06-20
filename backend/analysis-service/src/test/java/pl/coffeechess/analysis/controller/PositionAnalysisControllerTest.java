package pl.coffeechess.analysis.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pl.coffeechess.analysis.exception.GlobalExceptionHandler;
import pl.coffeechess.analysis.exception.InvalidFenException;
import pl.coffeechess.analysis.model.dto.PositionAnalysisResponse;
import pl.coffeechess.analysis.model.dto.PositionCandidateDto;
import pl.coffeechess.analysis.service.PositionAnalysisService;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PositionAnalysisControllerTest {

    private static final String START_FEN =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    @Mock
    private PositionAnalysisService positionAnalysisService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AnalysisController(positionAnalysisService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void postPosition_returnsCandidates() throws Exception {
        when(positionAnalysisService.analyzePosition(eq(START_FEN), eq(2)))
                .thenReturn(new PositionAnalysisResponse(
                        START_FEN,
                        2,
                        List.of(new PositionCandidateDto("e4", "e2e4", 0.27, null, 52.1, "e7e5"))
                ));

        mockMvc.perform(post("/api/analysis/position")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fen": "%s",
                                  "variants": 2
                                }
                                """.formatted(START_FEN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fen").value(START_FEN))
                .andExpect(jsonPath("$.variants").value(2))
                .andExpect(jsonPath("$.candidates[0].san").value("e4"))
                .andExpect(jsonPath("$.candidates[0].uci").value("e2e4"))
                .andExpect(jsonPath("$.candidates[0].continuationFirst").value("e7e5"));
    }

    @Test
    void postPosition_returnsBadRequestForInvalidFen() throws Exception {
        when(positionAnalysisService.analyzePosition(isNull(), isNull()))
                .thenThrow(new InvalidFenException("FEN is required."));

        mockMvc.perform(post("/api/analysis/position")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid FEN"));
    }
}
