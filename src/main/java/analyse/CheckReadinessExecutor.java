package analyse;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class CheckReadinessExecutor {
    private final InputStream inputStream;
    private final AnalyseProcessState analyseProcessState;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public CheckReadinessExecutor(InputStream inputStream, AnalyseProcessState analyseProcessState) {
        this.inputStream = inputStream;
        this.analyseProcessState = analyseProcessState;
    }

    void start() {
        executorService.execute(() -> {
            try (BufferedReader input = new BufferedReader(
                    new InputStreamReader(inputStream))) {
                String line;

                while ((line = input.readLine()) != null) {
                    log.info(line);
                    if (line.startsWith("NN eval")) {
                        log.info("E " + line);
                    }
                    if (line.startsWith("GTP ready, beginning main protocol loop")) {
                        analyseProcessState.isReady = true;
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
