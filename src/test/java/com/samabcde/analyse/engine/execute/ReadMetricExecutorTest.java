package com.samabcde.analyse.engine.execute;

import com.samabcde.analyse.core.AnalyseKey;
import com.samabcde.analyse.core.AnalyseTarget;
import com.samabcde.analyse.metric.MoveMetricExtractor;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

class ReadMetricExecutorTest {
    @Test
    public void Should_SetLastMoveInfo_When_ReceiveInfoMove() {
        String info = "info move L15 visits 121 utility -1.07502 winrate 0.00955206 scoreMean -28.424 scoreStdev 19.5573 scoreLead -28.424 scoreSelfplay -28.0574 prior 0.209432 lcb -0.0022216 utilityLcb -1.10799 order 0 pv L15 E5 M15 C13 C17 C18 D16 info move H2 visits 153 utility -1.08721 winrate 0.0115132 scoreMean -29.8217 scoreStdev 20.0817 scoreLead -29.8217 scoreSelfplay -29.389 prior 0.373352 lcb -0.00294137 utilityLcb -1.12768 order 1 pv H2 H8 L15 L10 N12 G9 M15 M10 M11 C13 info move G2 visits 130 utility -1.08636 winrate 0.0108509 scoreMean -29.2966 scoreStdev 20.1815 scoreLead -29.2966 scoreSelfplay -29.0387 prior 0.299997 lcb -0.000797726 utilityLcb -1.11897 order 2 pv G2 H8 L15 G9 M15 L10 M11 M10 N11 N15 info move H8 visits 49 utility -1.07285 winrate 0.0129406 scoreMean -27.5248 scoreStdev 17.6893 scoreLead -27.5248 scoreSelfplay -27.4359 prior 0.0198256 lcb -0.0161538 utilityLcb -1.15431 order 3 pv H8 D5 L15 L16 M15 N15 M16 info move L10 visits 25 utility -1.10385 winrate 0.00399732 scoreMean -28.1726 scoreStdev 15.6698 scoreLead -28.1726 scoreSelfplay -28.2602 prior 0.0379175 lcb -0.00740477 utilityLcb -1.13578 order 4 pv L10 H8 G2 G9 C13 info move B4 visits 5 utility -1.16511 winrate 0.00396565 scoreMean -40.3397 scoreStdev 21.6219 scoreLead -40.3397 scoreSelfplay -39.5244 prior 0.0176333 lcb -0.0933563 utilityLcb -1.43761 order 5 pv B4 D5 L15 M15 info move L9 visits 3 utility -1.17294 winrate 0.00463912 scoreMean -37.4653 scoreStdev 19.8482 scoreLead -37.4653 scoreSelfplay -36.7008 prior 0.00232647 lcb -1.17633 utilityLcb -4.47965 order 6 pv L9 H8 Q15 info move Q15 visits 1 utility -1.15063 winrate 0.00702433 scoreMean -37.7183 scoreStdev 18.1302 scoreLead -37.7183 scoreSelfplay -36.0593 prior 0.00409325 lcb -0.992976 utilityLcb -2.8 order 7 pv Q15 info move C5 visits 1 utility -1.20293 winrate 0.00577182 scoreMean -48.6297 scoreStdev 20.2033 scoreLead -48.6297 scoreSelfplay -45.184 prior 0.00357019 lcb -0.994228 utilityLcb -2.8 order 8 pv C5";
        AnalyseKey analyseKey = new AnalyseKey(AnalyseTarget.CANDIDATE, 1, "A1");
        Reader reader = new StringReader(info);
        AnalyseProcessState analyseProcessState = new AnalyseProcessState();
        ReadMetricExecutor readMetricExecutor = new ReadMetricExecutor(new BufferedReader(reader), analyseProcessState, new MoveMetricExtractor(), new ExecutorThreadFactory(new ExecutorExceptionHandler(analyseProcessState)));
        analyseProcessState.setCurrentAnalyseKey(analyseKey);
        assertThat(analyseProcessState.getLastMoveInfo()).isNull();
        readMetricExecutor.start();
        readMetricExecutor.stop();
        assertThat(analyseProcessState.getLastMoveInfo()).isNotNull();
    }

    @Test
    public void Should_setIsCompleteAnalysis_When_ReceiveVersionInfo() {
        String versionInfo = "= 2";
        Reader reader = new StringReader(versionInfo);
        AnalyseProcessState analyseProcessState = new AnalyseProcessState();
        ReadMetricExecutor readMetricExecutor = new ReadMetricExecutor(new BufferedReader(reader), analyseProcessState, new MoveMetricExtractor(), new ExecutorThreadFactory(new ExecutorExceptionHandler(analyseProcessState)));
        assertThat(analyseProcessState.isCompleteAnalyze()).isFalse();
        readMetricExecutor.start();
        readMetricExecutor.stop();
        assertThat(analyseProcessState.isCompleteAnalyze()).isTrue();
    }
}