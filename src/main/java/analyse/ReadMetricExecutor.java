package analyse;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ReadMetricExecutor {
    private final InputStream inputStream;
    private final AnalyseProcessState analyseProcessState;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MoveMetricExtractor moveMetricExtractor;

    public ReadMetricExecutor(InputStream inputStream, AnalyseProcessState analyseProcessState, MoveMetricExtractor moveMetricExtractor) {
        this.inputStream = inputStream;
        this.analyseProcessState = analyseProcessState;
        this.moveMetricExtractor = moveMetricExtractor;
    }

    void start() {
        executorService.execute(() -> {
            try (BufferedReader input = new BufferedReader(
                    new InputStreamReader(inputStream))) {
                String line;
                while ((line = input.readLine()) != null) {
                    log.debug(line);
                    if (line.startsWith("info move")) {
                        analyseProcessState.lastMoveMetric.set(moveMetricExtractor.extractMoveMetric(analyseProcessState.currentMoveNo, line));
                    }
                    if (line.equals("= 2")) {
                        analyseProcessState.isCompleteAnalyze.set(true);
                        log.debug("end current move analyse");
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
