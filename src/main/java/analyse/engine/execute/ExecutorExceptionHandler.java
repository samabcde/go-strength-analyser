package analyse.engine.execute;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExecutorExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final AnalyseProcessState analyseProcessState;

    public ExecutorExceptionHandler(AnalyseProcessState analyseProcessState) {
        this.analyseProcessState = analyseProcessState;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error(t.getName(), e);
        analyseProcessState.isErrorOccur = true;
    }
}
