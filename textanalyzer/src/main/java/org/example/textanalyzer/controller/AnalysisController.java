package org.example.textanalyzer.controller;


import org.example.textanalyzer.dto.AnalysisRequestDto;
import org.example.textanalyzer.dto.AnalysisStatusResponse;
import org.example.textanalyzer.service.AnalysisOrchestratorService;
import org.example.textanalyzer.util.AnalysisStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-контроллер:
 *  - POST /api/analyze  — запуск анализа
 *  - GET  /api/results/{id} — статус/результат
 *  - GET  /api/results      — список всех анализов пользователя
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisOrchestratorService orchestrator;

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisStatusResponse> start(@RequestBody AnalysisRequestDto dto,
                                                        Authentication auth) {

        Long id = orchestrator.startAnalysis(dto, auth.getName());

        AnalysisStatusResponse resp = new AnalysisStatusResponse();
        resp.setId(id);
        resp.setStatus(AnalysisStatus.PENDING);

        return ResponseEntity.accepted().body(resp);
    }

    @GetMapping("/results/{id}")
    public AnalysisStatusResponse get(@PathVariable Long id, Authentication auth) {
        return orchestrator.getStatus(id, auth.getName());
    }

    @GetMapping("/results")
    public List<AnalysisStatusResponse> all(Authentication auth) {
        return orchestrator.getAll(auth.getName());
    }
}
