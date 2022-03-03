package analyse;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ReadWinrateExecutor {
    private final InputStream inputStream;
    private final AnalyseProcessState analyseProcessState;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MoveMetricsExtractor moveMetricsExtractor;

    public ReadWinrateExecutor(InputStream inputStream, AnalyseProcessState analyseProcessState, MoveMetricsExtractor moveMetricsExtractor) {
        this.inputStream = inputStream;
        this.analyseProcessState = analyseProcessState;
        this.moveMetricsExtractor = moveMetricsExtractor;
    }

    void start() {
        executorService.execute(() -> {
            try (BufferedReader input = new BufferedReader(
                    new InputStreamReader(inputStream))) {
                String line;
                int moveNo = 0;
                while ((line = input.readLine()) != null) {
                    if (line.startsWith("info move")) {
                        log.info("I " + line);
                        analyseProcessState.lastMoveWinrate.set(moveMetricsExtractor.extractMoveMetrics(moveNo + 1, line));
                        if (analyseProcessState.isCompleteAnalyze.getAndSet(false)) {
                            log.info(line);
                            moveNo++;
                        }
                    }
                }
            } catch (IOException e) {
                log.error("Read kata ready input error", e);
            }
        });
    }

    void stop() {
        executorService.shutdown();
    }
}
