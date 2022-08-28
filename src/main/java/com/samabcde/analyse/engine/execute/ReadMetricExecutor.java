package com.samabcde.analyse.engine.execute;

import com.samabcde.analyse.info.MoveInfo;
import com.samabcde.analyse.metric.MoveMetricExtractor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ThreadFactory;

@Slf4j
public class ReadMetricExecutor extends AbstractExecutor {
    private final BufferedReader reader;
    private final MoveMetricExtractor moveMetricExtractor;

    public ReadMetricExecutor(BufferedReader reader, AnalyseProcessState analyseProcessState, MoveMetricExtractor moveMetricExtractor, ThreadFactory threadFactory) {
        super(analyseProcessState, threadFactory);
        this.reader = reader;
        this.moveMetricExtractor = moveMetricExtractor;
    }

    @Override
    protected Runnable task() {
        return () -> {
            try (BufferedReader input = reader) {
                String line;
                while ((line = input.readLine()) != null) {
                    log.debug(line);
                    if (line.startsWith("info move")) {
                        analyseProcessState.setLastMoveInfo(new MoveInfo(analyseProcessState.getCurrentAnalyseKey(), line));
                    }
                    if (line.equals("= 2")) {
                        analyseProcessState.completeAnalyze();
                        log.debug("end current move analyse");
                    }
                }
            } catch (IOException e) {
                log.error("Read kata ready input error", e);
                throw new UncheckedIOException(e);
            }
        };
    }

}
