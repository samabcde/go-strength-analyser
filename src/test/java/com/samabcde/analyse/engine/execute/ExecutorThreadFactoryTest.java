package com.samabcde.analyse.engine.execute;

import com.samabcde.analyse.engine.execute.ExecutorThreadFactory;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ExecutorThreadFactoryTest {
    private final Thread.UncaughtExceptionHandler exceptionHandler = mock(Thread.UncaughtExceptionHandler.class);

    @Test
    public void Given_ThreadCreatedByFactory_When_RunTaskWithException_Should_HandleUncaughtException() throws InterruptedException {
        ExecutorThreadFactory executorThreadFactory = new ExecutorThreadFactory(exceptionHandler);
        RuntimeException runtimeException = new RuntimeException();
        Thread thread = executorThreadFactory.newThread(() -> {
            throw runtimeException;
        });
        thread.start();
        while (thread.isAlive()) {
            Thread.sleep(100);
        }
        verify(exceptionHandler).uncaughtException(thread, runtimeException);
    }
}