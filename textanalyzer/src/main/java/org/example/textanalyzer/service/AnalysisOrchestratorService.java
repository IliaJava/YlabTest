package org.example.textanalyzer.service;

import org.example.textanalyzer.dto.AnalysisRequestDto;
import org.example.textanalyzer.dto.AnalysisStatusResponse;
import org.example.textanalyzer.entity.*;
import org.example.textanalyzer.model.*;
import org.example.textanalyzer.repository.AnalysisRepository;
import org.example.textanalyzer.util.AnalysisStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Оркестратор: создаёт запись анализа, запускает его асинхронно,
 * сохраняет результат и отдаёт статус/результат по запросу.
 */
@Service
public class AnalysisOrchestratorService {

    private final TextAnalysisService singleService;
    private final TextAnalysisService parallelService;

    private final AnalysisRepository repo;
    private final AuditService audit;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    public AnalysisOrchestratorService(
            @Qualifier("single") TextAnalysisService singleService,
            @Qualifier("parallel") TextAnalysisService parallelService,
            AnalysisRepository repo,
            AuditService audit
    ) {
        this.singleService = singleService;
        this.parallelService = parallelService;
        this.repo = repo;
        this.audit = audit;
    }

 //   private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Старт анализа: создаём сущность, логируем, запускаем асинхронно.
     */

    /**
     *принимает запрос на запуск анализа;
     * создаёт запись в БД (AnalysisEntity);
     * асинхронно запускает анализ (single или parallel);
     * по завершении сохраняет результаты (слова, ошибки, время, статус);
     * по запросу отдаёт статус и результат;
     * следит, чтобы пользователь видел только свои анализы.
     * */
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

        /**
        *вставляет запись в таблицу analyses;
        *генерирует id (PRIMARY KEY);
        *возвращает сущность с заполненным id
         */
        repo.save(entity);

        audit.log(username, "START", entity.getId(),
                "dir=" + entity.getDirectory() + ", mode=" + entity.getMode());
        /**
         *В пул потоков отправляется задача: runAsync(id, dto).
         * Текущий поток не ждёт завершения анализа.
         * Метод startAnalysis сразу возвращает id анализа.
        */
        executor.submit(() -> runAsync(entity.getId(), dto));

        return entity.getId();
        /**
          *Клиент (REST‑контроллер) получает id и может:
          *опрашивать статус по этому id;
         *позже запросить результат.
         */
    }

    /**
     * Асинхронное выполнение анализа.
     */
  /**  По id читается AnalysisEntity из БД.
    Статус меняется на RUNNING, сохраняется.
    Выбирается реализация:
    если mode = "multi" → parallelService,
    иначе → singleService.
    Вызывается service.analyze(...):
    передаётся путь к директории,
    минимальная длина слова,
    опциональный путь к стоп‑словам,
    количество потоков (для multi).
    Полученный AnalysisResult содержит:
    AnalysisInfo (директория, режим, время, количество файлов),
    список WordCount,
    список ErrorInfo.
    В сущность AnalysisEntity записываются:
    executionTimeMs,
    processedFiles,
    статус COMPLETED.
    Списки слов и ошибок из результата маппятся в WordCountEntity и ErrorEntity и привязываются к AnalysisEntity.
    Всё сохраняется в БД.
    В аудит пишется COMPLETED.
    Если что‑то пошло не так:
    статус → FAILED,
    сохраняется,
    в аудит пишется FAILED с текстом ошибки.
   */
    private void runAsync(Long id, AnalysisRequestDto dto) {

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

    /**
     * Получение статуса и (при завершении) результата анализа.
     */
/**
    По id читаем AnalysisEntity из БД.
    Проверяем, что username текущего пользователя совпадает с e.getUsername():
    если нет → AccessDeniedException (403).
    Создаём AnalysisStatusResponse, заполняем id и status.
    Если статус не COMPLETED:
    result остаётся null.
    Если статус COMPLETED:
    собираем AnalysisInfo из полей сущности,
    маппим WordCountEntity → WordCount,
    маппим ErrorEntity → ErrorInfo,
    создаём AnalysisResult и кладём в resp.result.
    Возвращаем resp — Jackson сериализует в JSON.
 */
    public AnalysisStatusResponse getStatus(Long id, String username) {
        AnalysisEntity e = repo.findById(id).orElseThrow();

        if (!e.getUsername().equals(username)) {
            throw new AccessDeniedException("Not your analysis");
        }

        AnalysisStatusResponse resp = new AnalysisStatusResponse();
        resp.setId(e.getId());
        resp.setStatus(e.getStatus());

        if (e.getStatus() == AnalysisStatus.COMPLETED) {
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
        }

        return resp;
    }

    /**
     * Список всех анализов пользователя.
     */
    /**
     Находим все AnalysisEntity по username.
     Для каждого вызываем getStatus(...) — то есть логика такая же, как для одного анализа.
     Возвращаем список AnalysisStatusResponse.
     */
    public List<AnalysisStatusResponse> getAll(String username) {
        return repo.findAllByUsername(username).stream()
                .map(e -> getStatus(e.getId(), username))
                .toList();
    }
}
