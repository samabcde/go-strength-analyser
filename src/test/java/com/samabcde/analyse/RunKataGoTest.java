package com.samabcde.analyse;

import com.samabcde.analyse.calculate.GameScore;
import com.samabcde.analyse.calculate.MoveScore;
import com.samabcde.analyse.core.ApplicationConfig;
import com.samabcde.analyse.engine.KataGoFactory;
import com.samabcde.analyse.engine.RunKataGo;
import com.samabcde.analyse.info.AnalyseInfoExporter;
import com.samabcde.analyse.info.AnalyseInfoImporter;
import com.samabcde.analyse.metric.MoveMetricExtractor;
import com.samabcde.analyse.metric.MoveMetricsScoreCalculator;
import com.samabcde.analyse.result.AnalyseResultExporter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = ApplicationConfig.class)
@TestPropertySource({"classpath:application.properties", "classpath:test.properties"})
class RunKataGoTest {

    @Autowired
    private ApplicationConfig applicationConfig;

    private RunKataGo runKataGo;

    private FakeKataGo fakeKataGo;

    @Mock
    AnalyseResultExporter analyseResultExporter;

    @Mock
    MoveMetricsScoreCalculator moveMetricsScoreCalculator;

    @Mock
    AnalyseInfoExporter analyseInfoExporter;

    @Mock
    KataGoFactory kataGoFactory;
    ExecutorService executorService;

    @BeforeEach
    void setup() {
        MoveMetricExtractor moveMetricExtractor = new MoveMetricExtractor();
        AnalyseInfoImporter analyseInfoImporter = new AnalyseInfoImporter();
        fakeKataGo = new FakeKataGo();
        when(kataGoFactory.createKataGoProcess()).thenReturn(fakeKataGo);
        when(moveMetricsScoreCalculator.calculateMoveScore(any())).thenReturn(new MoveScore(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        when(moveMetricsScoreCalculator.calculateGameScore(any())).thenReturn(new GameScore(BigDecimal.ZERO, BigDecimal.ZERO));
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> fakeKataGo.start());
        runKataGo = new RunKataGo(applicationConfig, moveMetricExtractor, analyseInfoExporter, analyseInfoImporter, analyseResultExporter, kataGoFactory, moveMetricsScoreCalculator);
    }

    @AfterEach
    void teardown() {
        fakeKataGo.destroy();
        executorService.shutdownNow();
    }

    @Test
    public void runSuccess() {
        runKataGo.run("kata_go", "-runTimeSec=1", "-sgfName=runKataGoTest");
        verify(analyseInfoExporter).export(any());
        verify(analyseResultExporter).export(any());
    }

}