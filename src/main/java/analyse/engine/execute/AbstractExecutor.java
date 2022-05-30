package analyse.engine.execute;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AbstractExecutor {
    protected final AnalyseProcessState analyseProcessState;
    protected final ExecutorService executorService;

    protected AbstractExecutor(AnalyseProcessState analyseProcessState, ThreadFactory threadFactory) {
        this.analyseProcessState = analyseProcessState;
        this.executorService = Executors.newSingleThreadExecutor(threadFactory);
    }

    static void sleep(long milliSec) {
        try {
            Thread.sleep(milliSec);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected abstract Runnable task();

    public final void start() {
        executorService.execute(task());
    }

    public final void stop() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("awaitTermination error occur", e);
        }
    }
}
