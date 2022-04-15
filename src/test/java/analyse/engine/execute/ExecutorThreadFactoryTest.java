package analyse.engine.execute;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ExecutorThreadFactoryTest {
    private final Thread.UncaughtExceptionHandler exceptionHandler = mock(Thread.UncaughtExceptionHandler.class);

    @Test
    public void Given_ThreadCreatedByFactory_When_RunTaskWithException_Should_HandleUncaughtException() {
        ExecutorThreadFactory executorThreadFactory = new ExecutorThreadFactory(exceptionHandler);
        RuntimeException runtimeException = new RuntimeException();
        Thread thread = executorThreadFactory.newThread(() -> {
            throw runtimeException;
        });
        thread.start();
        verify(exceptionHandler).uncaughtException(thread, runtimeException);
    }
}