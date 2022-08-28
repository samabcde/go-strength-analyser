package com.samabcde.analyse.engine.execute;

import com.samabcde.analyse.engine.execute.AnalyseProcessState;
import com.samabcde.analyse.engine.execute.ExecutorExceptionHandler;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ExecutorExceptionHandlerTest {
    private final AnalyseProcessState analyseProcessState = new AnalyseProcessState();
    private final Thread thread = mock(Thread.class);
    private final Exception exception = mock(Exception.class);

    @Test
    public void GivenExceptionHandler_WhenExceptionUncaught_ShouldFlagProcessStateErrorOccur() {
        assertThat(analyseProcessState.isErrorOccur()).isFalse();
        ExecutorExceptionHandler exceptionHandler = new ExecutorExceptionHandler(analyseProcessState);
        exceptionHandler.uncaughtException(thread, exception);
        assertThat(analyseProcessState.isErrorOccur()).isTrue();
    }
}