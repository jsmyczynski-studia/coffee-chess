package pl.coffeechess.analysis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.coffeechess.analysis.model.dto.AnalyzePositionRequest;
import pl.coffeechess.analysis.model.dto.PositionAnalysisResponse;
import pl.coffeechess.analysis.service.PositionAnalysisService;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final PositionAnalysisService positionAnalysisService;

    @PostMapping("/position")
    public ResponseEntity<PositionAnalysisResponse> analyzePosition(
            @RequestBody AnalyzePositionRequest request) {
        String fen = request == null ? null : request.fen();
        Integer variants = request == null ? null : request.variants();
        return ResponseEntity.ok(positionAnalysisService.analyzePosition(fen, variants));
    }

    @GetMapping("/position")
    public ResponseEntity<PositionAnalysisResponse> analyzePositionByQuery(
            @RequestParam String fen,
            @RequestParam(required = false) Integer variants) {
        return ResponseEntity.ok(positionAnalysisService.analyzePosition(fen, variants));
    }
}
