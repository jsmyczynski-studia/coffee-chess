package pl.coffeechess.analysis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.coffeechess.analysis.model.dto.AnalysisReportDto;
import pl.coffeechess.analysis.service.AnalysisService;

import java.util.UUID;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @GetMapping("/{gameId}")
    public ResponseEntity<AnalysisReportDto> getReport(@PathVariable UUID gameId) {
        return ResponseEntity.ok(analysisService.getReport(gameId));
    }
}