package org.example.textanalyzer.service;

import org.example.textanalyzer.dto.AnalysisRequestDto;
import org.example.textanalyzer.entity.AnalysisEntity;
import org.example.textanalyzer.entity.ErrorEntity;
import org.example.textanalyzer.entity.WordCountEntity;
import org.example.textanalyzer.model.AnalysisResult;
import org.example.textanalyzer.model.ErrorInfo;
import org.example.textanalyzer.model.WordCount;
import org.example.textanalyzer.repository.AnalysisRepository;
import org.example.textanalyzer.util.AnalysisStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.Optional;

@Service
public class AsyncAnalysisExecutor {

    private final AnalysisRepository repo;
    private final AuditService audit;
    private final TextAnalysisService singleService;
    private final TextAnalysisService parallelService;

    public AsyncAnalysisExecutor(
            AnalysisRepository repo,
            AuditService audit,
            @Qualifier("single") TextAnalysisService singleService,
            @Qualifier("parallel") TextAnalysisService parallelService
    ) {
        this.repo = repo;
        this.audit = audit;
        this.singleService = singleService;
        this.parallelService = parallelService;
    }

    @Transactional
    public void execute(Long id, AnalysisRequestDto dto) {

        AnalysisEntity entity = repo.findById(id).orElseThrow();
        entity.setStatus(AnalysisStatus.RUNNING);
        repo.save(entity);

        long start = System.currentTimeMillis();

        try {
            TextAnalysisService service =
                    "multi".equalsIgnoreCase(entity.getMode()) ? parallelService : singleService;

            AnalysisResult result = service.analyze(
                    Path.of(entity.getDirectory()),
                    entity.getMinWordLength(),
                    entity.getTopCount(),
                    Optional.ofNullable(dto.getStopwords()).map(Path::of),
                    entity.getThreads()
            );

            long time = System.currentTimeMillis() - start;

            entity.setExecutionTimeMs(time);
            entity.setProcessedFiles(result.getAnalysisInfo().getProcessedFiles());
            entity.setStatus(AnalysisStatus.COMPLETED);

            entity.getWords().clear();
            for (WordCount wc : result.getWords()) {
                WordCountEntity w = new WordCountEntity();
                w.setWord(wc.getWord());
                w.setCount(wc.getCount());
                w.setAnalysis(entity);
                entity.getWords().add(w);
            }

            entity.getErrors().clear();
            for (ErrorInfo ei : result.getErrors()) {
                ErrorEntity e = new ErrorEntity();
                e.setFile(ei.getFile());
                e.setMessage(ei.getMessage());
                e.setAnalysis(entity);
                entity.getErrors().add(e);
            }

            repo.save(entity);
            audit.log(entity.getUsername(), "COMPLETED", id, "ok");

        } catch (Exception ex) {
            entity.setStatus(AnalysisStatus.FAILED);
            repo.save(entity);
            audit.log(entity.getUsername(), "FAILED", id, ex.getMessage());
        }
    }
}
