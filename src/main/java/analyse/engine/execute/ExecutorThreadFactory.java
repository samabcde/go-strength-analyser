package analyse.engine.execute;

import java.util.concurrent.ThreadFactory;

public class ExecutorThreadFactory implements ThreadFactory {
    private final Thread.UncaughtExceptionHandler exceptionHandler;

    public ExecutorThreadFactory(Thread.UncaughtExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public Thread newThread(Runnable r) {
        final Thread thread = new Thread(r);
        thread.setUncaughtExceptionHandler(exceptionHandler);
        return thread;
    }
}