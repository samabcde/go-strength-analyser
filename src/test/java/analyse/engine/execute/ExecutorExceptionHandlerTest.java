package analyse.engine.execute;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ExecutorExceptionHandlerTest {
    private final AnalyseProcessState analyseProcessState = new AnalyseProcessState();
    private final Thread thread = mock(Thread.class);
    private final Exception exception = mock(Exception.class);

    @Test
    public void Given_When_Should() {
        ExecutorExceptionHandler exceptionHandler = new ExecutorExceptionHandler(analyseProcessState);
        exceptionHandler.uncaughtException(thread, exception);
        assertThat(analyseProcessState.isErrorOccur).isTrue();
    }
}