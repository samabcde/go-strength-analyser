package com.samabcde.analyse.engine.execute;

import com.samabcde.analyse.engine.execute.AnalyseProcessState;
import com.samabcde.analyse.engine.execute.CheckReadinessExecutor;
import com.samabcde.analyse.engine.execute.ExecutorExceptionHandler;
import com.samabcde.analyse.engine.execute.ExecutorThreadFactory;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckReadinessExecutorTest {

    @Test
    public void Should_IndicateProcessReady_When_Receive_GTP_Ready() {
        Reader reader = new StringReader("GTP ready, beginning main protocol loop");
        AnalyseProcessState analyseProcessState = new AnalyseProcessState();
        CheckReadinessExecutor executor = new CheckReadinessExecutor(
                new BufferedReader(reader), analyseProcessState, new ExecutorThreadFactory(new ExecutorExceptionHandler(analyseProcessState))
        );
        assertFalse(analyseProcessState.isReady());
        executor.start();
        executor.stop();
        assertTrue(analyseProcessState.isReady());
    }

    @Test
    public void Should_NotIndicateProcessReady_When_Not_Receive_GTP_Ready() {
        Reader reader = new StringReader("");
        AnalyseProcessState analyseProcessState = new AnalyseProcessState();
        CheckReadinessExecutor executor = new CheckReadinessExecutor(
                new BufferedReader(reader), analyseProcessState, new ExecutorThreadFactory(new ExecutorExceptionHandler(analyseProcessState))
        );
        assertFalse(analyseProcessState.isReady());
        executor.start();
        executor.stop();
        assertFalse(analyseProcessState.isReady());
    }

}