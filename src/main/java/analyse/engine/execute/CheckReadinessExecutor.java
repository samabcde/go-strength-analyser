package analyse.engine.execute;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ThreadFactory;

@Slf4j
public class CheckReadinessExecutor extends AbstractExecutor {
    private final InputStream inputStream;

    public CheckReadinessExecutor(InputStream inputStream, AnalyseProcessState analyseProcessState, ThreadFactory threadFactory) {
        super(analyseProcessState, threadFactory);
        this.inputStream = inputStream;
    }

    @Override
    protected Runnable task() {
        return () -> {
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
        };
    }

}
