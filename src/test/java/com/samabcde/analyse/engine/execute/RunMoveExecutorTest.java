package com.samabcde.analyse.engine.execute;

import com.samabcde.analyse.formula.FormulaV1;
import com.samabcde.analyse.metric.MoveMetricExtractor;
import com.samabcde.analyse.metric.MoveMetricsScoreCalculator;
import com.samabcde.analyse.sgf.SgfParser;
import com.samabcde.analyse.util.TestUtils;
import com.toomasr.sgf4j.parser.Game;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

class RunMoveExecutorTest {

    private static String emptySgf = """
            (;GM[1]FF[4]CA[UTF-8]AP[Sabaki:0.52.0]RU[japanese]KM[6.5]SZ[19]DT[2022-02-20];)
            """;

    private static String oneMoveSgf = """
            (;GM[1]FF[4]CA[UTF-8]AP[Sabaki:0.52.0]RU[japanese]KM[6.5]SZ[19]DT[2022-02-20];B[pd];)
            """;

    @Test
    public void Should_Send_BoardSize_And_Komi() {
        StringWriter writer = new StringWriter();
        Game game = SgfParser.parseGame(emptySgf);
        AnalyseProcessState analyseProcessState = new AnalyseProcessState();

        RunMoveExecutor executor = new RunMoveExecutor(new BufferedWriter(writer), game, analyseProcessState, 100,
                new MoveMetricExtractor(),
                new ExecutorThreadFactory(new ExecutorExceptionHandler(analyseProcessState)),
                new MoveMetricsScoreCalculator(new FormulaV1()));
        analyseProcessState.ready();
        executor.start();
        executor.stop();
        String expected = """
                                
                boardsize 19
                komi 6.5
                quit""";
        assertThat(writer.toString()).isEqualTo(TestUtils.normalizeTextBlockLineSeparator(expected));
        assertThat(analyseProcessState.isEnd()).isTrue();
    }
}