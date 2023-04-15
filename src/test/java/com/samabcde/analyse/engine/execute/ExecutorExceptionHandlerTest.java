package com.samabcde.analyse.engine.execute;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExecutorExceptionHandlerTest {
    private final AnalyseProcessState analyseProcessState = new AnalyseProcessState();
    private final Thread thread = mock(Thread.class);
    private final Exception exception = mock(Exception.class);

    @Test
    public void GivenExceptionHandler_WhenExceptionUncaught_ShouldFlagProcessStateErrorOccur() {
        assertThat(analyseProcessState.isErrorOccur()).isFalse();
        when(exception.getSuppressed()).thenReturn(new Throwable[]{});
        ExecutorExceptionHandler exceptionHandler = new ExecutorExceptionHandler(analyseProcessState);
        exceptionHandler.uncaughtException(thread, exception);
        assertThat(analyseProcessState.isErrorOccur()).isTrue();
    }
}