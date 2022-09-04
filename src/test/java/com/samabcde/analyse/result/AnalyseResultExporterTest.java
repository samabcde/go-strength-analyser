package com.samabcde.analyse.result;

import com.samabcde.analyse.calculate.GameScore;
import com.samabcde.analyse.calculate.MoveScore;
import com.samabcde.analyse.core.AnalyseKey;
import com.samabcde.analyse.core.AnalyseMetadata;
import com.samabcde.analyse.core.AnalyseTarget;
import com.samabcde.analyse.core.ApplicationConfig;
import com.samabcde.analyse.metric.MoveMetric;
import com.samabcde.analyse.metric.MoveMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AnalyseResultExporterTest {
    private static final String sgf = "(;GM[1]FF[4]CA[UTF-8]AP[Sabaki:0.52.0]RU[japanese]KM[6.5]SZ[19]DT[2022-02-20];B[pd];W[dp];B[pq];)";
    @Mock
    ApplicationConfig applicationConfig;

    AnalyseResultExporter analyseResultExporter;
    @TempDir
    Path anotherTempDir;

    @BeforeEach
    void setup() {
        analyseResultExporter = new AnalyseResultExporter(applicationConfig);
        assertTrue(anotherTempDir.resolve("sgfFolderPath").toFile().mkdir());
        when(applicationConfig.getSgfFolderPath()).thenReturn(anotherTempDir.resolve("sgfFolderPath"));
        assertTrue(anotherTempDir.resolve("outputFileFolder").toFile().mkdir());
        when(applicationConfig.getOutputFileFolder()).thenReturn(anotherTempDir.resolve("outputFileFolder").toString()+ File.separator);
    }

    @Test
    public void export() {
        AnalyseMetadata metadata = new AnalyseMetadata("dummy", sgf, 1);
        AnalyseResult analyseResult = AnalyseResult.builder()
                .metadata(metadata)
                .moveMetricsList(List.of(
                        MoveMetrics.builder().moveNo(1)
                                .moveScore(new MoveScore(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE))
                                .ai(MoveMetric.builder().analyseKey(new AnalyseKey(AnalyseTarget.AI, 1, "aa")).winrate(BigDecimal.ZERO).scoreLead(BigDecimal.ZERO).bestMove("aa").build())
                                .candidate(MoveMetric.builder().analyseKey(new AnalyseKey(AnalyseTarget.CANDIDATE, 1, "bb")).winrate(BigDecimal.ZERO).scoreLead(BigDecimal.ZERO).bestMove("aa").build())
                                .pass(MoveMetric.builder().analyseKey(new AnalyseKey(AnalyseTarget.CANDIDATE, 1, "")).winrate(BigDecimal.ZERO).scoreLead(BigDecimal.ZERO).bestMove("aa").build())
                                .build(),
                        MoveMetrics.builder().moveNo(2)
                                .moveScore(new MoveScore(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE))
                                .ai(MoveMetric.builder().analyseKey(new AnalyseKey(AnalyseTarget.AI, 2, "aa")).winrate(BigDecimal.ZERO).scoreLead(BigDecimal.ZERO).bestMove("aa").build())
                                .candidate(MoveMetric.builder().analyseKey(new AnalyseKey(AnalyseTarget.CANDIDATE, 2, "bb")).winrate(BigDecimal.ZERO).scoreLead(BigDecimal.ZERO).bestMove("aa").build())
                                .pass(MoveMetric.builder().analyseKey(new AnalyseKey(AnalyseTarget.CANDIDATE, 2, "")).winrate(BigDecimal.ZERO).scoreLead(BigDecimal.ZERO).bestMove("aa").build())
                                .build(),
                        MoveMetrics.builder().moveNo(3)
                                .moveScore(new MoveScore(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE))
                                .ai(MoveMetric.builder().analyseKey(new AnalyseKey(AnalyseTarget.AI, 3, "aa")).winrate(BigDecimal.ZERO).scoreLead(BigDecimal.ZERO).bestMove("aa").build())
                                .candidate(MoveMetric.builder().analyseKey(new AnalyseKey(AnalyseTarget.CANDIDATE, 3, "bb")).winrate(BigDecimal.ZERO).scoreLead(BigDecimal.ZERO).bestMove("aa").build())
                                .pass(MoveMetric.builder().analyseKey(new AnalyseKey(AnalyseTarget.CANDIDATE, 3, "")).winrate(BigDecimal.ZERO).scoreLead(BigDecimal.ZERO).bestMove("aa").build())
                                .build()
                ))
                .gameScore(new GameScore(BigDecimal.ONE, BigDecimal.ONE))
                .build();
        analyseResultExporter.export(analyseResult);
        assertTrue(anotherTempDir.resolve("sgfFolderPath").resolve("dummy_with_analyse.sgf").toFile().exists());
        assertTrue(anotherTempDir.resolve("outputFileFolder").resolve("dummy.txt").toFile().exists());
        assertTrue(anotherTempDir.resolve("outputFileFolder").resolve("dummy.json").toFile().exists());
    }
}