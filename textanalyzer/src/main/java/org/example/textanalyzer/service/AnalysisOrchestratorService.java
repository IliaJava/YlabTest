package org.example.textanalyzer.service;

import lombok.RequiredArgsConstructor;
import org.example.textanalyzer.dto.AnalysisRequestDto;
import org.example.textanalyzer.dto.AnalysisStatusResponse;
import org.example.textanalyzer.entity.AnalysisEntity;
import org.example.textanalyzer.model.AnalysisInfo;
import org.example.textanalyzer.model.AnalysisResult;
import org.example.textanalyzer.model.ErrorInfo;
import org.example.textanalyzer.model.WordCount;
import org.example.textanalyzer.repository.AnalysisRepository;
import org.example.textanalyzer.util.AnalysisStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class AnalysisOrchestratorService {

    private final AnalysisRepository repo;
    private final AuditService audit;
    private final AsyncAnalysisExecutor asyncExecutor;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Старт анализа: создаём запись, логируем, запускаем асинхронно.
     */
    @Transactional
    public Long startAnalysis(AnalysisRequestDto dto, String username) {

        AnalysisEntity entity = new AnalysisEntity();
        entity.setDirectory(dto.getDirectory());
        entity.setMinWordLength(Optional.ofNullable(dto.getMinWordLength()).orElse(1));
        entity.setTopCount(dto.getTop());
        entity.setMode(Optional.ofNullable(dto.getMode()).orElse("single"));
        entity.setThreads(Optional.ofNullable(dto.getThreads()).orElse(2));
        entity.setStatus(AnalysisStatus.PENDING);
        entity.setUsername(username);
        entity.setCreatedAt(Instant.now());

        repo.save(entity);

        audit.log(username, "START", entity.getId(),
                "dir=" + entity.getDirectory() + ", mode=" + entity.getMode());

        // ВАЖНО: вызываем асинхронный метод через Spring‑прокси
        executor.submit(() -> asyncExecutor.execute(entity.getId(), dto));

        return entity.getId();
    }

    /**
     * Получение статуса и результата анализа.
     */
    @Transactional(readOnly = true)
    public AnalysisStatusResponse getStatus(Long id, String username) {

        AnalysisEntity e = repo.findById(id).orElseThrow();

        if (!e.getUsername().equals(username)) {
            throw new AccessDeniedException("Not your analysis");
        }

        AnalysisStatusResponse resp = new AnalysisStatusResponse();
        resp.setId(e.getId());
        resp.setStatus(e.getStatus());

        if (e.getStatus() != AnalysisStatus.COMPLETED) {
            return resp;
        }

        AnalysisInfo info = new AnalysisInfo(
                e.getDirectory(),
                e.getMinWordLength(),
                e.getTopCount(),
                e.getMode(),
                e.getThreads(),
                e.getProcessedFiles(),
                e.getExecutionTimeMs()
        );

        List<WordCount> words = e.getWords().stream()
                .map(w -> new WordCount(w.getWord(), w.getCount()))
                .toList();

        List<ErrorInfo> errors = e.getErrors().stream()
                .map(er -> new ErrorInfo(er.getFile(), er.getMessage()))
                .toList();

        resp.setResult(new AnalysisResult(info, words, errors));

        return resp;
    }

    /**
     * Список всех анализов пользователя.
     */
    @Transactional(readOnly = true)
    public List<AnalysisStatusResponse> getAll(String username) {
        return repo.findAllByUsername(username).stream()
                .map(e -> getStatus(e.getId(), username))
                .toList();
    }
}
